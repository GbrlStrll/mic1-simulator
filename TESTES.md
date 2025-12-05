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
8. [Exemplo com Instruções ISA - Operações Aritméticas](#8-exemplo-com-instruções-isa---operações-aritméticas)
9. [Exemplo com Stack - Push e Pop](#9-exemplo-com-stack---push-e-pop)
10. [Exemplo de Cache de Dados - Leituras Repetidas](#10-exemplo-de-cache-de-dados---leituras-repetidas)
11. [Exemplo de Cache Completo - Instruções e Dados](#11-exemplo-de-cache-completo---instruções-e-dados)

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
    A=+1, C=A, PASSA, ENC
    A=A, B=+1, C=A, ADD, ENC
    A=A, B=+1, C=A, ADD, ENC
    A=A, B=+1, C=A, ADD, ENC
    A=A, B=+1, C=A, ADD, ENC

WRITE_LOOP:
    A=B, B=+1, C=B, ADD, ENC
    A=B, PASSA, MAR
    A=B, PASSA, MBR, WR

    A=A, B=-1, C=A, ADD, ENC

    A=A, PASSA, IFZ, GOTO DONE
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

## 8. Exemplo com Instruções ISA - Operações Aritméticas

**Descrição**: Demonstra operações aritméticas básicas usando instruções ISA (LOCO, STOD, ADDD) que interagem com a ALU

```asm
LOCO 10
STOD 100
LOCO 5
ADDD 100
STOD 101
```

**O que faz:**
1. **LOCO 10**: Carrega constante 10 no AC usando decomposição binária eficiente
2. **STOD 100**: Armazena AC (10) no endereço 100 da memória
3. **LOCO 5**: Carrega constante 5 no AC
4. **ADDD 100**: Soma valor do endereço 100 (10) ao AC (AC = 5 + 10 = 15)
5. **STOD 101**: Armazena resultado final (15) no endereço 101

**Fluxo de Execução:**
- Cada instrução ISA é expandida automaticamente em múltiplas microinstruções
- **LOCO**: Usa decomposição binária eficiente para valores grandes
- **STOD**: Escreve na memória usando MAR (endereço) e MBR (dado)
- **ADDD**: Lê da memória, soma com AC usando ALU e atualiza AC
- Todas as operações passam pela ALU para processamento

**Valores Esperados:**
- Endereço 100: 10 (0x0000000A)
- Endereço 101: 15 (0x0000000F)
- AC final: 15 (0x0000000F)

---

## 9. Exemplo com Stack - Push e Pop

**Descrição**: Demonstra operações básicas com a stack usando PUSH e POP

```asm
INIT:
    LOCO 4000
    A=AC, C=SP, PASSA, ENC

PUSH_VALUES:
    LOCO 10
    PUSH
    LOCO 20
    PUSH
    LOCO 30
    PUSH

POP_VALUES:
    POP
    STOD 201
    POP
    STOD 202
    POP
    STOD 203

DONE:
    GOTO DONE
```

**O que faz:**
1. **INIT**: Inicializa SP = 4000 (topo da pilha no endereço 4000)
2. **PUSH_VALUES**: Empilha três valores (10, 20, 30) na stack
   - Cada PUSH escreve AC na memória no endereço SP e depois decrementa SP
   - Após os 3 pushes: SP = 3997, stack contém [10, 20, 30] nos endereços 4000, 3999, 3998
3. **POP_VALUES**: Desempilha os valores na ordem inversa (LIFO - Last In First Out)
   - Cada POP incrementa SP, lê da memória e coloca em AC
   - Primeiro POP: AC = 30 (último valor empilhado, do endereço 3998)
   - Segundo POP: AC = 20 (do endereço 3999)
   - Terceiro POP: AC = 10 (do endereço 4000)
   - Cada valor desempilhado é armazenado em endereços diferentes da memória (201, 202, 203)
4. **DONE**: Loop infinito

**Fluxo de Execução:**
- **PUSH**: Copia AC para MBR, configura MAR com SP, escreve na memória, decrementa SP
- **POP**: Incrementa SP, configura MAR com SP, lê da memória para MBR, copia MBR para AC
- A pilha cresce para baixo (endereços menores), então SP decrementa ao empilhar
- **Nota**: A memória só aceita endereços entre 0 e 4095, por isso SP é inicializado com 4000

**Valores Esperados:**
- **SP inicial**: 4000
- **SP após 3 pushes**: 3997
- **SP após 3 pops**: 4000 (volta ao inicial)
- **Memória da stack**:
  - Endereço 4000: 10 (0x0000000A) - primeiro valor empilhado
  - Endereço 3999: 20 (0x00000014) - segundo valor empilhado
  - Endereço 3998: 30 (0x0000001E) - terceiro valor empilhado
- **Endereço 201**: 30 (0x0000001E) - primeiro valor desempilhado
- **Endereço 202**: 20 (0x00000014) - segundo valor desempilhado
- **Endereço 203**: 10 (0x0000000A) - terceiro valor desempilhado
- **AC final**: 10 (último valor desempilhado)

**Como testar:**
- Configure pause de 500-1000ms para observar cada operação
- Abra a janela **Main Memory** e navegue até os endereços 3997-4000 para ver a stack
- Observe o registrador **SP** decrementando durante PUSH e incrementando durante POP
- Verifique que os valores são desempilhados na ordem inversa (LIFO)
- Confirme que os endereços 201, 202, 203 contêm respectivamente 30, 20, 10
- Use step-by-step mode para ver cada microinstrução sendo executada

---

## 10. Exemplo de Cache de Dados - Leituras Repetidas

**Descrição**: Demonstra o funcionamento do cache de dados através de múltiplas leituras do mesmo endereço

```asm
INICIO:
    LOCO 10
    STOD x
    
    LODD x
    LODD x
    LODD x
    LODD x
    LODD x
    
    JUMP INICIO

x:
```

**O que faz:**
1. **INICIO**: Carrega constante 10 no AC e armazena na variável `x`
2. **Loop de leituras**: Lê o valor de `x` cinco vezes consecutivas
3. **JUMP INICIO**: Reinicia o processo em loop infinito

**Fluxo de Execução:**
- Primeira `LODD x`: **MISS** no Data Cache (carrega bloco contendo endereço de `x`)
- Próximas `LODD x`: **HITs** no Data Cache (dado já está no cache)
- Como estamos lendo o mesmo endereço repetidamente, o cache é muito eficiente
- O cache de dados mantém os dados em blocos de 4 palavras para acesso rápido

**Valores Esperados:**
- Endereço de `x`: 10 (0x0000000A)
- AC durante execução: 10 (valor lido de `x`)
- **Data Cache Hits**: Aumenta rapidamente após primeira leitura
- **Data Cache Misses**: Baixo (apenas primeira leitura)
- **Hit Rate**: Deve ficar alto (>80%) após algumas iterações

**Como testar:**
- Configure pause de 300-500ms para observar as estatísticas
- Abra a janela **Main Memory** e observe a seção **Cache Statistics** na parte inferior
- Execute o programa e observe:
  - Primeira `LODD x`: **Data Cache Miss** aumenta
  - Próximas `LODD x`: **Data Cache Hits** aumenta rapidamente
  - **Hit Rate** do Data Cache deve ficar alto (>80%) após algumas iterações
- Compare as estatísticas antes e depois de algumas iterações do loop
- Use step-by-step mode para ver cada leitura sendo executada

---

## 11. Exemplo de Cache Completo - Instruções e Dados

**Descrição**: Demonstra o funcionamento completo dos caches de instruções e dados em um programa com loop

```asm
INICIO:
    LOCO 0
    STOD contador
    
    LOCO 1
    STOD incremento
    
    LOCO 100
    STOD limite

LOOP:
    LODD contador
    ADDD incremento
    STOD contador
    
    LODD contador
    SUBD limite
    IFZ JUMP FIM
    
    JUMP LOOP

FIM:
    JUMP FIM

contador:
incremento:
limite:
```

**O que faz:**
1. **INICIO**: Inicializa três variáveis na memória (contador=0, incremento=1, limite=100)
2. **LOOP**: Incrementa contador, verifica se chegou ao limite
   - Lê `contador` e `incremento` da memória
   - Soma e armazena resultado de volta em `contador`
   - Compara `contador` com `limite`
   - Se igual, vai para FIM
   - Senão, continua o loop
3. **FIM**: Loop infinito quando contador atinge o limite

**Fluxo de Execução:**
- **Cache de Instruções**: 
  - Primeira execução: MISS (carrega instruções)
  - Próximas execuções: HITs (instruções já estão no cache)
  - Loop faz com que as mesmas instruções sejam executadas repetidamente
- **Cache de Dados**:
  - Primeira leitura/escrita: MISS (carrega blocos contendo variáveis)
  - Próximas leituras: HITs (dados já estão no cache)
  - Mesmos endereços (contador, incremento, limite) são acessados repetidamente
- Ambos os caches demonstram eficiência com padrões repetitivos

**Valores Esperados:**
- **contador**: Incrementa de 0 até 100
- **incremento**: Sempre 1
- **limite**: Sempre 100
- **Instruction Cache Hit Rate**: Alto (>80%) após primeira iteração
- **Data Cache Hit Rate**: Alto (>80%) após primeira iteração
- Ambos os caches devem mostrar muitos mais hits que misses

**Como testar:**
- Configure pause de 200-500ms para observar as estatísticas mudando
- Abra a janela **Main Memory** e observe ambas as seções de cache:
  - **Instruction Cache**: Hits, Misses, Hit Rate
  - **Data Cache**: Hits, Misses, Hit Rate
- Execute o programa e observe:
  - **Primeira iteração**: Ambos os caches têm misses iniciais
  - **Próximas iterações**: Ambos os caches têm hits altos
  - **Hit Rates**: Devem aumentar rapidamente e ficar altos (>80%)
- Observe os logs na janela CPU para ver indicações "[INST]" e "[DATA]" nos acessos
- Compare as estatísticas antes e depois de várias iterações
- Use reset e execute novamente para ver as estatísticas zerarem e crescerem novamente

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
