package mic1.controller;

//import javafx.fxml.FXML;
//import javafx.scene.control.Label;
//import javafx.scene.control.TableView;
//import mic1.model.CPU; // Importa o Modelo

/**
 * A "Ponte" (Controlador)
 *
 * O QUE FAZER AQUI:
 * 1. Usar '@FXML' para criar uma variável Java para CADA componente do FXML
 * que você precisa acessar (ex: @FXML private TableView registerTable;).
 *
 * 2. O nome da variável DEVE ser igual ao 'fx:id' no FXML.
 *
 * 3. Criar um método 'setModel(CPU model)'. Este método será chamado
 * pela classe 'CpuView' para "injetar" o cérebro.
 *
 * 4. Dentro do 'setModel', fazer o "Data Binding": ligar os componentes @FXML
 * às propriedades do modelo.
 * Ex: 'cyclesLabel.textProperty().bind(model.cyclesProperty().asString("Cycles: %d"))'
 * Ex: 'registerTable.setItems(model.getRegisterList())'
 *
 * 5. Implementar 'Initializable' (opcional) para configurar coisas que NÃO
 * dependem do modelo (como o 'PropertyValueFactory' das colunas da tabela).
 *
 * 6. Criar métodos '@FXML' para os eventos (ex: botões) e, dentro deles,
 * chamar a lógica do modelo (ex: 'model.runCycle()').
 */

public class CpuController {
    // Exemplo de variáveis @FXML
    // @FXML private TableView registerTable;
    // @FXML private Label cyclesLabel;

    // Exemplo do 'setModel'
    // private CPU cpuModel;
    // public void setModel(CPU model) {
    //     this.cpuModel = model;
    //     cyclesLabel.textProperty().bind(cpuModel.cyclesProperty().asString("Cycles: %d"));
    // }
}
