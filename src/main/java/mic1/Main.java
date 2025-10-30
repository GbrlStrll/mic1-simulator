package mic1;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.image.Image;

// --- Importações FXML (para o método 'abrirJanela' antigo) ---
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

// --- Importa os Modelos ---
import mic1.model.SourceCode;
import mic1.model.MainMemory; 
// (CPU e SimulationControls ainda não migrados)
// import mic1.model.CPU; 
// import mic1.model.SimulationControls;

// --- Importa as Views específicas (MVC) ---
import mic1.view.SourceCodeView; 
import mic1.view.MainMemoryView; // <<< MUDANÇA: Importa a nova MainMemoryView

/**
 * O "Lançador" (Aplicação Principal)
 *
 * Agora usando uma abordagem HÍBRIDA:
 * - Janelas SourceCode e MainMemory: Usam a arquitetura MVC (Model-View-Controller).
 * - Outras Janelas: Usam o carregamento direto de FXML via 'abrirJanela'.
 */
public class Main extends Application {

    private Image iconeAplicativo;

    // --- Instâncias dos Modelos ---
    // Criados no init() e passados para as Views/Controllers necessários
    private SourceCode sourceModel;
    private MainMemory memoryModel; 
    // private CPU cpuModel; // Você criará estes quando migrar as outras janelas
    // private SimulationControls controlsModel;

    @Override
    public void init() {
        // 1. Carrega o ícone (como antes)
        try {
            String caminhoIcone = "/mic1/icons/AppIcon.png";
            iconeAplicativo = new Image(getClass().getResourceAsStream(caminhoIcone));
            if (iconeAplicativo == null || iconeAplicativo.isError()) {
                throw new IOException("Ícone não encontrado ou corrompido.");
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar o ícone: " + e.getMessage());
        }

        // --- Cria as instâncias dos Modelos ---
        sourceModel = new SourceCode();
        
        // MainMemory NÃO é mais Singleton, é criado aqui
        memoryModel = new MainMemory(); 
        
        // --- Conecta os Modelos ---
        sourceModel.linkMainMemory(memoryModel); 
        
        // (Quando migrar a CPU, você faria algo como:)
        // cpuModel = new CPU();
        // cpuModel.linkMemory(memoryModel); 
    }

    @Override
    public void start(Stage stage) { 
        
        // --- NOVO: Usa SourceCodeView para a janela principal ---
        SourceCodeView sourceView = new SourceCodeView(sourceModel);
        // Passa o 'stage' principal para ser reutilizado
        sourceView.show(stage, iconeAplicativo, 1060, 0); 

        // --- <<< MUDANÇA: Usa MainMemoryView para a janela de Memória >>> ---
        // A MainMemoryView irá carregar o FXML e injetar o 'memoryModel'
        // no MainMemoryController.
        MainMemoryView memoryView = new MainMemoryView(memoryModel);
        memoryView.show(iconeAplicativo, 250, 0); // (Posição X/Y original)


        // --- MANTIDO: Carrega as outras 2 janelas (CPU, Controls) do jeito antigo ---
        try {
            // (CPU.fxml ainda será carregado da forma antiga)
            abrirJanela("/mic1/CPU.fxml", "CPU - MIC-1 Simulator", 250, 490);
        } catch (IOException e) {
            System.err.println("Erro ao carregar CPU.fxml:");
            e.printStackTrace();
        }
        
        // <<< MUDANÇA: A abertura do MainMemory.fxml foi REMOVIDA daqui >>>
        // try {
        //     abrirJanela("/mic1/MainMemory.fxml", ...); // <<< REMOVIDO
        // } catch ...

        try {
            // (SimulationControls.fxml ainda será carregado da forma antiga)
            abrirJanela("/mic1/SimulationControls.fxml", "Controls - MIC-1 Simulator", 1060, 540);
        } catch (IOException e) {
            System.err.println("Erro ao carregar SimulationControls.fxml:");
            e.printStackTrace();
        }
    }

    /**
     * Método auxiliar MANTIDO para as janelas NÃO MIGRADA AINDA (CPU, Controls).
     * Cria e exibe uma NOVA janela (Stage) diretamente do FXML.
     *
     * IMPORTANTE: Este método NÃO injeta modelos. Os controladores carregados
     * por este método (CPUController, SimulationControlsController) devem
     * usar Singletons ou precisarão de modificação manual.
     */
    private void abrirJanela(String fxmlFile, String title, double x, double y) throws IOException {
        
        // Usa FXMLLoader diretamente aqui
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();
        
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
        
        // NOTA: Como não estamos injetando modelos aqui, os controladores
        // de CPU e SimulationControls (se precisarem) teriam que usar
        // um padrão Singleton para acessar o 'memoryModel' ou 'cpuModel'.
    }

    public static void main(String[] args) {
        launch(args);
    }
}