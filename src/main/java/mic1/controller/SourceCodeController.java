package mic1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import mic1.assembler.Assembler;
import mic1.core.MicroInstruction;
import mic1.model.CPU;
import mic1.model.SourceCode;

public class SourceCodeController {
    @FXML private TextArea assemblyArea;
    @FXML private TextArea compiledArea;
    @FXML private Button assembleButton;
    @FXML private Button writeMemoryButton;

    private SourceCode sourceCodeModel;
    private CPU cpu;
    private Assembler assembler;
    private Assembler.AssemblyResult lastResult;

    public SourceCodeController() {
        this.assembler = new Assembler();
    }

    /**
     * Injeta o modelo SourceCode.
     * Faz o data binding bidirecional com a área de texto do assembly
     * e unidirecional com a área de texto compilada.
     */
    public void setModel(SourceCode model) {
        this.sourceCodeModel = model;
        assemblyArea.textProperty().bindBidirectional(model.assemblyCodeProperty());
        compiledArea.textProperty().bind(model.compiledCodeProperty());
    }

    /**
     * Injeta a instância da CPU.
     */
    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }

    /**
     * Chamado quando o botão "Assemble" é clicado.
     * Pega o texto da 'assemblyArea', chama o montador e exibe
     * o resultado ou os erros na 'compiledArea'.
     */
    @FXML
    private void handleAssemble() {
        String code = assemblyArea.getText();
        lastResult = assembler.assemble(code);

        if (lastResult.isSuccess()) {
            StringBuilder output = new StringBuilder();
            output.append(lastResult.getCompiledOutput());
            
            // Opcional: Adiciona informações de debug
            output.append("\n=== DEBUG INFO ===\n");
            MicroInstruction[] instructions = lastResult.getInstructions();
            for (int i = 0; i < instructions.length; i++) {
                MicroInstruction mi = instructions[i];
                if (mi == null) continue; // Pula endereços não utilizados
                output.append(String.format("%d: A=%d B=%d C=%d ALU=%s COND=%s ADDR=%d ENC=%s\n",
                        i, mi.getRegA(), mi.getRegB(), mi.getRegC(),
                        mi.getAluOp(), mi.getCond(), mi.getAddr(), mi.isEnc()));
            }

            sourceCodeModel.setCompiledCode(output.toString());
            sourceCodeModel.setAssemblyStatus("Assembly successful!");
        } else {
            // Se falhou, exibe os erros
            sourceCodeModel.setCompiledCode(lastResult.getErrorOutput());
            sourceCodeModel.setAssemblyStatus("Assembly failed with errors");
        }
    }

    /**
     * Chamado quando o botão "Write to Control Store" é clicado.
     * Pega as microinstruções do último 'assemble' bem-sucedido
     * e as escreve na memória de controle da CPU.
     */
    @FXML
    private void handleWriteMemory() {
        if (lastResult == null || !lastResult.isSuccess()) {
            sourceCodeModel.setCompiledCode("Please assemble code successfully first!");
            return;
        }

        if (cpu == null) {
            sourceCodeModel.setCompiledCode("CPU not initialized!");
            return;
        }

        // Escreve as microinstruções na CPU
        MicroInstruction[] instructions = lastResult.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i] != null) {
                cpu.setMicroInstruction(i, instructions[i]);
            }
        }

        sourceCodeModel.setAssemblyStatus("Microcode loaded into CPU control store!");
        sourceCodeModel.setCompiledCode(sourceCodeModel.getCompiledCode() + "\n\nMicrocode successfully loaded into CPU!");
    }
}