package mic1.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mic1.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * O "Cérebro" (Backend) da CPU. (MODELO)
 *
 * Responsabilidades:
 * 1. Manter o estado dos registradores (PC, AC, SP, IR, etc.).
 * 2. Conter a lógica para executar o ciclo de busca/decodificação/execução.
 * 3. Expor propriedades (Properties) dos registradores para a View (CpuView).
 */

public class CPU {
    private static final int CONTROL_STORE_SIZE = 512;

    private final Map<String, Register> registers;
    private final ObservableList<Register> registerList;
    private final ALU alu;
    private final Shifter shifter;
    private final MicroInstruction[] controlStore;

    private final IntegerProperty mpc;
    private final IntegerProperty mir;
    private final IntegerProperty cycleCount;
    private final BooleanProperty running;
    private final StringProperty status;
    private final ObservableList<String> microcodeLog;

    private MainMemory memory;

    public CPU() {
        this.registers = new HashMap<>();
        this.registerList = FXCollections.observableArrayList(reg ->
            reg != null ? new javafx.beans.Observable[] { reg.valueProperty() } : new javafx.beans.Observable[0]
        );
        this.alu = new ALU();
        this.shifter = new Shifter();
        this.controlStore = new MicroInstruction[CONTROL_STORE_SIZE];

        this.mpc = new SimpleIntegerProperty(0);
        this.mir = new SimpleIntegerProperty(0);
        this.cycleCount = new SimpleIntegerProperty(0);
        this.running = new SimpleBooleanProperty(false);
        this.status = new SimpleStringProperty("STOPPED");
        this.microcodeLog = FXCollections.observableArrayList();

        initializeRegisters();
        loadDefaultMicrocode();
    }

    private void initializeRegisters() {
        addRegister("PC", 0);
        addRegister("AC", 1);
        addRegister("SP", 2);
        addRegister("IR", 3);
        addRegister("TIR", 4);
        addRegister("0", 5, 0, true);
        addRegister("+1", 6, 1, true);
        addRegister("-1", 7, -1, true);
        addRegister("AMASK", 8, 0x0FFF, true);
        addRegister("SMASK", 9, 0x00FF, true);
        addRegister("A", 10);
        addRegister("B", 11);
        addRegister("C", 12);
        addRegister("D", 13);
        addRegister("E", 14);
        addRegister("F", 15);
    }

    private void addRegister(String name, int index) {
        addRegister(name, index, 0, false);
    }

    private void addRegister(String name, int index, int defaultValue, boolean readOnly) {
        Register reg = new Register(name, defaultValue, readOnly);
        registers.put(name, reg);
        if (registerList.size() <= index) {
            while (registerList.size() <= index) {
                registerList.add(null);
            }
        }
        registerList.set(index, reg);
    }

    private void loadDefaultMicrocode() {
        for (int i = 0; i < CONTROL_STORE_SIZE; i++) {
            controlStore[i] = new MicroInstruction.Builder()
                .addr(0)
                .regA(0)
                .regB(0)
                .regC(0)
                .build();
        }

        controlStore[0] = new MicroInstruction.Builder()
            .regB(0)
            .regC(3)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .rd(true)
            .addr(1)
            .build();
    }

    public void setMemory(MainMemory memory) {
        this.memory = memory;
    }

    public void executeCycle() {
        if (mpc.get() >= CONTROL_STORE_SIZE || controlStore[mpc.get()] == null) {
            logMicrocode("ERROR: Invalid MPC address: " + mpc.get());
            stop();
            return;
        }

        MicroInstruction mi = controlStore[mpc.get()];
        mir.set(mpc.get());

        int aluInputA = getAluInputA(mi);
        int aluInputB = getRegisterValue(mi.getRegB());

        logMicrocode(String.format(">>> MPC=%d | A=R%d(0x%X) B=R%d(0x%X) ALU=%s",
            mpc.get(), mi.getRegA(), aluInputA, mi.getRegB(), aluInputB, mi.getAluOp()));

        int aluResult = alu.execute(aluInputA, aluInputB, mi.getAluOp());
        int shiftedResult = shifter.shift(aluResult, mi.getShiftOp());

        if (mi.isEnc() && mi.getRegC() < registerList.size()) {
            Register targetReg = registerList.get(mi.getRegC());
            if (targetReg != null) {
                logMicrocode(String.format("    Writing R%d(%s) = 0x%X",
                    mi.getRegC(), targetReg.getName(), shiftedResult));
                targetReg.setValue(shiftedResult);
            }
        }

        if (mi.isMar() && memory != null) {
            memory.setMAR(shiftedResult);
            logMicrocode(String.format("    MAR = 0x%X (%d)", shiftedResult, shiftedResult));
        }

        if (mi.isMbr() && memory != null) {
            memory.setMBR(shiftedResult);
            logMicrocode(String.format("    MBR = 0x%X (%d)", shiftedResult, shiftedResult));
        }

        if (mi.isRd() && memory != null) {
            memory.read();
            logMicrocode(String.format("    RD: Memory[%d] -> MBR = 0x%X", memory.getMAR(), memory.getMBR()));
        }

        if (mi.isWr() && memory != null) {
            logMicrocode(String.format("    WR: MBR(0x%X) -> Memory[%d]", memory.getMBR(), memory.getMAR()));
            memory.write();
        }

        int nextMpc = calculateNextMpc(mi);
        logMicrocode(String.format("    COND=%s -> Next MPC=%d", mi.getCond(), nextMpc));
        mpc.set(nextMpc);

        cycleCount.set(cycleCount.get() + 1);
    }

    private int getAluInputA(MicroInstruction mi) {
        if (mi.isAmux() && memory != null) {
            return memory.getMBR();
        }
        return getRegisterValue(mi.getRegA());
    }

    private int getRegisterValue(int index) {
        if (index >= 0 && index < registerList.size() && registerList.get(index) != null) {
            return registerList.get(index).getValue();
        }
        return 0;
    }

    private int calculateNextMpc(MicroInstruction mi) {
        boolean shouldBranch = switch (mi.getCond()) {
            case NEGATIVE -> alu.isNegative();
            case ZERO -> alu.isZero();
            case ALWAYS -> true;
            case NONE -> false;
        };

        return shouldBranch ? mi.getAddr() : (mpc.get() + 1);
    }

    public void start() {
        running.set(true);
        status.set("RUNNING");
        logMicrocode("CPU started");
    }

    public void stop() {
        running.set(false);
        status.set("STOPPED");
        logMicrocode("CPU stopped");
    }

    public void pause() {
        running.set(false);
        status.set("PAUSED");
        logMicrocode("CPU paused");
    }

    public void reset() {
        for (Register reg : registerList) {
            if (reg != null) {
                reg.reset();
            }
        }
        alu.reset();
        mpc.set(0);
        mir.set(0);
        cycleCount.set(0);
        running.set(false);
        status.set("RESET");
        microcodeLog.clear();
        logMicrocode("CPU reset");
    }

    private void logMicrocode(String message) {
        microcodeLog.add(message);
        if (microcodeLog.size() > 1000) {
            microcodeLog.remove(0);
        }
    }

    public Register getRegister(String name) {
        return registers.get(name);
    }

    public Register getRegister(int index) {
        if (index >= 0 && index < registerList.size()) {
            return registerList.get(index);
        }
        return null;
    }

    public void setMicroInstruction(int address, MicroInstruction instruction) {
        if (address >= 0 && address < CONTROL_STORE_SIZE) {
            controlStore[address] = instruction;
        }
    }

    public ObservableList<Register> getRegisterList() {
        return registerList;
    }

    public IntegerProperty mpcProperty() {
        return mpc;
    }

    public IntegerProperty mirProperty() {
        return mir;
    }

    public IntegerProperty cycleCountProperty() {
        return cycleCount;
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public ObservableList<String> getMicrocodeLog() {
        return microcodeLog;
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getCycleCount() {
        return cycleCount.get();
    }

    public String getStatus() {
        return status.get();
    }
}