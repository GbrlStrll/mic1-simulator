package mic1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.*;
import mic1.model.*;

import java.io.IOException;

public class Main extends Application {
    private Image iconeAplicativo;

    private CPU cpu;
    private MainMemory memory;
    private SourceCode sourceCode;
    private SimulationControls controls;

    @Override
    public void init() {
        try {
            String caminhoIcone = "/mic1/icons/AppIcon.png";
            iconeAplicativo = new Image(getClass().getResourceAsStream(caminhoIcone));
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone: " + e.getMessage());
            // Aplicativo continuará sem ícone em caso de falha
        }

        cpu = new CPU();
        memory = new MainMemory();
        sourceCode = new SourceCode();
        controls = new SimulationControls();

        cpu.setMemory(memory);
    }

    @Override
    public void start(Stage stage) {
        if (iconeAplicativo != null) {
            stage.getIcons().add(iconeAplicativo);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/SourceCode.fxml"));
            Parent root = loader.load();
            SourceCodeController controller = loader.getController();
            controller.setModel(sourceCode);
            controller.setCPU(cpu);

            Scene scene = new Scene(root);
            stage.setTitle("Source Code - MIC-1 Simulator");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setX(1060);
            stage.setY(0);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar SourceCode.fxml:");
            e.printStackTrace();
        }

        try {
            abrirJanelaCPU("/mic1/CPU.fxml", "CPU - MIC-1 Simulator", 250, 490);
        } catch (IOException e) {
            System.err.println("Erro ao carregar CPU.fxml:");
            e.printStackTrace();
        }

        try {
            abrirJanelaMemoria("/mic1/MainMemory.fxml", "Main Memory - MIC-1 Simulator", 250, 0);
        } catch (IOException e) {
            System.err.println("Erro ao carregar MainMemory.fxml:");
            e.printStackTrace();
        }

        try {
            abrirJanelaControles("/mic1/SimulationControls.fxml", "Controls - MIC-1 Simulator", 1060, 540);
        } catch (IOException e) {
            System.err.println("Erro ao carregar SimulationControls.fxml:");
            e.printStackTrace();
        }
    }

    private void abrirJanelaCPU(String fxmlFile, String title, double x, double y) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        CpuController controller = loader.getController();
        controller.setModel(cpu);

        Stage novoStage = new Stage();
        if (iconeAplicativo != null) {
            novoStage.getIcons().add(iconeAplicativo);
        }

        novoStage.setTitle(title);
        novoStage.setScene(new Scene(root));
        novoStage.setResizable(true);
        novoStage.setX(x);
        novoStage.setY(y);
        novoStage.show();
    }

    private void abrirJanelaMemoria(String fxmlFile, String title, double x, double y) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        MainMemoryController controller = loader.getController();
        controller.setModel(memory);

        Stage novoStage = new Stage();
        if (iconeAplicativo != null) {
            novoStage.getIcons().add(iconeAplicativo);
        }

        novoStage.setTitle(title);
        novoStage.setScene(new Scene(root));
        novoStage.setResizable(true);
        novoStage.setX(x);
        novoStage.setY(y);
        novoStage.show();
    }

    private void abrirJanelaControles(String fxmlFile, String title, double x, double y) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        SimulationControlsController controller = loader.getController();
        controller.setModel(controls);
        controller.setCPU(cpu);
        controller.setMemory(memory);

        Stage novoStage = new Stage();
        if (iconeAplicativo != null) {
            novoStage.getIcons().add(iconeAplicativo);
        }

        novoStage.setTitle(title);
        novoStage.setScene(new Scene(root));
        novoStage.setResizable(true);
        novoStage.setX(x);
        novoStage.setY(y);
        novoStage.show();
    }

    /**
     * Método principal que inicia o aplicativo.
     * * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}