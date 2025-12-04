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
        // carrega icone do aplicativo para todas as janelas
        try {
            String caminhoIcone = "/mic1/icons/AppIcon.png";
            iconeAplicativo = new Image(getClass().getResourceAsStream(caminhoIcone));
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone: " + e.getMessage());
        }

        // inicializa modelos antes de criar qualquer interface
        // esses objetos sao compartilhados entre todas as janelas via injecao de dependencia
        cpu = new CPU();
        memory = new MainMemory();
        sourceCode = new SourceCode();
        controls = new SimulationControls();

        // conecta cpu com memoria para permitir operacoes de leitura e escrita
        cpu.setMemory(memory);
    }

    @Override
    public void start(Stage stage) {
        // aplica icone na janela principal
        if (iconeAplicativo != null) {
            stage.getIcons().add(iconeAplicativo);
        }

        // janela principal: editor de codigo fonte e area de compilacao
        // aqui o usuario escreve microcodigo e ve o resultado da montagem
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mic1/SourceCode.fxml"));
            Parent root = loader.load();
            SourceCodeController controller = loader.getController();
            controller.setModel(sourceCode);
            controller.setCPU(cpu);

            Scene scene = new Scene(root);
            stage.setTitle("Codigo Fonte - Simulador MIC-1");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setX(1060);
            stage.setY(0);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar SourceCode.fxml:");
            e.printStackTrace();
        }

        // abre janelas auxiliares posicionadas estrategicamente na tela
        // cada uma mostra uma parte diferente do estado da simulacao
        try {
            abrirJanelaCPU("/mic1/CPU.fxml", "CPU - Simulador MIC-1", 250, 490);
        } catch (IOException e) {
            System.err.println("Erro ao carregar CPU.fxml:");
            e.printStackTrace();
        }

        try {
            abrirJanelaMemoria("/mic1/MainMemory.fxml", "Memoria Principal - Simulador MIC-1", 250, 0);
        } catch (IOException e) {
            System.err.println("Erro ao carregar MainMemory.fxml:");
            e.printStackTrace();
        }

        try {
            abrirJanelaControles("/mic1/SimulationControls.fxml", "Controles - Simulador MIC-1", 1060, 540);
        } catch (IOException e) {
            System.err.println("Erro ao carregar SimulationControls.fxml:");
            e.printStackTrace();
        }
    }

    private void abrirJanelaCPU(String fxmlFile, String title, double x, double y) throws IOException {
        // padrao mvc: carrega fxml, obtem controller criado automaticamente e injeta modelo
        // todas as janelas compartilham a mesma instancia de cpu para sincronizacao automatica
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
        // mesma instancia de memoria e compartilhada entre cpu e esta janela
        // quando cpu escreve na memoria, a tabela atualiza automaticamente via observable
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
        // controller de controles precisa de acesso direto a cpu e memoria
        // para poder iniciar, pausar e resetar a simulacao
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

    public static void main(String[] args) {
        launch(args);
    }
}