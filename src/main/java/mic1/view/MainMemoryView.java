package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.MainMemoryController;
import mic1.model.MainMemory;
import java.io.IOException;

/**
 * Gerencia o carregamento da interface gráfica (FXML) para a janela da Memória Principal.
 *
 * Esta classe da View (View) implementa o padrão de Injeção de Dependência,
 * recebendo o modelo (MainMemory) em seu construtor e, em seguida,
 * injetando-o no controlador (MainMemoryController) durante o processo
 * de carregamento da janela.
 */
public class MainMemoryView {

    /** Referência à instância do modelo de dados (MainMemory). */
    private MainMemory memoryModel;

    /**
     * Constrói a View da Memória Principal.
     *
     * @param memoryModel A instância do modelo MainMemory (criada na classe Main) 
     * a ser associada a esta janela.
     */
    public MainMemoryView(MainMemory memoryModel) {
        this.memoryModel = memoryModel;
    }

    /**
     * Carrega o arquivo FXML da Memória Principal, injeta o modelo
     * no controlador e exibe a nova janela (Stage) na tela.
     *
     * @param icone O ícone da aplicação a ser usado na janela.
     * @param x A coordenada X inicial da posição da janela.
     * @param y A coordenada Y inicial da posição da janela.
     */
    public void show(Image icone, double x, double y) {
        try {
            // Carrega o FXML, que define a estrutura da UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/MainMemory.fxml"));
            
            // O .load() instancia a UI e o Controller associado (via fx:controller)
            Parent root = loader.load();

            // Obtém a instância do controlador que o FXMLLoader acabou de criar
            MainMemoryController controller = loader.getController();

            // Injeta o modelo de dados no controlador
            // (Conclui o ciclo de Injeção de Dependência)
            controller.setModel(this.memoryModel);
            
            // Configura e exibe a nova janela (Stage)
            Stage novoStage = new Stage();
            novoStage.setTitle("Main Memory - MIC-1 Simulator");
            novoStage.setScene(new Scene(root));
            novoStage.setResizable(true);
            novoStage.setX(x);
            novoStage.setY(y);
            
            if (icone != null) {
                novoStage.getIcons().add(icone);
            }
            
            novoStage.show();

        } catch (IOException e) {
            // Em caso de falha no carregamento do FXML
            System.err.println("Erro crítico ao carregar MainMemory.fxml:");
            e.printStackTrace();
        }
    }
}