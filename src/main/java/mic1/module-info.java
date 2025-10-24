/**
 * O "Porteiro" do Módulo Java (JPMS)
 *
 * O QUE FAZER AQUI:
 * 1. Definir o nome do seu módulo (ex: 'mic1').
 * 2. Listar todos os módulos JavaFX que seu projeto 'PRECISA' (requires).
 * 3. Dar 'PERMISSÃO' (opens) para que os módulos JavaFX possam "invadir"
 * seus pacotes e acessar seu código.
 *
 * POR QUE PERMITIR?
 * - 'javafx.fxml' (o FXML) precisa de permissão (opens) para acessar seu
 * pacote 'controller' e injetar os componentes (@FXML) nos seus controllers.
 * - 'javafx.base' (as Propriedades) precisa de permissão (opens) para acessar
 * seu pacote 'model' e permitir o "data binding" (ex: PropertyValueFactory
 * lendo os dados da sua classe de Modelo para a Tabela).
 * - 'javafx.graphics' precisa de permissão (opens) para acessar seu
 * pacote principal ('mic1') para poder lançar a aplicação (chamar o 'Main.java').
 */

// -----------------------------------------------------------------
// SEU CÓDIGO CORRIGIDO (Copie e cole este no seu arquivo)
// -----------------------------------------------------------------
module mic1 {
    // 1. O que seu módulo precisa para funcionar:
    requires javafx.controls; // Para botões, tabelas, labels...
    requires javafx.fxml;      // Para carregar os arquivos .fxml
    requires javafx.graphics;  // Para o 'Stage', 'Scene', 'Image'...

    // 2. Para quem você dá permissão (extremamente importante):
    
    // Abre o pacote principal para o JavaFX lançar o 'Main.java'
    opens mic1 to javafx.graphics; 
    
    // **A CORREÇÃO MAIS IMPORTANTE**
    // Abre seus controllers para o FXML (para que o @FXML funcione)
    opens mic1.controller to javafx.fxml; 
    
    // Abre seus modelos para o JavaFX (para que a TableView leia os dados)
    opens mic1.model to javafx.base; 
    
    // (O 'exports mic1;' do seu original não é necessário se você usar 'opens')
    //exports mic1;
}