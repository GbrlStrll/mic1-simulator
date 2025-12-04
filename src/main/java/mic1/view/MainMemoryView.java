package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.MainMemoryController;
import mic1.model.MainMemory;
import java.io.IOException;

public class MainMemoryView {

    private MainMemory memoryModel;

    public MainMemoryView(MainMemory memoryModel) {
        this.memoryModel = memoryModel;
    }

    public void show(Image icone, double x, double y) {
        // cria janela para visualizar conteudo da memoria principal
        // tabela mostra todas as palavras de memoria em diferentes formatos
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/MainMemory.fxml"));
            
            Parent root = loader.load();

            MainMemoryController controller = loader.getController();

            // modelo compartilhado garante que mudancas feitas pela cpu aparecem aqui
            controller.setModel(this.memoryModel);
            
            Stage novoStage = new Stage();
            novoStage.setTitle("Memoria Principal - Simulador MIC-1");
            novoStage.setScene(new Scene(root));
            novoStage.setResizable(true);
            novoStage.setX(x);
            novoStage.setY(y);
            
            if (icone != null) {
                novoStage.getIcons().add(icone);
            }
            
            novoStage.show();

        } catch (IOException e) {
            System.err.println("Erro crítico ao carregar MainMemory.fxml:");
            e.printStackTrace();
        }
    }
}