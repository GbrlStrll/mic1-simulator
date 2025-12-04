package mic1.controller;
    
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import mic1.model.CPU;
import mic1.model.MainMemory;
import mic1.model.SimulationControls;

import java.net.URL;
import java.util.ResourceBundle;

public class SimulationControlsController implements Initializable {
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;
    @FXML private Button resetButton;
    @FXML private TextField pauseField;
    @FXML private TextField pauseOnPCField;
    @FXML private CheckBox stepModeCheckbox;
    @FXML private Button stepButton;

    private SimulationControls controlsModel;
    private CPU cpu;
    private MainMemory memory;
    private SimulationTimer simulationTimer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // valores iniciais refletem configuracao padrao
        pauseField.setText("100");
        pauseOnPCField.setText("-1");
    }

    public void setModel(SimulationControls model) {
        this.controlsModel = model;
        // checkbox segue estado do modo passo a passo
        stepModeCheckbox.selectedProperty().bindBidirectional(model.stepByStepModeProperty());
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
        this.simulationTimer = new SimulationTimer();
    }

    public void setMemory(MainMemory memory) {
        this.memory = memory;
    }

    @FXML
    private void handlePlay() {
        // inicia simulacao: cpu comeca a executar ciclos
        // se modo passo a passo estiver desativado, timer executa ciclos automaticamente
        if (cpu == null) return;

        cpu.start();
        controlsModel.setRunning(true);

        if (!controlsModel.isStepByStepMode()) {
            simulationTimer.start();
        }
    }

    @FXML
    private void handlePause() {
        if (cpu == null) return;

        // pausa mantem estado para retomada rapida
        cpu.pause();
        controlsModel.setPaused(true);
        simulationTimer.stop();
    }

    @FXML
    private void handleStop() {
        if (cpu == null) return;

        // parada encerra loop e garante timer parado
        cpu.stop();
        controlsModel.setRunning(false);
        simulationTimer.stop();
    }

    @FXML
    private void handleReset() {
        if (cpu == null) return;

        // reset sincroniza cpu, memoria e ui antes de novo teste
        cpu.reset();
        if (memory != null) {
            memory.reset();
        }
        controlsModel.reset();
        simulationTimer.stop();
    }

    @FXML
    private void handleApplyPause() {
        try {
            int value = Integer.parseInt(pauseField.getText());
            controlsModel.setPauseBetweenSubcycles(value);
        } catch (NumberFormatException e) {
            pauseField.setText(String.valueOf(controlsModel.getPauseBetweenSubcycles()));
        }
    }

    @FXML
    private void handleApplyPausePC() {
        try {
            int value = Integer.parseInt(pauseOnPCField.getText());
            controlsModel.setPauseOnPC(value);
        } catch (NumberFormatException e) {
            pauseOnPCField.setText(String.valueOf(controlsModel.getPauseOnPC()));
        }
    }

    @FXML
    private void handleStep() {
        if (cpu == null) return;

        // executa um ciclo e avalia ponto de pausa configurado
        cpu.executeCycle();

        int pausePC = controlsModel.getPauseOnPC();
        if (pausePC >= 0 && cpu.getRegister("PC") != null) {
            int currentPC = cpu.getRegister("PC").getValue();
            if (currentPC == pausePC) {
                cpu.pause();
                controlsModel.setPaused(true);
            }
        }
    }

    private class SimulationTimer extends AnimationTimer {
        // timer executa ciclos da cpu em intervalos regulares
        // permite simulacao continua sem intervencao manual do usuario
        private long lastUpdate = 0;

        @Override
        public void handle(long now) {
            // para timer se cpu nao estiver mais rodando
            if (!cpu.isRunning()) {
                stop();
                return;
            }

            // calcula intervalo entre ciclos baseado na configuracao do usuario
            long pauseNanos = controlsModel.getPauseBetweenSubcycles() * 1_000_000L;

            if (now - lastUpdate >= pauseNanos) {
                // executa um ciclo e verifica se deve pausar em pc especifico
                cpu.executeCycle();
                lastUpdate = now;

                // verifica breakpoint configurado no program counter
                int pausePC = controlsModel.getPauseOnPC();
                if (pausePC >= 0 && cpu.getRegister("PC") != null) {
                    int currentPC = cpu.getRegister("PC").getValue();
                    if (currentPC == pausePC) {
                        cpu.pause();
                        controlsModel.setPaused(true);
                        stop();
                    }
                }
            }
        }
    }
}
