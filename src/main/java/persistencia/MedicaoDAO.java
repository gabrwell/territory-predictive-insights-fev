package persistencia;

import dominio.Medicao;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Acesso à tabela MEDICAO. Todo SQL usa PreparedStatement com parâmetros (?)
 * e todos os recursos são fechados com try-with-resources.
 */
public class MedicaoDAO {

    private static final String SQL_INSERIR =
            "INSERT INTO medicao (sensor_id, tipo, valor, data_hora) VALUES (?, ?, ?, ?)";
    private static final String SQL_HISTORICO_POR_SENSOR =
            "SELECT id, sensor_id, tipo, valor, data_hora FROM medicao WHERE sensor_id = ? ORDER BY data_hora, id";
    private static final String SQL_ULTIMAS_POR_SENSOR = """
            SELECT id, sensor_id, tipo, valor, data_hora FROM medicao WHERE sensor_id = ?
            ORDER BY data_hora DESC, id DESC LIMIT ?""";
    private static final String SQL_CONTAR_POR_SENSOR =
            "SELECT COUNT(*) FROM medicao WHERE sensor_id = ?";
    private static final String SQL_CONTAR_TOTAL =
            "SELECT COUNT(*) FROM medicao";

    private final ConexaoBanco conexaoBanco;

    public MedicaoDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco;
    }

    public Medicao salvar(Medicao medicao) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, medicao.getSensorId());
            ps.setString(2, medicao.getTipo().name());
            ps.setDouble(3, medicao.getValor());
            ps.setTimestamp(4, Timestamp.valueOf(medicao.getDataHora()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return medicao.comId(chaves.getLong(1));
                }
                throw new FonteIndisponivelException("O banco não devolveu o id da medição gravada.");
            }
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao gravar a medição do sensor "
                    + medicao.getSensorId() + " no banco: " + e.getMessage(), e);
        }
    }

    /** Histórico completo de um sensor, da leitura mais antiga para a mais recente. */
    public List<Medicao> buscarHistoricoPorSensor(String sensorId) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_HISTORICO_POR_SENSOR)) {
            ps.setString(1, sensorId);
            return lerMedicoes(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar o histórico do sensor "
                    + sensorId + ": " + e.getMessage(), e);
        }
    }

    /** Últimas N leituras de um sensor, devolvidas em ordem cronológica (mais antiga primeiro). */
    public List<Medicao> buscarUltimasPorSensor(String sensorId, int quantidade) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_ULTIMAS_POR_SENSOR)) {
            ps.setString(1, sensorId);
            ps.setInt(2, quantidade);
            List<Medicao> maisRecentesPrimeiro = lerMedicoes(ps);
            Collections.reverse(maisRecentesPrimeiro);
            return maisRecentesPrimeiro;
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao consultar as últimas leituras do sensor "
                    + sensorId + ": " + e.getMessage(), e);
        }
    }

    public int contarPorSensor(String sensorId) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_CONTAR_POR_SENSOR)) {
            ps.setString(1, sensorId);
            return lerContagem(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao contar as leituras do sensor "
                    + sensorId + ": " + e.getMessage(), e);
        }
    }

    public int contarTotal() throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_CONTAR_TOTAL)) {
            return lerContagem(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao contar as medições: " + e.getMessage(), e);
        }
    }

    private static int lerContagem(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static List<Medicao> lerMedicoes(PreparedStatement ps) throws SQLException, FonteIndisponivelException {
        List<Medicao> medicoes = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                medicoes.add(converter(rs));
            }
        }
        return medicoes;
    }

    private static Medicao converter(ResultSet rs) throws SQLException, FonteIndisponivelException {
        long id = rs.getLong("id");
        try {
            return new Medicao(id, rs.getString("sensor_id"), TipoSensor.valueOf(rs.getString("tipo")),
                    rs.getDouble("valor"), rs.getTimestamp("data_hora").toLocalDateTime());
        } catch (DadosInvalidosException | IllegalArgumentException e) {
            throw new FonteIndisponivelException("Registro de medição corrompido no banco (id " + id + "): "
                    + e.getMessage(), e);
        }
    }
}
