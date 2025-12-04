package mic1.core;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

public class Register {
    private final StringProperty name;
    private final IntegerProperty value;
    private final boolean isReadOnly;
    private final int defaultValue;

    public Register(String name, int defaultValue, boolean isReadOnly) {
        // registrador com propriedades observaveis para atualizacao automatica da ui
        // read-only impede escrita em registradores constantes como +1, -1, 0
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleIntegerProperty(defaultValue);
        this.defaultValue = defaultValue;
        this.isReadOnly = isReadOnly;
    }

    public Register(String name, int defaultValue) {
        this(name, defaultValue, false);
    }

    public Register(String name) {
        this(name, 0, false);
    }

    public int getValue() {
        return value.get();
    }

    public void setValue(int newValue) {
        // protecao contra escrita em registradores read-only
        // registradores constantes mantem valores fixos mesmo se tentarem escrever
        if (!isReadOnly) {
            value.set(newValue);
        }
    }

    public IntegerProperty valueProperty() {
        return value;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void reset() {
        // restaura valor inicial do registrador
        // usado quando cpu e resetada para voltar ao estado inicial
        if (!isReadOnly) {
            value.set(defaultValue);
        }
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    @Override
    public String toString() {
        return String.format("%s: 0x%08X (%d)", getName(), getValue(), getValue());
    }
}
