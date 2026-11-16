package persistencia;

import dominio.Alerta;
import dominio.NivelAlerta;
import dominio.TipoSensor;
import excecao.FonteIndisponivelException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Acesso à tabela ALERTA (PreparedStatement + try-with-resources). */
public class AlertaDAO {

    private static final String SQL_INSERIR = """
            INSERT INTO alerta (sensor_id, tipo, nivel, valor_previsto, limite, estrategia, motivo, data_hora)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";
    private static final String SQL_LISTAR_TODOS = """
            SELECT id, sensor_id, tipo, nivel, valor_previsto, limite, estrategia, motivo, data_hora
            FROM alerta ORDER BY data_hora DESC, id DESC""";
    private static final String SQL_LISTAR_RECENTES = """
            SELECT id, sensor_id, tipo, nivel, valor_previsto, limite, estrategia, motivo, data_hora
            FROM alerta ORDER BY data_hora DESC, id DESC LIMIT ?""";
    private static final String SQL_LISTAR_POR_SENSOR = """
            SELECT id, sensor_id, tipo, nivel, valor_previsto, limite, estrategia, motivo, data_hora
            FROM alerta WHERE sensor_id = ? ORDER BY data_hora DESC, id DESC""";

    private final ConexaoBanco conexaoBanco;

    public AlertaDAO(ConexaoBanco conexaoBanco) {
        this.conexaoBanco = conexaoBanco;
    }

    public Alerta salvar(Alerta alerta) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_INSERIR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, alerta.getSensorId());
            ps.setString(2, alerta.getTipo().name());
            ps.setString(3, alerta.getNivel().name());
            ps.setDouble(4, alerta.getValorPrevisto());
            ps.setDouble(5, alerta.getLimiteUltrapassado());
            ps.setString(6, alerta.getEstrategia());
            ps.setString(7, alerta.getMotivo());
            ps.setTimestamp(8, Timestamp.valueOf(alerta.getDataHora()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    return alerta.comId(chaves.getLong(1));
                }
                throw new FonteIndisponivelException("O banco não devolveu o id do alerta gravado.");
            }
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao gravar o alerta do sensor "
                    + alerta.getSensorId() + ": " + e.getMessage(), e);
        }
    }

    public List<Alerta> listarTodos() throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_LISTAR_TODOS)) {
            return lerAlertas(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao listar os alertas: " + e.getMessage(), e);
        }
    }

    public List<Alerta> listarRecentes(int quantidade) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_LISTAR_RECENTES)) {
            ps.setInt(1, quantidade);
            return lerAlertas(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao listar os alertas recentes: " + e.getMessage(), e);
        }
    }

    public List<Alerta> listarPorSensor(String sensorId) throws FonteIndisponivelException {
        try (Connection conexao = conexaoBanco.abrirConexao();
             PreparedStatement ps = conexao.prepareStatement(SQL_LISTAR_POR_SENSOR)) {
            ps.setString(1, sensorId);
            return lerAlertas(ps);
        } catch (SQLException e) {
            throw new FonteIndisponivelException("Falha ao listar os alertas do sensor "
                    + sensorId + ": " + e.getMessage(), e);
        }
    }

    private static List<Alerta> lerAlertas(PreparedStatement ps) throws SQLException, FonteIndisponivelException {
        List<Alerta> alertas = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                alertas.add(converter(rs));
            }
        }
        return alertas;
    }

    private static Alerta converter(ResultSet rs) throws SQLException, FonteIndisponivelException {
        long id = rs.getLong("id");
        try {
            return new Alerta(id, rs.getString("sensor_id"), TipoSensor.valueOf(rs.getString("tipo")),
                    NivelAlerta.valueOf(rs.getString("nivel")), rs.getDouble("valor_previsto"),
                    rs.getDouble("limite"), rs.getString("estrategia"), rs.getString("motivo"),
                    rs.getTimestamp("data_hora").toLocalDateTime());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new FonteIndisponivelException("Registro de alerta corrompido no banco (id " + id + "): "
                    + e.getMessage(), e);
        }
    }
}
