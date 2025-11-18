package mic1.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mic1.core.MemoryWord;

/**
 * Modelo (Model) que representa a Memória Principal do simulador MIC-1.
 *
 * Esta classe gerencia o armazenamento de dados brutos (simulando a RAM)
 * e também fornece uma representação de dados observável (ObservableList)
 * para a interface gráfica (TableView).
 *
 * Ela simula a operação do barramento de memória através dos
 * registradores MAR (Memory Address Register) e MBR (Memory Buffer Register).
 */
public class MainMemory {
    private static final int MEMORY_SIZE = 4096;

    /** Armazenamento de backend (simulação da RAM) usando um array de inteiros. */
    private final int[] memory;
    
    /** Lista observável que serve como fonte de dados para a TableView da UI. */
    private final ObservableList<MemoryWord> memoryList;
    
    // Registradores de interface da memória
    private final IntegerProperty mar;
    private final IntegerProperty mbr;
    
    /** Propriedade booleana para notificar a UI quando a memória muda. */
    private final BooleanProperty memoryChanged;

    public MainMemory() {
        this.memory = new int[MEMORY_SIZE];
        this.memoryList = FXCollections.observableArrayList(memWord ->
                // Permite que a TableView observe mudanças em *qualquer*
                // propriedade de um MemoryWord
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

        initializeMemoryList();
    }

    /**
     * Preenche a lista observável (memoryList) com
     * entradas de valor zero.
     */
    private void initializeMemoryList() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memoryList.add(new MemoryWord(i, 0));
        }
    }

    /**
     * Simula um ciclo de ESCRITA na memória.
     * Pega o valor do MBR e o escreve no endereço do MAR.
     */
    public void write() {
        int address = mar.get();
        if (isValidAddress(address)) {
            memory[address] = mbr.get();
            // Atualiza também a lista da UI
            memoryList.get(address).setValue(mbr.get());
            // Notifica o controller para dar refresh na tabela
            memoryChanged.set(!memoryChanged.get()); 
        }
    }

    /**
     * Simula um ciclo de LEITURA da memória.
     * Lê o valor do endereço no MAR e o coloca no MBR.
     */
    public void read() {
        int address = mar.get();
        if (isValidAddress(address)) {
            mbr.set(memory[address]);
        } else {
            mbr.set(0); // Retorna 0 se o endereço for inválido
        }
    }

    // --- Getters e Setters para MAR e MBR ---

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

    // --- Métodos de acesso direto (para carregar programas, etc.) ---

    /**
     * Escreve um valor diretamente em um endereço,
     * sem usar o ciclo MAR/MBR.
     */
    public void writeWord(int address, int value) {
        if (isValidAddress(address)) {
            memory[address] = value;
            memoryList.get(address).setValue(value);
            memoryChanged.set(!memoryChanged.get());
        }
    }

    /**
     * Lê um valor diretamente de um endereço,
     * sem usar o ciclo MAR/MBR.
     */
    public int readWord(int address) {
        if (isValidAddress(address)) {
            return memory[address];
        }
        return 0;
    }

    /**
     * Reseta a memória para o estado inicial (tudo zero).
     */
    public void reset() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = 0;
            memoryList.get(i).setValue(0);
        }
        mar.set(0);
        mbr.set(0);
    }

    // --- Getters para a UI ---

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

    /**
     * Carrega um array de inteiros (programa) na memória
     * a partir de um endereço inicial.
     */
    public void loadProgram(int[] program, int startAddress) {
        for (int i = 0; i < program.length && (startAddress + i) < MEMORY_SIZE; i++) {
            writeWord(startAddress + i, program[i]);
        }
    }
}