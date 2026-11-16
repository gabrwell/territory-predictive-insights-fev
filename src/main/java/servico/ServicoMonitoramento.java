package servico;

import dominio.Alerta;
import dominio.EstrategiaPrevisao;
import dominio.Limiares;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.ResultadoPrevisao;
import dominio.Sensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negócio do monitoramento: ler sensores e gravar, prever a partir do
 * histórico do banco, comparar com os limiares e emitir/gravar alertas.
 *
 * Recebe os DAOs pelo construtor, então nos testes eles são trocados por mocks (Mockito).
 */
public class ServicoMonitoramento {

    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;
    private final ConfiguracaoSistema configuracao;
    private final FabricaPrevisao fabricaPrevisao;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO,
                                ConfiguracaoSistema configuracao, FabricaPrevisao fabricaPrevisao) {
        this.medicaoDAO = medicaoDAO;
        this.alertaDAO = alertaDAO;
        this.configuracao = configuracao;
        this.fabricaPrevisao = fabricaPrevisao;
    }

    // ------------------------------------------------------------------ leituras

    /** Lê o sensor, valida a medição e grava no banco. Valor inválido NÃO é gravado. */
    public Medicao registrarLeitura(Sensor sensor) throws FonteIndisponivelException, DadosInvalidosException {
        double valor = sensor.lerValor();
        return registrarValor(sensor, valor);
    }

    /** Registra um valor informado manualmente (ex.: leitura de régua feita por um agente em campo). */
    public Medicao registrarMedicaoManual(Sensor sensor, double valor)
            throws FonteIndisponivelException, DadosInvalidosException {
        return registrarValor(sensor, valor);
    }

    private Medicao registrarValor(Sensor sensor, double valor)
            throws FonteIndisponivelException, DadosInvalidosException {
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valor, LocalDateTime.now());
        return medicaoDAO.salvar(medicao);
    }

    // ------------------------------------------------------------------ previsões e alertas

    /**
     * Roda todas as estratégias ativas sobre o histórico do banco e GRAVA os alertas emitidos.
     */
    public List<AnalisePrevisao> rodarPrevisoesEAlertas(Sensor sensor)
            throws FonteIndisponivelException, DadosInvalidosException {
        List<AnalisePrevisao> analises = new ArrayList<>();
        for (AnalisePrevisao analise : preverSensor(sensor)) {
            Optional<Alerta> alerta = analise.getAlerta();
            if (alerta.isPresent()) {
                Alerta gravado = alertaDAO.salvar(alerta.get());
                analise = new AnalisePrevisao(analise.getSensorId(), analise.getTipo(),
                        analise.getResultado(), gravado, analise.getSignificado());
            }
            analises.add(analise);
        }
        return analises;
    }

    /**
     * Roda as previsões SEM gravar alertas (usado no status completo, para não duplicar alertas).
     */
    public List<AnalisePrevisao> preverSensor(Sensor sensor) throws FonteIndisponivelException, DadosInvalidosException {
        List<Medicao> historico = medicaoDAO.buscarUltimasPorSensor(
                sensor.getId(), configuracao.getQuantidadeHistoricoParaPrevisao());

        List<AnalisePrevisao> analises = new ArrayList<>();
        for (EstrategiaPrevisao estrategia : fabricaPrevisao.criarEstrategiasAtivas()) {
            ResultadoPrevisao resultado = estrategia.prever(historico); // polimorfismo: cada uma calcula do seu jeito
            Optional<Alerta> alerta = avaliarAlerta(sensor, resultado);
            analises.add(new AnalisePrevisao(sensor.getId(), sensor.getTipo(), resultado,
                    alerta.orElse(null), explicarSignificado(sensor, resultado, alerta)));
        }
        return analises;
    }

    /** Compara a previsão com os limiares do ConfiguracaoSistema. Previsão não confiável nunca gera alerta. */
    public Optional<Alerta> avaliarAlerta(Sensor sensor, ResultadoPrevisao resultado) {
        if (!resultado.isConfiavel()) {
            return Optional.empty();
        }
        Limiares limiares = configuracao.getLimiares(sensor.getTipo());
        double previsto = resultado.getValorPrevisto();
        return limiares.classificar(previsto).map(nivel -> {
            double limite = limiares.limiteDo(nivel);
            String motivo = String.format(
                    "Sensor %s (%s): a estratégia '%s' prevê %s, acima do limite de %s (%s).",
                    sensor.getId(), sensor.getTipo().getDescricao(), resultado.getEstrategia(),
                    sensor.getTipo().formatar(previsto), nivel.getRotulo(), sensor.getTipo().formatar(limite));
            return new Alerta(null, sensor.getId(), sensor.getTipo(), nivel, previsto, limite,
                    resultado.getEstrategia(), motivo, LocalDateTime.now());
        });
    }

    private String explicarSignificado(Sensor sensor, ResultadoPrevisao resultado, Optional<Alerta> alerta) {
        if (!resultado.isConfiavel()) {
            return "Sem alerta: o sistema prefere ficar calado a avisar com base em poucos dados.";
        }
        if (alerta.isPresent()) {
            NivelAlerta nivel = alerta.get().getNivel();
            return nivel.getRotulo() + "! " + alerta.get().getMotivo() + " Recomendação: " + nivel.getRecomendacao();
        }
        Limiares limiares = configuracao.getLimiares(sensor.getTipo());
        return "Situação normal: abaixo do limite de ATENÇÃO ("
                + sensor.getTipo().formatar(limiares.getAtencao()) + ").";
    }

    // ------------------------------------------------------------------ histórico

    public List<Medicao> buscarHistoricoPorSensor(String sensorId) throws FonteIndisponivelException {
        return medicaoDAO.buscarHistoricoPorSensor(sensorId);
    }

    public int contarMedicoesPorSensor(String sensorId) throws FonteIndisponivelException {
        return medicaoDAO.contarPorSensor(sensorId);
    }

    public int contarMedicoes() throws FonteIndisponivelException {
        return medicaoDAO.contarTotal();
    }

    public List<Alerta> listarAlertas() throws FonteIndisponivelException {
        return alertaDAO.listarTodos();
    }

    public List<Alerta> listarAlertasRecentes(int quantidade) throws FonteIndisponivelException {
        return alertaDAO.listarRecentes(quantidade);
    }
}
