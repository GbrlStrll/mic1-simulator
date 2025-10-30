package mic1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Modelo responsável pelo código-fonte exibido na janela "Source Code".
 *
 * Esta classe fornece:
 * - Propriedades para ligação (data-binding) com a view.
 * - Um montador (assembler) simplificado em duas passagens para traduzir
 *   o código assembly em representações binárias textuais.
 * - Função para gravar o código compilado na memória principal.
 */
public class SourceCode {

    /** Código assembly editável pela UI. */
    private final StringProperty assemblyCode = new SimpleStringProperty("");

    /** Texto com o código compilado ou mensagens de estado/erro. */
    private final StringProperty compiledCode = new SimpleStringProperty(
            "Pressione 'Assemble' para compilar.\n\n" +
            "Pressione 'Write code to Main Memory' para enviar os dados para a memória principal."
    );

    /** Referência ao modelo de memória principal (injetada por Main). */
    private MainMemory mainMemory;

    /* Tabela de símbolos (labels e variáveis) construída na primeira passagem. */
    private final Map<String, Integer> symbolTable = new HashMap<>();

    /* Mapeamento de mnemônicos para seus opcodes binários (como texto). */
    private final Map<String, String> opcodes = new HashMap<>();

    /**
     * Inicializa a tabela de opcodes conhecida pelo montador.
     */
    public SourceCode() {
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

    /** Retorna a propriedade do código assembly para uso em data-binding. */
    public StringProperty assemblyCodeProperty() {
        return assemblyCode;
    }

    /** Retorna a propriedade do código compilado/saida do montador. */
    public StringProperty compiledCodeProperty() {
        return compiledCode;
    }

    /**
     * Injeta a instância de MainMemory a ser usada para operações de escrita.
     * @param memory instância de MainMemory
     */
    public void linkMainMemory(MainMemory memory) {
        this.mainMemory = memory;
    }

    /**
     * Montador em duas passagens.
     *
     * Passagem 1: constrói a tabela de símbolos (labels e variáveis).
     * Passagem 2: gera a representação binária textual das instruções.
     *
     * Em caso de erro, a propriedade {@code compiledCode} recebe uma
     * mensagem de erro descritiva para exibição na UI.
     */
    public void assemble() {
        symbolTable.clear();
        StringBuilder compiledText = new StringBuilder();
        String[] lines = assemblyCode.get().split("\n");
        int currentAddress = 0;
        int variableAddress = 4095;

        try {
            /* PASSAGEM 1: identificar todos os símbolos (labels e variáveis). */
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("/")) {
                    continue;
                }

                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    String label = parts[0].trim();
                    String instruction = parts[1].trim();

                    if (symbolTable.containsKey(label)) {
                        throw new RuntimeException("Erro: Label duplicado '" + label + "'");
                    }

                    if (instruction.isEmpty()) {
                        symbolTable.put(label, variableAddress);
                        variableAddress--;
                    } else {
                        symbolTable.put(label, currentAddress);
                    }

                    line = instruction;
                }

                if (!line.isEmpty()) {
                    currentAddress++;
                }
            }

            /* PASSAGEM 2: traduzir instruções usando a tabela de símbolos. */
            currentAddress = 0;
            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("/")) {
                    continue;
                }
                if (line.contains(":")) {
                    line = line.split(":", 2)[1].trim();
                }
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+", 2);
                String mnemonic = parts[0].toUpperCase();
                String operand = (parts.length > 1) ? parts[1].trim() : null;

                if (opcodes.containsKey(mnemonic)) {
                    String opcode = opcodes.get(mnemonic);

                    if (opcode.length() == 16) {
                        compiledText.append(opcode).append(" / ").append(mnemonic).append("\n");

                    } else if (opcode.length() == 8) {
                        int y = Integer.parseInt(operand);
                        if (y < 0 || y > 255) {
                            throw new RuntimeException("Erro: Operando 'y' fora do range (0-255) na linha " + currentAddress);
                        }
                        String y_bits = String.format("%8s", Integer.toBinaryString(y)).replace(' ', '0');
                        compiledText.append(opcode).append(y_bits).append(" / ").append(mnemonic).append(" ").append(y).append("\n");

                    } else {
                        int x;
                        String x_bits;

                        if (mnemonic.equals("LOCO")) {
                            x = Integer.parseInt(operand);
                            String bits = Integer.toBinaryString(x);
                            if (bits.length() > 12) {
                                x_bits = bits.substring(bits.length() - 12);
                            } else if (x < 0) {
                                x_bits = bits.substring(Integer.SIZE - 12);
                            } else {
                                x_bits = String.format("%12s", bits).replace(' ', '0');
                            }
                        } else {
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
     * Transfere o conteúdo da propriedade {@code compiledCode} para a
     * MainMemory. Linhas inválidas ou mensagens de erro são ignoradas.
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
                    continue;
                }

                String binaryCode = line.split("/")[0].trim();

                if (binaryCode.length() == 16) {
                    long longVal = Long.parseLong(binaryCode, 2);
                    short shortVal = (short) longVal;
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