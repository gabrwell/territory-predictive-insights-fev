package dominio;

/**
 * Resultado de uma estratégia de previsão: o valor previsto, quantas leituras
 * sustentaram o cálculo, se é confiável e uma explicação legível.
 */
public final class ResultadoPrevisao {

    private final String estrategia;
    private final double valorPrevisto;
    private final int leiturasUsadas;
    private final boolean confiavel;
    private final String explicacao;

    public ResultadoPrevisao(String estrategia, double valorPrevisto, int leiturasUsadas,
                             boolean confiavel, String explicacao) {
        this.estrategia = estrategia;
        this.valorPrevisto = valorPrevisto;
        this.leiturasUsadas = leiturasUsadas;
        this.confiavel = confiavel;
        this.explicacao = explicacao;
    }

    /** Resultado em que o sistema "se cala": não há histórico suficiente para prever. */
    public static ResultadoPrevisao insuficiente(String estrategia, int disponiveis, int necessarias) {
        return new ResultadoPrevisao(estrategia, Double.NaN, disponiveis, false, String.format(
                "Previsão suspensa: há %d leitura(s) no banco e são necessárias pelo menos %d. "
                        + "Com tão pouco histórico o número seria um chute.", disponiveis, necessarias));
    }

    public String getEstrategia() {
        return estrategia;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public int getLeiturasUsadas() {
        return leiturasUsadas;
    }

    public boolean isConfiavel() {
        return confiavel;
    }

    public String getExplicacao() {
        return explicacao;
    }
}
