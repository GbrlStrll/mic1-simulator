package mic1.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import mic1.core.MemoryWord;
import mic1.model.MainMemory;
import mic1.model.Cache;

import java.net.URL;
import java.util.ResourceBundle;

public class MainMemoryController implements Initializable {
    @FXML private TableView<MemoryWord> memoryTable;
    @FXML private TableColumn<MemoryWord, Integer> addressColumn;
    @FXML private TableColumn<MemoryWord, String> binaryColumn;
    @FXML private TableColumn<MemoryWord, String> decimalColumn;
    @FXML private TableColumn<MemoryWord, String> hexColumn;
    @FXML private Label instructionCacheHits;
    @FXML private Label instructionCacheMisses;
    @FXML private Label instructionCacheHitRate;
    @FXML private Label dataCacheHits;
    @FXML private Label dataCacheMisses;
    @FXML private Label dataCacheHitRate;

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
        memoryTable.setItems(this.memoryModel.getMemoryList());

        this.memoryModel.memoryChangedProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                memoryTable.refresh();
                updateCacheStats();
            });
        });
        
        updateCacheStats();
        
        Cache.CacheStats instStats = memoryModel.getInstructionCache().getStats();
        Cache.CacheStats dataStats = memoryModel.getDataCache().getStats();
        
        instStats.hitsProperty().addListener((obs, oldVal, newVal) -> updateCacheStats());
        instStats.missesProperty().addListener((obs, oldVal, newVal) -> updateCacheStats());
        dataStats.hitsProperty().addListener((obs, oldVal, newVal) -> updateCacheStats());
        dataStats.missesProperty().addListener((obs, oldVal, newVal) -> updateCacheStats());
    }
    
    private void updateCacheStats() {
        if (memoryModel == null) return;
        
        Cache.CacheStats instStats = memoryModel.getInstructionCache().getStats();
        Cache.CacheStats dataStats = memoryModel.getDataCache().getStats();
        
        if (instructionCacheHits != null) {
            instructionCacheHits.setText("Hits: " + instStats.getHits());
        }
        if (instructionCacheMisses != null) {
            instructionCacheMisses.setText("Misses: " + instStats.getMisses());
        }
        if (instructionCacheHitRate != null) {
            instructionCacheHitRate.setText("Hit Rate: " + instStats.getHitRate() + "%");
        }
        if (dataCacheHits != null) {
            dataCacheHits.setText("Hits: " + dataStats.getHits());
        }
        if (dataCacheMisses != null) {
            dataCacheMisses.setText("Misses: " + dataStats.getMisses());
        }
        if (dataCacheHitRate != null) {
            dataCacheHitRate.setText("Hit Rate: " + dataStats.getHitRate() + "%");
        }
    }
}