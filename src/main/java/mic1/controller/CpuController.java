package mic1.controller;

import javafx.fxml.FXML;
// Importe aqui os componentes do JavaFX que você usará (ex: Label, TextField)
// Ex: import javafx.scene.control.Label;
import mic1.model.CPU; // Importa o Modelo

/**
 * O "Ponte" (Controlador) para a janela da CPU.
 *
 * Responsabilidades:
 * 1. Conectar os componentes @FXML (definidos no CPU.fxml) com o Modelo.
 * 2. Ligar (bind) as propriedades do Modelo (ex: pc, ac) aos componentes da View.
 * 3. (Este controlador geralmente não lida com cliques de botão,
 * pois a CPU é controlada pelo SimulationControls).
 */
public class CpuController {

    // --- Componentes da View (Injetados pelo FXML) ---
    // TODO: Adicione os @FXML para os Labels/TextFields da sua CPU.fxml
    // Exemplo:
    // @FXML
    // private Label pcValueLabel;
    //
    // @FXML
    // private Label acValueLabel;
    //
    // @FXML
    // private Label spValueLabel;


    // --- Referência ao Modelo ---
    private CPU cpuModel;

    /**
     * Este método é o "ponto de entrada" principal.
     * Ele é chamado pela classe CpuView para injetar o "cérebro" (o Modelo).
     *
     * ISSO CORRIGE O ERRO "setModel is undefined".
     */
    public void setModel(CPU model) {
        this.cpuModel = model;

        // --- A MÁGICA (Data Binding) ---
        // TODO: Ligue as propriedades do seu modelo de CPU aos seus @FXML Labels
        //
        // Exemplo (se você tiver um 'pcProperty()' no seu modelo CPU.java):
        // pcValueLabel.textProperty().bind(cpuModel.pcProperty().asString());
        // acValueLabel.textProperty().bind(cpuModel.acProperty().asString());
        // spValueLabel.textProperty().bind(cpuModel.spProperty().asString());
    }

}