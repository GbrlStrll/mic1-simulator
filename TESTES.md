# 🧪 Exemplos de Teste para o Simulador MIC-1

Este arquivo contém diversos exemplos de código microcode para testar o simulador MIC-1.

---

## 📚 Índice

1. [Exemplo Básico - Contador Simples](#1-exemplo-básico---contador-simples)
2. [Exemplo de Incremento Contínuo](#2-exemplo-de-incremento-contínuo)
3. [Exemplo Intermediário - Soma com Reset](#3-exemplo-intermediário---soma-com-reset)
4. [Exemplo Avançado - Fetch-Decode-Execute](#4-exemplo-avançado---fetch-decode-execute)
5. [Exemplo de Operações Lógicas](#5-exemplo-de-operações-lógicas)
6. [Exemplo de Escrita na Memória](#6-exemplo-de-escrita-na-memória)
7. [Exemplo Completo - Loop com Memória](#7-exemplo-completo---loop-com-memória)

---

## 1. Exemplo Básico - Contador Simples

**Descrição**: Incrementa o registrador AC e depois reinicia (0→1→0→1...)

```asm
START:
    A=0, C=AC, PASSA, ENC
    A=AC, B=+1, C=AC, ADD, ENC
    GOTO START
```

**O que faz:**
- Ciclo 0: AC = 0
- Ciclo 1: AC = 1
- Ciclo 2: Volta para START (AC = 0 novamente)
- Loop infinito

**Como testar:**
- Configure "Pause between subcycles: 1000ms"
- Marque "Execute in Step-by-Step Mode"
- Observe AC alternando entre 0 e 1

---

## 2. Exemplo de Incremento Contínuo

**Descrição**: AC incrementa indefinidamente (0→1→2→3→4...)

```asm
START:
    A=0, C=AC, PASSA, ENC

LOOP:
    A=AC, B=+1, C=AC, ADD, ENC
    GOTO LOOP
```

**O que faz:**
- Primeira execução: zera AC
- Depois: fica em loop incrementando para sempre
- AC: 0, 1, 2, 3, 4, 5, 6...

**Como testar:**
- Configure "Pause between subcycles: 500ms"
- Clique em PLAY
- Observe AC crescendo continuamente
- Use PAUSE para ver o valor atual

---

## 3. Exemplo Intermediário - Soma com Reset

**Descrição**: Soma valores nos registradores A e AC, depois reinicia

```asm
START:
    A=0, C=AC, PASSA, ENC
    A=0, C=A, PASSA, ENC

SOMA:
    A=AC, B=+1, C=AC, ADD, ENC
    A=A, B=+1, C=A, ADD, ENC
    B=AC, IFZ, GOTO FIM
    GOTO SOMA

FIM:
    A=A, C=SP, PASSA, ENC
    GOTO START
```

**O que faz:**
- Incrementa AC e registrador A
- Quando AC chega a zero (overflow), vai para FIM
- Copia A para SP e reinicia

**Como testar:**
- Configure "Pause when on PC: 0" para parar no início
- Use step-by-step mode
- Observe AC e A incrementando juntos

---

## 4. Exemplo Avançado - Fetch-Decode-Execute

**Descrição**: Simula um ciclo básico de processador

```asm
FETCH:
    B=PC, C=MAR, PASSA, MAR
    B=PC, A=+1, C=PC, ADD, ENC, RD
    B=MBR, C=IR, PASSA, ENC, AMUX

DECODE:
    B=IR, C=TIR, PASSA, ENC

EXECUTE_ADD:
    B=AC, A=+1, C=AC, ADD, ENC
    GOTO CHECK

EXECUTE_SUB:
    B=AC, A=-1, C=AC, ADD, ENC
    GOTO CHECK

CHECK:
    B=AC, IFN, GOTO RESET
    B=AC, IFZ, GOTO RESET
    GOTO FETCH

RESET:
    A=0, C=AC, PASSA, ENC
    A=0, C=PC, PASSA, ENC
    GOTO FETCH
```

**O que faz:**
- **FETCH**: Busca instrução da memória
- **DECODE**: Decodifica no TIR
- **EXECUTE**: Executa ADD ou SUB
- **CHECK**: Verifica flags e reseta se necessário

**Como testar:**
- Escreva alguns valores na memória primeiro
- Configure pause longo para ver cada fase
- Observe PC, IR, TIR, MAR, MBR mudando

---

## 5. Exemplo de Operações Lógicas

**Descrição**: Testa AND, NOT e shifts

```asm
INIT:
    A=+1, C=A, PASSA, ENC
    A=+1, C=B, PASSA, ENC

TEST_AND:
    A=A, B=B, C=AC, AND, ENC
    GOTO TEST_NOT

TEST_NOT:
    A=AC, C=AC, NOT, ENC
    GOTO TEST_SHIFT

TEST_SHIFT:
    A=AC, C=AC, SHL, ENC
    A=AC, C=AC, SHR, ENC
    GOTO INIT
```

**O que faz:**
- Inicializa A=1, B=1
- Testa AND (1 & 1 = 1)
- Testa NOT (~1 = -2 em complemento de 2)
- Testa shift left e right
- Reinicia

**Como testar:**
- Observe os valores binários na tabela de registradores
- Use step-by-step para ver cada operação
- Compare os valores hexadecimais

---

## 6. Exemplo de Escrita na Memória

**Descrição**: Escreve valores específicos em endereços da memória

```asm
INIT:
    A=0, C=AC, PASSA, ENC

WRITE_ADDR_0:
    A=0, PASSA, MAR
    A=+1, B=+1, ADD, MBR, WR

WRITE_ADDR_1:
    A=+1, PASSA, MAR
    A=+1, B=+1, C=AC, ADD, ENC
    A=AC, B=+1, ADD, MBR, WR

WRITE_ADDR_2:
    A=+1, B=+1, ADD, MAR
    A=AC, B=+1, C=AC, ADD, ENC
    A=AC, B=+1, ADD, MBR, WR

DONE:
    GOTO DONE
```

**O que faz:**
- **Endereço 0**: Escreve 2 (1+1)
- **Endereço 1**: Escreve 3 (2+1)
- **Endereço 2**: Escreve 4 (3+1)
- Fica em loop infinito

**Como testar:**
- Execute até DONE
- **Abra a janela Main Memory**
- Você verá:
  - Endereço 0 = 0x00000002 (2 em decimal)
  - Endereço 1 = 0x00000003 (3 em decimal)
  - Endereço 2 = 0x00000004 (4 em decimal)

---

## 7. Exemplo Completo - Loop com Memória

**Descrição**: Escreve valores incrementais em múltiplos endereços de memória

```asm
START:
    A=0, C=B, PASSA, ENC
    A=+1, B=+1, B=+1, B=+1, B=+1, ADD, C=A, ENC

WRITE_LOOP:
    A=B, B=+1, C=B, ADD, ENC
    A=B, PASSA, MAR
    A=B, PASSA, MBR, WR

    A=A, B=-1, C=A, ADD, ENC

    B=A, IFZ, GOTO DONE
    GOTO WRITE_LOOP

DONE:
    GOTO DONE
```

**O que faz:**
1. **START**: Inicializa B=0 (contador de endereços) e A=5 (contador de iterações)
2. **WRITE_LOOP**: Escreve valores incrementais na memória
   - Incrementa B (B = B + 1)
   - Define MAR = B (seleciona endereço)
   - Define MBR = B e escreve (escreve o valor no endereço)
   - Decrementa A (A = A - 1)
   - Se A = 0, pula para DONE
   - Senão, continua o loop
3. **DONE**: Loop infinito

**Como testar:**
- Configure pause de 200-500ms
- Abra a janela Main Memory
- Você verá 5 escritas sequenciais:
  - Endereço 1 = 0x00000001 (1 em decimal)
  - Endereço 2 = 0x00000002 (2 em decimal)
  - Endereço 3 = 0x00000003 (3 em decimal)
  - Endereço 4 = 0x00000004 (4 em decimal)
  - Endereço 5 = 0x00000005 (5 em decimal)
- Observe MAR e MBR mudando a cada iteração

---

## 📖 Sintaxe do Microcode

### Registradores Disponíveis:
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
- **A-F** (10-15): Registradores de propósito geral

### Operações da ALU:
- **ADD**: A + B
- **AND**: A & B
- **PASSA** (ou PASS): Passa A (ignora B)
- **NOT** (ou NOTA): ~A (inversão bit a bit)

### Sinais de Controle:
- **MAR**: Carrega Memory Address Register
- **MBR**: Carrega Memory Buffer Register
- **RD**: Lê da memória (Memory[MAR] → MBR)
- **WR**: Escreve na memória (MBR → Memory[MAR])
- **ENC**: Enable C-bus (escreve no registrador C)
- **AMUX**: Usa MBR como entrada A da ALU
- **SHL**: Shift left (deslocamento à esquerda)
- **SHR**: Shift right (deslocamento à direita)

### Condições de Branch:
- **IFZ**: Branch if zero (se resultado = 0)
- **IFN**: Branch if negative (se resultado < 0)
- **GOTO**: Branch incondicional

### Formato Geral:
```
LABEL:
    A=<reg>, B=<reg>, C=<reg>, <ALU_OP>, <SHIFT>, <SINAIS>, <COND>
```

**Exemplo:**
```
SOMA:
    A=AC, B=+1, C=AC, ADD, ENC
```
Significa: AC = AC + 1 (com escrita habilitada)

---

## 🎯 Dicas de Teste

### Para Debug:
1. Marque **"Execute in Step-by-Step Mode"**
2. Configure **"Pause between subcycles: 1000ms"** ou mais
3. Use **"Go through a cycle"** para avançar manualmente
4. Observe o **log de microcode** na janela CPU

### Para Ver Memória:
1. Use instruções com **MAR**, **MBR**, **WR**
2. Abra a janela **Main Memory**
3. Configure pause longo para ver as mudanças
4. Observe as 4 representações (endereço, binário, decimal, hex)

### Para Breakpoints:
1. Configure **"Pause when on PC: X"** (onde X é o endereço)
2. O simulador pausará automaticamente nesse endereço
3. Útil para debug de loops específicos

### Para Velocidade:
- **Muito lento**: 2000ms ou mais
- **Normal**: 500-1000ms
- **Rápido**: 100-200ms
- **Muito rápido**: 10-50ms

---

## ⚠️ Problemas Comuns

### AC não incrementa:
- Verifique se **ENC** está ativado
- Confirme que **C=AC** está definido
- Veja se o GOTO está indo para o endereço correto

### Memória não muda:
- Certifique-se de usar **MAR** antes de **WR**
- Defina **MBR** com o valor a escrever
- Use **WR** para executar a escrita

### Loop infinito inesperado:
- Verifique se os labels estão corretos
- Confirme que GOTO aponta para o label certo
- Use o log de microcode para ver o MPC

### Registradores read-only:
- Registradores 0, +1, -1, AMASK, SMASK **não podem** ser escritos
- Tentar escrever neles não causa erro, mas é ignorado

---

## 🚀 Próximos Passos

Agora que você tem esses exemplos, experimente:

1. **Modificar os exemplos** para entender como funcionam
2. **Combinar técnicas** de diferentes exemplos
3. **Criar seus próprios programas** de microcode
4. **Implementar algoritmos simples** (fibonacci, fatorial, etc)
5. **Explorar a memória** com padrões complexos

Divirta-se explorando o MIC-1! 🎉
