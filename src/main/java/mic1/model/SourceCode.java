package mic1.model;
    
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SourceCode {
    private final StringProperty assemblyCode;
    private final StringProperty compiledCode;
    private final StringProperty assemblyStatus;

    public SourceCode() {
        // modelo gerencia codigo fonte e resultado da compilacao
        // propriedades observaveis mantem ui sincronizada com estado do modelo
        this.assemblyCode = new SimpleStringProperty("");
        this.compiledCode = new SimpleStringProperty("");
        this.assemblyStatus = new SimpleStringProperty("Pronto");
    }

    public String getAssemblyCode() {
        return assemblyCode.get();
    }

    public void setAssemblyCode(String code) {
        assemblyCode.set(code);
    }

    public StringProperty assemblyCodeProperty() {
        return assemblyCode;
    }

    public String getCompiledCode() {
        return compiledCode.get();
    }

    public void setCompiledCode(String code) {
        compiledCode.set(code);
    }

    public StringProperty compiledCodeProperty() {
        return compiledCode;
    }

    public String getAssemblyStatus() {
        return assemblyStatus.get();
    }

    public void setAssemblyStatus(String status) {
        assemblyStatus.set(status);
    }

    public StringProperty assemblyStatusProperty() {
        return assemblyStatus;
    }

    public void clear() {
        assemblyCode.set("");
        compiledCode.set("");
        assemblyStatus.set("Pronto");
    }
}