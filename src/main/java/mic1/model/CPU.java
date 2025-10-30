package mic1.model;

// Importe os modelos que a CPU precisa conhecer
import mic1.model.MainMemory;

/**
 * O "Cérebro" (Backend) da CPU. (MODELO)
 *
 * Responsabilidades:
 * 1. Manter o estado dos registradores (PC, AC, SP, IR, etc.).
 * 2. Conter a lógica para executar o ciclo de busca/decodificação/execução.
 * 3. Expor propriedades (Properties) dos registradores para a View (CpuView).
 */
public class CPU {

    // --- Dependências ---
    private MainMemory memory;

    // TODO: Adicionar os registradores como JavaFX Properties
    // Ex: private final IntegerProperty pc = new SimpleIntegerProperty(0);
    // Ex: private final IntegerProperty ac = new SimpleIntegerProperty(0);
    // Ex: private final IntegerProperty sp = new SimpleIntegerProperty(4095);

    /**
     * Construtor da CPU.
     */
    public CPU() {
        // Inicialize os registradores aqui
        System.out.println("Modelo da CPU criado.");
    }

    /**
     * (MÉTODO ADICIONADO)
     * Permite que o Main.java injete a Memória Principal na CPU.
     * Isso corrige o erro em Main.java.
     */
    public void linkMemory(MainMemory memory) {
        this.memory = memory;
    }

    // TODO: Implementar a lógica principal da CPU
    // public void executeCycle() {
    //     if (memory == null) return;
    //     // 1. Fetch (memory.read(pc.get()))
    //     // 2. Decode
    //     // 3. Execute
    // }

    // TODO: Adicionar os getters para as Properties (para o Controller fazer bind)
    // public IntegerProperty pcProperty() { return pc; }
    // public IntegerProperty acProperty() { return ac; }
}