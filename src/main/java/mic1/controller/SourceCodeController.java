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

    public void setModel(SourceCode model) {
        this.sourceCodeModel = model;
        // texto digitado reflete direto no modelo
        assemblyArea.textProperty().bindBidirectional(model.assemblyCodeProperty());
        compiledArea.textProperty().bind(model.compiledCodeProperty());
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }

    @FXML
    private void handleAssemble() {
        // processo de montagem: converte texto assembly em microinstrucoes binarias
        // primeiro passa: identifica labels e monta instrucoes
        // segunda passa: resolve enderecos de labels em instrucoes goto
        String code = assemblyArea.getText();
        lastResult = assembler.assemble(code);

        if (lastResult.isSuccess()) {
            // montagem bem sucedida: mostra codigo compilado e informacoes de debug
            StringBuilder output = new StringBuilder();
            output.append(lastResult.getCompiledOutput());
            
            // adiciona detalhes de cada microinstrucao para facilitar debug
            output.append("\n=== DEBUG INFO ===\n");
            MicroInstruction[] instructions = lastResult.getInstructions();
            for (int i = 0; i < instructions.length; i++) {
                MicroInstruction mi = instructions[i];
                if (mi == null) continue;
                output.append(String.format("%d: A=%d B=%d C=%d ALU=%s COND=%s ADDR=%d ENC=%s\n",
                        i, mi.getRegA(), mi.getRegB(), mi.getRegC(),
                        mi.getAluOp(), mi.getCond(), mi.getAddr(), mi.isEnc()));
            }

            sourceCodeModel.setCompiledCode(output.toString());
            sourceCodeModel.setAssemblyStatus("Montagem concluida com sucesso!");
        } else {
            // montagem falhou: mostra lista de erros encontrados
            sourceCodeModel.setCompiledCode(lastResult.getErrorOutput());
            sourceCodeModel.setAssemblyStatus("Montagem falhou com erros");
        }
    }

    @FXML
    private void handleWriteMemory() {
        // carrega microinstrucoes compiladas na control store da cpu
        // cada instrucao vai para o endereco correspondente na store
        if (lastResult == null || !lastResult.isSuccess()) {
            sourceCodeModel.setCompiledCode("Por favor monte o codigo com sucesso primeiro!");
            return;
        }

        if (cpu == null) {
            sourceCodeModel.setCompiledCode("CPU nao inicializada!");
            return;
        }

        // escreve cada microinstrucao no endereco correspondente
        // enderecos vazios (null) sao ignorados para manter instrucoes anteriores
        MicroInstruction[] instructions = lastResult.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i] != null) {
                cpu.setMicroInstruction(i, instructions[i]);
            }
        }

        sourceCodeModel.setAssemblyStatus("Microcodigo carregado na control store da CPU!");
        sourceCodeModel.setCompiledCode(sourceCodeModel.getCompiledCode() + "\n\nMicrocodigo carregado com sucesso na CPU!");
    }
}