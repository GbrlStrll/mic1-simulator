package mic1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mic1.model.SourceCode;

/**
 * Controlador para a janela "Source Code".
 *
 * Responsabilidades:
 * - Realizar o binding entre componentes JavaFX e o modelo {@link SourceCode}.
 * - Encaminhar ações da UI para o modelo (montagem e escrita na memória).
 */
public class SourceCodeController {

    @FXML
    private TextArea assemblyTextArea;

    @FXML
    private TextArea compiledTextArea;

    private SourceCode sourceModel;

    /**
     * Injeta o modelo e configura as ligações entre view e modelo.
     * @param model instância de {@link SourceCode}
     */
    public void setModel(SourceCode model) {
        this.sourceModel = model;

        assemblyTextArea.textProperty().bindBidirectional(model.assemblyCodeProperty());
        compiledTextArea.textProperty().bind(model.compiledCodeProperty());
    }

    /**
     * Handler invocado pelo botão "Assemble". Dispara o processo de montagem.
     */
    @FXML
    private void handleAssembleButton() {
        if (sourceModel != null) {
            sourceModel.assemble();
        }
    }

    /**
     * Handler invocado pelo botão "Write code to Main Memory". Transfere o
     * código compilado para a memória principal.
     */
    @FXML
    private void handleWriteToMemoryButton() {
        if (sourceModel != null) {
            sourceModel.writeToMemory();
        }
    }
}