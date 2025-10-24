# MIC-1 Simulator

Este é um simulador do processador MIC-1 desenvolvido em Java usando JavaFX. O simulador fornece uma interface gráfica interativa para visualizar e controlar a execução do processador MIC-1.

## Requisitos

Para executar este projeto, você precisa ter instalado em seu computador:

1. Java Development Kit (JDK) 21 ou superior
   - Você pode baixar o JDK em: [https://www.oracle.com/br/java/technologies/downloads/#jdk21-windows]
   - Certifique-se de que a variável de ambiente JAVA_HOME está configurada corretamente

2. Apache Maven
   - Você pode baixar o Maven em: [https://maven.apache.org/download.cgi]
   - Certifique-se de que o Maven está adicionado ao PATH do sistema

## Como executar o projeto

1. Clone este repositório:
```bash
git clone [https://github.com/GbrlStrll/mic1-simulator.git]
cd mic1-simulator
```

2. Compile o projeto usando Maven:
```bash
mvn clean compile
```

3. Execute o simulador:
```bash
mvn javafx:run
```

## Interface do Simulador

O simulador abre quatro janelas diferentes:

1. **Source Code**
   - Permite visualizar e editar o código fonte

2. **Main Memory**
   - Mostra o conteúdo da memória principal

3. **CPU**
   - Exibe o estado atual da CPU

4. **Controls**
   - Contém os controles de simulação

## Estrutura do Projeto

O projeto segue uma arquitetura MVC (Model-View-Controller):

- `model/` - Contém as classes que representam o estado do simulador
- `view/` - Contém as classes responsáveis pela interface gráfica
- `controller/` - Contém a lógica de controle entre o modelo e a view
- `resources/` - Contém os arquivos FXML, CSS e ícones da interface

## Desenvolvimento

Para desenvolver o projeto no Visual Studio Code ou outra IDE, você precisará:

1. Configurar o JDK 21 na sua IDE
2. Importar o projeto como um projeto Maven
3. Scene Builder (para edição da interface gráfica)
   - Você pode baixar o Scene Builder em: [https://gluonhq.com/products/scene-builder/]
   - O Scene Builder é usado para visualizar e editar os arquivos FXML da interface gráfica
   - Os arquivos FXML estão localizados em `src/main/resources/mic1/`

### Extensões Recomendadas do VS Code

As seguintes extensões são recomendadas para o desenvolvimento:

1. Extension Pack for Java
   - ID: `vscjava.vscode-java-pack`
   - Inclui as principais extensões para desenvolvimento Java

2. - ID: `bilalekrem.scenebuilderextension`
   - Abre arquivos FXML no SceneBuilder diretamente do VSCode

## Padrão de Commits

Este projeto segue o padrão [Conventional Commits](https://www.conventionalcommits.org/) para mensagens de commit. Cada commit deve seguir o formato:

```
<tipo>[escopo opcional]: <descrição>

[corpo opcional]

[rodapé(s) opcional(is)]
```

### Tipos de Commit

- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `docs`: Alterações na documentação
- `style`: Formatação, ponto e vírgula faltando, etc; Sem alterações no código
- `refactor`: Refatoração do código
- `test`: Adição ou correção de testes
- `chore`: Atualizações de tarefas de build, configurações, etc; Sem alterações no código

### Exemplos

```bash
# Nova funcionalidade
git commit -m "feat: adiciona simulação de interrupção na CPU"

# Correção de bug
git commit -m "fix: corrige cálculo do registrador PC"

# Documentação
git commit -m "docs: atualiza instruções de instalação"

# Refatoração
git commit -m "refactor: simplifica lógica do ciclo de busca"

# Commit com escopo
git commit -m "feat(memory): implementa cache L1"

# Commit com descrição e corpo
git commit -m "feat: adiciona painel de registradores

Adiciona um novo painel que mostra o estado atual de todos os 
registradores da CPU em tempo real durante a simulação."
```

## Resolução de Problemas

Se você encontrar o erro "JavaFX runtime components are missing":
1. Verifique se está usando o comando `mvn javafx:run` para executar
2. Confirme se o JDK 21 está instalado e configurado corretamente
3. Verifique se todas as dependências foram baixadas corretamente com `mvn dependency:tree`