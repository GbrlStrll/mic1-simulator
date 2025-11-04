package mic1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
<<<<<<< Updated upstream
import javafx.stage.Stage;
import java.io.IOException;
=======
>>>>>>> Stashed changes
import javafx.scene.image.Image;
import javafx.stage.Stage;
import mic1.controller.*;
import mic1.model.*;

<<<<<<< Updated upstream



//import mic1.model.*; // Importa todos os modelos
//import mic1.view.*;  // Importa todas as views

/**
 * O "Lançador" (Aplicação Principal)
 *
 * O QUE FAZER AQUI:
 *
 * 1. No método 'init()':
 * a. Carregar recursos globais (como o 'iconeAplicativo').
 * b. CRIAR AS INSTÂNCIAS (new) de todos os seus Modelos (ex: cpuModel = new CPU();).
 * c. Se os modelos precisam conversar (ex: CPU precisa da Memória),
 * passar a referência para eles aqui (ex: cpuModel.linkMemory(memoryModel);).
 *
 * 2. No método 'start()':
 * a. CRIAR AS INSTÂNCIAS (new) de todas as suas Views
 * (ex: CpuView cpuView = new CpuView(cpuModel);).
 * b. Chamar o método '.show()' de cada View, passando o ícone e
 * as coordenadas de posicionamento.
 * c. (O método antigo 'abrirJanela' não é mais necessário,
 * pois cada View agora sabe como se abrir).
 */




/**
 * Classe principal do simulador MIC-1.
 * Responsável por inicializar a interface gráfica e gerenciar as janelas do aplicativo.
 */
public class Main extends Application {

    /** Ícone usado em todas as janelas do aplicativo */
    private Image iconeAplicativo;

    /**
     * Inicialização dos recursos do aplicativo.
     * Executado antes da interface gráfica ser criada.
     * Carrega recursos estáticos como o ícone do aplicativo.
     */
=======
import java.io.IOException;

public class Main extends Application {
    private Image iconeAplicativo;

    private CPU cpu;
    private MainMemory memory;
    private SourceCode sourceCode;
    private SimulationControls controls;

>>>>>>> Stashed changes
    @Override
    public void init() {
        try {
            String caminhoIcone = "/mic1/icons/AppIcon.png";
            iconeAplicativo = new Image(getClass().getResourceAsStream(caminhoIcone));
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone: " + e.getMessage());
            // Aplicativo continuará sem ícone em caso de falha
        }
<<<<<<< Updated upstream
    }


    /**
     * Ponto de entrada principal da interface gráfica.
     * Inicializa e exibe todas as janelas do simulador.
     * 
     * @param stage Janela principal fornecida pelo JavaFX
     */
    @Override
    public void start(Stage stage) { 
        // Configuração do ícone na janela principal
        if (iconeAplicativo != null) {
            stage.getIcons().add(iconeAplicativo);
        }

        // Inicialização da janela do código fonte
        try {
            // Carrega o layout FXML
            Parent root = FXMLLoader.load(getClass().getResource("/mic1/SourceCode.fxml"));
            Scene scene = new Scene(root);
            
            // Configura a janela
            stage.setTitle("Source Code - MIC-1 Simulator");
            stage.setScene(scene);
            stage.setResizable(true);
            
            // Posiciona no canto superior direito
            stage.setX(1060); 
            stage.setY(0); 
            
            // Exibe a janela
            stage.show();

        } catch (IOException e) {
            System.err.println("Erro ao carregar SourceCode.fxml:");
            e.printStackTrace();
        }

        // Inicialização das janelas secundárias
        try {
            // Janela da CPU (posicionada à esquerda, embaixo)
            abrirJanela("/mic1/CPU.fxml", "CPU - MIC-1 Simulator", 250, 490);
        } catch (IOException e) {
            System.err.println("Erro ao carregar CPU.fxml:");
            e.printStackTrace();
        }
        
        try {
            // Janela da memória principal (posicionada à esquerda, em cima)
            abrirJanela("/mic1/MainMemory.fxml", "Main Memory - MIC-1 Simulator", 250, 0);
        } catch (IOException e) {
            System.err.println("Erro ao carregar MainMemory.fxml:");
            e.printStackTrace();
        }

        try {
            // Janela de controles (posicionada à direita, embaixo)
            abrirJanela("/mic1/SimulationControls.fxml", "Controls - MIC-1 Simulator", 1060, 540);
=======

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
            controller.setMemory(memory);

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
>>>>>>> Stashed changes
        } catch (IOException e) {
            System.err.println("Erro ao carregar SimulationControls.fxml:");
            e.printStackTrace();
        }
    }

<<<<<<< Updated upstream
    /**
     * Cria e configura uma nova janela do simulador.
     * 
     * @param fxmlFile Caminho do arquivo FXML com o layout da janela
     * @param title Título a ser exibido na barra de título da janela
     * @param x Posição horizontal da janela na tela
     * @param y Posição vertical da janela na tela
     * @throws IOException Se houver erro ao carregar o arquivo FXML
     */
    private void abrirJanela(String fxmlFile, String title, double x, double y) throws IOException {
        // Carregamento do layout
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        
        // Criação da nova janela
        Stage novoStage = new Stage();
=======
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
>>>>>>> Stashed changes

        // Configuração do ícone
        if (iconeAplicativo != null) {
            novoStage.getIcons().add(iconeAplicativo);
        }
        
        // Configuração da janela
        novoStage.setTitle(title);
        novoStage.setScene(new Scene(root));
        novoStage.setResizable(true);

        // Posicionamento na tela
        novoStage.setX(x);
        novoStage.setY(y);
        
        // Exibição da janela
        novoStage.show();
    }

    /**
     * Método principal que inicia o aplicativo.
     * 
     * @param args Argumentos da linha de comando (não utilizados)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
