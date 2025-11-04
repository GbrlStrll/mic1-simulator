package mic1.controller;

<<<<<<< Updated upstream
public class SourceCodeController {
    
=======
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import mic1.assembler.Assembler;
import mic1.core.MicroInstruction;
import mic1.model.CPU;
import mic1.model.MainMemory;
import mic1.model.SourceCode;

public class SourceCodeController {
    @FXML private TextArea assemblyArea;
    @FXML private TextArea compiledArea;
    @FXML private Button assembleButton;
    @FXML private Button writeMemoryButton;

    private SourceCode sourceCodeModel;
    private CPU cpu;
    private MainMemory memory;
    private Assembler assembler;
    private Assembler.AssemblyResult lastResult;

    public SourceCodeController() {
        this.assembler = new Assembler();
    }

    public void setModel(SourceCode model) {
        this.sourceCodeModel = model;
        assemblyArea.textProperty().bindBidirectional(model.assemblyCodeProperty());
        compiledArea.textProperty().bind(model.compiledCodeProperty());
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }

    public void setMemory(MainMemory memory) {
        this.memory = memory;
    }

    @FXML
    private void handleAssemble() {
        String code = assemblyArea.getText();
        lastResult = assembler.assemble(code);

        if (lastResult.isSuccess()) {
            StringBuilder output = new StringBuilder();
            output.append(lastResult.getCompiledOutput());
            output.append("\n=== DEBUG INFO ===\n");

            MicroInstruction[] instructions = lastResult.getInstructions();
            for (int i = 0; i < instructions.length; i++) {
                MicroInstruction mi = instructions[i];
                output.append(String.format("%d: A=%d B=%d C=%d ALU=%s COND=%s ADDR=%d ENC=%s\n",
                    i, mi.getRegA(), mi.getRegB(), mi.getRegC(),
                    mi.getAluOp(), mi.getCond(), mi.getAddr(), mi.isEnc()));
            }

            sourceCodeModel.setCompiledCode(output.toString());
            sourceCodeModel.setAssemblyStatus("Assembly successful!");
        } else {
            sourceCodeModel.setCompiledCode(lastResult.getErrorOutput());
            sourceCodeModel.setAssemblyStatus("Assembly failed with errors");
        }
    }

    @FXML
    private void handleWriteMemory() {
        if (lastResult == null || !lastResult.isSuccess()) {
            sourceCodeModel.setCompiledCode("Please assemble code first!");
            return;
        }

        if (cpu == null) {
            sourceCodeModel.setCompiledCode("CPU not initialized!");
            return;
        }

        MicroInstruction[] instructions = lastResult.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            cpu.setMicroInstruction(i, instructions[i]);
        }

        sourceCodeModel.setAssemblyStatus("Microcode loaded into CPU control store!");
        sourceCodeModel.setCompiledCode(sourceCodeModel.getCompiledCode() + "\n\nMicrocode successfully loaded into CPU!");
    }
>>>>>>> Stashed changes
}
