package dominio;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Alerta emitido quando uma previsão ultrapassa um limiar.
 * Guarda o "porquê": sensor, valor previsto, limite estourado e estratégia usada.
 */
public class Alerta {

    private final Long id;
    private final String sensorId;
    private final TipoSensor tipo;
    private final NivelAlerta nivel;
    private final double valorPrevisto;
    private final double limiteUltrapassado;
    private final String estrategia;
    private final String motivo;
    private final LocalDateTime dataHora;

    public Alerta(Long id, String sensorId, TipoSensor tipo, NivelAlerta nivel, double valorPrevisto,
                  double limiteUltrapassado, String estrategia, String motivo, LocalDateTime dataHora) {
        this.id = id;
        this.sensorId = Objects.requireNonNull(sensorId, "sensorId");
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.nivel = Objects.requireNonNull(nivel, "nivel");
        this.valorPrevisto = valorPrevisto;
        this.limiteUltrapassado = limiteUltrapassado;
        this.estrategia = Objects.requireNonNull(estrategia, "estrategia");
        this.motivo = Objects.requireNonNull(motivo, "motivo");
        this.dataHora = Objects.requireNonNull(dataHora, "dataHora");
    }

    public Alerta comId(long novoId) {
        return new Alerta(novoId, sensorId, tipo, nivel, valorPrevisto, limiteUltrapassado,
                estrategia, motivo, dataHora);
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

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiteUltrapassado() {
        return limiteUltrapassado;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-8s %s%n      %s%n      Recomendação: %s",
                dataHora.format(Medicao.FORMATO_DATA), nivel.getRotulo(), sensorId, motivo,
                nivel.getRecomendacao());
    }
}
