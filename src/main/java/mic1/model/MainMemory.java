package mic1.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Arrays;

/**
 * O "Cérebro" (Backend) da Memória Principal. (MODELO)
 */
public class MainMemory {

    public static final int MEMORY_SIZE = 4096;
    private final int[] memoryArray;
    private final ObservableList<MemoryEntry> memoryData;

    /**
     * Classe interna para representar uma linha na TableView da Memória.
     *
     * (Lógica de C2 corrigida para aplicar APENAS ao LOCO)
     */
    public static class MemoryEntry {
        private final SimpleStringProperty address = new SimpleStringProperty();
        private final SimpleStringProperty binaryValue = new SimpleStringProperty();
        private final SimpleStringProperty decimalValue = new SimpleStringProperty();
        private final SimpleStringProperty hexValue = new SimpleStringProperty();

        public MemoryEntry(int address, int value) {
            update(address, value); 
        }

        /** * Atualiza todas as representações (Bin, Dec, Hex) com base no valor.
         * @param value O valor de 16 bits *assinado* (ex: -1) lido da memória.
         */
        public void updateValue(int value) {
            int instruction = value; 

            // 1. Coluna "Binary Value": Mostra a instrução bruta de 16 bits
            this.binaryValue.set(formatBinary(instruction));

            // --- Lógica de Decodificação (Disassembly) ---
            int unsignedValue = Short.toUnsignedInt((short) instruction);
            int top4bits = (unsignedValue >> 12) & 0xF; 
            int top8bits = (unsignedValue >> 8) & 0xFF;

            int operandForDecimal = 0; // Valor final para a coluna Decimal
            int operandForHex = 0;     // Valor final para a coluna Hex

            // CASO 1: Instrução com operando 'x' de 12 bits
            if (top4bits <= 14) { 
                int operand12 = unsignedValue & 0xFFF; 
                operandForHex = operand12; // Hex sempre exibe os bits brutos (ex: 0FFF)

                // --- CORREÇÃO (Refinada) ---
                // Apenas LOCO (opcode 0111) trata o operando como C2 (assinado).
                // Todos os outros (LODD, STOD, JUMP) o tratam como um endereço (sem sinal).
                
                if (top4bits == 7) { // SE O OPCODE FOR 0111 (LOCO)
                    // Faz a extensão de sinal dos 12 bits
                    if ((operand12 & 0x800) != 0) { 
                        operandForDecimal = operand12 | 0xFFFFF000; // É negativo (ex: -1)
                    } else {
                        operandForDecimal = operand12; // É positivo (ex: 5)
                    }
                } else {
                    // NÃO É LOCO (é LODD, STOD, JUMP, etc.)
                    // Trata como um endereço positivo (sem sinal)
                    operandForDecimal = operand12; // Exibe 4095 como 4095
                }
                // --- FIM DA CORREÇÃO REFINADA ---

            } 
            // CASO 2: Instrução com operando 'y' de 8 bits
            else if (top8bits == 0xFC || top8bits == 0xFE) {
                int operand8 = unsignedValue & 0xFF; 
                operandForHex = operand8; 

                // Operandos 'y' (INSP/DESP) são sempre *sem sinal* (0-255)
                // Portanto, não fazemos extensão de sinal.
                operandForDecimal = operand8;
            }
            // CASO 3: Instrução sem operando
            else {
                operandForDecimal = 0;
                operandForHex = 0;
            }

            // 2. Coluna "Decimal Value": Mostra o operando interpretado
            this.decimalValue.set(String.valueOf(operandForDecimal));
            
            // 3. Coluna "Hexadecimal Value": Mostra o operando *não assinado*
            this.hexValue.set(String.format("%04X", operandForHex));
        }
        
        /** Atualiza o endereço e o valor */
        public void update(int address, int value) {
            this.address.set(String.format("%04d", address)); // (Correção 4)
            updateValue(value);
        }

        // --- Getters e Métodos de Propriedade ---
        public String getAddress() { return address.get(); }
        public StringProperty addressProperty() { return address; }
        public String getBinaryValue() { return binaryValue.get(); }
        public StringProperty binaryValueProperty() { return binaryValue; }
        public String getDecimalValue() { return decimalValue.get(); }
        public StringProperty decimalValueProperty() { return decimalValue; }
        public String getHexValue() { return hexValue.get(); }
        public StringProperty hexValueProperty() { return hexValue; }
    }

    // --- (Restante da classe MainMemory.java) ---

    public MainMemory() {
        this.memoryArray = new int[MEMORY_SIZE];
        this.memoryData = FXCollections.observableArrayList();
        initializeMemory();
    }

    private void initializeMemory() {
        Arrays.fill(memoryArray, 0); 
        memoryData.clear();
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memoryData.add(new MemoryEntry(i, 0));
        }
        System.out.println("Memória: Inicializada com " + MEMORY_SIZE + " posições.");
    }

    public void write(int address, int value) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IndexOutOfBoundsException("Endereço de memória inválido: " + address);
        }
        memoryArray[address] = value;
        if (address < memoryData.size()) {
             memoryData.get(address).updateValue(value);
        }
    }

public int read(int address) {
        if (address < 0 || address >= MEMORY_SIZE) {
            throw new IndexOutOfBoundsException("Endereço de memória inválido: " + address);
        }
        return memoryArray[address];
    }

    public void clearMemory() {
        Arrays.fill(memoryArray, 0); 
        for (int i = 0; i < memoryData.size(); i++) {
            memoryData.get(i).updateValue(0);
        }
        System.out.println("Memória: Limpa.");
    }

    public void notifyUpdate() {
        if (memoryData.size() > 0) {
            MemoryEntry entry = memoryData.get(0);
            memoryData.set(0, entry); 
        }
        System.out.println("Memória: Notificação de atualização forçada.");
    }

    public ObservableList<MemoryEntry> getMemoryData() {
        return memoryData;
    }

    private static String formatBinary(int value) {
        short shortValue = (short) value; 
        String binary = Integer.toBinaryString(Short.toUnsignedInt(shortValue));
        return String.format("%16s", binary).replace(' ', '0');
    }
}