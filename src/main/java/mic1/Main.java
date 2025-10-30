package mic1;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.image.Image;

// --- Importa os Modelos ---
import mic1.model.SourceCode;
import mic1.model.MainMemory; 
import mic1.model.CPU; 
import mic1.model.SimulationControls;

// --- Importa as Views específicas (MVC) ---
import mic1.view.SourceCodeView; 
import mic1.view.MainMemoryView; 
import mic1.view.CpuView;
import mic1.view.SimulationControlsView;

/**
 * O "Lançador" (Aplicação Principal)
 *
 * Agora 100% migrado para a arquitetura MVC (Model-View-Controller).
 * * Responsabilidades:
 * 1. (init) Criar os Modelos (os "cérebros").
 * 2. (init) Injetar dependências entre os Modelos.
 * 3. (start) Criar as Views (as "janelas").
 * 4. (start) Injetar o Modelo correspondente em cada View.
 */
public class Main extends Application {

    private Image iconeAplicativo;

    // --- Instâncias dos Modelos ---
    // Criados no init() e passados para as Views/Controllers necessários
    private SourceCode sourceModel;
    private MainMemory memoryModel; 
    private CPU cpuModel; 
    private SimulationControls controlsModel;

    /**
     * Etapa 1: Criação e conexão dos Modelos (Back-end)
     */
    @Override
    public void init() {
        // 1. Carrega o ícone
        try {
            String caminhoIcone = "/mic1/icons/AppIcon.png";
            iconeAplicativo = new Image(getClass().getResourceAsStream(caminhoIcone));
            if (iconeAplicativo == null || iconeAplicativo.isError()) {
                throw new IOException("Ícone não encontrado ou corrompido.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone: " + e.getMessage());
        }

        // --- 2. Cria as instâncias de TODOS os Modelos ---
        sourceModel = new SourceCode();
        memoryModel = new MainMemory(); 
        cpuModel = new CPU();
        controlsModel = new SimulationControls();
        
        // --- 3. Conecta os Modelos (Injeção de Dependência) ---
        
        // O SourceCode precisa da Memória para gravar o programa
        sourceModel.linkMainMemory(memoryModel); 
        
        // A CPU precisa da Memória para ler/escrever durante a execução
        cpuModel.linkMemory(memoryModel); 
        
        // Os Controles orquestram tudo:
        // (Assumindo que você criará estes métodos no SimulationControls.java)
        controlsModel.linkCpu(cpuModel);         // Para Play, Pause, Step
        controlsModel.linkMemory(memoryModel);  // Para Resetar a memória
        controlsModel.linkSourceCode(sourceModel); // Para Resetar o código (se necessário)
    }

    /**
     * Etapa 2: Criação e exibição das Views (Front-end)
     */
    @Override
    public void start(Stage stage) { 
        
        // --- Cria e exibe a View do SourceCode ---
        // (Usa o 'stage' principal)
        SourceCodeView sourceView = new SourceCodeView(sourceModel);
        sourceView.show(stage, iconeAplicativo, 1060, 0); 

        // --- Cria e exibe a View da MainMemory ---
        MainMemoryView memoryView = new MainMemoryView(memoryModel);
        memoryView.show(iconeAplicativo, 250, 0);

        // --- Cria e exibe a View da CPU ---
        CpuView cpuView = new CpuView(cpuModel);
        cpuView.show(iconeAplicativo, 250, 490); // (Posição X/Y original)

        // --- Cria e exibe a View dos Controles ---
        SimulationControlsView controlsView = new SimulationControlsView(controlsModel);
        controlsView.show(iconeAplicativo, 1060, 540); // (Posição X/Y original)
    }

    /**
     * (O método auxiliar 'abrirJanela' não é mais necessário e foi removido)
     */

    public static void main(String[] args) {
        launch(args);
    }
}