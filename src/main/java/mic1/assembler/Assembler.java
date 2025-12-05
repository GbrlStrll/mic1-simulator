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
        
        // 1. AC = 0 (inicializa acumulador com zero)
        result.add(new MicroInstruction.Builder()
            .regA(5) // registrador constante 0 (indice 5) como entrada A porque LOCO precisa inicializar AC com zero antes de somar valor
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(1) // AC (indice 1) como destino porque LOCO carrega constante no acumulador (AC = 0 inicialmente)
            .aluOp(ALU.Operation.PASS_A) // passa valor 0 para resultado porque precisamos zerar AC antes de construir valor (LOCO: AC = value)
            .enc(true) // habilita escrita em C porque sem isso AC nao seria inicializado com zero
            .addr(currentAddress + 1) // proxima microinstrucao (incremento ou decremento)
            .build());
        
        // 2. AC = AC + 1 (repete value vezes para valores positivos)
        if (value > 0) {
            for (int i = 0; i < value; i++) {
                result.add(new MicroInstruction.Builder()
                    .regA(1) // AC como entrada A porque estamos incrementando AC repetidamente (AC = AC + 1, value vezes)
                    .regB(6) // registrador constante +1 (indice 6) como entrada B porque precisamos somar 1 ao AC a cada iteracao
                    .regC(1) // AC como destino porque estamos construindo valor positivo incrementando AC (LOCO: AC = 0 + 1 + 1 + ...)
                    .aluOp(ALU.Operation.ADD) // AC + 1 porque para valores pequenos construimos somando 1 repetidamente (mais eficiente que decomposicao binaria)
                    .enc(true) // habilita escrita em C porque sem isso AC nao seria atualizado a cada incremento
                    .addr(currentAddress + result.size() + 1) // proxima microinstrucao (proximo incremento ou fim)
                    .build());
            }
        // 3. AC = AC + (-1) (repete |value| vezes para valores negativos)
        } else if (value < 0) {
            for (int i = 0; i < Math.abs(value); i++) {
                result.add(new MicroInstruction.Builder()
                    .regA(1) // AC como entrada A porque estamos decrementando AC repetidamente (AC = AC + (-1), |value| vezes)
                    .regB(7) // registrador constante -1 (indice 7) como entrada B porque precisamos subtrair 1 do AC a cada iteracao (soma com -1)
                    .regC(1) // AC como destino porque estamos construindo valor negativo decrementando AC (LOCO: AC = 0 + (-1) + (-1) + ...)
                    .aluOp(ALU.Operation.ADD) // AC + (-1) porque para valores negativos pequenos construimos somando -1 repetidamente
                    .enc(true) // habilita escrita em C porque sem isso AC nao seria atualizado a cada decremento
                    .addr(currentAddress + result.size() + 1) // proxima microinstrucao (proximo decremento ou fim)
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
        
        // 1. AC = 0 (inicializa acumulador com zero)
        result.add(new MicroInstruction.Builder()
            .regA(5) // registrador constante 0 (indice 5) como entrada A porque LOCO precisa inicializar AC com zero antes de construir valor
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(1) // AC (indice 1) como destino porque LOCO carrega constante no acumulador (AC = 0 inicialmente)
            .aluOp(ALU.Operation.PASS_A) // passa valor 0 para resultado porque precisamos zerar AC antes de construir valor grande via decomposicao binaria
            .enc(true) // habilita escrita em C porque sem isso AC nao seria inicializado com zero
            .addr(currentAddress + 1) // proxima microinstrucao (construcao de componentes)
            .build());
        
        List<Integer> components = decomposeValueBinary(Math.abs(value));
        
        // 2. Constrói cada componente binário e adiciona ao AC
        for (int component : components) {
            // componente 1: simples incremento
            if (component == 1) {
                int nextAddr = currentAddress + result.size() + 1;
                result.add(new MicroInstruction.Builder()
                    .regA(1) // AC como entrada A porque estamos adicionando componente 1 ao AC (AC = AC + 1)
                    .regB(6) // registrador constante +1 (indice 6) como entrada B porque componente 1 e simplesmente somar 1
                    .regC(1) // AC como destino porque estamos construindo valor somando componentes binarios ao AC
                    .aluOp(ALU.Operation.ADD) // AC + 1 porque componente 1 e adicionado diretamente sem necessidade de shifts
                    .enc(true) // habilita escrita em C porque sem isso AC nao seria atualizado com componente
                    .addr(nextAddr) // proxima microinstrucao (proximo componente ou fim)
                    .build());
            } else {
                // calcula numero de shifts necessarios para construir potencia de 2
                int power = 0;
                int temp = component;
                while (temp > 1) {
                    temp >>= 1;
                    power++;
                }
                
                // A = 0 (inicializa temporário A)
                result.add(new MicroInstruction.Builder()
                    .regA(5) // registrador constante 0 (indice 5) como entrada A porque precisamos inicializar registrador temporario A com zero
                    .regB(0) // nao usado (PASS_A ignora B)
                    .regC(10) // registrador temporario A (indice 10) como destino porque vamos construir potencia de 2 aqui antes de adicionar ao AC
                    .aluOp(ALU.Operation.PASS_A) // passa 0 para resultado porque precisamos inicializar temporario A antes de construir potencia de 2
                    .enc(true) // habilita escrita em C porque sem isso temporario A nao seria inicializado
                    .addr(currentAddress + result.size() + 1) // proxima: colocar 1 no temporario A
                    .build());
                
                // A = A + 1 (coloca 1 no temporário A)
                result.add(new MicroInstruction.Builder()
                    .regA(10) // temporario A como entrada A porque vamos incrementar de 0 para 1 (inicio da construcao de potencia de 2)
                    .regB(6) // registrador constante +1 (indice 6) como entrada B porque precisamos colocar 1 no temporario A
                    .regC(10) // temporario A como destino porque estamos inicializando com 1 antes de aplicar shifts para construir potencia de 2
                    .aluOp(ALU.Operation.ADD) // temporario A + 1 porque precisamos colocar 1 no temporario A (base para shifts)
                    .enc(true) // habilita escrita em C porque sem isso temporario A nao seria inicializado com 1
                    .addr(currentAddress + result.size() + 1) // proxima: aplicar shifts left
                    .build());
                
                // A = A << 1 (repete power vezes para construir potência de 2)
                for (int i = 0; i < power; i++) {
                    int nextAddr = currentAddress + result.size() + 1;
                    result.add(new MicroInstruction.Builder()
                        .regA(10) // temporario A como entrada A porque estamos aplicando shift left no valor (construindo potencia de 2)
                        .regB(0) // nao usado (PASS_A ignora B, shift e operacao unaria)
                        .regC(10) // temporario A como destino porque shift left dobra o valor (1 -> 2 -> 4 -> 8 -> ...)
                        .aluOp(ALU.Operation.PASS_A) // passa valor de A para resultado porque shift e aplicado no resultado da ALU antes de escrever
                        .shiftOp(Shifter.ShiftOperation.LEFT) // shift left porque cada shift dobra o valor (construindo potencia de 2: 2^power)
                        .enc(true) // habilita escrita em C porque sem isso temporario A nao seria atualizado com valor shiftado
                        .addr(nextAddr) // proxima microinstrucao (proximo shift ou adicionar ao AC)
                        .build());
                }
                
                // AC = AC + A (adiciona componente construído ao AC)
                int nextAddr = currentAddress + result.size() + 1;
                result.add(new MicroInstruction.Builder()
                    .regA(1) // AC como entrada A porque estamos adicionando componente construido ao acumulador (AC = AC + componente)
                    .regB(10) // temporario A como entrada B porque temporario A contem potencia de 2 construida (componente binario)
                    .regC(1) // AC como destino porque estamos construindo valor final somando componentes binarios ao AC
                    .aluOp(ALU.Operation.ADD) // AC + temporario A porque componente binario construido e adicionado ao AC (decomposicao: value = soma de componentes)
                    .enc(true) // habilita escrita em C porque sem isso AC nao seria atualizado com componente
                    .addr(nextAddr) // proxima microinstrucao (proximo componente ou complemento de 2 se negativo)
                    .build());
            }
        }
        
        // 3. Para valores negativos: aplica complemento de 2
        if (value < 0) {
            int nextAddr = currentAddress + result.size() + 1;
            // AC = NOT(AC) (primeiro passo do complemento de 2)
            result.add(new MicroInstruction.Builder()
                .regA(1) // AC como entrada A porque vamos inverter todos os bits do AC (primeiro passo do complemento de 2)
                .regB(0) // nao usado (NOT_A ignora B)
                .regC(1) // AC como destino porque complemento de 2: NOT(AC) + 1 (primeiro fazemos NOT)
                .aluOp(ALU.Operation.NOT_A) // NOT(AC) porque para valores negativos precisamos aplicar complemento de 2 (inverter bits e somar 1)
                .enc(true) // habilita escrita em C porque sem isso AC nao seria atualizado com valor invertido
                .addr(nextAddr) // proxima: somar 1 para completar complemento de 2
                .build());
            
            // AC = AC + 1 (completa complemento de 2)
            result.add(new MicroInstruction.Builder()
                .regA(1) // AC (agora com bits invertidos) como entrada A porque precisamos somar 1 para completar complemento de 2
                .regB(6) // registrador constante +1 (indice 6) como entrada B porque complemento de 2 = NOT(value) + 1
                .regC(1) // AC como destino porque estamos completando complemento de 2 (AC = NOT(AC) + 1)
                .aluOp(ALU.Operation.ADD) // AC + 1 porque segundo passo do complemento de 2 e somar 1 ao valor invertido
                .enc(true) // habilita escrita em C porque sem isso AC nao seria atualizado com valor negativo final
                .addr(currentAddress + result.size() + 1) // proxima microinstrucao (fim)
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
                .regA(5) // 5 = constante 0 (inicializa com zero)
                .regB(0) // 0 = PC (nao usado)
                .regC(tempReg) // registrador temporario de destino
                .aluOp(ALU.Operation.PASS_A) // copia 0 para destino
                .enc(true) // habilita escrita
                .addr(currentAddress + result.size() + 1)
                .build());
            return result;
        }
        
        // inicializa registrador temporario com zero
        result.add(new MicroInstruction.Builder()
            .regA(5) // 5 = constante 0
            .regB(0) // 0 = PC (nao usado)
            .regC(tempReg) // registrador temporario de destino
            .aluOp(ALU.Operation.PASS_A) // copia 0 para destino
            .enc(true) // habilita escrita
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
                    .regA(tempReg) // registrador temporario (valor atual)
                    .regB(6) // 6 = constante +1
                    .regC(tempReg) // registrador temporario (atualizado)
                    .aluOp(ALU.Operation.ADD) // adiciona 1
                    .enc(true) // habilita escrita
                    .addr(nextAddr)
                    .build());
            } else {
                // inicializa registrador temporario B com zero
                result.add(new MicroInstruction.Builder()
                    .regA(5) // 5 = constante 0
                    .regB(0) // 0 = PC (nao usado)
                    .regC(11) // 11 = registrador temporario B
                    .aluOp(ALU.Operation.PASS_A) // zera temp B
                    .enc(true) // habilita escrita
                    .addr(currentAddress + result.size() + 1)
                    .build());
                
                // coloca 1 no registrador temporario B
                result.add(new MicroInstruction.Builder()
                    .regA(11) // 11 = registrador temporario B
                    .regB(6) // 6 = constante +1
                    .regC(11) // 11 = registrador temporario B
                    .aluOp(ALU.Operation.ADD) // B = B + 1 (poe 1 em B)
                    .enc(true) // habilita escrita
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
                        .regA(11) // 11 = registrador temporario B
                        .regB(0) // 0 = PC (nao usado)
                        .regC(11) // 11 = registrador temporario B
                        .aluOp(ALU.Operation.PASS_A) // passa B para shifter
                        .shiftOp(Shifter.ShiftOperation.LEFT) // shift left
                        .enc(true) // habilita escrita
                        .addr(nextAddr)
                        .build());
                }
                
                // adiciona componente construido ao registrador temporario de endereco
                int nextAddr = currentAddress + result.size() + 1;
                result.add(new MicroInstruction.Builder()
                    .regA(tempReg) // registrador temporario de endereco
                    .regB(11) // 11 = registrador temporario B (componente)
                    .regC(tempReg) // registrador temporario de endereco
                    .aluOp(ALU.Operation.ADD) // soma componente ao endereco
                    .enc(true) // habilita escrita
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
        
        // 1. Constrói endereço no registrador temporário A (índice 10)
        List<MicroInstruction> setAddr = setMemoryAddress(address, 10);
        result.addAll(setAddr);
        
        // 2. MAR = A (configura endereço de memória)
        int nextAddr = currentAddress + result.size();
        result.add(new MicroInstruction.Builder()
            .regA(10) // 10 = registrador temporario A (contem endereco calculado)
            .regB(0) // 0 = PC (nao usado nesta operacao)
            .regC(0) // 0 = PC (nao usado, escrita desabilitada)
            .aluOp(ALU.Operation.PASS_A) // passa endereco para MAR
            .mar(true) // carrega MAR com endereco
            .addr(nextAddr + 1) // proxima: leitura da memoria
            .build());
        
        // 3. Lê memória: Memory[MAR] → MBR
        // Ciclo separado necessário: protocolo MIC-1 requer MAR configurado antes de RD
        // Operação ALU irrelevante: quando rd=true, apenas sinal de controle importa, operandos são ignorados
        result.add(new MicroInstruction.Builder()
            .regA(0) // Não usado (apenas sinal RD importa)
            .regB(0) // Não usado
            .regC(0) // Não usado (enc=false, resultado vai para MBR não registrador)
            .aluOp(ALU.Operation.ADD) // Operação irrelevante (qualquer operação serve quando apenas RD importa)
            .rd(true) // Executa leitura: Memory[MAR] → MBR (dado lido fica disponível em MBR)
            .addr(nextAddr + 2) // Próxima: copiar MBR para AC
            .build());
        
        // 4. AC = MBR (via AMUX)
        result.add(new MicroInstruction.Builder()
            .regA(0) // 0 = PC (nao usado, entrada vem de MBR via AMUX)
            .regB(0) // 0 = PC (nao usado)
            .regC(1) // 1 = AC (destino do carregamento)
            .aluOp(ALU.Operation.PASS_A) // passa MBR para AC
            .enc(true) // habilita escrita no AC
            .amux(true) // seleciona MBR como entrada A
            .addr(currentAddress + result.size() + 1)
            .build());
        
        fixLastAddress(result);
        return result;
    }

    // expande STOD: armazena valor do acumulador na memoria
    private List<MicroInstruction> expandSTOD(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int address = parseOperand(operand);
        
        // 1. Constrói endereço no registrador temporário A (índice 10)
        List<MicroInstruction> setAddr = setMemoryAddress(address, 10);
        result.addAll(setAddr);
        
        // 2. MAR = A (configura endereço de memória)
        int nextAddr = currentAddress + result.size();
        result.add(new MicroInstruction.Builder()
            .regA(10) // 10 = registrador temporario A (contem endereco)
            .regB(0) // 0 = PC (nao usado)
            .regC(0) // 0 = PC (nao usado)
            .aluOp(ALU.Operation.PASS_A) // passa endereco para MAR
            .mar(true) // carrega MAR
            .addr(nextAddr + 1) // proxima: copiar AC para MBR
            .build());
        
        // 3. MBR = AC (copia valor do acumulador para MBR)
        result.add(new MicroInstruction.Builder()
            .regA(1) // 1 = AC (valor a ser escrito na memoria)
            .regB(0) // 0 = PC (nao usado)
            .regC(0) // 0 = PC (nao usado)
            .aluOp(ALU.Operation.PASS_A) // passa AC para MBR
            .mbr(true) // carrega MBR
            .addr(nextAddr + 2) // proxima: escrita na memoria
            .build());
        
        // 4. Escreve memória: MBR → Memory[MAR]
        result.add(new MicroInstruction.Builder()
            .regA(0) // 0 = PC (nao usado, apenas escrita)
            .regB(0) // 0 = PC (nao usado)
            .regC(0) // 0 = PC (nao usado)
            .aluOp(ALU.Operation.ADD) // operacao irrelevante
            .wr(true) // escreve MBR na memoria
            .addr(currentAddress + result.size() + 1)
            .build());
        
        fixLastAddress(result);
        return result;
    }

    // expande ADDD: soma valor da memoria ao acumulador
    private List<MicroInstruction> expandADDD(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int address = parseOperand(operand);
        
        // 1. Constrói endereço no registrador temporário A (índice 10)
        List<MicroInstruction> setAddr = setMemoryAddress(address, 10);
        result.addAll(setAddr);
        
        // 2. MAR = A (configura endereço de memória)
        int nextAddr = currentAddress + result.size();
        result.add(new MicroInstruction.Builder()
            .regA(10) // 10 = registrador temporario A (contem endereco)
            .regB(0) // 0 = PC (nao usado)
            .regC(0) // 0 = PC (nao usado)
            .aluOp(ALU.Operation.PASS_A) // passa endereco para MAR
            .mar(true) // carrega MAR
            .addr(nextAddr + 1) // proxima: leitura da memoria
            .build());
        
        // 3. Lê memória: Memory[MAR] → MBR
        // Ciclo separado necessário: protocolo MIC-1 requer que MAR seja configurado em ciclo anterior antes de executar RD
        // Operação ALU irrelevante: quando rd=true, apenas sinal de controle RD importa, operandos da ALU são ignorados
        result.add(new MicroInstruction.Builder()
            .regA(0) // Não usado (apenas sinal RD importa, operandos ignorados)
            .regB(0) // Não usado
            .regC(0) // Não usado (enc=false, resultado vai para MBR não registrador)
            .aluOp(ALU.Operation.ADD) // Operação irrelevante (qualquer operação serve quando apenas RD importa)
            .rd(true) // Executa leitura: Memory[MAR] → MBR (dado lido fica disponível em MBR para próxima microinstrução)
            .addr(nextAddr + 2) // Próxima: soma AC + MBR
            .build());
        
        // 4. AC = AC + MBR (soma valor lido da memória com acumulador)
        result.add(new MicroInstruction.Builder()
            .regA(0) // 0 = PC (nao usado, entrada vem de MBR via AMUX)
            .regB(1) // 1 = AC (entrada para soma)
            .regC(1) // 1 = AC (destino da soma)
            .aluOp(ALU.Operation.ADD) // AC = AC + MBR
            .enc(true) // habilita escrita no AC
            .amux(true) // seleciona MBR como entrada A
            .addr(currentAddress + result.size() + 1)
            .build());
        
        fixLastAddress(result);
        return result;
    }

    // expande SUBD: subtrai valor da memoria do acumulador
    private List<MicroInstruction> expandSUBD(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int address = parseOperand(operand);
        
        // 1. Constrói endereço no registrador temporário A (índice 10)
        List<MicroInstruction> setAddr = setMemoryAddress(address, 10);
        result.addAll(setAddr);
        
        // 2. MAR = A (configura endereço de memória)
        int nextAddr = currentAddress + result.size();
        result.add(new MicroInstruction.Builder()
            .regA(10) // 10 = registrador temporario A (contem endereco)
            .regB(0) // 0 = PC (nao usado)
            .regC(0) // 0 = PC (nao usado)
            .aluOp(ALU.Operation.PASS_A) // passa endereco para MAR
            .mar(true) // carrega MAR
            .addr(nextAddr + 1) // proxima: leitura da memoria
            .build());
        
        // 3. Lê memória: Memory[MAR] → MBR
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (apenas sinal rd importa)
            .regB(0) // nao usado (apenas sinal rd importa)
            .regC(0) // nao usado (enc=false, resultado vai para MBR nao registrador)
            .aluOp(ALU.Operation.ADD) // operacao arbitraria (operandos ignorados quando apenas rd importa)
            .rd(true) // Memory[MAR] -> MBR (le valor da memoria e coloca em MBR)
            .addr(nextAddr + 2) // proxima: copiar MBR para temporario B
            .build());
        
        // 4. B = MBR (salva valor lido em temporário B para calcular complemento de 2)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (amux=true sobrescreve: entrada A vem de MBR)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(11) // 11 = registrador temporario B (usado para salvar valor de MBR)
            .aluOp(ALU.Operation.PASS_A) // passa valor de MBR (via AMUX) para resultado
            .enc(true) // habilita escrita em C
            .amux(true) // entrada A vem de MBR
            .addr(nextAddr + 3) // proxima: calcular complemento de 2
            .build());
        
        // 5. B = NOT(B) (primeiro passo do complemento de 2)
        result.add(new MicroInstruction.Builder()
            .regA(11) // 11 = registrador temporario B (contem valor a inverter)
            .regB(0) // nao usado (NOT_A ignora B)
            .regC(11) // 11 = registrador temporario B (destino da inversao)
            .aluOp(ALU.Operation.NOT_A) // NOT(B) - inverte bits para complemento de 2
            .enc(true) // habilita escrita em C
            .addr(nextAddr + 4) // proxima: somar 1
            .build());
        
        // 6. B = B + 1 (completa complemento de 2: -MBR)
        result.add(new MicroInstruction.Builder()
            .regA(11) // 11 = registrador temporario B (valor invertido)
            .regB(6) // 6 = registrador constante +1
            .regC(11) // 11 = registrador temporario B (resultado do complemento de 2)
            .aluOp(ALU.Operation.ADD) // B + 1 - completa complemento de 2
            .enc(true) // habilita escrita em C
            .addr(nextAddr + 5) // proxima: somar AC + (-MBR)
            .build());
        
        // 7. AC = AC + B (subtrai: AC - MBR = AC + (-MBR))
        result.add(new MicroInstruction.Builder()
            .regA(1) // AC como entrada A porque SUBD subtrai valor da memoria do acumulador (AC = AC - MBR = AC + (-MBR))
            .regB(11) // temporario B (contem -MBR) como entrada B porque subtracao e implementada como soma com complemento de 2 (AC + (-MBR))
            .regC(1) // AC como destino porque SUBD armazena resultado de volta no acumulador (AC = AC - Memory[address])
            .aluOp(ALU.Operation.ADD) // AC + (-MBR) porque subtracao e implementada como AC + complemento_de_2(MBR) = AC - MBR
            .enc(true) // habilita escrita em C porque sem isso AC nao seria atualizado com resultado da subtracao
            .addr(currentAddress + result.size() + 1) // proxima microinstrucao (fixLastAddress pode ajustar)
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
        
        // 1. PC = addr (salto incondicional para endereço de destino)
        result.add(new MicroInstruction.Builder()
            .regA(5) // registrador constante 0 (indice 5) como entrada A porque operacao e apenas para ativar condicao ALWAYS (valor nao importa)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // operacao arbitraria porque apenas condicao ALWAYS importa (salto sempre acontece independente do resultado da ALU)
            .cond(MicroInstruction.Condition.ALWAYS) // condicao ALWAYS porque JUMP e salto incondicional (sempre pula para endereco de destino)
            .addr(addr) // endereco de destino porque quando condicao ALWAYS e satisfeita, MPC e atualizado com este valor (salto incondicional)
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
        
        // 1. PC = addr se AC < 0 (salto condicional se acumulador negativo)
        result.add(new MicroInstruction.Builder()
            .regA(1) // AC como entrada A porque precisamos passar AC pela ALU para atualizar flag negativo (ALU atualiza flags baseado no resultado)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // passa valor de AC para resultado porque precisamos apenas atualizar flags da ALU (flag negativo e baseado no resultado)
            .cond(MicroInstruction.Condition.NEGATIVE) // condicao NEGATIVE porque JNEG pula se AC < 0 (flag negativo da ALU indica se resultado da ultima operacao foi negativo)
            .addr(addr) // endereco de destino porque quando condicao NEGATIVE e satisfeita (AC < 0), MPC e atualizado com este valor (salto condicional)
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
        
        // 1. PC = addr se AC == 0 (salto condicional se acumulador zero)
        result.add(new MicroInstruction.Builder()
            .regA(1) // AC como entrada A porque precisamos passar AC pela ALU para atualizar flag zero (ALU atualiza flags baseado no resultado)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // passa valor de AC para resultado porque precisamos apenas atualizar flags da ALU (flag zero e baseado no resultado)
            .cond(MicroInstruction.Condition.ZERO) // condicao ZERO porque JZER pula se AC == 0 (flag zero da ALU indica se resultado da ultima operacao foi zero)
            .addr(addr) // endereco de destino porque quando condicao ZERO e satisfeita (AC == 0), MPC e atualizado com este valor (salto condicional)
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
        
        // 1. Se AC < 0, pula próxima instrução (continua sequencialmente se negativo)
        result.add(new MicroInstruction.Builder()
            .regA(1) // AC como entrada A porque precisamos passar AC pela ALU para atualizar flag negativo (ALU atualiza flags baseado no resultado)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // passa valor de AC para resultado porque precisamos apenas atualizar flags da ALU (flag negativo e baseado no resultado)
            .cond(MicroInstruction.Condition.NEGATIVE) // condicao NEGATIVE porque se AC < 0, pulamos proxima instrucao (implementa logica: se negativo, nao pula; se positivo, pula)
            .addr(currentAddress + 1) // proxima microinstrucao porque se AC < 0, continuamos sequencialmente (nao pulamos para destino)
            .build());
        
        // 2. PC = addr (salto para destino se AC >= 0, ou seja, AC positivo)
        result.add(new MicroInstruction.Builder()
            .regA(5) // registrador constante 0 (indice 5) como entrada A porque operacao e apenas para ativar condicao ALWAYS (valor nao importa)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // operacao arbitraria porque apenas condicao ALWAYS importa (se chegamos aqui, AC >= 0, entao pulamos)
            .cond(MicroInstruction.Condition.ALWAYS) // condicao ALWAYS porque se chegamos aqui (AC nao negativo), sempre pulamos para destino (JPOS: AC > 0)
            .addr(addr) // endereco de destino porque quando condicao ALWAYS e satisfeita, MPC e atualizado com este valor (salto condicional para positivo)
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
        
        // 1. Se AC == 0, pula próxima instrução (continua sequencialmente se zero)
        result.add(new MicroInstruction.Builder()
            .regA(1) // AC como entrada A porque precisamos passar AC pela ALU para atualizar flag zero (ALU atualiza flags baseado no resultado)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // passa valor de AC para resultado porque precisamos apenas atualizar flags da ALU (flag zero e baseado no resultado)
            .cond(MicroInstruction.Condition.ZERO) // condicao ZERO porque se AC == 0, pulamos proxima instrucao (implementa logica: se zero, nao pula; se nao zero, pula)
            .addr(currentAddress + 1) // proxima microinstrucao porque se AC == 0, continuamos sequencialmente (nao pulamos para destino)
            .build());
        
        // 2. PC = addr (salto para destino se AC != 0)
        result.add(new MicroInstruction.Builder()
            .regA(5) // registrador constante 0 (indice 5) como entrada A porque operacao e apenas para ativar condicao ALWAYS (valor nao importa)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // operacao arbitraria porque apenas condicao ALWAYS importa (se chegamos aqui, AC != 0, entao pulamos)
            .cond(MicroInstruction.Condition.ALWAYS) // condicao ALWAYS porque se chegamos aqui (AC nao zero), sempre pulamos para destino (JNZE: AC != 0)
            .addr(addr) // endereco de destino porque quando condicao ALWAYS e satisfeita, MPC e atualizado com este valor (salto condicional para nao zero)
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
        
        // 1. MBR = PC + 1 (calcula endereço de retorno)
        result.add(new MicroInstruction.Builder()
            .regA(0) // PC como entrada A porque precisamos calcular PC+1 (endereco de retorno apos CALL)
            .regB(6) // registrador constante +1 (indice 6) como entrada B porque endereco de retorno e PC+1 (proxima instrucao apos CALL)
            .regC(0) // nao usado (enc=false, resultado vai para MBR nao registrador)
            .aluOp(ALU.Operation.ADD) // PC + 1 porque endereco de retorno e proxima instrucao apos CALL (precisamos salvar para RETN restaurar depois)
            .mbr(true) // resultado vai para MBR (valor sera escrito na pilha)
            .addr(currentAddress + 1) // proxima: configurar MAR com SP
            .build());
        
        // 2. MAR = SP (configura endereço da pilha)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP (Stack Pointer, indice 2) como entrada A porque pilha usa SP para apontar proxima posicao disponivel
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C)
            .aluOp(ALU.Operation.PASS_A) // passa valor de SP para resultado porque precisamos colocar endereco da pilha em MAR antes de escrever
            .mar(true) // resultado vai para MAR (endereco necessario antes de ler/escrever memoria)
            .addr(currentAddress + 2) // proxima: escrever MBR na pilha
            .build());
        
        // 3. Escreve memória: MBR → Memory[MAR] (salva endereço de retorno na pilha)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (apenas sinal wr importa)
            .regB(0) // nao usado (apenas sinal wr importa)
            .regC(0) // nao usado (enc=false, escrita nao usa registrador)
            .aluOp(ALU.Operation.ADD) // operacao arbitraria (operandos ignorados quando apenas wr importa)
            .wr(true) // MBR -> Memory[MAR] (escreve endereco de retorno na pilha no endereco apontado por SP)
            .addr(currentAddress + 3) // proxima: incrementar SP
            .build());
        
        // 4. SP = SP + 1 (atualiza ponteiro da pilha após empilhar)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP como entrada A porque precisamos incrementar SP apos empilhar (pilha cresce para baixo, entao incrementamos)
            .regB(6) // registrador constante +1 (indice 6) como entrada B porque incrementamos SP em 1 apos empilhar
            .regC(2) // SP como destino porque SP precisa apontar para proxima posicao disponivel na pilha apos empilhar
            .aluOp(ALU.Operation.ADD) // SP + 1 porque apos empilhar endereco de retorno, SP deve apontar para proxima posicao livre
            .enc(true) // habilita escrita em C porque sem isso SP nao seria atualizado
            .addr(currentAddress + 4) // proxima: saltar para subrotina
            .build());
        
        // 5. PC = addr (salta para endereço da subrotina)
        result.add(new MicroInstruction.Builder()
            .regA(5) // registrador constante 0 (indice 5) como entrada A porque operacao e apenas para ativar condicao ALWAYS (valor nao importa)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // operacao arbitraria porque apenas condicao ALWAYS importa (salto sempre acontece)
            .cond(MicroInstruction.Condition.ALWAYS) // condicao ALWAYS porque CALL sempre pula para endereco da subrotina (salto incondicional)
            .addr(addr) // endereco da subrotina porque quando condicao ALWAYS e satisfeita, MPC e atualizado com este valor (chamada de subrotina)
            .build());
        
        return result;
    }

    // expande RETN: retorna de subrotina restaurando endereco de retorno da pilha
    private List<MicroInstruction> expandRETN() {
        List<MicroInstruction> result = new ArrayList<>();
        
        // 1. SP = SP - 1 (decrementa ponteiro da pilha antes de desempilhar)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP como entrada A porque precisamos decrementar SP antes de desempilhar (pilha cresce para baixo, entao decrementamos primeiro)
            .regB(7) // registrador constante -1 (indice 7) como entrada B porque decrementamos SP em 1 antes de desempilhar
            .regC(2) // SP como destino porque SP precisa apontar para posicao onde esta endereco de retorno antes de ler
            .aluOp(ALU.Operation.ADD) // SP + (-1) porque antes de desempilhar precisamos apontar SP para posicao onde esta endereco de retorno
            .enc(true) // habilita escrita em C porque sem isso SP nao seria atualizado
            .addr(currentAddress + 1) // proxima: configurar MAR com SP
            .build());
        
        // 2. MAR = SP (configura endereço da pilha)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP (agora decrementado) como entrada A porque precisamos ler endereco de retorno da posicao apontada por SP
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C)
            .aluOp(ALU.Operation.PASS_A) // passa valor de SP para resultado porque precisamos colocar endereco da pilha em MAR antes de ler
            .mar(true) // resultado vai para MAR (endereco necessario antes de ler/escrever memoria)
            .addr(currentAddress + 2) // proxima: ler endereco de retorno da pilha
            .build());
        
        // 3. Lê memória: Memory[MAR] → MBR (lê endereço de retorno da pilha)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (apenas sinal rd importa)
            .regB(0) // nao usado (apenas sinal rd importa)
            .regC(0) // nao usado (enc=false, resultado vai para MBR nao registrador)
            .aluOp(ALU.Operation.ADD) // operacao arbitraria (operandos ignorados quando apenas rd importa)
            .rd(true) // Memory[MAR] -> MBR (le endereco de retorno da pilha)
            .amux(true) // amux=true porque proxima microinstrucao precisa usar MBR como entrada A da ALU (para atualizar PC)
            .addr(currentAddress + 3) // proxima: atualizar PC com endereco de retorno
            .build());
        
        // 4. PC = MBR (restaura endereço de retorno via AMUX)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (amux=true sobrescreve: entrada A vem de MBR)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C, PC e atualizado via campo addr quando condicao e satisfeita)
            .aluOp(ALU.Operation.PASS_A) // passa valor de MBR (via AMUX) para resultado porque precisamos apenas copiar endereco de retorno para PC sem operacao aritmetica
            .cond(MicroInstruction.Condition.ALWAYS) // condicao ALWAYS porque RETN sempre retorna (salto incondicional para endereco de retorno)
            .amux(true) // entrada A vem de MBR porque endereco de retorno esta em MBR (lido da pilha) e precisamos usar via AMUX para atualizar PC
            .addr(0) // endereco 0 porque quando amux=true e condicao ALWAYS, PC e atualizado com valor de MBR (endereco de retorno) em vez de campo addr
            .build());
        
        return result;
    }

    // expande PUSH: empilha valor do acumulador na pilha
    private List<MicroInstruction> expandPUSH() {
        List<MicroInstruction> result = new ArrayList<>();
        
        // 1. MBR = AC (copia valor do acumulador para MBR)
        result.add(new MicroInstruction.Builder()
            .regA(1) // AC como entrada A porque PUSH empilha valor do acumulador na pilha (precisamos copiar AC para MBR)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false, resultado vai para MBR nao registrador)
            .aluOp(ALU.Operation.PASS_A) // passa valor de AC para resultado porque precisamos apenas copiar AC para MBR sem operacao aritmetica
            .mbr(true) // resultado vai para MBR (valor sera escrito na pilha)
            .addr(currentAddress + 1) // proxima: configurar MAR com SP
            .build());
        
        // 2. MAR = SP (configura endereço da pilha)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP (Stack Pointer, indice 2) como entrada A porque pilha usa SP para apontar proxima posicao disponivel
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C)
            .aluOp(ALU.Operation.PASS_A) // passa valor de SP para resultado porque precisamos colocar endereco da pilha em MAR antes de escrever
            .mar(true) // resultado vai para MAR (endereco necessario antes de ler/escrever memoria)
            .addr(currentAddress + 2) // proxima: escrever valor na pilha
            .build());
        
        // 3. Escreve memória: MBR → Memory[MAR] (empilha valor do AC)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (apenas sinal wr importa)
            .regB(0) // nao usado (apenas sinal wr importa)
            .regC(0) // nao usado (enc=false, escrita nao usa registrador)
            .aluOp(ALU.Operation.ADD) // operacao arbitraria (operandos ignorados quando apenas wr importa)
            .wr(true) // MBR -> Memory[MAR] (escreve valor do AC na pilha no endereco apontado por SP)
            .addr(currentAddress + 3) // proxima: decrementar SP
            .build());
        
        // 4. SP = SP - 1 (atualiza ponteiro da pilha após empilhar)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP como entrada A porque precisamos decrementar SP apos empilhar (pilha cresce para baixo, entao decrementamos)
            .regB(7) // registrador constante -1 (indice 7) como entrada B porque decrementamos SP em 1 apos empilhar
            .regC(2) // SP como destino porque SP precisa apontar para proxima posicao disponivel na pilha apos empilhar
            .aluOp(ALU.Operation.ADD) // SP + (-1) porque apos empilhar valor, SP deve apontar para proxima posicao livre (pilha cresce para baixo)
            .enc(true) // habilita escrita em C porque sem isso SP nao seria atualizado
            .addr(currentAddress + 4) // proxima microinstrucao (fim)
            .build());
        
        return result;
    }

    // expande POP: desempilha valor da pilha para o acumulador
    private List<MicroInstruction> expandPOP() {
        List<MicroInstruction> result = new ArrayList<>();
        
        // 1. SP = SP + 1 (incrementa ponteiro da pilha antes de desempilhar)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP como entrada A porque precisamos incrementar SP antes de desempilhar (pilha cresce para baixo, entao incrementamos primeiro)
            .regB(6) // registrador constante +1 (indice 6) como entrada B porque incrementamos SP em 1 antes de desempilhar
            .regC(2) // SP como destino porque SP precisa apontar para posicao onde esta valor antes de ler
            .aluOp(ALU.Operation.ADD) // SP + 1 porque antes de desempilhar precisamos apontar SP para posicao onde esta valor (pilha cresce para baixo)
            .enc(true) // habilita escrita em C porque sem isso SP nao seria atualizado
            .addr(currentAddress + 1) // proxima: configurar MAR com SP
            .build());
        
        // 2. MAR = SP (configura endereço da pilha)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP (agora incrementado) como entrada A porque precisamos ler valor da posicao apontada por SP
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C)
            .aluOp(ALU.Operation.PASS_A) // passa valor de SP para resultado porque precisamos colocar endereco da pilha em MAR antes de ler
            .mar(true) // resultado vai para MAR (endereco necessario antes de ler/escrever memoria)
            .addr(currentAddress + 2) // proxima: ler valor da pilha
            .build());
        
        // 3. Lê memória: Memory[MAR] → MBR (lê valor da pilha)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (apenas sinal rd importa)
            .regB(0) // nao usado (apenas sinal rd importa)
            .regC(0) // nao usado (enc=false, resultado vai para MBR nao registrador)
            .aluOp(ALU.Operation.ADD) // operacao arbitraria (operandos ignorados quando apenas rd importa)
            .rd(true) // Memory[MAR] -> MBR (le valor da pilha)
            .amux(true) // amux=true porque proxima microinstrucao precisa usar MBR como entrada A da ALU (para copiar para AC)
            .addr(currentAddress + 3) // proxima: copiar MBR para AC
            .build());
        
        // 4. AC = MBR (via AMUX)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (amux=true sobrescreve: entrada A vem de MBR)
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(1) // AC como destino porque POP desempilha valor da pilha para o acumulador (AC = valor desempilhado)
            .aluOp(ALU.Operation.PASS_A) // passa valor de MBR (via AMUX) para resultado porque precisamos apenas copiar MBR para AC sem operacao aritmetica
            .enc(true) // habilita escrita em C porque sem isso AC nao seria atualizado com valor desempilhado
            .amux(true) // entrada A vem de MBR porque valor lido da pilha esta em MBR (nao em registrador) e ALU precisa acessar via AMUX para usar MBR como operando
            .addr(currentAddress + 4) // proxima microinstrucao (fim)
            .build());
        
        return result;
    }

    private List<MicroInstruction> expandPUSHI(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int value = parseOperand(operand);
        
        // 1. A = 0 (inicializa temporário A com zero)
        result.add(new MicroInstruction.Builder()
            .regA(5) // registrador constante 0 (indice 5) como entrada A porque PUSHI precisa inicializar temporario A com zero antes de construir valor
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(10) // registrador temporario A (indice 10) como destino porque vamos construir valor aqui antes de empilhar
            .aluOp(ALU.Operation.PASS_A) // passa valor 0 para resultado porque precisamos inicializar temporario A antes de construir valor
            .enc(true) // habilita escrita em C porque sem isso temporario A nao seria inicializado com zero
            .addr(currentAddress + 1) // proxima: construir valor incrementando temporario A
            .build());
        
        // 2. A = A + 1 (repete value vezes para construir constante)
        for (int i = 0; i < value; i++) {
            result.add(new MicroInstruction.Builder()
                .regA(10) // temporario A como entrada A porque estamos incrementando temporario A repetidamente (temporario A = temporario A + 1, value vezes)
                .regB(6) // registrador constante +1 (indice 6) como entrada B porque precisamos somar 1 ao temporario A a cada iteracao
                .regC(10) // temporario A como destino porque estamos construindo valor somando 1 repetidamente (PUSHI: empilha constante)
                .aluOp(ALU.Operation.ADD) // temporario A + 1 porque para valores pequenos construimos somando 1 repetidamente (mais eficiente que decomposicao binaria)
                .enc(true) // habilita escrita em C porque sem isso temporario A nao seria atualizado a cada incremento
                .addr(currentAddress + result.size() + 1) // proxima microinstrucao (proximo incremento ou copiar para MBR)
                .build());
        }
        
        // 3. MBR = A (copia valor constante construído para MBR)
        result.add(new MicroInstruction.Builder()
            .regA(10) // temporario A (contem valor construido) como entrada A porque precisamos copiar valor para MBR antes de empilhar
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false, resultado vai para MBR nao registrador)
            .aluOp(ALU.Operation.PASS_A) // passa valor de temporario A para resultado porque precisamos apenas copiar valor para MBR sem operacao aritmetica
            .mbr(true) // resultado vai para MBR (valor sera escrito na pilha)
            .addr(currentAddress + result.size() + 1) // proxima: configurar MAR com SP
            .build());
        
        // 4. MAR = SP (configura endereço da pilha)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP (Stack Pointer, indice 2) como entrada A porque pilha usa SP para apontar proxima posicao disponivel
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C)
            .aluOp(ALU.Operation.PASS_A) // passa valor de SP para resultado porque precisamos colocar endereco da pilha em MAR antes de escrever
            .mar(true) // resultado vai para MAR (endereco necessario antes de ler/escrever memoria)
            .addr(currentAddress + result.size() + 1) // proxima: escrever valor na pilha
            .build());
        
        // 5. Escreve memória: MBR → Memory[MAR] (empilha valor constante)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (apenas sinal wr importa)
            .regB(0) // nao usado (apenas sinal wr importa)
            .regC(0) // nao usado (enc=false, escrita nao usa registrador)
            .aluOp(ALU.Operation.ADD) // operacao arbitraria (operandos ignorados quando apenas wr importa)
            .wr(true) // MBR -> Memory[MAR] (escreve valor constante na pilha no endereco apontado por SP)
            .addr(currentAddress + result.size() + 1) // proxima: decrementar SP
            .build());
        
        // 6. SP = SP - 1 (atualiza ponteiro da pilha após empilhar)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP como entrada A porque precisamos decrementar SP apos empilhar (pilha cresce para baixo, entao decrementamos)
            .regB(7) // registrador constante -1 (indice 7) como entrada B porque decrementamos SP em 1 apos empilhar
            .regC(2) // SP como destino porque SP precisa apontar para proxima posicao disponivel na pilha apos empilhar
            .aluOp(ALU.Operation.ADD) // SP + (-1) porque apos empilhar valor, SP deve apontar para proxima posicao livre (pilha cresce para baixo)
            .enc(true) // habilita escrita em C porque sem isso SP nao seria atualizado
            .addr(currentAddress + result.size() + 1) // proxima microinstrucao (fim)
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
        
        // 1. SP = SP + 1 (incrementa ponteiro da pilha antes de desempilhar)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP como entrada A porque precisamos incrementar SP antes de desempilhar (pilha cresce para baixo, entao incrementamos primeiro)
            .regB(6) // registrador constante +1 (indice 6) como entrada B porque incrementamos SP em 1 antes de desempilhar
            .regC(2) // SP como destino porque SP precisa apontar para posicao onde esta valor antes de ler
            .aluOp(ALU.Operation.ADD) // SP + 1 porque antes de desempilhar precisamos apontar SP para posicao onde esta valor (pilha cresce para baixo)
            .enc(true) // habilita escrita em C porque sem isso SP nao seria atualizado
            .addr(currentAddress + 1) // proxima: configurar MAR com SP
            .build());
        
        // 2. MAR = SP (configura endereço da pilha)
        result.add(new MicroInstruction.Builder()
            .regA(2) // SP (agora incrementado) como entrada A porque precisamos ler valor da posicao apontada por SP
            .regB(0) // nao usado (PASS_A ignora B)
            .regC(0) // nao usado (enc=false desabilita escrita em C)
            .aluOp(ALU.Operation.PASS_A) // passa valor de SP para resultado porque precisamos colocar endereco da pilha em MAR antes de ler
            .mar(true) // resultado vai para MAR (endereco necessario antes de ler/escrever memoria)
            .addr(currentAddress + 2) // proxima: ler valor da pilha
            .build());
        
        // 3. Lê memória: Memory[MAR] → MBR (desempilha valor, mas não copia para AC)
        result.add(new MicroInstruction.Builder()
            .regA(0) // nao usado (apenas sinal rd importa)
            .regB(0) // nao usado (apenas sinal rd importa)
            .regC(0) // nao usado (enc=false, resultado vai para MBR nao registrador)
            .aluOp(ALU.Operation.ADD) // operacao arbitraria (operandos ignorados quando apenas rd importa)
            .rd(true) // Memory[MAR] -> MBR (le valor da pilha)
            .amux(true) // amux=true porque valor lido esta em MBR e pode ser usado em microinstrucoes subsequentes se necessario
            .addr(currentAddress + 3) // proxima microinstrucao (fim - POPI apenas desempilha, nao copia para AC)
            .build());
        
        return result;
    }

    private List<MicroInstruction> expandINSP(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int value = parseOperand(operand);
        
        // SP = SP - 1 (repete value vezes para alocar espaço na pilha)
        for (int i = 0; i < value; i++) {
            result.add(new MicroInstruction.Builder()
                .regA(2) // SP como entrada A porque INSP incrementa SP (aloca espaco na pilha) repetidamente (SP = SP + 1, value vezes)
                .regB(7) // registrador constante -1 (indice 7) como entrada B porque incrementamos SP somando -1 (pilha cresce para baixo, entao incrementar e somar -1)
                .regC(2) // SP como destino porque estamos alocando espaco na pilha incrementando SP (SP aponta para proxima posicao disponivel)
                .aluOp(ALU.Operation.ADD) // SP + (-1) porque pilha cresce para baixo, entao incrementar SP (alocar espaco) e decrementar endereco (somar -1)
                .enc(true) // habilita escrita em C porque sem isso SP nao seria atualizado a cada incremento
                .addr(currentAddress + result.size() + 1) // proxima microinstrucao (proximo incremento ou fim)
                .build());
        }
        
        return result;
    }

    private List<MicroInstruction> expandDESP(String operand) {
        List<MicroInstruction> result = new ArrayList<>();
        int value = parseOperand(operand);
        
        // SP = SP + 1 (repete value vezes para desalocar espaço na pilha)
        for (int i = 0; i < value; i++) {
            result.add(new MicroInstruction.Builder()
                .regA(2) // SP como entrada A porque DESP decrementa SP (desaloca espaco na pilha) repetidamente (SP = SP - 1, value vezes)
                .regB(6) // registrador constante +1 (indice 6) como entrada B porque decrementamos SP somando +1 (pilha cresce para baixo, entao decrementar e somar +1)
                .regC(2) // SP como destino porque estamos desalocando espaco na pilha decrementando SP (SP aponta para posicao anterior)
                .aluOp(ALU.Operation.ADD) // SP + 1 porque pilha cresce para baixo, entao decrementar SP (desalocar espaco) e incrementar endereco (somar +1)
                .enc(true) // habilita escrita em C porque sem isso SP nao seria atualizado a cada decremento
                .addr(currentAddress + result.size() + 1) // proxima microinstrucao (proximo decremento ou fim)
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
