package mic1.core;

public class CacheLine {
    private boolean valid;
    private int tag;
    private int[] data;
    private long lastAccessTime;
    
    public CacheLine(int blockSize) {
        // inicia invalida para evitar leitura de lixo de memoria antes do primeiro carregamento
        this.valid = false;
        this.tag = -1;
        this.data = new int[blockSize];
        this.lastAccessTime = 0;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public int getTag() {
        return tag;
    }
    
    public void setTag(int tag) {
        this.tag = tag;
    }
    
    public int[] getData() {
        return data;
    }
    
    public void setData(int[] data) {
        // usa arraycopy para garantir que o cache tenha sua propria copia dos dados, isolada da origem
        System.arraycopy(data, 0, this.data, 0, Math.min(data.length, this.data.length));
    }
    
    public long getLastAccessTime() {
        return lastAccessTime;
    }
    
    public void updateAccessTime(long time) {
        // atualiza timestamp para identificar linhas antigas na politica de substituicao lru
        this.lastAccessTime = time;
    }
    
    public void invalidate() {
        this.valid = false;
        this.tag = -1;
    }
}
