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

    private final int[] memory;
    
    private final ObservableList<MemoryWord> memoryList;
    
    private final IntegerProperty mar;
    private final IntegerProperty mbr;
    
    private final BooleanProperty memoryChanged;

    public MainMemory() {
        // array interno simula a memoria fisica (ram)
        this.memory = new int[MEMORY_SIZE];
        
        // lista observavel sincronizada com o array para atualizacao automatica da ui
        // detecta mudancas em qualquer propriedade de cada palavra de memoria
        this.memoryList = FXCollections.observableArrayList(memWord ->
                new javafx.beans.Observable[] {
                        memWord.valueProperty(),
                        memWord.binaryValueProperty(),
                        memWord.decimalValueProperty(),
                        memWord.hexValueProperty()
                }
        );
        
        // registradores de interface de memoria seguem o padrao mic-1
        this.mar = new SimpleIntegerProperty(0);
        this.mbr = new SimpleIntegerProperty(0);
        this.memoryChanged = new SimpleBooleanProperty(false);

        initializeMemoryList();
    }

    private void initializeMemoryList() {
        // prepara tabela reativa com 4096 palavras zeradas
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memoryList.add(new MemoryWord(i, 0));
        }
    }

    public void write() {
        // operacao de escrita seguindo protocolo mic-1
        // cpu coloca endereco em mar e dado em mbr, depois chama write()
        int address = mar.get();
        if (isValidAddress(address)) {
            memory[address] = mbr.get();
            memoryList.get(address).setValue(mbr.get());
            memoryChanged.set(!memoryChanged.get()); 
        }
    }

    public void read() {
        // operacao de leitura seguindo protocolo mic-1
        // cpu coloca endereco em mar, chama read(), e resultado fica em mbr
        int address = mar.get();
        if (isValidAddress(address)) {
            mbr.set(memory[address]);
        } else {
            mbr.set(0);
        }
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
        // zera toda a memoria e registradores de interface
        // sincroniza array interno e lista observavel
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = 0;
            memoryList.get(i).setValue(0);
        }
        mar.set(0);
        mbr.set(0);
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
        // carrega vetor sequencialmente a partir do endereco informado
        for (int i = 0; i < program.length && (startAddress + i) < MEMORY_SIZE; i++) {
            writeWord(startAddress + i, program[i]);
        }
    }
}