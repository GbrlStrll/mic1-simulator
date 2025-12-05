package mic1.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mic1.core.MemoryWord;

public class MainMemory {
    private static final int MEMORY_SIZE = 4096;
    private static final int INSTRUCTION_MEMORY_START = 0;
    private static final int DATA_MEMORY_THRESHOLD = 2048;

    private final int[] memory;
    
    private final ObservableList<MemoryWord> memoryList;
    
    private final IntegerProperty mar;
    private final IntegerProperty mbr;
    
    private final BooleanProperty memoryChanged;
    
    private final Cache instructionCache;
    private final Cache dataCache;
    
    private boolean isInstructionAccess;

    public MainMemory() {
        // array interno simula a memoria fisica (ram)
        this.memory = new int[MEMORY_SIZE];
        
        // lista observavel sincronizada com o array para atualizacao automatica da ui
        this.memoryList = FXCollections.observableArrayList(memWord ->
                new javafx.beans.Observable[] {
                        memWord.valueProperty(),
                        memWord.binaryValueProperty(),
                        memWord.decimalValueProperty(),
                        memWord.hexValueProperty()
                }
        );
        
        this.mar = new SimpleIntegerProperty(0);
        this.mbr = new SimpleIntegerProperty(0);
        this.memoryChanged = new SimpleBooleanProperty(false);
        this.isInstructionAccess = false;

        // separa caches para evitar que acesso a dados expulse instrucoes uteis (conflito de cache)
        this.instructionCache = new Cache(memory, 64, 4, 4);
        this.dataCache = new Cache(memory, 64, 4, 4);

        initializeMemoryList();
    }

    private void initializeMemoryList() {
        // prepara tabela reativa com 4096 palavras zeradas
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memoryList.add(new MemoryWord(i, 0));
        }
    }

    public void write() {
        // escrita atualiza memoria e cache para manter dados sincronizados em todo sistema
        int address = mar.get();
        if (isValidAddress(address)) {
            // direciona para cache correto para nao poluir cache de instrucoes com dados e vice-versa
            Cache cache = isInstructionAccess(address) ? instructionCache : dataCache;
            cache.write(address, mbr.get());
            memory[address] = mbr.get();
            memoryList.get(address).setValue(mbr.get());
            memoryChanged.set(!memoryChanged.get()); 
        }
    }

    public void read() {
        // leitura prioriza cache por ser muito mais rapido que memoria principal
        int address = mar.get();
        if (isValidAddress(address)) {
            Cache cache = isInstructionAccess(address) ? instructionCache : dataCache;
            int value = cache.read(address);
            mbr.set(value);
        } else {
            mbr.set(0);
        }
    }
    
    public void setInstructionAccess(boolean isInstruction) {
        // permite que a cpu informe explicitamente o tipo de acesso (fetch vs dados)
        this.isInstructionAccess = isInstruction;
    }
    
    private boolean isInstructionAccess(int address) {
        // determina se acesso deve ir para cache de instrucoes ou dados
        // usa flag explicita da cpu ou heuristica baseada no endereco
        return isInstructionAccess || address < DATA_MEMORY_THRESHOLD;
    }

    public void setMAR(int value) {
        if (isValidAddress(value)) {
            mar.set(value);
        }
    }

    public void setMBR(int value) {
        mbr.set(value);
    }

    public int getMAR() {
        return mar.get();
    }

    public int getMBR() {
        return mbr.get();
    }

    public IntegerProperty marProperty() {
        return mar;
    }

    public IntegerProperty mbrProperty() {
        return mbr;
    }

    public void writeWord(int address, int value) {
        // acesso direto sem usar protocolo mar/mbr
        // usado para carregar programas ou inicializar memoria
        if (isValidAddress(address)) {
            memory[address] = value;
            memoryList.get(address).setValue(value);
            memoryChanged.set(!memoryChanged.get());
        }
    }

    public int readWord(int address) {
        // acesso direto sem usar protocolo mar/mbr
        // usado para inspecao ou debug
        if (isValidAddress(address)) {
            return memory[address];
        }
        return 0;
    }

    public void reset() {
        // zera toda a memoria e limpa ambos os caches
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = 0;
            memoryList.get(i).setValue(0);
        }
        mar.set(0);
        mbr.set(0);
        instructionCache.reset();
        dataCache.reset();
    }

    public ObservableList<MemoryWord> getMemoryList() {
        return memoryList;
    }

    public int getSize() {
        return MEMORY_SIZE;
    }

    public BooleanProperty memoryChangedProperty() {
        return memoryChanged;
    }

    private boolean isValidAddress(int address) {
        return address >= 0 && address < MEMORY_SIZE;
    }

    public void loadProgram(int[] program, int startAddress) {
        // flush no cache de instrucoes eh crucial aqui para evitar execucao de codigo antigo/obsoleto
        for (int i = 0; i < program.length && (startAddress + i) < MEMORY_SIZE; i++) {
            writeWord(startAddress + i, program[i]);
        }
        instructionCache.flush();
    }
    
    public Cache getInstructionCache() {
        return instructionCache;
    }
    
    public Cache getDataCache() {
        return dataCache;
    }
}