package mic1.model;
    
import javafx.beans.property.*;

public class SimulationControls {
    private final IntegerProperty pauseBetweenSubcycles;
    private final IntegerProperty pauseOnPC;
    private final BooleanProperty stepByStepMode;
    private final BooleanProperty isRunning;
    private final BooleanProperty isPaused;

    public SimulationControls() {
        // modelo gerencia estado dos controles de simulacao
        // propriedades observaveis permitem ui reagir a mudancas automaticamente
        this.pauseBetweenSubcycles = new SimpleIntegerProperty(100);
        this.pauseOnPC = new SimpleIntegerProperty(-1);
        this.stepByStepMode = new SimpleBooleanProperty(false);
        this.isRunning = new SimpleBooleanProperty(false);
        this.isPaused = new SimpleBooleanProperty(false);
    }

    public int getPauseBetweenSubcycles() {
        return pauseBetweenSubcycles.get();
    }

    public void setPauseBetweenSubcycles(int value) {
        if (value >= 0) {
            pauseBetweenSubcycles.set(value);
        }
    }

    public IntegerProperty pauseBetweenSubcyclesProperty() {
        return pauseBetweenSubcycles;
    }

    public int getPauseOnPC() {
        return pauseOnPC.get();
    }

    public void setPauseOnPC(int value) {
        pauseOnPC.set(value);
    }

    public IntegerProperty pauseOnPCProperty() {
        return pauseOnPC;
    }

    public boolean isStepByStepMode() {
        return stepByStepMode.get();
    }

    public void setStepByStepMode(boolean value) {
        stepByStepMode.set(value);
    }

    public BooleanProperty stepByStepModeProperty() {
        return stepByStepMode;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void setRunning(boolean value) {
        isRunning.set(value);
    }

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }

    public boolean isPaused() {
        return isPaused.get();
    }

    public void setPaused(boolean value) {
        isPaused.set(value);
    }

    public BooleanProperty isPausedProperty() {
        return isPaused;
    }

    public void reset() {
        pauseBetweenSubcycles.set(100);
        pauseOnPC.set(-1);
        stepByStepMode.set(false);
        isRunning.set(false);
        isPaused.set(false);
    }
}