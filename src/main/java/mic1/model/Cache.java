package mic1.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import mic1.core.CacheLine;

public class Cache {
    public static class CacheStats {
        // mantem contadores de desempenho para exibicao na ui
        private final LongProperty hits;
        private final LongProperty misses;
        private final IntegerProperty hitRate;
        
        public CacheStats() {
            this.hits = new SimpleLongProperty(0);
            this.misses = new SimpleLongProperty(0);
            this.hitRate = new SimpleIntegerProperty(0);
        }
        
        public void recordHit() {
            hits.set(hits.get() + 1);
            updateHitRate();
        }
        
        public void recordMiss() {
            misses.set(misses.get() + 1);
            updateHitRate();
        }
        
        private void updateHitRate() {
            // recalcula taxa de acerto sempre que hits ou misses mudam
            long total = hits.get() + misses.get();
            if (total > 0) {
                hitRate.set((int) ((hits.get() * 100) / total));
            } else {
                hitRate.set(0);
            }
        }
        
        public void reset() {
            hits.set(0);
            misses.set(0);
            hitRate.set(0);
        }
        
        public LongProperty hitsProperty() {
            return hits;
        }
        
        public LongProperty missesProperty() {
            return misses;
        }
        
        public IntegerProperty hitRateProperty() {
            return hitRate;
        }
        
        public long getHits() {
            return hits.get();
        }
        
        public long getMisses() {
            return misses.get();
        }
        
        public int getHitRate() {
            return hitRate.get();
        }
    }
    
    private static final int DEFAULT_CACHE_SIZE = 64;
    private static final int DEFAULT_BLOCK_SIZE = 4;
    private static final int DEFAULT_ASSOCIATIVITY = 4;
    
    private final int cacheSize;
    private final int blockSize;
    private final int associativity;
    private final int numSets;
    private final CacheLine[][] sets;
    private final CacheStats stats;
    private long accessCounter;
    
    private final int[] mainMemory;
    
    public Cache(int[] mainMemory) {
        this(mainMemory, DEFAULT_CACHE_SIZE, DEFAULT_BLOCK_SIZE, DEFAULT_ASSOCIATIVITY);
    }
    
    public Cache(int[] mainMemory, int cacheSize, int blockSize, int associativity) {
        // configura associatividade por conjunto para reduzir conflitos de mapeamento direto
        this.mainMemory = mainMemory;
        this.cacheSize = cacheSize;
        this.blockSize = blockSize;
        this.associativity = associativity;
        
        // divide cache em conjuntos menores para balancear complexidade de busca e taxa de hits
        this.numSets = cacheSize / (blockSize * associativity);
        this.sets = new CacheLine[numSets][associativity];
        this.stats = new CacheStats();
        this.accessCounter = 0;
        
        initializeCache();
    }
    
    private void initializeCache() {
        for (int i = 0; i < numSets; i++) {
            for (int j = 0; j < associativity; j++) {
                sets[i][j] = new CacheLine(blockSize);
            }
        }
    }
    
    public int read(int address) {
        accessCounter++;
        
        // mapeia endereco para conjunto especifico, reduzindo espaco de busca
        int blockNumber = address / blockSize;
        int blockOffset = address % blockSize;
        int setIndex = blockNumber % numSets;
        int tag = blockNumber / numSets;
        
        CacheLine[] set = sets[setIndex];
        
        // busca paralela no conjunto: se tag bater, eh hit e retornamos dado rapido
        for (CacheLine line : set) {
            if (line.isValid() && line.getTag() == tag) {
                line.updateAccessTime(accessCounter);
                stats.recordHit();
                return line.getData()[blockOffset];
            }
        }
        
        // miss: trazemos bloco da memoria lenta e substituimos o menos usado (lru) para otimizar futuro
        stats.recordMiss();
        CacheLine victim = findLRULine(set);
        loadBlock(victim, blockNumber, tag);
        victim.updateAccessTime(accessCounter);
        return victim.getData()[blockOffset];
    }
    
    public void write(int address, int value) {
        accessCounter++;
        int blockNumber = address / blockSize;
        int blockOffset = address % blockSize;
        int setIndex = blockNumber % numSets;
        int tag = blockNumber / numSets;
        
        CacheLine[] set = sets[setIndex];
        
        // se bloco ja esta no cache, atualizamos para manter coerencia (write hit)
        boolean found = false;
        for (CacheLine line : set) {
            if (line.isValid() && line.getTag() == tag) {
                line.getData()[blockOffset] = value;
                line.updateAccessTime(accessCounter);
                stats.recordHit();
                found = true;
                break;
            }
        }
        
        // se nao esta, trazemos para cache pois provavelmente sera lido em breve (localidade temporal)
        if (!found) {
            stats.recordMiss();
            CacheLine victim = findLRULine(set);
            loadBlock(victim, blockNumber, tag);
            victim.getData()[blockOffset] = value;
            victim.updateAccessTime(accessCounter);
        }
        
        // usamos write-through para garantir que memoria principal nunca fique obsoleta
        int memoryAddress = blockNumber * blockSize + blockOffset;
        if (memoryAddress < mainMemory.length) {
            mainMemory[memoryAddress] = value;
        }
    }
    
    private CacheLine findLRULine(CacheLine[] set) {
        // procura linha nao usada ha mais tempo para substituir, assumindo que dados antigos sao menos relevantes
        CacheLine lru = set[0];
        for (CacheLine line : set) {
            if (!line.isValid()) {
                // prioriza usar espacos vazios antes de descartar dados validos
                return line;
            }
            if (line.getLastAccessTime() < lru.getLastAccessTime()) {
                lru = line;
            }
        }
        return lru;
    }
    
    private void loadBlock(CacheLine line, int blockNumber, int tag) {
        // carrega bloco inteiro aproveitando localidade espacial (dados vizinhos tendem a ser acessados juntos)
        int startAddress = blockNumber * blockSize;
        int[] blockData = new int[blockSize];
        
        for (int i = 0; i < blockSize; i++) {
            int addr = startAddress + i;
            if (addr < mainMemory.length) {
                blockData[i] = mainMemory[addr];
            } else {
                blockData[i] = 0;
            }
        }
        
        line.setData(blockData);
        line.setTag(tag);
        line.setValid(true);
    }
    
    public void invalidate(int address) {
        int blockNumber = address / blockSize;
        int setIndex = blockNumber % numSets;
        int tag = blockNumber / numSets;
        
        CacheLine[] set = sets[setIndex];
        for (CacheLine line : set) {
            if (line.isValid() && line.getTag() == tag) {
                line.invalidate();
                break;
            }
        }
    }
    
    public void flush() {
        for (int i = 0; i < numSets; i++) {
            for (int j = 0; j < associativity; j++) {
                sets[i][j].invalidate();
            }
        }
    }
    
    public void reset() {
        flush();
        stats.reset();
        accessCounter = 0;
    }
    
    public CacheStats getStats() {
        return stats;
    }
    
    public int getCacheSize() {
        return cacheSize;
    }
    
    public int getBlockSize() {
        return blockSize;
    }
    
    public int getAssociativity() {
        return associativity;
    }
    
    public int getNumSets() {
        return numSets;
    }
    
    public CacheLine[][] getSets() {
        return sets;
    }
}
