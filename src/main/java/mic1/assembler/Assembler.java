package mic1.assembler;

import mic1.core.ALU;
import mic1.core.MicroInstruction;
import mic1.core.Shifter;

import java.util.*;

public class Assembler {
    private static final int SMALL_VALUE_THRESHOLD = 16;
    
    private final Map<String, Integer> labels;
    private final List<String> errors;
    private int currentAddress;

    public Assembler() {
        this.labels = new HashMap<>();
        this.errors = new ArrayList<>();
        this.currentAddress = 0;
    }

    public AssemblyResult assemble(String sourceCode) {
        // primeira passada: processa linhas do codigo fonte
        // identifica labels, expande instrucoes ISA em microinstrucoes e constroi mapa de enderecos
        labels.clear();
        errors.clear();
        currentAddress = 0;

        String[] lines = sourceCode.split("\n");
        List<MicroInstruction> instructions = new ArrayList<>();
        Map<Integer, String> instructionMap = new HashMap<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // ignora linhas vazias e comentarios
            if (line.isEmpty() || line.startsWith("/") || line.startsWith("#") || line.startsWith(";")) {
                continue;
            }

            // registra labels com endereco atual
            if (line.endsWith(":")) {
                String label = line.substring(0, line.length() - 1).trim();
                if (!label.isEmpty()) {
                    labels.put(label, currentAddress);
                }
                continue;
            }

            try {
                List<MicroInstruction> insts = parseLine(line);
                if (insts != null && !insts.isEmpty()) {
                    for (MicroInstruction inst : insts) {
                        instructions.add(inst);
                        instructionMap.put(currentAddress, line);
                        currentAddress++;
                    }
                }
            } catch (Exception e) {
                errors.add(String.format("Linha %d: %s - %s", i + 1, line, e.getMessage()));
            }
        }

        // segunda passada: resolve referencias a labels em instrucoes goto
        resolveLabels(instructions, instructionMap);

        // garante que ultima instrucao sempre aponta para endereco valido
        if (!instructions.isEmpty()) {
            MicroInstruction last = instructions.get(instructions.size() - 1);
            int lastAddr = currentAddress - 1;
            int nextAddr = last.getAddr();
            
            // se endereco de destino esta alem do limite OU se condicao e NONE (incremento automatico),
            // aponta para ultima instrucao com condicao ALWAYS (loop infinito)
            if (nextAddr >= 512 || nextAddr < 0 || last.getCond() == MicroInstruction.Condition.NONE) {
                MicroInstruction.Builder builder = new MicroInstruction.Builder()
                    .regA(last.getRegA())
                    .regB(last.getRegB())
                    .regC(last.getRegC())
                    .aluOp(last.getAluOp())
                    .shiftOp(last.getShiftOp())
                    .amux(last.isAmux())
                    .cond(MicroInstruction.Condition.ALWAYS)
                    .mbr(last.isMbr())
                    .mar(last.isMar())
                    .rd(last.isRd())
                    .wr(last.isWr())
                    .enc(last.isEnc())
                    .addr(lastAddr);
                instructions.set(instructions.size() - 1, builder.build());
            }
        }

        return new AssemblyResult(
            instructions.toArray(new MicroInstruction[0]),
            instructionMap,
            errors,
            errors.isEmpty()
        );
    }

    private List<MicroInstruction> parseLine(String line) {
        // divide linha em tokens separados por espacos ou virgulas
        String[] tokens = line.split("[\\s,]+");
        if (tokens.length == 0) {
            return null;
        }

        String firstToken = tokens[0].trim().toUpperCase();
        
        // verifica se e instrucao ISA e expande em microinstrucoes
        if (isISAInstruction(firstToken)) {
            return parseISAInstruction(line, tokens);
        }

        MicroInstruction.Builder builder = new MicroInstruction.Builder();
        builder.addr(currentAddress);

        for (String token : tokens) {
            token = token.trim().toUpperCase();
            if (token.isEmpty()) continue;

            parseToken(token, builder);
        }

        List<MicroInstruction> result = new ArrayList<>();
        result.add(builder.build());
        return result;
    }

    // verifica se token e uma instrucao ISA de alto nivel
    private boolean isISAInstruction(String token) {
        return token.equals("LOCO") || token.equals("LODL") || token.equals("STOD") || 
               token.equals("STOL") || token.equals("ADDD") || token.equals("SUBD") || 
               token.equals("ADDL") || token.equals("SUBL") || token.equals("JUMP") || 
               token.equals("JNEG") || token.equals("JZER") || token.equals("JPOS") || 
               token.equals("JNZE") || token.equals("CALL") || token.equals("RETN") || 
               token.equals("PUSH") || token.equals("POP") || token.equals("PUSHI") || 
               token.equals("POPI") || token.equals("RET0") || token.equals("RET1") || 
               token.equals("RET2") || token.equals("RET3") || token.equals("RET4") || 
               token.equals("RET5") || token.equals("RET6") || token.equals("RET7") || 
               token.equals("LODD") || token.equals("LODI") || token.equals("STOI") ||
               token.equals("INSP") || token.equals("DESP");
    }

    // dispatcher para instrucoes ISA: identifica mnemonic e chama metodo de expansao correspondente
    private List<MicroInstruction> parseISAInstruction(String line, String[] tokens) {
        List<MicroInstruction> result = new ArrayList<>();
        String mnemonic = tokens[0].trim().toUpperCase();
        String operand = tokens.length > 1 ? tokens[1].trim() : "";

        switch (mnemonic) {
            case "LOCO":
                result.addAll(expandLOCO(operand));
                break;
            case "LODD":
            case "LODL":
                result.addAll(expandLODD(operand));
                break;
            case "STOD":
            case "STOL":
                result.addAll(expandSTOD(operand));
                break;
            case "ADDD":
            case "ADDL":
                result.addAll(expandADDD(operand));
                break;
            case "SUBD":
            case "SUBL":
                result.addAll(expandSUBD(operand));
                break;
            case "JUMP":
                result.addAll(expandJUMP(operand));
                break;
            case "JNEG":
                result.addAll(expandJNEG(operand));
                break;
            case "JZER":
                result.addAll(expandJZER(operand));
                break;
            case "JPOS":
                result.addAll(expandJPOS(operand));
                break;
            case "JNZE":
                result.addAll(expandJNZE(operand));
                break;
            case "CALL":
                result.addAll(expandCALL(operand));
                break;
            case "RETN":
                result.addAll(expandRETN());
                break;
            case "PUSH":
                result.addAll(expandPUSH());
                break;
            case "POP":
                result.addAll(expandPOP());
                break;
            case "PUSHI":
                result.addAll(expandPUSHI(operand));
                break;
            case "POPI":
                result.addAll(expandPOPI());
                break;
            case "INSP":
                result.addAll(expandINSP(operand));
                break;
            case "DESP":
                result.addAll(expandDESP(operand));
                break;
            default:
                errors.add("Instrucao ISA desconhecida: " + mnemonic);
        }

        return result;
    }

    // expande LOCO: carrega constante no acumulador
    // usa metodo direto para valores pequenos ou decomposicao binaria para valores grandes
    private List<MicroInstruction> expandLOCO(String operand) {
        int value = parseOperand(operand);
        
        if (Math.abs(value) < SMALL_VALUE_THRESHOLD) {
            return expandLOCODirect(value);
        } else {
            return expandLOCOViaMemory(value);
        }
    }
    
    private List<MicroInstruction> expandLOCODirect(int value) {
        List<MicroInstruction> result = new ArrayList<>();
        
        result.add(new MicroInstruction.Builder()
            .regA(5)
            .regB(0)
            .regC(1)
            .aluOp(ALU.Operation.PASS_A)
            .enc(true)
            .addr(currentAddress + 1)
            .build());
        
        if (value > 0) {
            for (int i = 0; i < value; i++) {
                result.add(new MicroInstruction.Builder()
                    .regA(1)
                    .regB(6)
                    .regC(1)
                    .aluOp(ALU.Operation.ADD)
                    .enc(true)
                    .addr(currentAddress + result.size() + 1)
                    .build());
            }
        // decrementa AC para valores negativos
        } else if (value < 0) {
            for (int i = 0; i < Math.abs(value); i++) {
                result.add(new MicroInstruction.Builder()
                    .regA(1)
                    .regB(7)
                    .regC(1)
                    .aluOp(ALU.Operation.ADD)
                    .enc(true)
                    .addr(currentAddress + result.size() + 1)
                    .build());
            }
        }
        
        if (!result.isEmpty()) {
            fixLastAddress(result);
        }
        
        return result;
    }
    
    private List<MicroInstruction> expandLOCOViaMemory(int value) {
        List<MicroInstruction> result = new ArrayList<>();
        
        result.add(new MicroInstruction.Builder()
            .regA(5)
            .regB(0)
            .regC(1)
            .aluOp(ALU.Operation.PASS_A)
            .enc(true)
            .addr(currentAddress + 1)
            .build());
        
        List<Integer> components = decomposeValueBinary(Math.abs(value));
        
        // constroi cada componente e adiciona ao AC
        for (int component : components) {
            // componente 1: simples incremento
            if (component == 1) {
                int nextAddr = currentAddress + result.size() + 1;
                result.add(new MicroInstruction.Builder()
                    .regA(1)
                    .regB(6)
                    .regC(1)
                    .aluOp(ALU.Operation.ADD)
                    .enc(true)
                    .addr(nextAddr)
                    .build());
            } else {
                // calcula numero de shifts necessarios para construir potencia de 2
                int power = 0;
                int temp = component;
                while (temp > 1) {
                    temp >>= 1;
                    power++;
                }
                
                result.add(new MicroInstruction.Builder()
                    .regA(5)
                    .regB(0)
                    .regC(10)
                    .aluOp(ALU.Operation.PASS_A)
                    .enc(true)
                    .addr(currentAddress + result.size() + 1)
                    .build());
                
                result.add(new MicroInstruction.Builder()
                    .regA(10)
                    .regB(6)
                    .regC(10)
                    .aluOp(ALU.Operation.ADD)
                    .enc(true)
                    .addr(currentAddress + result.size() + 1)
                    .build());
                
                for (int i = 0; i < power; i++) {
                    int nextAddr = currentAddress + result.size() + 1;
                    result.add(new MicroInstruction.Builder()
                        .regA(10)
                        .regB(10)
                        .regC(10)
                        .aluOp(ALU.Operation.ADD)
                        .shiftOp(Shifter.ShiftOperation.LEFT)
                        .enc(true)
                        .addr(nextAddr)
                        .build());
                }
                
                // adiciona componente construido ao AC
                int nextAddr = currentAddress + result.size() + 1;
                result.add(new MicroInstruction.Builder()
                    .regA(1)
                    .regB(10)
                    .regC(1)
                    .aluOp(ALU.Operation.ADD)
                    .enc(true)
                    .addr(nextAddr)
                    .build());
            }
        }
        
        // para valores negativos: aplica complemento de 2
        if (value < 0) {
            int nextAddr = currentAddress + result.size() + 1;
            result.add(new MicroInstruction.Builder()
                .regA(1)
                .regB(0)
                .regC(1)
                .aluOp(ALU.Operation.NOT_A)
                .enc(true)
                .addr(nextAddr)
                .build());
            
            result.add(new MicroInstruction.Builder()
                .regA(1)
                .regB(6)
                .regC(1)
                .aluOp(ALU.Operation.ADD)
                .enc(true)
                .addr(currentAddress + result.size() + 1)
                .build());
        }
        
        fixLastAddress(result);
        return result;
    }
    
    // decompoe valor em componentes binarios (potencias de 2)
    // exemplo: 101 = 64 + 32 + 4 + 1
    private List<Integer> decomposeValueBinary(int value) {
        List<Integer> components = new ArrayList<>();
        
        if (value == 0) {
            return components;
        }
        
        // percorre bits do valor e adiciona potencias de 2 encontradas
        int bit = 1;
        int remaining = value;
        
        while (remaining > 0 && bit <= value) {
            if ((remaining & bit) != 0) {
                components.add(bit);
                remaining -= bit;
            }
            bit <<= 1;
        }
        
        return components;
    }
    
    // constroi endereco de memoria usando decomposicao binaria eficiente
    // resultado e armazenado no registrador temporario especificado
    private List<MicroInstruction> setMemoryAddress(int address, int tempReg) {
        List<MicroInstruction> result = new ArrayList<>();
        
        // caso especial: endereco zero
        if (address == 0) {
            result.add(new MicroInstruction.Builder()
                .regA(5)
                .regB(0)
                .regC(tempReg)
                .aluOp(ALU.Operation.PASS_A)
                .enc(true)
                .addr(currentAddress + result.size() + 1)
                .build());
            return result;
        }
        
        // inicializa registrador temporario com zero
        result.add(new MicroInstruction.Builder()
            .regA(5)
            .regB(0)
            .regC(tempReg)
            .aluOp(ALU.Operation.PASS_A)
            .enc(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        // decompoe endereco em componentes binarios
        List<Integer> components = decomposeValueBinary(address);
        
        // constroi cada componente e adiciona ao registrador temporario
        for (int component : components) {
            // componente 1: simples incremento
            if (component == 1) {
                int nextAddr = currentAddress + result.size() + 1;
                result.add(new MicroInstruction.Builder()
                    .regA(tempReg)
                    .regB(6)
                    .regC(tempReg)
                    .aluOp(ALU.Operation.ADD)
                    .enc(true)
                    .addr(nextAddr)
                    .build());
            } else {
                // inicializa registrador temporario B com zero
                result.add(new MicroInstruction.Builder()
                    .regA(5)
                    .regB(0)
                    .regC(11)
                    .aluOp(ALU.Operation.PASS_A)
                    .enc(true)
                    .addr(currentAddress + result.size() + 1)
                    .build());
                
                // coloca 1 no registrador temporario B
                result.add(new MicroInstruction.Builder()
                    .regA(11)
                    .regB(6)
                    .regC(11)
                    .aluOp(ALU.Operation.ADD)
                    .enc(true)
                    .addr(currentAddress + result.size() + 1)
                    .build());
                
                // calcula numero de shifts necessarios para construir potencia de 2
                int power = 0;
                int temp = component;
                while (temp > 1) {
                    temp >>= 1;
                    power++;
                }
                
                // aplica shifts left para construir potencia de 2
                for (int i = 0; i < power; i++) {
                    int nextAddr = currentAddress + result.size() + 1;
                    result.add(new MicroInstruction.Builder()
                        .regA(11)
                        .regB(5)
                        .regC(11)
                        .aluOp(ALU.Operation.PASS_A)
                        .shiftOp(Shifter.ShiftOperation.LEFT)
                        .enc(true)
                        .addr(nextAddr)
                        .build());
                }
                
                // adiciona componente construido ao registrador temporario de endereco
                int nextAddr = currentAddress + result.size() + 1;
                result.add(new MicroInstruction.Builder()
                    .regA(tempReg)
                    .regB(11)
                    .regC(tempReg)
                    .aluOp(ALU.Operation.ADD)
                    .enc(true)
                    .addr(nextAddr)
                    .build());
            }
        }
        
        return result;
    }
    
    // corrige campo addr da ultima microinstrucao para apontar para proximo endereco valido
    // se proximo endereco estiver alem do limite (512), aponta para ela mesma (loop infinito)
    private void fixLastAddress(List<MicroInstruction> instructions) {
        if (instructions.isEmpty()) return;
        
        MicroInstruction last = instructions.get(instructions.size() - 1);
        int nextAddr = currentAddress + instructions.size();
        int lastIndex = currentAddress + instructions.size() - 1;
        
        // se proximo endereco esta alem do limite da control store (512), faz loop infinito
        if (nextAddr >= 512 || nextAddr < 0) {
            nextAddr = lastIndex;
        }
        
        MicroInstruction.Builder builder = new MicroInstruction.Builder()
            .regA(last.getRegA())
            .regB(last.getRegB())
            .regC(last.getRegC())
            .aluOp(last.getAluOp())
            .shiftOp(last.getShiftOp())
            .amux(last.isAmux())
            .cond(last.getCond())
            .mbr(last.isMbr())
            .mar(last.isMar())
            .rd(last.isRd())
            .wr(last.isWr())
            .enc(last.isEnc())
            .addr(nextAddr);
        instructions.set(instructions.size() - 1, builder.build());
    }

    // expande LODD: carrega valor da memoria no acumulador
    private List<MicroInstruction> expandLODD(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int address = parseOperand(operand);
        
        // constroi endereco de memoria no registrador temporario A
        List<MicroInstruction> setAddr = setMemoryAddress(address, 10);
        result.addAll(setAddr);
        
        // configura MAR com endereco calculado
        int nextAddr = currentAddress + result.size();
        result.add(new MicroInstruction.Builder()
            .regA(10)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(nextAddr + 1)
            .build());
        
        // le valor da memoria para MBR
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .rd(true)
            .addr(nextAddr + 2)
            .build());
        
        // copia MBR para AC via AMUX
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(1)
            .aluOp(ALU.Operation.PASS_A)
            .enc(true)
            .amux(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        fixLastAddress(result);
        return result;
    }

    // expande STOD: armazena valor do acumulador na memoria
    private List<MicroInstruction> expandSTOD(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int address = parseOperand(operand);
        
        // constroi endereco de memoria no registrador temporario A
        List<MicroInstruction> setAddr = setMemoryAddress(address, 10);
        result.addAll(setAddr);
        
        // configura MAR com endereco calculado
        int nextAddr = currentAddress + result.size();
        result.add(new MicroInstruction.Builder()
            .regA(10)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(nextAddr + 1)
            .build());
        
        // copia AC para MBR
        result.add(new MicroInstruction.Builder()
            .regA(1)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mbr(true)
            .addr(nextAddr + 2)
            .build());
        
        // escreve MBR na memoria no endereco especificado por MAR
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .wr(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        fixLastAddress(result);
        return result;
    }

    // expande ADDD: soma valor da memoria ao acumulador
    private List<MicroInstruction> expandADDD(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int address = parseOperand(operand);
        
        // constroi endereco de memoria no registrador temporario A
        List<MicroInstruction> setAddr = setMemoryAddress(address, 10);
        result.addAll(setAddr);
        
        // configura MAR com endereco calculado
        int nextAddr = currentAddress + result.size();
        result.add(new MicroInstruction.Builder()
            .regA(10)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(nextAddr + 1)
            .build());
        
        // le valor da memoria para MBR
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .rd(true)
            .addr(nextAddr + 2)
            .build());
        
        // soma AC + MBR e armazena resultado no AC
        // AMUX=true: entrada A vem de MBR, entrada B vem de AC (regB=1)
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(1)
            .regC(1)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .amux(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        fixLastAddress(result);
        return result;
    }

    // expande SUBD: subtrai valor da memoria do acumulador
    private List<MicroInstruction> expandSUBD(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int address = parseOperand(operand);
        
        // constroi endereco de memoria no registrador temporario A
        List<MicroInstruction> setAddr = setMemoryAddress(address, 10);
        result.addAll(setAddr);
        
        // configura MAR com endereco calculado
        int nextAddr = currentAddress + result.size();
        result.add(new MicroInstruction.Builder()
            .regA(10)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(nextAddr + 1)
            .build());
        
        // le valor da memoria para MBR
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .rd(true)
            .addr(nextAddr + 2)
            .build());
        
        // copia MBR para registrador temporario B via AMUX
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(11)
            .aluOp(ALU.Operation.PASS_A)
            .enc(true)
            .amux(true)
            .addr(nextAddr + 3)
            .build());
        
        // calcula complemento de 2 de MBR: NOT(MBR) + 1
        result.add(new MicroInstruction.Builder()
            .regA(11)
            .regB(0)
            .regC(11)
            .aluOp(ALU.Operation.NOT_A)
            .enc(true)
            .addr(nextAddr + 4)
            .build());
        
        result.add(new MicroInstruction.Builder()
            .regA(11)
            .regB(6)
            .regC(11)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .addr(nextAddr + 5)
            .build());
        
        // subtrai: AC - MBR = AC + (-MBR) = AC + NOT(MBR) + 1
        result.add(new MicroInstruction.Builder()
            .regA(1)
            .regB(11)
            .regC(1)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        fixLastAddress(result);
        return result;
    }

    // expande JUMP: salto incondicional para endereco ou label
    private List<MicroInstruction> expandJUMP(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        Integer addr = labels.get(operand);
        if (addr == null) {
            try {
                addr = Integer.parseInt(operand);
            } catch (NumberFormatException e) {
                errors.add("Label ou endereco invalido: " + operand);
                addr = currentAddress + 1;
            }
        }
        
        // atualiza PC com endereco de destino (salto incondicional)
        result.add(new MicroInstruction.Builder()
            .regA(5)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.ALWAYS)
            .addr(addr)
            .build());
        
        return result;
    }

    // expande JNEG: salto condicional se AC negativo
    private List<MicroInstruction> expandJNEG(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        Integer addr = labels.get(operand);
        if (addr == null) {
            try {
                addr = Integer.parseInt(operand);
            } catch (NumberFormatException e) {
                errors.add("Label ou endereco invalido: " + operand);
                addr = currentAddress + 1;
            }
        }
        
        // atualiza PC se flag negativo estiver setada
        result.add(new MicroInstruction.Builder()
            .regA(1)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.NEGATIVE)
            .addr(addr)
            .build());
        
        return result;
    }

    // expande JZER: salto condicional se AC zero
    private List<MicroInstruction> expandJZER(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        Integer addr = labels.get(operand);
        if (addr == null) {
            try {
                addr = Integer.parseInt(operand);
            } catch (NumberFormatException e) {
                errors.add("Label ou endereco invalido: " + operand);
                addr = currentAddress + 1;
            }
        }
        
        // atualiza PC se flag zero estiver setada
        result.add(new MicroInstruction.Builder()
            .regA(1)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.ZERO)
            .addr(addr)
            .build());
        
        return result;
    }

    // expande JPOS: salto condicional se AC positivo (nao negativo e nao zero)
    private List<MicroInstruction> expandJPOS(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        Integer addr = labels.get(operand);
        if (addr == null) {
            try {
                addr = Integer.parseInt(operand);
            } catch (NumberFormatException e) {
                errors.add("Label ou endereco invalido: " + operand);
                addr = currentAddress + 1;
            }
        }
        
        // pula proxima instrucao se negativo (continua se positivo)
        result.add(new MicroInstruction.Builder()
            .regA(1)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.NEGATIVE)
            .addr(currentAddress + 1)
            .build());
        
        // atualiza PC com endereco de destino
        result.add(new MicroInstruction.Builder()
            .regA(5)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.ALWAYS)
            .addr(addr)
            .build());
        
        return result;
    }

    // expande JNZE: salto condicional se AC nao zero
    private List<MicroInstruction> expandJNZE(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        Integer addr = labels.get(operand);
        if (addr == null) {
            try {
                addr = Integer.parseInt(operand);
            } catch (NumberFormatException e) {
                errors.add("Label ou endereco invalido: " + operand);
                addr = currentAddress + 1;
            }
        }
        
        // pula proxima instrucao se zero (continua se nao zero)
        result.add(new MicroInstruction.Builder()
            .regA(1)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.ZERO)
            .addr(currentAddress + 1)
            .build());
        
        // atualiza PC com endereco de destino
        result.add(new MicroInstruction.Builder()
            .regA(5)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.ALWAYS)
            .addr(addr)
            .build());
        
        return result;
    }

    // expande CALL: chama subrotina salvando endereco de retorno na pilha
    private List<MicroInstruction> expandCALL(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        Integer addr = labels.get(operand);
        if (addr == null) {
            try {
                addr = Integer.parseInt(operand);
            } catch (NumberFormatException e) {
                errors.add("Label ou endereco invalido: " + operand);
                addr = currentAddress + 1;
            }
        }
        
        // coloca PC+1 no MBR (endereco de retorno)
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(6)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .mbr(true)
            .addr(currentAddress + 1)
            .build());
        
        // configura MAR com SP
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(currentAddress + 2)
            .build());
        
        // salva endereco de retorno na pilha
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .wr(true)
            .addr(currentAddress + 3)
            .build());
        
        // incrementa SP
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(6)
            .regC(2)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .addr(currentAddress + 4)
            .build());
        
        // salta para endereco da subrotina
        result.add(new MicroInstruction.Builder()
            .regA(5)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.ALWAYS)
            .addr(addr)
            .build());
        
        return result;
    }

    // expande RETN: retorna de subrotina restaurando endereco de retorno da pilha
    private List<MicroInstruction> expandRETN() {
        List<MicroInstruction> result = new ArrayList<>();
        
        // decrementa SP
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(7)
            .regC(2)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .addr(currentAddress + 1)
            .build());
        
        // configura MAR com SP
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(currentAddress + 2)
            .build());
        
        // le endereco de retorno da pilha para MBR
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .rd(true)
            .amux(true)
            .addr(currentAddress + 3)
            .build());
        
        // atualiza PC com endereco de retorno via AMUX
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .cond(MicroInstruction.Condition.ALWAYS)
            .amux(true)
            .addr(0)
            .build());
        
        return result;
    }

    // expande PUSH: empilha valor do acumulador na pilha
    private List<MicroInstruction> expandPUSH() {
        List<MicroInstruction> result = new ArrayList<>();
        
        // copia AC para MBR
        result.add(new MicroInstruction.Builder()
            .regA(1)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mbr(true)
            .addr(currentAddress + 1)
            .build());
        
        // configura MAR com SP
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(currentAddress + 2)
            .build());
        
        // escreve valor na pilha
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .wr(true)
            .addr(currentAddress + 3)
            .build());
        
        // decrementa SP (pilha cresce para baixo)
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(7)
            .regC(2)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .addr(currentAddress + 4)
            .build());
        
        return result;
    }

    // expande POP: desempilha valor da pilha para o acumulador
    private List<MicroInstruction> expandPOP() {
        List<MicroInstruction> result = new ArrayList<>();
        
        // incrementa SP (pilha cresce para baixo)
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(6)
            .regC(2)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .addr(currentAddress + 1)
            .build());
        
        // configura MAR com SP
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(currentAddress + 2)
            .build());
        
        // le valor da pilha para MBR
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .rd(true)
            .amux(true)
            .addr(currentAddress + 3)
            .build());
        
        // copia MBR para AC via AMUX
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(1)
            .aluOp(ALU.Operation.PASS_A)
            .enc(true)
            .amux(true)
            .addr(currentAddress + 4)
            .build());
        
        return result;
    }

    private List<MicroInstruction> expandPUSHI(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int value = parseOperand(operand);
        
        result.add(new MicroInstruction.Builder()
            .regA(5)
            .regB(0)
            .regC(10)
            .aluOp(ALU.Operation.PASS_A)
            .enc(true)
            .addr(currentAddress + 1)
            .build());
        
        for (int i = 0; i < value; i++) {
            result.add(new MicroInstruction.Builder()
                .regA(10)
                .regB(6)
                .regC(10)
                .aluOp(ALU.Operation.ADD)
                .enc(true)
                .addr(currentAddress + result.size() + 1)
                .build());
        }
        
        result.add(new MicroInstruction.Builder()
            .regA(10)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mbr(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .wr(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(7)
            .regC(2)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .addr(currentAddress + result.size() + 1)
            .build());
        
        if (!result.isEmpty()) {
            MicroInstruction last = result.get(result.size() - 1);
            MicroInstruction.Builder builder = new MicroInstruction.Builder()
                .regA(last.getRegA())
                .regB(last.getRegB())
                .regC(last.getRegC())
                .aluOp(last.getAluOp())
                .shiftOp(last.getShiftOp())
                .amux(last.isAmux())
                .cond(last.getCond())
                .mbr(last.isMbr())
                .mar(last.isMar())
                .rd(last.isRd())
                .wr(last.isWr())
                .enc(last.isEnc())
                .addr(currentAddress + result.size());
            result.set(result.size() - 1, builder.build());
        }
        
        return result;
    }

    private List<MicroInstruction> expandPOPI() {
        List<MicroInstruction> result = new ArrayList<>();
        
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(6)
            .regC(2)
            .aluOp(ALU.Operation.ADD)
            .enc(true)
            .addr(currentAddress + 1)
            .build());
        
        result.add(new MicroInstruction.Builder()
            .regA(2)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.PASS_A)
            .mar(true)
            .addr(currentAddress + 2)
            .build());
        
        result.add(new MicroInstruction.Builder()
            .regA(0)
            .regB(0)
            .regC(0)
            .aluOp(ALU.Operation.ADD)
            .rd(true)
            .amux(true)
            .addr(currentAddress + 3)
            .build());
        
        return result;
    }

    private List<MicroInstruction> expandINSP(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int value = parseOperand(operand);
        
        for (int i = 0; i < value; i++) {
            result.add(new MicroInstruction.Builder()
                .regA(2)
                .regB(7)
                .regC(2)
                .aluOp(ALU.Operation.ADD)
                .enc(true)
                .addr(currentAddress + result.size() + 1)
                .build());
        }
        
        return result;
    }

    private List<MicroInstruction> expandDESP(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int value = parseOperand(operand);
        
        for (int i = 0; i < value; i++) {
            result.add(new MicroInstruction.Builder()
                .regA(2)
                .regB(6)
                .regC(2)
                .aluOp(ALU.Operation.ADD)
                .enc(true)
                .addr(currentAddress + result.size() + 1)
                .build());
        }
        
        return result;
    }

    // converte operando em valor numerico: tenta resolver como label primeiro, depois como numero
    private int parseOperand(String operand) {
        if (operand == null || operand.isEmpty()) {
            return 0;
        }
        
        operand = operand.trim();
        
        // verifica se e um label conhecido
        Integer labelAddr = labels.get(operand);
        if (labelAddr != null) {
            return labelAddr;
        }
        
        // tenta converter para numero
        try {
            return Integer.parseInt(operand);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // processa token de microinstrucao e atualiza builder correspondente
    private void parseToken(String token, MicroInstruction.Builder builder) {
        // registradores A, B, C
        if (token.startsWith("A=")) {
            int reg = parseRegister(token.substring(2));
            builder.regA(reg);
        } else if (token.startsWith("B=")) {
            int reg = parseRegister(token.substring(2));
            builder.regB(reg);
        } else if (token.startsWith("C=")) {
            int reg = parseRegister(token.substring(2));
            builder.regC(reg);
        // operacoes ALU
        } else if (token.equals("ADD")) {
            builder.aluOp(ALU.Operation.ADD);
        } else if (token.equals("AND")) {
            builder.aluOp(ALU.Operation.AND);
        } else if (token.equals("PASS") || token.equals("PASSA")) {
            builder.aluOp(ALU.Operation.PASS_A);
        } else if (token.equals("NOT") || token.equals("NOTA")) {
            builder.aluOp(ALU.Operation.NOT_A);
        // operacoes de shift
        } else if (token.equals("SHL") || token.equals("LEFT")) {
            builder.shiftOp(Shifter.ShiftOperation.LEFT);
        } else if (token.equals("SHR") || token.equals("RIGHT")) {
            builder.shiftOp(Shifter.ShiftOperation.RIGHT);
        // controle de memoria e barramentos
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
        // condicoes e saltos
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

    // converte token de condicao em enum correspondente
    private void parseCondition(String token, MicroInstruction.Builder builder) {
        if (token.contains("N")) {
            builder.cond(MicroInstruction.Condition.NEGATIVE);
        } else if (token.contains("Z")) {
            builder.cond(MicroInstruction.Condition.ZERO);
        }
    }

    // converte nome de registrador em codigo numerico
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
        // segunda passada: substitui nomes de labels por enderecos numericos
        // procura instrucoes goto e atualiza campo addr com endereco do label
        for (int i = 0; i < instructions.size(); i++) {
            String line = instructionMap.get(i);
            if (line != null && line.toUpperCase().contains("GOTO")) {
                String[] parts = line.split("[\\s,]+");
                for (int j = 0; j < parts.length; j++) {
                    String part = parts[j];
                    // suporta formato "GOTO label" e "GOTOlabel"
                    if (part.toUpperCase().equals("GOTO") && j + 1 < parts.length) {
                        String label = parts[j + 1].trim();
                        Integer addr = labels.get(label);
                        if (addr != null) {
                            // reconstrói instrucao com endereco resolvido
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
                return "Montagem concluida com sucesso!";
            }
            return String.join("\n", errors);
        }
    }
}
