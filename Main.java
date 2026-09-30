import java.io.*;
import java.util.*;

public class Main {

    static long comparacoes, movimentos;

    // ---------------- Algoritmos ----------------

    static int buscaLinear(int[] a, int chave) {
        for (int i = 0; i < a.length; i++) {
            comparacoes++;
            if (a[i] == chave) return i;
        }
        return -1;
    }

    static int buscaBinaria(int[] a, int chave) {
        int lo = 0, hi = a.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            comparacoes++; // uma comparacao por iteracao
            if (a[mid] == chave) return mid;
            if (a[mid] < chave) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    static void selectionSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                comparacoes++;
                if (a[j] < a[min]) min = j;
            }
            if (min != i) {
                int t = a[i]; a[i] = a[min]; a[min] = t;
                movimentos++; // 1 troca
            }
        }
    }

    static void mergeSort(int[] a) {
        int[] aux = new int[a.length];
        ms(a, aux, 0, a.length - 1);
    }

    private static void ms(int[] a, int[] aux, int lo, int hi) {
        if (lo >= hi) return;
        int mid = (lo + hi) >>> 1;
        ms(a, aux, lo, mid);
        ms(a, aux, mid + 1, hi);
        merge(a, aux, lo, mid, hi);
    }

    private static void merge(int[] a, int[] aux, int lo, int mid, int hi) {
        System.arraycopy(a, lo, aux, lo, hi - lo + 1);
        movimentos += (hi - lo + 1); // copia para o auxiliar
        int i = lo, j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) { a[k] = aux[j++]; }
            else if (j > hi) { a[k] = aux[i++]; }
            else {
                comparacoes++;
                if (aux[j] < aux[i]) a[k] = aux[j++];
                else a[k] = aux[i++];
            }
            movimentos++; // escrita de volta em a[]
        }
    }

    // ---------------- Utilitarios ----------------

    static int[] gerarDados(int n, String tipo, Random rnd) {
        int[] a = new int[n];
        if (tipo.equals("Aleatorio")) {
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(10 * n);
        } else { // Ordenado
            for (int i = 0; i < n; i++) a[i] = i * 2; // valores pares crescentes
        }
        return a;
    }

    static boolean estaOrdenado(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }

    static boolean mesmoConteudo(int[] original, int[] ordenado) {
        int[] c = original.clone();
        Arrays.sort(c);
        return Arrays.equals(c, ordenado);
    }

    // ---------------- Experimentos ----------------

    static final int EXECUCOES = 4;      // 1 warm-up + 3 validas
    static final int CONSULTAS = 1000;   // consultas por execucao de busca

    public static void main(String[] args) throws IOException {
        List<Integer> tamanhos = new ArrayList<>(List.of(100, 1000, 5000, 10000, 50000, 100000));
        if (args.length > 0 && args[0].equals("500000")) tamanhos.add(500000);
        String[] tipos = {"Aleatorio", "Ordenado"};
        String[] algoritmos = {"BuscaLinear", "BuscaBinaria", "SelectionSort", "MergeSort"};

        try (PrintWriter bruto = new PrintWriter("dados_brutos.csv");
             PrintWriter resumo = new PrintWriter("resumo.csv")) {

            bruto.println("algoritmo,tipo_dado,n,execucao,tempo_ns,comparacoes,trocas_movimentos,valido");
            resumo.println("algoritmo,tipo_dado,n,tempo_medio_ns,comparacoes_media,trocas_movimentos_media,valido");

            for (int n : tamanhos) {
                for (String tipo : tipos) {
                    Random rnd = new Random(42 + n);
                    int[] dados = gerarDados(n, tipo, rnd);
                    // Busca binaria exige dados ordenados (copia ordenada, fora da medicao)
                    int[] dadosOrdenados = dados.clone();
                    Arrays.sort(dadosOrdenados);

                    for (String alg : algoritmos) {
                        if (alg.equals("SelectionSort") && n > 100000) {
                            System.out.printf("Pulando SelectionSort n=%d (muito lento)%n", n);
                            continue;
                        }
                        // Busca binaria sobre dados aleatorios: usa a copia ordenada
                        boolean ehBusca = alg.startsWith("Busca");

                        double somaTempo = 0, somaComp = 0, somaMov = 0;
                        boolean tudoValido = true;

                        for (int exec = 1; exec <= EXECUCOES; exec++) {
                            comparacoes = 0; movimentos = 0;
                            boolean valido;
                            long tempo;

                            if (ehBusca) {
                                int[] base = alg.equals("BuscaBinaria") ? dadosOrdenados : dados;
                                // consultas: metade presentes, metade ausentes (pior caso)
                                int[] chaves = new int[CONSULTAS];
                                boolean[] existe = new boolean[CONSULTAS];
                                Random r = new Random(7 + exec);
                                for (int q = 0; q < CONSULTAS; q++) {
                                    if (q % 2 == 0) { chaves[q] = base[r.nextInt(n)]; existe[q] = true; }
                                    else { chaves[q] = -1 - r.nextInt(1000); existe[q] = false; }
                                }
                                valido = true;
                                long ini = System.nanoTime();
                                int[] res = new int[CONSULTAS];
                                for (int q = 0; q < CONSULTAS; q++) {
                                    res[q] = alg.equals("BuscaBinaria") ? buscaBinaria(base, chaves[q])
                                                                       : buscaLinear(base, chaves[q]);
                                }
                                tempo = (System.nanoTime() - ini) / CONSULTAS; // ns por consulta
                                for (int q = 0; q < CONSULTAS; q++) {
                                    if (existe[q] && (res[q] < 0 || base[res[q]] != chaves[q])) valido = false;
                                    if (!existe[q] && res[q] != -1) valido = false;
                                }
                                comparacoes /= CONSULTAS; // media por consulta
                            } else {
                                int[] copia = dados.clone();
                                long ini = System.nanoTime();
                                if (alg.equals("SelectionSort")) selectionSort(copia);
                                else mergeSort(copia);
                                tempo = System.nanoTime() - ini;
                                valido = estaOrdenado(copia) && mesmoConteudo(dados, copia);
                            }

                            bruto.printf("%s,%s,%d,%d,%d,%d,%d,%b%n", alg, tipo, n, exec, tempo,
                                    comparacoes, movimentos, valido);
                            tudoValido &= valido;

                            if (exec > 1) { // descarta warm-up
                                somaTempo += tempo; somaComp += comparacoes; somaMov += movimentos;
                            }
                        }
                        int k = EXECUCOES - 1;
                        resumo.printf(Locale.US, "%s,%s,%d,%.1f,%.1f,%.1f,%b%n", alg, tipo, n,
                                somaTempo / k, somaComp / k, somaMov / k, tudoValido);
                        System.out.printf(Locale.US, "%-13s %-9s n=%-7d tempo=%.1f ns  comp=%.0f  mov=%.0f  ok=%b%n",
                                alg, tipo, n, somaTempo / k, somaComp / k, somaMov / k, tudoValido);
                    }
                }
            }
        }
        System.out.println("\nArquivos gerados: dados_brutos.csv e resumo.csv");
    }
}
