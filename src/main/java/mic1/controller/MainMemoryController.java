package mic1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import mic1.model.MainMemory; // Importa o Modelo
import mic1.model.MainMemory.MemoryEntry; // Importa a classe interna do Modelo

/**
 * O "Ponte" (Controlador) para a janela MainMemory.
 *
 * Responsabilidades:
 * 1. Conectar a TableView e suas colunas (definidas no FXML) com o Modelo (MainMemory).
 * 2. Configurar as colunas para exibir os dados corretos (Address, Binary, etc.).
 * 3. Ligar a TableView à lista de dados observável do Modelo.
 *
 * NOTA: Este controller foi adaptado para o padrão de Injeção de Dependência
 * (igual ao SourceCodeController) e NÃO usa mais Singleton (MainMemory.getInstance()).
 * O método 'setModel' deve ser chamado (ex: pelo Main.java) para injetar o
 * modelo de memória.
 */
public class MainMemoryController {

    // --- Componentes da View (Injetados pelo FXML) ---
    // Os 'fx:id' no FXML devem corresponder a estes nomes

    @FXML
    private TableView<MemoryEntry> memoryTable; // A tabela principal

    @FXML
    private TableColumn<MemoryEntry, String> addressColumn; // Coluna de Endereço

    @FXML
    private TableColumn<MemoryEntry, String> binaryColumn; // Coluna Binária

    @FXML
    private TableColumn<MemoryEntry, String> decimalColumn; // Coluna Decimal

    @FXML
    private TableColumn<MemoryEntry, String> hexColumn; // Coluna Hexadecimal

    // --- Referência ao Modelo (ainda não preenchida) ---
    private MainMemory memoryModel;

    /**
     * Este método é o "ponto de entrada" principal.
     * Ele deve ser chamado pela classe que carrega o FXML (ex: Main.java)
     * para injetar o "cérebro" (o Modelo).
     *
     * O método 'initialize()' (do 'Initializable') não é usado para que
     * possamos garantir que o modelo seja injetado ANTES da configuração.
     */
    public void setModel(MainMemory model) {
        this.memoryModel = model;

        // --- A MÁGICA (Data Binding da Tabela) ---

        // 1. Configura as Colunas da Tabela
        //    Diz a cada coluna qual método getter da classe MemoryEntry usar.
        //    O nome ("address", "binaryValue", etc.) DEVE corresponder
        //    exatamente aos nomes dos métodos getAddress(), getBinaryValue()
        //    na classe MainMemory.MemoryEntry.
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        binaryColumn.setCellValueFactory(new PropertyValueFactory<>("binaryValue"));
        decimalColumn.setCellValueFactory(new PropertyValueFactory<>("decimalValue"));
        hexColumn.setCellValueFactory(new PropertyValueFactory<>("hexValue"));

        // 2. Conecta a Tabela aos Dados do Modelo
        //    Diz à TableView para exibir os itens da lista observável do modelo.
        //    Qualquer mudança na lista (feita pelo Modelo) será refletida
        //    automaticamente na tabela.
        memoryTable.setItems(memoryModel.getMemoryData());
    }
}