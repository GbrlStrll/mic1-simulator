package mic1.model;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
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

    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    private final BooleanProperty isStepMode = new SimpleBooleanProperty(false);
    private final StringProperty subcycleSpeed = new SimpleStringProperty("100");
    private final StringProperty pcPauseValue = new SimpleStringProperty("");
    
    // --- Controle de Thread ---
    private Task<Void> simulationTask;
    private final Object simulationLock = new Object();
    /**
     * Construtor dos Controles de Simulação.
     */
    public SimulationControls() {
        System.out.println("Modelo de Controles criado.");

        startSimulationEngine();
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
    
    public void play() {
        if (isRunning.get()) {
            return; 
        } 
        isRunning.set(true);
        synchronized (simulationLock) {
            simulationLock.notify();
        }
    }
    
    public void pause() {
        if (!isRunning.get()) {
            return; 
        }
        System.out.println("Model: Comando PAUSE");
        isRunning.set(false);
    }
    
    public void reset() {
        System.out.println("Model: Comando RESET");
        pause(); // Garante que a simulação pare
        
        // Chama os métodos de reset dos outros modelos (se eles existirem)
        if (cpu != null) {
            // cpu.resetRegisters(); // Exemplo
        }
        if (memory != null) {
            // memory.clearMemory(); // Exemplo
        }
        System.out.println("Model: Simulação resetada.");
    }

    public void stepCycle() {
        System.out.println("Model: Comando STEP");
        if (isRunning.get()) {
            pause(); // Se estava rodando, pausa
        }

        if (cpu != null) {
            // TODO: Executar UM ciclo da CPU
            // Ex: cpu.executeCycle();
            System.out.println("Model: Um ciclo executado.");
        }
    }

    private void startSimulationEngine() {
        simulationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // O loop principal do simulador
                    while (!isCancelled()) {
                        
                        // --- LÓGICA DE PAUSA (O "SONECA") ---
                        synchronized (simulationLock) {
                            while (!isRunning.get()) {
                                System.out.println("Thread Sim: Pausada, esperando 'notify'...");
                                simulationLock.wait(); // Dorme até que play() chame notify()
                                System.out.println("Thread Sim: Acordou!");
                            }
                        }
                        // Se saiu do 'while', é porque isRunning é true.

                        // --- LÓGICA DE EXECUÇÃO ---
                        
                        // 1. Executa um ciclo da CPU
                        // (Operações que mexem na UI devem ser feitas com Platform.runLater)
                        if (cpu != null) {
                            Platform.runLater(() -> {
                                // TODO: Chamar o método real da sua CPU
                                // cpu.executeCycle(); 
                            });
                            System.out.println("Thread Sim: Executando um ciclo...");
                        }


                        // 2. Lida com o "Step Mode"
                        if (isStepMode.get()) {
                            System.out.println("Thread Sim: Modo Passo-a-Passo, pausando.");
                            Platform.runLater(() -> pause()); // Pausa na thread da UI
                            continue; // Volta ao início do loop (e vai para a lógica de pausa)
                        }
                        
                        // 3. Lida com a velocidade (Pause between subcycles)
                        long speed = Long.parseLong(subcycleSpeed.get()); // Pega o valor do TextField
                        if (speed > 0) {
                            Thread.sleep(speed); // Dorme pela quantidade de ms
                        }

                        // 4. Lida com "Pause on PC"
                        String pcTarget = pcPauseValue.get();
                        if (!pcTarget.isEmpty() /* && cpu.getPC().equals(pcTarget) */) {
                            // TODO: Comparar o PC atual com o pcTarget
                            // Se for igual, pausar:
                            // Platform.runLater(() -> pause());
                        }
                    }
                } catch (InterruptedException e) {
                    if (isCancelled()) {
                        System.out.println("Thread Sim: Simulação interrompida.");
                    }
                } catch (Exception e) {
                    System.err.println("Erro crítico na thread de simulação:");
                    e.printStackTrace();
                }
                return null;
            }
        };

        // Inicia a thread de simulação
        Thread simThread = new Thread(simulationTask);
        simThread.setDaemon(true); // Garante que a thread morra quando o app fechar
        simThread.start();
    }
    
    // --- Getters de Propriedades (Para o Controller fazer Data Binding) ---
    // O Controller usará estes métodos dentro do setModel()

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }

    public BooleanProperty isStepModeProperty() {
        return isStepMode;
    }

    public StringProperty subcycleSpeedProperty() {
        return subcycleSpeed;
    }

    public StringProperty pcPauseValueProperty() {
        return pcPauseValue;
    }
}
