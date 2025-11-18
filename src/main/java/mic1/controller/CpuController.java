package mic1.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Circle;
import mic1.core.Register;
import mic1.model.CPU;

import java.net.URL;
import java.util.ResourceBundle;

public class CpuController implements Initializable {
    @FXML private TableView<Register> registerTable;
    @FXML private TableColumn<Register, String> registerNameColumn;
    @FXML private TableColumn<Register, Integer> registerValueColumn;
    @FXML private TextArea microcodeLogArea;
    @FXML private Label cyclesLabel;
    @FXML private Label statusLabel;
    @FXML private Circle statusIndicator;

    private CPU cpuModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Define como obter os dados para cada coluna
        registerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        registerValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        // Formata a coluna de valor para exibir em Hex e Decimal
        registerValueColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Formata como "0x0000FFFF (65535)"
                    setText(String.format("0x%08X (%d)", item, item));
                }
            }
        });
    }

    /**
     * Injeta o modelo (CPU) neste controlador.
     * Este método é chamado pela classe Main para conectar a lógica (model)
     * com a interface (controller).
     */
    public void setModel(CPU model) {
        this.cpuModel = model;

        registerTable.setItems(this.cpuModel.getRegisterList());

        cyclesLabel.textProperty().bind(
            this.cpuModel.cycleCountProperty().asString("Cycles: %d")
        );

        statusLabel.textProperty().bind(this.cpuModel.statusProperty());

        model.runningProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                statusIndicator.getStyleClass().remove("status-indicator-off");
                statusIndicator.getStyleClass().add("status-indicator-on");
            } else {
                statusIndicator.getStyleClass().remove("status-indicator-on");
                statusIndicator.getStyleClass().add("status-indicator-off");
            }
        });

        this.cpuModel.getMicrocodeLog().addListener((javafx.collections.ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (String log : change.getAddedSubList()) {
                        microcodeLogArea.appendText(log + "\n");
                    }
                }
            }
        });
    }
}