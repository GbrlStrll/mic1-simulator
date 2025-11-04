package mic1.model;

<<<<<<< Updated upstream
public class SourceCode {
    
=======
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SourceCode {
    private final StringProperty assemblyCode;
    private final StringProperty compiledCode;
    private final StringProperty assemblyStatus;

    public SourceCode() {
        this.assemblyCode = new SimpleStringProperty("");
        this.compiledCode = new SimpleStringProperty("");
        this.assemblyStatus = new SimpleStringProperty("Ready");
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
        assemblyStatus.set("Ready");
    }
>>>>>>> Stashed changes
}
