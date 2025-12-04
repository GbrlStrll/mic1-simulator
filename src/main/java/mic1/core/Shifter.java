package mic1.core;

public class Shifter {
    public enum ShiftOperation {
        NONE(0),
        RIGHT(1),
        LEFT(2);

        private final int code;

        ShiftOperation(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ShiftOperation fromCode(int code) {
            for (ShiftOperation op : values()) {
                if (op.code == code) {
                    return op;
                }
            }
            return NONE;
        }
    }

    public int shift(int value, ShiftOperation operation) {
        // aplica operacao de shift no resultado da alu
        // usado para multiplicacao/divisao por 2 ou ajuste de bits
        return switch (operation) {
            case RIGHT -> value >> 1;
            case LEFT -> value << 1;
            case NONE -> value;
        };
    }
}
