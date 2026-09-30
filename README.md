# A Corrida da Eficiência — Projeto e Análise de Algoritmos

Este repositório contém a implementação prática e a análise de desempenho de algoritmos clássicos de ordenação e busca, desenvolvidos para o estudo de caso da **DataFlow Corp.**.

## Algoritmos Implementados

* **Buscas:** Busca Linear e Busca Binária

* **Ordenações:** Selection Sort e Merge Sort

## Métricas Analisadas

* Tempo de execução em nanorsegundos (`System.nanoTime()`)

* Quantidade média de comparações

* Quantidade média de trocas e movimentações de memória


## Como Executar
1. Compilar o código Java:
```bash
javac Main.java
```

2. Executar os testes (tamanhos de $n$ até $100.000$):
```bash
java Main

```
3. Executar incluindo $n = 500.000$ (ignora o Selection Sort para evitar tempo excessivo):
```bash
java Main 500000

```
Após a execução, serão gerados automaticamente os ficheiros `dados_brutos.csv` e `resumo.csv` com as métricas obtidas.
