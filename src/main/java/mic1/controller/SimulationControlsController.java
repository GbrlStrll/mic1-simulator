package mic1.controller;

import javafx.fxml.FXML;
// Importe aqui os componentes do JavaFX que você usará (ex: Button, TextField, CheckBox)
// Ex: import javafx.scene.control.Button;
// Ex: import javafx.scene.control.TextField;
// Ex: import javafx.scene.control.CheckBox;
import mic1.model.SimulationControls; // Importa o Modelo

/**
 * O "Ponte" (Controlador) para a janela de Controles de Simulação.
 *
 * Responsabilidades:
 * 1. Conectar os componentes @FXML (definidos no SimulationControls.fxml) com o Modelo.
 * 2. Ligar (bind) as propriedades do Modelo (ex: simulationSpeed) aos componentes da View.
 * 3. Chamar a lógica do Modelo (ex: controlsModel.play()) quando um botão é clicado.
 */
public class SimulationControlsController {

    // --- Componentes da View (Injetados pelo FXML) ---
    // TODO: Adicione os @FXML para os Botões, TextFields, etc.
    // Exemplo:
    // @FXML
    // private Button playButton;
    //
    // @FXML
    // private Button pauseButton;
    //
    // @FXML
    // private Button resetButton;
    //
    // @FXML
    // private TextField speedTextField;
    //
    // @FXML
    // private CheckBox stepModeCheckBox;


    // --- Referência ao Modelo ---
    private SimulationControls controlsModel;

    /**
     * Este método é o "ponto de entrada" principal.
     * Ele é chamado pela classe SimulationControlsView para injetar o "cérebro" (o Modelo).
     *
     * ISSO CORRIGE O ERRO "setModel is undefined".
     */
    public void setModel(SimulationControls model) {
        this.controlsModel = model;

        // --- A MÁGICA (Data Binding) ---
        // TODO: Ligue as propriedades do seu modelo de Controles aos seus @FXML
        //
        // Exemplo (se você tiver um 'speedProperty()' no modelo):
        // speedTextField.textProperty().bindBidirectional(controlsModel.speedProperty());
        // stepModeCheckBox.selectedProperty().bindBidirectional(controlsModel.stepModeProperty());
    }

    // --- Métodos de Evento (Chamados pelo FXML) ---
    
    // TODO: Adicione os métodos 'handle' para os seus botões.
    // O onAction="#handlePlayButton" no FXML chamará este método:
    //
    // @FXML
    // private void handlePlayButton() {
    //     if (controlsModel != null) {
    //         controlsModel.play(); // Delega a ação para o modelo
    //     }
    // }
    //
    // @FXML
    // private void handlePauseButton() {
    //     if (controlsModel != null) {
    //         controlsModel.pause();
    //     }
    // }
    //
    // @FXML
    // private void handleResetButton() {
    //     if (controlsModel != null) {
    //         controlsModel.reset();
    //     }
    // }

}