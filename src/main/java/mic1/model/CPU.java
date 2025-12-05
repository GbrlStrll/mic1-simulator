package mic1.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mic1.core.*;

import java.util.HashMap;
import java.util.Map;

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
        // registradores sao armazenados em hashmap para acesso por nome e lista observavel para ui
        // a lista observavel detecta mudancas em qualquer propriedade dos registradores
        this.registers = new HashMap<>();
        this.registerList = FXCollections.observableArrayList(reg ->
            reg != null ? new javafx.beans.Observable[] { reg.valueProperty() } : new javafx.beans.Observable[0]
        );
        this.alu = new ALU();
        this.shifter = new Shifter();
        this.controlStore = new MicroInstruction[CONTROL_STORE_SIZE];

        // propriedades observaveis permitem que a ui se atualize automaticamente
        this.mpc = new SimpleIntegerProperty(0);
        this.mir = new SimpleIntegerProperty(0);
        this.cycleCount = new SimpleIntegerProperty(0);
        this.running = new SimpleBooleanProperty(false);
        this.status = new SimpleStringProperty("PARADA");
        this.microcodeLog = FXCollections.observableArrayList();

        // inicializa bancada de registradores e preenche control store com valores padrao
        initializeRegisters();
        loadDefaultMicrocode();
    }

    private void initializeRegisters() {
        // registradores principais da arquitetura mic-1
        addRegister("PC", 0);
        addRegister("AC", 1);
        addRegister("SP", 2);
        addRegister("IR", 3);
        addRegister("TIR", 4);
        
        // registradores constantes de leitura apenas (read-only)
        // usados como operandos imediatos nas operacoes da alu
        addRegister("0", 5, 0, true);
        addRegister("+1", 6, 1, true);
        addRegister("-1", 7, -1, true);
        addRegister("AMASK", 8, 0x0FFF, true);
        addRegister("SMASK", 9, 0x00FF, true);
        
        // registradores temporarios para uso geral
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
        // preenche toda a control store com instrucoes nulas para evitar comportamento indefinido
        // se mpc apontar para endereco sem instrucao valida, a cpu para
        for (int i = 0; i < CONTROL_STORE_SIZE; i++) {
            controlStore[i] = new MicroInstruction.Builder()
                .addr(0)
                .regA(0)
                .regB(0)
                .regC(0)
                .build();
        }

        // microinstrucao inicial: le memoria no endereco apontado por pc
        // esta e a primeira instrucao executada quando a cpu inicia
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
        // ciclo completo de execucao de uma microinstrucao
        // segue o padrao fetch-decode-execute da arquitetura mic-1
        
        // validacao e busca
        if (mpc.get() >= CONTROL_STORE_SIZE || controlStore[mpc.get()] == null) {
            logMicrocode("ERRO: Endereco MPC invalido: " + mpc.get());
            stop();
            return;
        }

        // FASE 1: FETCH (Buscar a microinstrução)
        // Pega a microinstrução atual da Control Store usando MPC como índice
        MicroInstruction mi = controlStore[mpc.get()];
        mir.set(mpc.get());

        // FASE 2: DECODE (Decodificar - preparar operandos)
        // Pega valores dos registradores que serão usados pela ALU
        int aluInputA = getAluInputA(mi);  // Entrada A (pode vir de reg ou MBR)
        int aluInputB = getRegisterValue(mi.getRegB());  // Entrada B (sempre de reg)

        logMicrocode(String.format(">>> MPC=%d | A=R%d(0x%X) B=R%d(0x%X) ALU=%s",
            mpc.get(), mi.getRegA(), aluInputA, mi.getRegB(), aluInputB, mi.getAluOp()));

        // FASE 3: EXECUTE
        int aluResult = alu.execute(aluInputA, aluInputB, mi.getAluOp());

        // FASE 4: TRANSFORM (Shifter)
        int shiftedResult = shifter.shift(aluResult, mi.getShiftOp());

        // FASE 5: STORE (Guardar resultado)
        // Se microinstrução diz para escrever (ENC=true):
        if (mi.isEnc() && mi.getRegC() < registerList.size()) {
            Register targetReg = registerList.get(mi.getRegC());
            if (targetReg != null) {
                logMicrocode(String.format("    Writing R%d(%s) = 0x%X",
                    mi.getRegC(), targetReg.getName(), shiftedResult));
                targetReg.setValue(shiftedResult);
            }
        }

        // Escrita em MAR (endereço de memória)
        if (mi.isMar() && memory != null) {
            memory.setMAR(shiftedResult);
            logMicrocode(String.format("    MAR = 0x%X (%d)", shiftedResult, shiftedResult));
        }

        // Escrita em MBR (dado a escrever/lido)
        if (mi.isMbr() && memory != null) {
            memory.setMBR(shiftedResult);
            logMicrocode(String.format("    MBR = 0x%X (%d)", shiftedResult, shiftedResult));
        }

        // FASE 6: MEMORY (Operações de memória)
        if (mi.isRd() && memory != null) {
            // identifica se eh busca de instrucao (pc -> ir) ou dado normal
            boolean isInstructionRead = (mi.getRegB() == 0 && mi.getRegC() == 3);
            memory.setInstructionAccess(isInstructionRead);
            memory.read(); // Lê Memory[MAR] → MBR
            String cacheType = isInstructionRead ? "INST" : "DATA";
            logMicrocode(String.format("    RD[%s]: Memory[%d] -> MBR = 0x%X", cacheType, memory.getMAR(), memory.getMBR()));
        }

        if (mi.isWr() && memory != null) {
            // escritas sao sempre tratadas como acesso a dados
            memory.setInstructionAccess(false);
            logMicrocode(String.format("    WR[DATA]: MBR(0x%X) -> Memory[%d]", memory.getMBR(), memory.getMAR()));
            memory.write(); // Escreve MBR → Memory[MAR]
        }

        // FASE 7: NEXT (Calcular próxima microinstrução)
        // pode ser sequencial ou condicional baseado em flags da alu
        int nextMpc = calculateNextMpc(mi);
        logMicrocode(String.format("    COND=%s -> Next MPC=%d", mi.getCond(), nextMpc));

        mpc.set(nextMpc); // Atualiza ponteiro para próxima microinstrução
        cycleCount.set(cycleCount.get() + 1); // Conta mais um ciclo
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
        // mpc (microprogram counter) aponta para proxima microinstrucao na control store
        // shouldBranch indica se deve fazer jump condicional ou continuar sequencialmente
        // se true: pula para endereco especificado em mi.getAddr()
        // se false: avanca para proxima posicao (mpc + 1)
        boolean shouldBranch = switch (mi.getCond()) {
            case NEGATIVE -> alu.isNegative();
            case ZERO -> alu.isZero();
            case ALWAYS -> true;
            case NONE -> false;
        };

        // retorna endereco de destino do jump ou proxima instrucao sequencial
        return shouldBranch ? mi.getAddr() : (mpc.get() + 1);
    }

    public void start() {
        running.set(true);
        status.set("EXECUTANDO");
        logMicrocode("CPU iniciada");
    }

    public void stop() {
        running.set(false);
        status.set("PARADA");
        logMicrocode("CPU parada");
    }

    public void pause() {
        running.set(false);
        status.set("PAUSADA");
        logMicrocode("CPU pausada");
    }

    public void reset() {
        // restaura todos os registradores para valores iniciais
        // registradores read-only mantem seus valores constantes
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
        status.set("RESETADO");
        microcodeLog.clear();
        logMicrocode("CPU resetada");
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