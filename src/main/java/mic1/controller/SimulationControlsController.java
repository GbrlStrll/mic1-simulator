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
        pauseField.setText("100");
        pauseOnPCField.setText("-1");
    }

    public void setModel(SimulationControls model) {
        this.controlsModel = model;
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

        cpu.pause();
        controlsModel.setPaused(true);
        simulationTimer.stop();
    }

    @FXML
    private void handleStop() {
        if (cpu == null) return;

        cpu.stop();
        controlsModel.setRunning(false);
        simulationTimer.stop();
    }

    @FXML
    private void handleReset() {
        if (cpu == null) return;

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
        private long lastUpdate = 0;

        @Override
        public void handle(long now) {
            if (!cpu.isRunning()) {
                stop();
                return;
            }

            long pauseNanos = controlsModel.getPauseBetweenSubcycles() * 1_000_000L;

            if (now - lastUpdate >= pauseNanos) {
                cpu.executeCycle();
                lastUpdate = now;

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
