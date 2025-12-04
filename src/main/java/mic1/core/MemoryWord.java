package mic1.core;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class MemoryWord {
    private final IntegerProperty address;
    private final IntegerProperty value;
    private final StringProperty binaryValue;
    private final StringProperty decimalValue;
    private final StringProperty hexValue;

    public MemoryWord(int address, int value) {
        this.address = new SimpleIntegerProperty(address);
        this.value = new SimpleIntegerProperty(value);
        this.binaryValue = new SimpleStringProperty();
        this.decimalValue = new SimpleStringProperty();
        this.hexValue = new SimpleStringProperty();
        updateRepresentations();
    }

    public int getAddress() {
        return address.get();
    }

    public IntegerProperty addressProperty() {
        return address;
    }

    public int getValue() {
        return value.get();
    }

    public void setValue(int newValue) {
        value.set(newValue);
        updateRepresentations();
    }

    public IntegerProperty valueProperty() {
        return value;
    }

    public String getBinaryValue() {
        return binaryValue.get();
    }

    public StringProperty binaryValueProperty() {
        return binaryValue;
    }

    public String getDecimalValue() {
        return decimalValue.get();
    }

    public StringProperty decimalValueProperty() {
        return decimalValue;
    }

    public String getHexValue() {
        return hexValue.get();
    }

    public StringProperty hexValueProperty() {
        return hexValue;
    }

    private void updateRepresentations() {
        // quando valor muda, atualiza todas as representacoes automaticamente
        // mantem binario, decimal e hex sincronizados para exibicao na tabela
        int val = value.get();

        // formata binario com 32 bits, tratando numeros negativos corretamente
        String binary = String.format("%32s", Integer.toBinaryString(val)).replace(' ', '0');
        if (val < 0) {
            binary = binary.substring(binary.length() - 32);
        }
        binaryValue.set(binary);

        decimalValue.set(String.valueOf(val));
        hexValue.set(String.format("0x%08X", val));
    }
}
