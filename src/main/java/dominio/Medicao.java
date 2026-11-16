package dominio;

import excecao.DadosInvalidosException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Uma leitura de sensor. Só existe se for válida: o construtor valida tudo
 * e lança DadosInvalidosException quando o valor está fora da faixa física do sensor.
 */
public class Medicao {

    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final Long id;
    private final String sensorId;
    private final TipoSensor tipo;
    private final double valor;
    private final LocalDateTime dataHora;

    /** Cria uma medição nova (ainda sem id do banco). */
    public Medicao(String sensorId, TipoSensor tipo, double valor, LocalDateTime dataHora)
            throws DadosInvalidosException {
        this(null, sensorId, tipo, valor, dataHora);
    }

    /** Reconstrói uma medição já gravada (com id). Também valida: o banco pode ter dado corrompido. */
    public Medicao(Long id, String sensorId, TipoSensor tipo, double valor, LocalDateTime dataHora)
            throws DadosInvalidosException {
        validar(sensorId, tipo, valor, dataHora);
        this.id = id;
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    private static void validar(String sensorId, TipoSensor tipo, double valor, LocalDateTime dataHora)
            throws DadosInvalidosException {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("Medição sem identificação de sensor.");
        }
        if (tipo == null) {
            throw new DadosInvalidosException("Medição do sensor " + sensorId + " sem tipo definido.");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("Medição do sensor " + sensorId + " sem data/hora.");
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new DadosInvalidosException("Medição de " + tipo.getDescricaoMinuscula()
                    + " não numérica no sensor " + sensorId + ".");
        }
        if (valor < 0) {
            throw new DadosInvalidosException(String.format("Medição de %s negativa (%s) no sensor %s.",
                    tipo.getDescricaoMinuscula(), tipo.formatar(valor), sensorId));
        }
        if (!tipo.aceita(valor)) {
            throw new DadosInvalidosException(String.format(
                    "Medição de %s fora da faixa válida (%s) no sensor %s: o aceitável é de %s a %s.",
                    tipo.getDescricaoMinuscula(), tipo.formatar(valor), sensorId,
                    tipo.formatar(tipo.getValorMinimo()), tipo.formatar(tipo.getValorMaximo())));
        }
    }

    /** Devolve uma cópia com o id gerado pelo banco (os dados já foram validados). */
    public Medicao comId(long novoId) {
        try {
            return new Medicao(novoId, sensorId, tipo, valor, dataHora);
        } catch (DadosInvalidosException e) {
            // Impossível na prática: os mesmos dados acabaram de passar pela validação.
            throw new IllegalStateException("Medição válida tornou-se inválida ao receber id: " + e.getMessage(), e);
        }
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) = %s",
                dataHora.format(FORMATO_DATA), sensorId, tipo.getDescricao(), tipo.formatar(valor));
    }
}
