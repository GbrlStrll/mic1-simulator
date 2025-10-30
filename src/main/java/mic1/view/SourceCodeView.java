package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.SourceCodeController;
import mic1.model.SourceCode;
import java.io.IOException;

/**
 * Classe responsável por criar e exibir a janela "Source Code".
 *
 * Recebe o modelo {@link mic1.model.SourceCode} no construtor, carrega o
 * FXML correspondente, injeta o controller e exibe o Stage.
 */
public class SourceCodeView {

    private final SourceCode sourceModel;

    /**
     * Cria a view com o modelo fornecido.
     * @param sourceModel modelo de domínio associado à janela
     */
    public SourceCodeView(SourceCode sourceModel) {
        this.sourceModel = sourceModel;
    }

    /**
     * Configura e exibe o Stage da janela "Source Code".
     *
     * @param stage Stage a ser usado para exibir a janela
     * @param icone ícone da aplicação (pode ser nulo)
     * @param x posição X da janela
     * @param y posição Y da janela
     */
    public void show(Stage stage, Image icone, double x, double y) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/SourceCode.fxml"));
            Parent root = loader.load();

            SourceCodeController controller = loader.getController();
            controller.setModel(this.sourceModel);

            stage.setTitle("Source Code - MIC-1 Simulator");
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setX(x);
            stage.setY(y);

            if (icone != null) {
                stage.getIcons().add(icone);
            }

            stage.show();

        } catch (IOException e) {
            System.err.println("Erro ao carregar SourceCode.fxml:");
            e.printStackTrace();
        }
    }
}