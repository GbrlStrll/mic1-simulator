package mic1.model;

//import javafx.beans.property.StringProperty;
//import javafx.collections.ObservableList;
// ... (outros imports de 'javafx.beans.property')

/**
 * O "Cérebro" (Backend)
 *
 * O QUE FAZER AQUI:
 * 1. Definir os dados da sua aplicação (ex: registradores, status da simulação, log).
 * 2. Usar Propriedades JavaFX (StringProperty, IntegerProperty, ObservableList)
 * para guardar esses dados. Isso permite que a View "assista" a mudanças.
 * 3. Escrever toda a LÓGICA DE NEGÓCIO (ex: runCycle(), decodeInstruction(), etc.).
 * 4. NÃO importar nada do 'javafx.scene' (como Button, Label, TableView).
 * Este arquivo não deve saber que uma interface gráfica existe.
 * 5. Fornecer getters públicos para as Propriedades (ex: cyclesProperty())
 * e Listas (ex: getRegisterList()).
 *
 * Exemplo:
 * private final IntegerProperty cycles = new SimpleIntegerProperty(0);
 * private final ObservableList<Register> registerList = FXCollections.observableArrayList();
 *
 * public void runCycle() {
 * // ... lógica ...
 * cycles.set(cycles.get() + 1);
 * }
 *
 * public IntegerProperty cyclesProperty() { return cycles; }
 * public ObservableList<Register> getRegisterList() { return registerList; }
 */

public class CPU {
    
}
