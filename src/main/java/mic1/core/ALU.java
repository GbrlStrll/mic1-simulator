package mic1.core;

public class ALU {
    public enum Operation {
        ADD(0),
        AND(1),
        PASS_A(2),
        NOT_A(3);

        private final int code;

        Operation(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Operation fromCode(int code) {
            for (Operation op : values()) {
                if (op.code == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Invalid operation code: " + code);
        }
    }

    private boolean zeroFlag;
    private boolean negativeFlag;

    public ALU() {
        this.zeroFlag = false;
        this.negativeFlag = false;
    }

    public int execute(int a, int b, Operation operation) {
        // executa operacao alu e atualiza flags de condicao
        // flags sao usados para jumps condicionais na microinstrucao
        int result = switch (operation) {
            case ADD -> a + b;
            case AND -> a & b;
            case PASS_A -> a;
            case NOT_A -> ~a;
        };

        updateFlags(result);
        return result;
    }

    private void updateFlags(int result) {
        // atualiza flags zero e negativo baseado no resultado da operacao
        // esses flags determinam se jumps condicionais devem ser tomados
        zeroFlag = (result == 0);
        negativeFlag = (result < 0);
    }

    public boolean isZero() {
        return zeroFlag;
    }

    public boolean isNegative() {
        return negativeFlag;
    }

    public void reset() {
        zeroFlag = false;
        negativeFlag = false;
    }
}
