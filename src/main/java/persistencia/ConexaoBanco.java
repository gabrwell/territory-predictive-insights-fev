package persistencia;

import excecao.FonteIndisponivelException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Abre conexões com o banco H2 e cria as tabelas na primeira execução.
 * O banco fica em arquivo (pasta ./dados), então o histórico sobrevive ao reiniciar.
 */
public class ConexaoBanco {

    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private final String url;

    public ConexaoBanco(String url) {
        this.url = url;
    }

    public Connection abrirConexao() throws SQLException {
        return DriverManager.getConnection(url, USUARIO, SENHA);
    }

    /** Executa o schema.sql (CREATE TABLE IF NOT EXISTS): seguro rodar a cada inicialização. */
    public void inicializarEsquema() throws FonteIndisponivelException {
        String script = lerScript("/schema.sql");
        try (Connection conexao = abrirConexao()) {
            for (String comando : script.split(";")) {
                String sql = removerComentarios(comando).trim();
                if (sql.isEmpty()) {
                    continue;
                }
                // Script fixo do projeto (schema.sql), sem nenhum dado digitado pelo usuário.
                try (PreparedStatement ps = conexao.prepareStatement(sql)) {
                    ps.execute();
                }
            }
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Não foi possível preparar o banco de dados em '" + url
                    + "': " + e.getMessage(), e);
        }
    }

    private static String lerScript(String recurso) throws FonteIndisponivelException {
        try (InputStream entrada = ConexaoBanco.class.getResourceAsStream(recurso)) {
            if (entrada == null) {
                throw new FonteIndisponivelException("Script " + recurso + " não encontrado no classpath.");
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FonteIndisponivelException("Erro ao ler o script " + recurso + ": " + e.getMessage(), e);
        }
    }

    private static String removerComentarios(String comando) {
        StringBuilder limpo = new StringBuilder();
        for (String linha : comando.split("\\R")) {
            if (!linha.trim().startsWith("--")) {
                limpo.append(linha).append('\n');
            }
        }
        return limpo.toString();
    }

    public String getUrl() {
        return url;
    }
}
