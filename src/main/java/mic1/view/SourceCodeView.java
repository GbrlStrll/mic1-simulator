package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.SourceCodeController; // Importa o Controller
import mic1.model.SourceCode;               // Importa o Model
import java.io.IOException;

/**
 * O "Construtor da Janela" (View - Lógica de Exibição)
 *
 * Responsabilidades:
 * 1. Receber o Modelo (SourceCode) em seu construtor.
 * 2. Carregar o FXML (SourceCode.fxml).
 * 3. Pegar o Controller (SourceCodeController) do FXML.
 * 4. "Injetar" o Modelo no Controller (o passo mais importante do MVC).
 * 5. Configurar e exibir o Stage (a janela).
 */
public class SourceCodeView {

    // --- Referência ao Modelo ---
    private SourceCode sourceModel;

    /**
     * O construtor recebe o "cérebro" (o Modelo) do Main.java.
     */
    public SourceCodeView(SourceCode sourceModel) {
        this.sourceModel = sourceModel;
    }

    /**
     * Este método faz o que o 'start()' do seu Main.java costumava fazer
     * para esta janela específica.
     *
     * @param stage O 'Stage' principal fornecido pelo JavaFX (Main.java).
     * @param icone O ícone do aplicativo carregado pelo Main.java.
     * @param x A posição X da janela.
     * @param y A posição Y da janela.
     */
    public void show(Stage stage, Image icone, double x, double y) {
        try {
            // 1. Cria o FXMLLoader, apontando para o FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/SourceCode.fxml"));
            
            // 2. Carrega o FXML.
            // (Neste momento, o FXML lê 'fx:controller' e cria uma instância do SourceCodeController)
            Parent root = loader.load();

            // 3. Pega a instância do Controller que o FXML acabou de criar
            SourceCodeController controller = loader.getController();

            // 4. A "MÁGICA" DO MVC: Injeta o Modelo no Controlador
            // (Agora o Controller tem acesso ao Modelo e pode fazer o data binding)
            controller.setModel(this.sourceModel);
            
            // 5. Configura o Stage (o 'stage' principal que recebemos)
            stage.setTitle("Source Code - MIC-1 Simulator");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setX(x);
            stage.setY(y);
            
            if (icone != null) {
                stage.getIcons().add(icone);
            }
            
            // 6. Exibe a janela
            stage.show();

        } catch (IOException e) {
            System.err.println("Erro crítico ao carregar SourceCode.fxml:");
            e.printStackTrace();
        }
    }
}