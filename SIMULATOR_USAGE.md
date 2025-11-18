# MIC-1 Simulator - Guia de Uso

## Como Executar

```bash
mvn javafx:run
```

## Janelas do Simulador

O simulador abre 4 janelas:

1. **CPU** (esquerda, embaixo) - Visualização de registradores e log de microcode
2. **Main Memory** (esquerda, topo) - Visualização da memória principal
3. **Source Code** (direita, topo) - Editor de código assembly
4. **Simulation Controls** (direita, embaixo) - Controles de simulação

## Como Usar

### 1. Escrever Microcode

Na janela **Source Code**, escreva o microcode usando a sintaxe:

```
A=<reg>, B=<reg>, C=<reg>, ADD|AND|PASSA|NOT, SHL|SHR, MAR, MBR, RD, WR, ENC, AMUX
```

Exemplo:
```
START:
    B=PC, C=IR, MAR, RD
    B=PC, C=PC, ADD, A=+1, ENC
    GOTO START
```

### 2. Compilar

Clique em **Assemble** para compilar o código.

### 3. Carregar na CPU

Clique em **Write code to Main Memory** para carregar o microcode na CPU.

### 4. Executar

Na janela **Simulation Controls**:
- **PLAY**: Executa continuamente
- **PAUSE**: Pausa a execução
- **STOP**: Para a execução
- **RESET**: Reseta o simulador
- **Go through a cycle**: Executa um ciclo (modo step-by-step)

### 5. Configurações

- **Pause between subcycles**: Define o intervalo entre ciclos (ms)
- **Pause when on PC**: Define um breakpoint no PC
- **Execute in Step-by-Step Mode**: Ativa modo passo a passo

## Registradores Disponíveis

- **PC** (0): Program Counter
- **AC** (1): Accumulator
- **SP** (2): Stack Pointer
- **IR** (3): Instruction Register
- **TIR** (4): Temporary Instruction Register
- **0** (5): Constante 0 (read-only)
- **+1** (6): Constante +1 (read-only)
- **-1** (7): Constante -1 (read-only)
- **AMASK** (8): Address Mask (read-only)
- **SMASK** (9): Stack Mask (read-only)
- **A-F** (10-15): General Purpose Registers

## Operações da ALU

- **ADD**: A + B
- **AND**: A & B
- **PASSA**: A (pass-through)
- **NOT**: ~A (inversão)

## Sinais de Controle

- **MAR**: Carrega o MAR (Memory Address Register)
- **MBR**: Carrega o MBR (Memory Buffer Register)
- **RD**: Lê da memória
- **WR**: Escreve na memória
- **ENC**: Habilita escrita no registrador C
- **AMUX**: Seleciona MBR como entrada A da ALU
- **SHL**: Shift left
- **SHR**: Shift right
- **IFZ**: Branch if zero
- **IFN**: Branch if negative
- **GOTO**: Branch incondicional
