package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.SimulationControlsController; // Importa o Controller
import mic1.model.SimulationControls;                // Importa o Model
import java.io.IOException;

/**
 * O "Construtor da Janela" (View - Lógica de Exibição) para os Controles de Simulação.
 *
 * Responsabilidades:
 * 1. Receber o Modelo (SimulationControls) em seu construtor.
 * 2. Carregar o FXML (SimulationControls.fxml).
 * 3. Pegar o Controller (SimulationControlsController) do FXML.
 * 4. "Injetar" o Modelo no Controller (o passo mais importante do MVC).
 * 5. Configurar e exibir um NOVO Stage (a janela) para os Controles.
 */
public class SimulationControlsView {

    // --- Referência ao Modelo ---
    private SimulationControls controlsModel;

    /**
     * O construtor recebe o "cérebro" (o Modelo) do Main.java.
     */
    public SimulationControlsView(SimulationControls controlsModel) {
        this.controlsModel = controlsModel;
    }

    /**
     * Este método faz o que o 'abrirJanela()' do seu Main.java costumava fazer
     * para esta janela específica. Ele cria e exibe uma NOVA janela.
     *
     * @param icone O ícone do aplicativo carregado pelo Main.java.
     * @param x A posição X da janela.
     * @param y A posição Y da janela.
     */
    public void show(Image icone, double x, double y) {
        try {
            // 1. Cria o FXMLLoader, apontando para o FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/SimulationControls.fxml"));
            
            // 2. Carrega o FXML.
            // (Neste momento, o FXML lê 'fx:controller' e cria uma instância do SimulationControlsController)
            Parent root = loader.load();

            // 3. Pega a instância do Controller que o FXML acabou de criar
            SimulationControlsController controller = loader.getController();

            // 4. A "MÁGICA" DO MVC: Injeta o Modelo no Controlador
            // (Agora o Controller tem acesso ao Modelo e pode fazer o data binding e lidar com eventos)
            controller.setModel(this.controlsModel);
            
            // 5. Configura um NOVO Stage (esta janela é separada da principal)
            Stage novoStage = new Stage();
            novoStage.setTitle("Controls - MIC-1 Simulator");
            novoStage.setScene(new Scene(root));
            novoStage.setResizable(true);
            novoStage.setX(x);
            novoStage.setY(y);
            
            if (icone != null) {
                novoStage.getIcons().add(icone);
            }
            
            // 6. Exibe a janela
            novoStage.show();

        } catch (IOException e) {
            System.err.println("Erro crítico ao carregar SimulationControls.fxml:");
            e.printStackTrace();
        }
    }
}