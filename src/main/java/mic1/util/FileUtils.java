package mic1.util;

// Imports de Java I/O (Input/Output)
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A "Caixa de Ferramentas" (Utilitários)
 *
 * O QUE FAZER AQUI:
 * 1. Escrever código genérico e reutilizável que NÃO é específico
 * da sua lógica de simulação (Model), da sua interface (View)
 * ou da sua cola (Controller).
 *
 * 2. Métodos em classes 'Util' são (quase) sempre 'static'.
 * Isso significa que você NUNCA precisa criar uma instância
 * (ex: 'new FileUtils()').
 * Você os chama diretamente: 'FileUtils.readFile(meuCaminho);'
 *
 * 3. Esta classe deve ser "burra" (no bom sentido). Ela não sabe
 * *o que* está lendo. O 'SourceCodeController' pode usar esta
 * classe para ler um "código-fonte", mas o 'FileUtils' em si
 * só sabe "ler um arquivo de texto".
 *
 * 4. Exemplos de métodos perfeitos para esta classe:
 * - 'public static String readFile(String path)'
 * - 'public static void writeFile(String path, String content)'
 * - 'public static boolean fileExists(String path)'
 */

public class FileUtils {
    /**
     * Construtor privado para impedir que alguém crie uma instância
     * desta classe utilitária (já que todos os métodos são static).
     */
    private FileUtils() {
        // Impede a instanciação
    }

    /**
     * Lê todo o conteúdo de um arquivo e o retorna como uma String.
     *
     * @param path O caminho completo para o arquivo (ex: "C:/temp/codigo.asm")
     * @return O conteúdo do arquivo como uma String.
     * @throws IOException Se o arquivo não puder ser lido.
     */
    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * Escreve (ou sobrescreve) um conteúdo de String em um arquivo.
     *
     * @param path O caminho completo para o arquivo (ex: "C:/temp/compilado.bin")
     * @param content O texto a ser escrito no arquivo.
     * @throws IOException Se o arquivo não puder ser escrito.
     */
    public static void writeFile(String path, String content) throws IOException {
        Files.write(Paths.get(path), content.getBytes());
    }
}
