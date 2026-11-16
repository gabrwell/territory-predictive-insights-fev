package dominio;

import java.util.Locale;

/**
 * Tipos de sensor usados no recorte "Clima local: chuva intensa e risco de alagamento".
 * Cada tipo conhece a faixa FÍSICA válida de leitura: fora dela a medição é rejeitada.
 */
public enum TipoSensor {

    PLUVIOMETRO("Chuva", "mm/h", 0.0, 300.0),
    NIVEL_CORREGO("Nível do córrego", "m", 0.0, 10.0),
    UMIDADE_SOLO("Umidade do solo", "%", 0.0, 100.0);

    private final String descricao;
    private final String unidade;
    private final double valorMinimo;
    private final double valorMaximo;

    TipoSensor(String descricao, String unidade, double valorMinimo, double valorMaximo) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.valorMinimo = valorMinimo;
        this.valorMaximo = valorMaximo;
    }

    public boolean aceita(double valor) {
        return !Double.isNaN(valor) && valor >= valorMinimo && valor <= valorMaximo;
    }

    public double limitarAFaixa(double valor) {
        return Math.max(valorMinimo, Math.min(valorMaximo, valor));
    }

    public String formatar(double valor) {
        return String.format("%.1f %s", valor, unidade);
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDescricaoMinuscula() {
        return descricao.toLowerCase(Locale.ROOT);
    }

    public String getUnidade() {
        return unidade;
    }

    public double getValorMinimo() {
        return valorMinimo;
    }

    public double getValorMaximo() {
        return valorMaximo;
    }
}
