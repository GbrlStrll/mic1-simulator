package mic1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * O "Cérebro" (Backend) da janela SourceCode. (MODELO)
 * (Com a lógica do Montador CORRIGIDA)
 */
public class SourceCode {

    // --- Propriedades (O "Estado" da View) ---
    private final StringProperty assemblyCode = new SimpleStringProperty("");

    private final StringProperty compiledCode = new SimpleStringProperty(
            "Pressione 'Assemble' para compilar.\n\n" +
            "Pressione 'Write code to Main Memory' para enviar os dados para a memória principal."
    );

    // --- Dependências ---
    private MainMemory mainMemory; 

    // --- Lógica do Montador (Assembler) ---
    private final Map<String, Integer> symbolTable = new HashMap<>();
    private final Map<String, String> opcodes = new HashMap<>();

    /**
     * Construtor: Preenche a tabela de opcodes.
     */
    public SourceCode() {
        // (O preenchimento do 'opcodes' está correto e permanece o mesmo)
        opcodes.put("LODD", "0000");
        opcodes.put("STOD", "0001");
        opcodes.put("ADDD", "0010");
        opcodes.put("SUBD", "0011");
        opcodes.put("JPOS", "0100");
        opcodes.put("JZER", "0101");
        opcodes.put("JUMP", "0110");
        opcodes.put("LOCO", "0111");
        opcodes.put("LODL", "1000");
        opcodes.put("STOL", "1001");
        opcodes.put("ADDL", "1010");
        opcodes.put("SUBL", "1011");
        opcodes.put("JNEG", "1100");
        opcodes.put("JNZE", "1101");
        opcodes.put("CALL", "1110");
        opcodes.put("PSHI", "1111000000000000");
        opcodes.put("POPI", "1111001000000000");
        opcodes.put("PUSH", "1111010000000000");
        opcodes.put("POP",  "1111011000000000");
        opcodes.put("RETN", "1111100000000000");
        opcodes.put("SWAP", "1111101000000000");
        opcodes.put("INSP", "11111100");
        opcodes.put("DESP", "11111110");
    }

    // --- Getters das Propriedades (Para o Controller fazer o Data Binding) ---

    public StringProperty assemblyCodeProperty() {
        return assemblyCode;
    }

    public StringProperty compiledCodeProperty() {
        return compiledCode;
    }

    // --- Injeção de Dependência (Chamado pelo Main.java) ---

    public void linkMainMemory(MainMemory memory) {
        this.mainMemory = memory;
    }

    // --- Lógica de Negócio (Chamado pelo Controller) ---

    /**
     * O "Montador" (Assembler).
     * Converte o texto em assemblyCode para binário em compiledCode.
     * Esta é uma implementação "two-pass" (duas passagens) simplificada.
     */
    public void assemble() {
        symbolTable.clear();
        StringBuilder compiledText = new StringBuilder();
        String[] lines = assemblyCode.get().split("\n");
        int currentAddress = 0;
        int variableAddress = 4095; // Variáveis começam no topo da memória

        try {
            // --- PASSAGEM 1 (CORRIGIDA): Construir a Tabela de Símbolos ---
            // Esta passagem encontra TODOS os LABELS (de instrução e de variável).
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("/")) {
                    continue; // Ignora comentários e linhas vazias
                }

                // Verifica se há um label (ex: "LOOP:", "x:")
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String label = parts[0].trim();
                    String instruction = parts[1].trim();

                    if (symbolTable.containsKey(label)) {
                        throw new RuntimeException("Erro: Label duplicado '" + label + "'");
                    }

                    if (instruction.isEmpty()) {
                        // É uma declaração de variável (ex: "x:")
                        symbolTable.put(label, variableAddress);
                        variableAddress--; // Próxima variável ficará no endereço anterior
                    } else {
                        // É um label de instrução (ex: "INICIO: LOCO 5")
                        symbolTable.put(label, currentAddress);
                    }
                    
                    line = instruction; // Pega o resto da linha (pode ser vazio)
                }
                
                // Se a linha não estiver vazia (após remover o label), 
                // ela ocupa um endereço de instrução
                if (!line.isEmpty()) {
                    currentAddress++; // Instrução ocupa 1 palavra
                }
            }
            // --- FIM DA PASSAGEM 1 CORRIGIDA ---


            // --- PASSAGEM 2: Traduzir o código ---
            // (Esta passagem não precisa de modificação, pois agora 
            // a tabela de símbolos está correta)
            currentAddress = 0;
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("/")) {
                    continue;
                }
                if (line.contains(":")) {
                    line = line.split(":", 2)[1].trim(); // Remove o label
                }
                if (line.isEmpty()) continue; // Linha era só um label (ex: "x:")

                String[] parts = line.split("\\s+", 2);
                String mnemonic = parts[0].toUpperCase();
                String operand = (parts.length > 1) ? parts[1].trim() : null;

                if (opcodes.containsKey(mnemonic)) {
                    String opcode = opcodes.get(mnemonic);
                    
                    if (opcode.length() == 16) { // Instrução de 16 bits (ex: PUSH, RETN)
                        compiledText.append(opcode).append(" / ").append(mnemonic).append("\n");
                        
                    } else if (opcode.length() == 8) { // Instrução com 'y' (ex: INSP)
                        int y = Integer.parseInt(operand);
                        if (y < 0 || y > 255) {
                            throw new RuntimeException("Erro: Operando 'y' fora do range (0-255) na linha " + currentAddress);
                        }
                        String y_bits = String.format("%8s", Integer.toBinaryString(y)).replace(' ', '0');
                        compiledText.append(opcode).append(y_bits).append(" / ").append(mnemonic).append(" ").append(y).append("\n");

                    } else { // Instrução de 4 bits com 'x' (ex: LODD, JUMP, LOCO)
                        int x;
                        String x_bits;
                        
                        if (mnemonic.equals("LOCO")) {
                            x = Integer.parseInt(operand); // LOCO usa uma constante (pode ser negativa)
                            String bits = Integer.toBinaryString(x);
                            if (bits.length() > 12) {
                                x_bits = bits.substring(bits.length() - 12);
                            } else if (x < 0) {
                                x_bits = bits.substring(Integer.SIZE - 12);
                            } else {
                                x_bits = String.format("%12s", bits).replace(' ', '0');
                            }
                        } else {
                            // Outras (LODD, JUMP, etc.) usam um label
                            if (operand == null) {
                                throw new RuntimeException("Erro: Mnemônico '" + mnemonic + "' requer um operando na linha " + currentAddress);
                            }
                            if (!symbolTable.containsKey(operand)) {
                                throw new RuntimeException("Erro: Símbolo '" + operand + "' não definido na linha " + currentAddress);
                            }
                            x = symbolTable.get(operand);
                            if (x < 0 || x > 4095) {
                                throw new RuntimeException("Erro: Operando 'x' (símbolo '" + operand + "') fora do range (0-4095) na linha " + currentAddress);
                            }
                            x_bits = String.format("%12s", Integer.toBinaryString(x)).replace(' ', '0');
                        }
                        
                        compiledText.append(opcode).append(x_bits).append(" / ").append(mnemonic).append(" ").append(operand).append(" (").append(x).append(")").append("\n");
                    }
                } else {
                    throw new RuntimeException("Erro: Mnemônico desconhecido '" + mnemonic + "' na linha " + currentAddress);
                }
                currentAddress++;
            }
            compiledCode.set(compiledText.toString());

        } catch (Exception e) {
            compiledCode.set("ERRO NA MONTAGEM:\n" + e.getMessage() + "\n\n(Verifique seu código e tente novamente)");
        }
    }

    /**
     * Grava o código binário (da caixa "Compiled") na Memória Principal.
     * (Este método está CORRETO para o Problema 1 - Complemento de 2)
     */
    public void writeToMemory() {
        if (mainMemory == null) {
            System.err.println("Erro: Modelo da MainMemory não foi injetado no SourceCode.");
            compiledCode.set("ERRO: O modelo de Memória não está conectado.\n" + compiledCode.get());
            return;
        }
        
        String[] lines = compiledCode.get().split("\n");
        int address = 0;
        
        try {
            mainMemory.clearMemory(); 
            
            for (String line : lines) {
                if (line.isEmpty() || line.startsWith("/") || line.startsWith("ERRO")) {
                    continue; // Ignora comentários e erros
                }
                
                String binaryCode = line.split("/")[0].trim();
                
                if (binaryCode.length() == 16) {
                    
                    // Converte a string binária de 16 bits para um 'short' (C2)
                    long longVal = Long.parseLong(binaryCode, 2);
                    short shortVal = (short) longVal;
                    
                    // Escreve na memória.
                    mainMemory.write(address, shortVal);
                    
                    address++;
                }
            }
            
            if (!compiledCode.get().startsWith("ERRO")) {
                compiledCode.set(compiledCode.get() + "\n\n// Código gravado na Memória Principal");
            }
            mainMemory.notifyUpdate(); 
            
        } catch (Exception e) {
            compiledCode.set("ERRO AO GRAVAR NA MEMÓRIA:\n" + e.getMessage());
        }
    }
}