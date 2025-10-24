package mic1.view;

//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.image.Image;
//import javafx.stage.Stage;
//import mic1.controller.CpuController; // Importa o Controller
//import mic1.model.CPU; // Importa o Model
//import java.io.IOException;

/**
 * O "Construtor da Janela" (View - Lógica de Exibição)
 *
 * O QUE FAZER AQUI:
 * 1. Criar um construtor que recebe o 'Modelo' correspondente.
 * Ex: public CpuView(CPU cpuModel) { this.cpuModel = cpuModel; }
 *
 * 2. Criar um método 'show(Image icone, double x, double y)'.
 *
 * 3. Dentro do 'show', fazer a "Mágica do MVC":
 * a. Criar um 'FXMLLoader' apontando para o ".fxml" (ex: "/mic1/CPU.fxml").
 * b. Chamar 'loader.load()' para carregar o FXML (isso também cria o Controller).
 * c. Pegar a instância do Controller: 'CpuController c = loader.getController()'.
 * d. INJETAR o Modelo no Controlador: 'c.setModel(this.cpuModel)'.
 * e. Criar o 'new Stage()', 'new Scene()', definir título, ícone, posição
 * e chamar 'stage.show()'.
 */

public class CpuView {
    // Exemplo:
    // private CPU cpuModel;
    //
    // public CpuView(CPU cpuModel) {
    //     this.cpuModel = cpuModel;
    // }
    //
    // public void show(Image icone, double x, double y) {
    //     try {
    //         FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/CPU.fxml"));
    //         Parent root = loader.load();
    //         CpuController controller = loader.getController();
    //         controller.setModel(this.cpuModel);
    //
    //         Stage stage = new Stage();
    //         stage.setTitle("CPU - MIC-1 Simulator");
    //         // ... (configurar stage) ...
    //         stage.show();
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}
