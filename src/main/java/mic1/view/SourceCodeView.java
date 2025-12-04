package mic1.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.SourceCodeController;
import mic1.model.SourceCode;
import java.io.IOException;

public class SourceCodeView {

    private final SourceCode sourceModel;

    public SourceCodeView(SourceCode sourceModel) {
        this.sourceModel = sourceModel;
    }

    public void show(Stage stage, Image icone, double x, double y) {
        // janela principal: editor de codigo fonte e area de compilacao
        // usuario escreve microcodigo em assembly e ve resultado da montagem
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/SourceCode.fxml"));
            Parent root = loader.load();

            SourceCodeController controller = loader.getController();
            // binding bidirecional mantem texto do editor sincronizado com modelo
            controller.setModel(this.sourceModel);

            stage.setTitle("Codigo Fonte - Simulador MIC-1");
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