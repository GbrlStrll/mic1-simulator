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
        // configura colunas da tabela para usar propriedades dos objetos register
        // javafx automaticamente atualiza quando as propriedades mudam
        registerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        registerValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        // customiza formato da coluna de valores para mostrar hex e decimal
        registerValueColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("0x%08X (%d)", item, item));
                }
            }
        });
    }

    public void setModel(CPU model) {
        // injecao de dependencia: conecta controller com modelo
        // todas as atualizacoes no modelo refletem automaticamente na ui
        this.cpuModel = model;

        // tabela de registradores observa lista do modelo
        // quando registradores mudam, tabela atualiza automaticamente
        registerTable.setItems(this.cpuModel.getRegisterList());

        // labels seguem propriedades do modelo via binding
        cyclesLabel.textProperty().bind(
            this.cpuModel.cycleCountProperty().asString("Ciclos: %d")
        );

        statusLabel.textProperty().bind(this.cpuModel.statusProperty());

        // indicador visual muda de cor baseado no estado running
        model.runningProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                statusIndicator.getStyleClass().remove("status-indicator-off");
                statusIndicator.getStyleClass().add("status-indicator-on");
            } else {
                statusIndicator.getStyleClass().remove("status-indicator-on");
                statusIndicator.getStyleClass().add("status-indicator-off");
            }
        });

        // area de log recebe mensagens do modelo conforme microinstrucoes sao executadas
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