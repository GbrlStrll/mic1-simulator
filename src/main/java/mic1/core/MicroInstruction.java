package mic1.core;

public class MicroInstruction {
    public enum Condition {
        NONE(0),
        NEGATIVE(1),
        ZERO(2),
        ALWAYS(3);

        private final int code;

        Condition(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Condition fromCode(int code) {
            for (Condition c : values()) {
                if (c.code == code) {
                    return c;
                }
            }
            return NONE;
        }
    }

    private final int addr;
    private final int regA;
    private final int regB;
    private final int regC;
    private final ALU.Operation aluOp;
    private final Shifter.ShiftOperation shiftOp;
    private final boolean amux;
    private final Condition cond;
    private final boolean mbr;
    private final boolean mar;
    private final boolean rd;
    private final boolean wr;
    private final boolean enc;

    private MicroInstruction(Builder builder) {
        this.addr = builder.addr;
        this.regA = builder.regA;
        this.regB = builder.regB;
        this.regC = builder.regC;
        this.aluOp = builder.aluOp;
        this.shiftOp = builder.shiftOp;
        this.amux = builder.amux;
        this.cond = builder.cond;
        this.mbr = builder.mbr;
        this.mar = builder.mar;
        this.rd = builder.rd;
        this.wr = builder.wr;
        this.enc = builder.enc;
    }

    public int getAddr() { return addr; }
    public int getRegA() { return regA; }
    public int getRegB() { return regB; }
    public int getRegC() { return regC; }
    public ALU.Operation getAluOp() { return aluOp; }
    public Shifter.ShiftOperation getShiftOp() { return shiftOp; }
    public boolean isAmux() { return amux; }
    public Condition getCond() { return cond; }
    public boolean isMbr() { return mbr; }
    public boolean isMar() { return mar; }
    public boolean isRd() { return rd; }
    public boolean isWr() { return wr; }
    public boolean isEnc() { return enc; }

    public static class Builder {
        private int addr = 0;
        private int regA = 0;
        private int regB = 0;
        private int regC = 0;
        private ALU.Operation aluOp = ALU.Operation.ADD;
        private Shifter.ShiftOperation shiftOp = Shifter.ShiftOperation.NONE;
        private boolean amux = false;
        private Condition cond = Condition.NONE;
        private boolean mbr = false;
        private boolean mar = false;
        private boolean rd = false;
        private boolean wr = false;
        private boolean enc = false;

        public Builder addr(int addr) { this.addr = addr; return this; }
        public Builder regA(int regA) { this.regA = regA; return this; }
        public Builder regB(int regB) { this.regB = regB; return this; }
        public Builder regC(int regC) { this.regC = regC; return this; }
        public Builder aluOp(ALU.Operation aluOp) { this.aluOp = aluOp; return this; }
        public Builder shiftOp(Shifter.ShiftOperation shiftOp) { this.shiftOp = shiftOp; return this; }
        public Builder amux(boolean amux) { this.amux = amux; return this; }
        public Builder cond(Condition cond) { this.cond = cond; return this; }
        public Condition getCond() { return this.cond; }
        public Builder mbr(boolean mbr) { this.mbr = mbr; return this; }
        public Builder mar(boolean mar) { this.mar = mar; return this; }
        public Builder rd(boolean rd) { this.rd = rd; return this; }
        public Builder wr(boolean wr) { this.wr = wr; return this; }
        public Builder enc(boolean enc) { this.enc = enc; return this; }

        public MicroInstruction build() {
            return new MicroInstruction(this);
        }
    }

    @Override
    public String toString() {
        return String.format("MicroInst[A=%d,B=%d,C=%d,ALU=%s,SH=%s,ADDR=0x%02X]",
                regA, regB, regC, aluOp, shiftOp, addr);
    }
}
