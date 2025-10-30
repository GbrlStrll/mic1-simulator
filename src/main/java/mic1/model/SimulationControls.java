package mic1.model;

// Importe os modelos que os Controles precisam orquestrar
import mic1.model.CPU;
import mic1.model.MainMemory;
import mic1.model.SourceCode;

/**
 * O "Cérebro" (Backend) dos Controles de Simulação. (MODELO)
 *
 * Responsabilidades:
 * 1. Manter o estado da simulação (Ex: rodando, pausado, velocidade).
 * 2. Conter a lógica para 'Play', 'Pause', 'Reset', 'Step'.
 * 3. Orquestrar os outros modelos (chamar cpu.executeCycle(), memory.clear(), etc.).
 */
public class SimulationControls {

    // --- Dependências (os modelos que este orquestra) ---
    private CPU cpu;
    private MainMemory memory;
    private SourceCode sourceCode;

    // TODO: Adicionar propriedades do estado da simulação
    // Ex: private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    // Ex: private final StringProperty simulationSpeed = new SimpleStringProperty("1000");

    /**
     * Construtor dos Controles de Simulação.
     */
    public SimulationControls() {
        System.out.println("Modelo de Controles criado.");
    }

    // --- (MÉTODOS ADICIONADOS) ---
    // Permitem que o Main.java injete os outros modelos.
    // Isso corrige os erros em Main.java.

    public void linkCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public void linkMemory(MainMemory memory) {
        this.memory = memory;
    }

    public void linkSourceCode(SourceCode sourceCode) {
        this.sourceCode = sourceCode;
    }

    // --- Lógica de Negócio (a ser implementada) ---

    // TODO: Implementar a lógica que o 'SimulationControlsController' chamará
    
    // public void play() {
    //     System.out.println("Play (Lógica do Modelo)");
    //     // Iniciar o loop de simulação (ex: usando um Timeline ou Service do JavaFX)
    //     // Dentro do loop: cpu.executeCycle();
    // }
    
    // public void pause() {
    //     System.out.println("Pause (Lógica do Modelo)");
    //     // Parar o loop de simulação
    // }
    
    // public void reset() {
    //     System.out.println("Reset (Lógica do Modelo)");
    //     // pause();
    //     // memory.clearMemory();
    //     // cpu.resetRegisters();
    // }
}