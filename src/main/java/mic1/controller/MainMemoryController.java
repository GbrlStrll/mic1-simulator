package mic1.controller;

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
        // Define como obter os dados para cada coluna
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        binaryColumn.setCellValueFactory(new PropertyValueFactory<>("binaryValue"));
        decimalColumn.setCellValueFactory(new PropertyValueFactory<>("decimalValue"));
        hexColumn.setCellValueFactory(new PropertyValueFactory<>("hexValue"));
    }

    /**
     * Injeta o modelo (MainMemory) neste controlador.
     * Este método é chamado pela classe Main para conectar a lógica (model)
     * com a interface (controller).
     */
    public void setModel(MainMemory model) {
        this.memoryModel = model;

        // 1. Liga a tabela para observar a lista de palavras de memória.
        memoryTable.setItems(this.memoryModel.getMemoryList());

        // 2. Adiciona um "ouvinte" para atualizações.
        //    Quando o modelo notificar que a memória mudou (em outra thread),
        //    a tabela será atualizada na thread principal do JavaFX.
        this.memoryModel.memoryChangedProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                memoryTable.refresh();
            });
        });
    }
}