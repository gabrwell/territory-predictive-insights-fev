package servico;

import dominio.Alerta;
import dominio.ResultadoPrevisao;
import dominio.TipoSensor;

import java.util.Optional;

/**
 * Junta tudo o que o usuário precisa ver sobre uma previsão de um sensor:
 * o resultado, o alerta (se houve) e o que aquilo significa.
 */
public final class AnalisePrevisao {

    private final String sensorId;
    private final TipoSensor tipo;
    private final ResultadoPrevisao resultado;
    private final Alerta alerta;
    private final String significado;

    public AnalisePrevisao(String sensorId, TipoSensor tipo, ResultadoPrevisao resultado,
                           Alerta alerta, String significado) {
        this.sensorId = sensorId;
        this.tipo = tipo;
        this.resultado = resultado;
        this.alerta = alerta;
        this.significado = significado;
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    public ResultadoPrevisao getResultado() {
        return resultado;
    }

    public Optional<Alerta> getAlerta() {
        return Optional.ofNullable(alerta);
    }

    public String getSignificado() {
        return significado;
    }

    public String formatar() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %s (%s) - %s%n", sensorId, tipo.getDescricao(), resultado.getEstrategia()));
        if (resultado.isConfiavel()) {
            sb.append(String.format("    Valor previsto: %s%n", tipo.formatar(resultado.getValorPrevisto())));
        }
        sb.append("    Como foi calculado: ").append(resultado.getExplicacao()).append(System.lineSeparator());
        sb.append("    O que significa: ").append(significado);
        return sb.toString();
    }
}
