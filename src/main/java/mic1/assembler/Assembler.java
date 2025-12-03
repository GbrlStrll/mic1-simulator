package mic1.assembler;

import mic1.core.ALU;
import mic1.core.MicroInstruction;
import mic1.core.Shifter;

import java.util.*;

public class Assembler {
    private final Map<String, Integer> labels;
    private final List<String> errors;
    private int currentAddress;

    public Assembler() {
        this.labels = new HashMap<>();
        this.errors = new ArrayList<>();
        this.currentAddress = 0;
    }

    public AssemblyResult assemble(String sourceCode) {
        labels.clear();
        errors.clear();
        currentAddress = 0;

        String[] lines = sourceCode.split("\n");
        List<MicroInstruction> instructions = new ArrayList<>();
        Map<Integer, String> instructionMap = new HashMap<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.isEmpty() || line.startsWith(";")) {
                continue;
            }

            if (line.endsWith(":")) {
                String label = line.substring(0, line.length() - 1).trim();
                labels.put(label, currentAddress);
                continue;
            }

            try {
                MicroInstruction inst = parseLine(line);
                if (inst != null) {
                    instructions.add(inst);
                    instructionMap.put(currentAddress, line);
                    currentAddress++;
                }
            } catch (Exception e) {
                errors.add(String.format("Line %d: %s - %s", i + 1, line, e.getMessage()));
            }
        }

        resolveLabels(instructions, instructionMap);

        return new AssemblyResult(
            instructions.toArray(new MicroInstruction[0]),
            instructionMap,
            errors,
            errors.isEmpty()
        );
    }

    private MicroInstruction parseLine(String line) {
        String[] tokens = line.split("[\\s,]+");
        if (tokens.length == 0) {
            return null;
        }

        MicroInstruction.Builder builder = new MicroInstruction.Builder();
        builder.addr(currentAddress);

        for (String token : tokens) {
            token = token.trim().toUpperCase();
            if (token.isEmpty()) continue;

            parseToken(token, builder);
        }

        return builder.build();
    }

    private void parseToken(String token, MicroInstruction.Builder builder) {
        if (token.startsWith("A=")) {
            int reg = parseRegister(token.substring(2));
            builder.regA(reg);
        } else if (token.startsWith("B=")) {
            int reg = parseRegister(token.substring(2));
            builder.regB(reg);
        } else if (token.startsWith("C=")) {
            int reg = parseRegister(token.substring(2));
            builder.regC(reg);
        } else if (token.equals("ADD")) {
            builder.aluOp(ALU.Operation.ADD);
        } else if (token.equals("AND")) {
            builder.aluOp(ALU.Operation.AND);
        } else if (token.equals("PASS") || token.equals("PASSA")) {
            builder.aluOp(ALU.Operation.PASS_A);
        } else if (token.equals("NOT") || token.equals("NOTA")) {
            builder.aluOp(ALU.Operation.NOT_A);
        } else if (token.equals("SHL") || token.equals("LEFT")) {
            builder.shiftOp(Shifter.ShiftOperation.LEFT);
        } else if (token.equals("SHR") || token.equals("RIGHT")) {
            builder.shiftOp(Shifter.ShiftOperation.RIGHT);
        } else if (token.equals("MAR")) {
            builder.mar(true);
        } else if (token.equals("MBR")) {
            builder.mbr(true);
        } else if (token.equals("RD") || token.equals("READ")) {
            builder.rd(true);
        } else if (token.equals("WR") || token.equals("WRITE")) {
            builder.wr(true);
        } else if (token.equals("ENC")) {
            builder.enc(true);
        } else if (token.equals("AMUX")) {
            builder.amux(true);
        } else if (token.startsWith("IF")) {
            parseCondition(token, builder);
        } else if (token.startsWith("GOTO")) {
            String label = token.substring(4).trim();
            Integer addr = labels.get(label);
            if (addr != null) {
                builder.addr(addr);
            }
            if (builder.getCond() == MicroInstruction.Condition.NONE) {
                builder.cond(MicroInstruction.Condition.ALWAYS);
            }
        }
    }

    private void parseCondition(String token, MicroInstruction.Builder builder) {
        if (token.contains("N")) {
            builder.cond(MicroInstruction.Condition.NEGATIVE);
        } else if (token.contains("Z")) {
            builder.cond(MicroInstruction.Condition.ZERO);
        }
    }

    private int parseRegister(String regName) {
        return switch (regName) {
            case "PC" -> 0;
            case "AC" -> 1;
            case "SP" -> 2;
            case "IR" -> 3;
            case "TIR" -> 4;
            case "0" -> 5;
            case "+1" -> 6;
            case "-1" -> 7;
            case "AMASK" -> 8;
            case "SMASK" -> 9;
            case "A" -> 10;
            case "B" -> 11;
            case "C" -> 12;
            case "D" -> 13;
            case "E" -> 14;
            case "F" -> 15;
            default -> {
                try {
                    yield Integer.parseInt(regName);
                } catch (NumberFormatException e) {
                    yield 0;
                }
            }
        };
    }

    private void resolveLabels(List<MicroInstruction> instructions, Map<Integer, String> instructionMap) {
        for (int i = 0; i < instructions.size(); i++) {
            String line = instructionMap.get(i);
            if (line != null && line.toUpperCase().contains("GOTO")) {
                String[] parts = line.split("[\\s,]+");
                for (int j = 0; j < parts.length; j++) {
                    String part = parts[j];
                    if (part.toUpperCase().equals("GOTO") && j + 1 < parts.length) {
                        String label = parts[j + 1].trim();
                        Integer addr = labels.get(label);
                        if (addr != null) {
                            MicroInstruction old = instructions.get(i);
                            MicroInstruction.Builder builder = new MicroInstruction.Builder()
                                .addr(addr)
                                .regA(old.getRegA())
                                .regB(old.getRegB())
                                .regC(old.getRegC())
                                .aluOp(old.getAluOp())
                                .shiftOp(old.getShiftOp())
                                .amux(old.isAmux())
                                .cond(old.getCond())
                                .mbr(old.isMbr())
                                .mar(old.isMar())
                                .rd(old.isRd())
                                .wr(old.isWr())
                                .enc(old.isEnc());
                            instructions.set(i, builder.build());
                            break;
                        }
                    } else if (part.toUpperCase().startsWith("GOTO") && part.length() > 4) {
                        String label = part.substring(4).trim();
                        Integer addr = labels.get(label);
                        if (addr != null) {
                            MicroInstruction old = instructions.get(i);
                            MicroInstruction.Builder builder = new MicroInstruction.Builder()
                                .addr(addr)
                                .regA(old.getRegA())
                                .regB(old.getRegB())
                                .regC(old.getRegC())
                                .aluOp(old.getAluOp())
                                .shiftOp(old.getShiftOp())
                                .amux(old.isAmux())
                                .cond(old.getCond())
                                .mbr(old.isMbr())
                                .mar(old.isMar())
                                .rd(old.isRd())
                                .wr(old.isWr())
                                .enc(old.isEnc());
                            instructions.set(i, builder.build());
                            break;
                        }
                    }
                }
            }
        }
    }

    public static class AssemblyResult {
        private final MicroInstruction[] instructions;
        private final Map<Integer, String> sourceMap;
        private final List<String> errors;
        private final boolean success;

        public AssemblyResult(MicroInstruction[] instructions, Map<Integer, String> sourceMap,
                            List<String> errors, boolean success) {
            this.instructions = instructions;
            this.sourceMap = sourceMap;
            this.errors = errors;
            this.success = success;
        }

        public MicroInstruction[] getInstructions() {
            return instructions;
        }

        public Map<Integer, String> getSourceMap() {
            return sourceMap;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getCompiledOutput() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < instructions.length; i++) {
                String source = sourceMap.get(i);
                sb.append(String.format("%03d: %s\n", i, source != null ? source : ""));
            }
            return sb.toString();
        }

        public String getErrorOutput() {
            if (errors.isEmpty()) {
                return "Assembly successful!";
            }
            return String.join("\n", errors);
        }
    }
}
