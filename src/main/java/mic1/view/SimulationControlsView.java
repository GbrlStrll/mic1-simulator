package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.SimulationControlsController;
import mic1.model.SimulationControls;
import java.io.IOException;

public class SimulationControlsView {

    private SimulationControls controlsModel;

    public SimulationControlsView(SimulationControls controlsModel) {
        this.controlsModel = controlsModel;
    }

    public void show(Image icone, double x, double y) {
        // janela de controles permite usuario controlar execucao da simulacao
        // botoes play/pause/stop/reset e configuracoes de velocidade
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/SimulationControls.fxml"));
            
            Parent root = loader.load();

            SimulationControlsController controller = loader.getController();

            // modelo de controles gerencia estado da simulacao
            controller.setModel(this.controlsModel);
            
            Stage novoStage = new Stage();
            novoStage.setTitle("Controles - Simulador MIC-1");
            novoStage.setScene(new Scene(root));
            novoStage.setResizable(true);
            novoStage.setX(x);
            novoStage.setY(y);
            
            if (icone != null) {
                novoStage.getIcons().add(icone);
            }
            
            novoStage.show();

        } catch (IOException e) {
            System.err.println("Erro crítico ao carregar SimulationControls.fxml:");
            e.printStackTrace();
        }
    }
}