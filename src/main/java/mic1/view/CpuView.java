package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.CpuController;
import mic1.model.CPU;
import java.io.IOException;

public class CpuView {

    private CPU cpuModel;

    public CpuView(CPU cpuModel) {
        this.cpuModel = cpuModel;
    }

    public void show(Image icone, double x, double y) {
        // padrao view: carrega fxml, obtem controller e injeta modelo
        // esta view cria janela separada para exibir estado da cpu
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/CPU.fxml"));
            
            Parent root = loader.load();

            CpuController controller = loader.getController();

            // injecao de dependencia conecta controller com modelo compartilhado
            // todas as janelas que mostram cpu usam a mesma instancia
            controller.setModel(this.cpuModel);
            
            Stage novoStage = new Stage();
            novoStage.setTitle("CPU - Simulador MIC-1");
            novoStage.setScene(new Scene(root));
            novoStage.setResizable(true);
            novoStage.setX(x);
            novoStage.setY(y);
            
            if (icone != null) {
                novoStage.getIcons().add(icone);
            }
            
            novoStage.show();

        } catch (IOException e) {
            System.err.println("Erro crítico ao carregar CPU.fxml:");
            e.printStackTrace();
        }
    }
}