package mic1.model;

<<<<<<< Updated upstream
public class MainMemory {
    
=======
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
        this.memory = new int[MEMORY_SIZE];
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

        initializeMemoryList();
    }

    private void initializeMemoryList() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memoryList.add(new MemoryWord(i, 0));
        }
    }

    public void write() {
        int address = mar.get();
        if (isValidAddress(address)) {
            memory[address] = mbr.get();
            memoryList.get(address).setValue(mbr.get());
            memoryChanged.set(!memoryChanged.get());
        }
    }

    public void read() {
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
        if (isValidAddress(address)) {
            memory[address] = value;
            memoryList.get(address).setValue(value);
            memoryChanged.set(!memoryChanged.get());
        }
    }

    public int readWord(int address) {
        if (isValidAddress(address)) {
            return memory[address];
        }
        return 0;
    }

    public void reset() {
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
        for (int i = 0; i < program.length && (startAddress + i) < MEMORY_SIZE; i++) {
            writeWord(startAddress + i, program[i]);
        }
    }
>>>>>>> Stashed changes
}
