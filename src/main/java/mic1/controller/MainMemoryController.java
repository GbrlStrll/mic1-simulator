package mic1.controller;

<<<<<<< Updated upstream
public class MainMemoryController {
    
=======
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import mic1.core.MemoryWord;
import mic1.model.MainMemory;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMemoryController implements Initializable {
    @FXML private TableView<MemoryWord> memoryTable;
    @FXML private TableColumn<MemoryWord, Integer> addressColumn;
    @FXML private TableColumn<MemoryWord, String> binaryColumn;
    @FXML private TableColumn<MemoryWord, String> decimalColumn;
    @FXML private TableColumn<MemoryWord, String> hexColumn;

    private MainMemory memoryModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        binaryColumn.setCellValueFactory(new PropertyValueFactory<>("binaryValue"));
        decimalColumn.setCellValueFactory(new PropertyValueFactory<>("decimalValue"));
        hexColumn.setCellValueFactory(new PropertyValueFactory<>("hexValue"));
    }

    public void setModel(MainMemory model) {
        this.memoryModel = model;
        memoryTable.setItems(model.getMemoryList());

        model.memoryChangedProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                memoryTable.refresh();
            });
        });
    }
>>>>>>> Stashed changes
}
