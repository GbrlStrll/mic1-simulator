package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.MainMemoryController; // Importa o Controller
import mic1.model.MainMemory;                 // Importa o Model
import java.io.IOException;

/**
 * O "Construtor da Janela" (View - Lógica de Exibição) para a Memória Principal.
 *
 * Responsabilidades:
 * 1. Receber o Modelo (MainMemory) em seu construtor.
 * 2. Carregar o FXML (MainMemory.fxml).
 * 3. Pegar o Controller (MainMemoryController) do FXML.
 * 4. "Injetar" o Modelo no Controller (o passo mais importante do MVC).
 * 5. Configurar e exibir um NOVO Stage (a janela) para a memória.
 */
public class MainMemoryView {

    // --- Referência ao Modelo ---
    private MainMemory memoryModel;

    /**
     * O construtor recebe o "cérebro" (o Modelo) do Main.java.
     */
    public MainMemoryView(MainMemory memoryModel) {
        this.memoryModel = memoryModel;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/MainMemory.fxml"));
            
            // 2. Carrega o FXML.
            // (Neste momento, o FXML lê 'fx:controller' e cria uma instância do MainMemoryController)
            Parent root = loader.load();

            // 3. Pega a instância do Controller que o FXML acabou de criar
            MainMemoryController controller = loader.getController();

            // 4. A "MÁGICA" DO MVC: Injeta o Modelo no Controlador
            // (Agora o Controller tem acesso ao Modelo e pode fazer o data binding da tabela)
            controller.setModel(this.memoryModel);
            
            // 5. Configura um NOVO Stage (esta janela é separada da principal)
            Stage novoStage = new Stage();
            novoStage.setTitle("Main Memory - MIC-1 Simulator");
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
            System.err.println("Erro crítico ao carregar MainMemory.fxml:");
            e.printStackTrace();
        }
    }
}