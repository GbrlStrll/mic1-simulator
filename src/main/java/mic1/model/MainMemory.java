package mic1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Arrays;

/**
 * Modelo (Model) que representa a Memória Principal do simulador MIC-1.
 *
 * Esta classe gerencia o armazenamento de dados brutos (simulando a RAM)
 * e também fornece uma representação de dados observável (ObservableList)
 * para a interface gráfica (TableView), realizando a decodificação
 * das instruções para exibição.
 */
public class MainMemory {

    /** O tamanho total da memória principal, correspondente a 2^12 endereços. */
    public static final int MEMORY_SIZE = 4096;
    
    /** Armazenamento de backend (simulação da RAM) usando um array de inteiros. */
    private final int[] memoryArray;
    
    /** Lista observável que serve como fonte de dados para a TableView da UI. */
    private final ObservableList<MemoryEntry> memoryData;

    /**
     * Classe interna que representa uma única linha (uma entrada) na TableView da memória.
     *
     * Esta classe é responsável por pegar um valor de 16 bits da memória
     * e formatá-lo em múltiplas representações (Endereço, Binário, Decimal, Hex)
     * para exibição. Ela contém a lógica de "disassembly" para a UI.
     */
    public static class MemoryEntry {
        
        /** Propriedades JavaFX para data binding com as colunas da TableView. */
        private final SimpleStringProperty address = new SimpleStringProperty();
        private final SimpleStringProperty binaryValue = new SimpleStringProperty();
        private final SimpleStringProperty decimalValue = new SimpleStringProperty();
        private final SimpleStringProperty hexValue = new SimpleStringProperty();

        /**
         * Constrói uma nova entrada de memória.
         * @param address O endereço desta entrada (0-4095).
         * @param value O valor inicial de 16 bits neste endereço.
         */
        public MemoryEntry(int address, int value) {
            update(address, value); 
        }

        /**
         * Atualiza as propriedades de exibição com base em um novo valor de 16 bits.
         * Contém a lógica de decodificação para extrair o operando.
         *
         * @param value O valor de 16 bits (armazenado como int) lido da memória.
         */
        public void updateValue(int value) {
            int instruction = value; 
            
            // Define o valor binário bruto (a palavra completa de 16 bits)
            this.binaryValue.set(formatBinary(instruction));

            // Inicia a decodificação para as colunas Decimal e Hex
            int unsignedValue = Short.toUnsignedInt((short) instruction);
            int top4bits = (unsignedValue >> 12) & 0xF; // Opcode principal
            int top8bits = (unsignedValue >> 8) & 0xFF; // Para opcodes estendidos

            int operandForDecimal = 0; // Valor final para a coluna Decimal
            int operandForHex = 0;     // Valor final para a coluna Hex

            // CASO 1: Instrução com operando 'x' de 12 bits (Opcodes 0000-1110)
            if (top4bits <= 14) { 
                int operand12 = unsignedValue & 0xFFF; // Isola os 12 bits do operando
                operandForHex = operand12; // Hex exibe o valor bruto (ex: 0FFF)

                // Lógica de Complemento de 2:
                // Apenas o opcode 0111 (LOCO) trata o operando como assinado.
                // Outros (LODD, STOD, JUMP) o tratam como um endereço (sem sinal).
                if (top4bits == 7) { // 0111 == LOCO
                    // Faz a extensão de sinal de 12 bits para 32 bits
                    if ((operand12 & 0x800) != 0) { // Verifica o bit de sinal (12º bit)
                        operandForDecimal = operand12 | 0xFFFFF000; // Negativo (ex: -1)
                    } else {
                        operandForDecimal = operand12; // Positivo (ex: 5)
                    }
                } else {
                    // Trata o operando como um endereço (sempre positivo)
                    operandForDecimal = operand12; // Exibe 4095 como 4095
                }
            } 
            // CASO 2: Instrução com operando 'y' de 8 bits (INSP, DESP)
            else if (top8bits == 0xFC || top8bits == 0xFE) {
                int operand8 = unsignedValue & 0xFF; // Isola os 8 bits do operando
                operandForHex = operand8; 
                
                // Operandos 'y' são sempre sem sinal (0-255)
                operandForDecimal = operand8;
            }
            // CASO 3: Instrução sem operando (PUSH, POP, etc.)
            else {
                operandForDecimal = 0;
                operandForHex = 0;
            }

            // Define os valores finais processados para a UI
            this.decimalValue.set(String.valueOf(operandForDecimal));
            this.hexValue.set(String.format("%04X", operandForHex));
        }
        
        /**
         * Atualiza o endereço e o valor desta entrada.
         * @param address O endereço (formatado como decimal).
         * @param value O novo valor de 16 bits.
         */
        public void update(int address, int value) {
            this.address.set(String.format("%04d", address)); 
            updateValue(value);
        }

        /**
         * Métodos Getters e de Propriedade (Property Methods).
         * Utilizados pelo PropertyValueFactory no Controller para
         * vincular (bind) as colunas da TableView a esta instância.
         */
        public String getAddress() { return address.get(); }
        public StringProperty addressProperty() { return address; }
        public String getBinaryValue() { return binaryValue.get(); }
        public StringProperty binaryValueProperty() { return binaryValue; }
        public String getDecimalValue() { return decimalValue.get(); }
        public StringProperty decimalValueProperty() { return decimalValue; }
        public String getHexValue() { return hexValue.get(); }
        public StringProperty hexValueProperty() { return hexValue; }
    }

    /**
     * Constrói o modelo da Memória Principal.
     * Inicializa o array de backend e a lista observável da UI.
     */
    public MainMemory() {
        this.memoryArray = new int[MEMORY_SIZE];
        this.memoryData = FXCollections.observableArrayList();
        initializeMemory();
    }

    /**
     * Preenche o array de memória com zeros e popula a
     * lista observável (memoryData) com entradas de valor zero.
     */
    private void initializeMemory() {
        Arrays.fill(memoryArray, 0); 
        memoryData.clear();
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memoryData.add(new MemoryEntry(i, 0));
        }
        System.out.println("Memória: Inicializada com " + MEMORY_SIZE + " posições.");
    }

    /**
     * Escreve um valor de 16 bits em um endereço de memória específico.
     *
     * Este método atualiza tanto o array de backend (memoryArray) quanto a
     * lista observável da UI (memoryData), disparando a atualização da TableView.
     *
     * @param address O endereço (0-4095) onde o valor será escrito.
     * @param value O valor de 16 bits (passado como int, preservando o sinal).
     * @throws IndexOutOfBoundsException Se o endereço for inválido.
     */
    public void write(int address, int value) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IndexOutOfBoundsException("Endereço de memória inválido: " + address);
        }
        memoryArray[address] = value;
        if (address < memoryData.size()) {
             memoryData.get(address).updateValue(value);
        }
    }

    /**
     * Lê o valor de 16 bits de um endereço de memória específico.
     *
     * @param address O endereço (0-4095) de onde o valor será lido.
     * @return O valor de 16 bits (como int, preservando o sinal).
     * @throws IndexOutOfBoundsException Se o endereço for inválido.
     */
    public int read(int address) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IndexOutOfBoundsException("Endereço de memória inválido: " + address);
        }
        return memoryArray[address];
    }

    /**
     * Limpa todo o conteúdo da memória, zerando o array de backend e
     * atualizando todas as entradas na lista observável para zero.
     */
    public void clearMemory() {
        Arrays.fill(memoryArray, 0); 
        for (int i = 0; i < memoryData.size(); i++) {
            memoryData.get(i).updateValue(0);
        }
        System.out.println("Memória: Limpa.");
    }

    /**
     * Força uma notificação de atualização para a TableView.
     * Útil após grandes operações (como carregar um programa),
     * embora a atualização por item em write() seja geralmente suficiente.
     */
    public void notifyUpdate() {
        if (memoryData.size() > 0) {
            MemoryEntry entry = memoryData.get(0);
            memoryData.set(0, entry); 
        }
        System.out.println("Memória: Notificação de atualização forçada.");
    }

    /**
     * Retorna a lista observável de entradas de memória.
     * O Controller (MainMemoryController) usa este método para
     * vincular (bind) a TableView aos dados.
     *
     * @return A ObservableList de MemoryEntry.
     */
    public ObservableList<MemoryEntry> getMemoryData() {
        return memoryData;
    }

    /**
     * Formata um valor de 16 bits (passado como int) para uma String
     * binária de 16 dígitos (Complemento de 2).
     *
     * @param value O valor (ex: -1).
     * @return A representação em string (ex: "1111111111111111").
     */
    private static String formatBinary(int value) {
        short shortValue = (short) value; 
        String binary = Integer.toBinaryString(Short.toUnsignedInt(shortValue));
        return String.format("%16s", binary).replace(' ', '0');
    }
}