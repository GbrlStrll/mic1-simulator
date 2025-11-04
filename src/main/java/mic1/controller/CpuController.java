package mic1.controller;

<<<<<<< Updated upstream
//import javafx.fxml.FXML;
//import javafx.scene.control.Label;
//import javafx.scene.control.TableView;
//import mic1.model.CPU; // Importa o Modelo

/**
 * A "Ponte" (Controlador)
 *
 * O QUE FAZER AQUI:
 * 1. Usar '@FXML' para criar uma variável Java para CADA componente do FXML
 * que você precisa acessar (ex: @FXML private TableView registerTable;).
 *
 * 2. O nome da variável DEVE ser igual ao 'fx:id' no FXML.
 *
 * 3. Criar um método 'setModel(CPU model)'. Este método será chamado
 * pela classe 'CpuView' para "injetar" o cérebro.
 *
 * 4. Dentro do 'setModel', fazer o "Data Binding": ligar os componentes @FXML
 * às propriedades do modelo.
 * Ex: 'cyclesLabel.textProperty().bind(model.cyclesProperty().asString("Cycles: %d"))'
 * Ex: 'registerTable.setItems(model.getRegisterList())'
 *
 * 5. Implementar 'Initializable' (opcional) para configurar coisas que NÃO
 * dependem do modelo (como o 'PropertyValueFactory' das colunas da tabela).
 *
 * 6. Criar métodos '@FXML' para os eventos (ex: botões) e, dentro deles,
 * chamar a lógica do modelo (ex: 'model.runCycle()').
 */

public class CpuController {
    // Exemplo de variáveis @FXML
    // @FXML private TableView registerTable;
    // @FXML private Label cyclesLabel;

    // Exemplo do 'setModel'
    // private CPU cpuModel;
    // public void setModel(CPU model) {
    //     this.cpuModel = model;
    //     cyclesLabel.textProperty().bind(cpuModel.cyclesProperty().asString("Cycles: %d"));
    // }
=======
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
        registerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        registerValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

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
        this.cpuModel = model;

        registerTable.setItems(model.getRegisterList());

        cyclesLabel.textProperty().bind(
            model.cycleCountProperty().asString("Cycles: %d")
        );

        statusLabel.textProperty().bind(model.statusProperty());

        model.runningProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                statusIndicator.getStyleClass().remove("status-indicator-off");
                statusIndicator.getStyleClass().add("status-indicator-on");
            } else {
                statusIndicator.getStyleClass().remove("status-indicator-on");
                statusIndicator.getStyleClass().add("status-indicator-off");
            }
        });

        model.getMicrocodeLog().addListener((javafx.collections.ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (String log : change.getAddedSubList()) {
                        microcodeLogArea.appendText(log + "\n");
                    }
                }
            }
        });
    }
>>>>>>> Stashed changes
}
