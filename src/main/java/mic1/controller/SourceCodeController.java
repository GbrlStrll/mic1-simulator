package mic1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mic1.model.SourceCode;

/**
 * O "Ponte" (Controlador) para a janela SourceCode.
 *
 * Responsabilidades:
 * 1. Conectar os componentes @FXML (definidos no FXML) com o Modelo.
 * 2. Ligar (bind) as propriedades do Modelo (ex: assemblyCode) aos componentes da View.
 * 3. Chamar a lógica do Modelo (ex: sourceModel.assemble()) quando um botão é clicado.
 */
public class SourceCodeController {

    // --- Componentes da View (Injetados pelo FXML) ---
    
    // O 'fx:id' no FXML deve ser "assemblyTextArea"
    @FXML
    private TextArea assemblyTextArea;

    // O 'fx:id' no FXML deve ser "compiledTextArea"
    @FXML
    private TextArea compiledTextArea;

    // --- Referência ao Modelo ---
    
    private SourceCode sourceModel;

    /**
     * Este método é o "ponto de entrada" principal.
     * Ele é chamado pela classe SourceCodeView para injetar o "cérebro" (o Modelo).
     */
    public void setModel(SourceCode model) {
        this.sourceModel = model;

        // --- A MÁGICA (Data Binding) ---

        // 1. Ligação Bidirecional (Two-Way Binding)
        // - O que o usuário digita na TextArea ATUALIZA o Modelo.
        // - O que o Modelo carregar (ex: texto padrão) ATUALIZA a TextArea.
        assemblyTextArea.textProperty().bindBidirectional(model.assemblyCodeProperty());

        // 2. Ligação Unidirecional (One-Way Binding)
        // - O que o Modelo calcular (o código compilado) ATUALIZA a TextArea.
        // - O usuário não pode digitar (já que o FXML tem editable="false").
        compiledTextArea.textProperty().bind(model.compiledCodeProperty());
    }

    // --- Métodos de Evento (Chamados pelo FXML) ---

    /**
     * Este método é chamado quando o botão "Assemble" é clicado.
     * (O 'onAction' no FXML deve ser "#handleAssembleButton")
     */
    @FXML
    private void handleAssembleButton() {
        if (sourceModel != null) {
            // Chama a lógica de negócio que está no Modelo
            sourceModel.assemble();
        }
    }

    /**
     * Este método é chamado quando o botão "Write code to Main Memory" é clicado.
     * (O 'onAction' no FXML deve ser "#handleWriteToMemoryButton")
     */
    @FXML
    private void handleWriteToMemoryButton() {
        if (sourceModel != null) {
            // Chama a lógica de negócio que está no Modelo
            sourceModel.writeToMemory();
        }
    }
}