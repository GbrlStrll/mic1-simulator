package mic1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import mic1.model.MainMemory;
import mic1.model.MainMemory.MemoryEntry;

/**
 * Controlador (Controller) para a janela da Memória Principal (MainMemory.fxml).
 *
 * Esta classe atua como a ponte entre a View (FXML) e o Model (MainMemory).
 * Sua principal responsabilidade é injetar os componentes da UI e
 * configurar o data binding (ligação de dados) entre a TableView
 * e a lista observável do modelo.
 *
 * Este controlador segue um padrão de Injeção de Dependência,
 * onde o modelo (MainMemory) é injetado pela classe View (MainMemoryView).
 */
public class MainMemoryController {

    /** A TableView principal injetada do FXML (fx:id="memoryTable"). */
    @FXML
    private TableView<MemoryEntry> memoryTable;

    /** A coluna "Address" injetada do FXML (fx:id="addressColumn"). */
    @FXML
    private TableColumn<MemoryEntry, String> addressColumn;

    /** A coluna "Binary Value" injetada do FXML (fx:id="binaryColumn"). */
    @FXML
    private TableColumn<MemoryEntry, String> binaryColumn;

    /** A coluna "Decimal Value" injetada do FXML (fx:id="decimalColumn"). */
    @FXML
    private TableColumn<MemoryEntry, String> decimalColumn;

    /** A coluna "Hexadecimal Value" injetada do FXML (fx:id="hexColumn"). */
    @FXML
    private TableColumn<MemoryEntry, String> hexColumn;

    /** Referência ao modelo de dados (o "cérebro") da Memória Principal. */
    private MainMemory memoryModel;

    /**
     * Injeta o modelo (MainMemory) neste controlador e inicializa o data binding.
     *
     * Este método é chamado pela MainMemoryView após o FXML ser carregado,
     * completando o ciclo MVC por meio da Injeção de Dependência.
     *
     * @param model A instância do MainMemory criada na classe Main.
     */
    public void setModel(MainMemory model) {
        this.memoryModel = model;

        // --- Configuração do Data Binding da Tabela ---

        // 1. Configura as CellValueFactories:
        // Diz a cada coluna qual propriedade (getter) da classe MemoryEntry
        // ela deve observar para obter seu valor.
        // Os nomes ("address", "binaryValue", etc.) devem corresponder
        // exatamente aos métodos (ex: getAddress(), getBinaryValue())
        // na classe MainMemory.MemoryEntry.
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        binaryColumn.setCellValueFactory(new PropertyValueFactory<>("binaryValue"));
        decimalColumn.setCellValueFactory(new PropertyValueFactory<>("decimalValue"));
        hexColumn.setCellValueFactory(new PropertyValueFactory<>("hexValue"));

        // 2. Vincula (bind) a Tabela à Lista Observável:
        // Conecta a TableView diretamente à ObservableList do modelo.
        // Qualquer alteração na lista (feita pelo modelo) será
        // refletida automaticamente na TableView.
        memoryTable.setItems(memoryModel.getMemoryData());
    }
}