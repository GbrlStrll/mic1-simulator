package mic1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
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
    @FXML
    private Button playButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button resetButton;
    
    @FXML
    private TextField subcycleSpeedField;

    @FXML
    private Button subcycleApplyButton;

    @FXML
    private TextField pcPauseField;

    @FXML 
    private Button pcPauseApplyButton;

    @FXML 
    private CheckBox stepModeCheckBox;

    @FXML 
    private Button stepCycleButton;

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
    // (Preenchendo o segundo TODO)
    // Isso sincroniza a UI com o Modelo automaticamente.

    // 1. Sincroniza os campos e o checkbox
    stepModeCheckBox.selectedProperty().bindBidirectional(controlsModel.isStepModeProperty());
    subcycleSpeedField.textProperty().bindBidirectional(controlsModel.subcycleSpeedProperty());
    pcPauseField.textProperty().bindBidirectional(controlsModel.pcPauseValueProperty());

    // 2. Lógica de UI: Desabilita/Habilita botões baseado no estado 'isRunning'
    
    // O botão PLAY é desabilitado QUANDO a simulação ESTÁ rodando
    playButton.disableProperty().bind(controlsModel.isRunningProperty());
    
    // Os botões PAUSE e STOP são desabilitados QUANDO a simulação NÃO ESTÁ rodando
    pauseButton.disableProperty().bind(controlsModel.isRunningProperty().not());
    stopButton.disableProperty().bind(controlsModel.isRunningProperty().not());
    
    // O botão de "Step" (Go through a cycle) só é habilitado quando a simulação está PAUSADA
    stepCycleButton.disableProperty().bind(controlsModel.isRunningProperty());
}

    // --- Métodos de Evento (Chamados pelo FXML) ---
    
    // TODO: Adicione os métodos 'handle' para os seus botões.
    // O onAction="#handlePlayButton" no FXML chamará este método:
    //
    @FXML
    private void handlePlayButton() {
        if (controlsModel != null) {
            controlsModel.play(); // Delega a ação para o modelo
        }
    }

    @FXML
    private void handlePauseButton() {
        if (controlsModel != null) {
            controlsModel.pause();
        }
    }
    
    @FXML
    private void handleStopButton() {
        if (controlsModel != null) {
            controlsModel.pause(); 
        }
    }

    @FXML
    private void handleResetButton() {
        if (controlsModel != null) {
            controlsModel.reset();
        }
    }

    @FXML
    private void handleStepCycle() {
        if (controlsModel != null) {
            controlsModel.stepCycle();
        }
    }

    @FXML
    private void handleSubcycleApply() {
        System.out.println("Subcycle speed aplicado (automaticamente via binding).");
    }

    @FXML
    private void handlePcPauseApply() {
        System.out.println("PC Pause aplicado (automaticamente via binding).");
 
    }
}