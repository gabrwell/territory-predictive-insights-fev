package dominio;

/**
 * Níveis de alerta em ordem crescente de gravidade.
 * Cada nível carrega uma recomendação de ação para quem toma a decisão.
 */
public enum NivelAlerta {

    ATENCAO("ATENÇÃO",
            "Acompanhar a evolução e verificar bueiros e bocas de lobo dos pontos baixos."),
    ALERTA("ALERTA",
            "Avisar os moradores das áreas baixas e deixar rotas de saída e abrigos preparados."),
    CRITICO("CRÍTICO",
            "Acionar a Defesa Civil e avaliar a retirada preventiva das famílias em área de risco.");

    private final String rotulo;
    private final String recomendacao;

    NivelAlerta(String rotulo, String recomendacao) {
        this.rotulo = rotulo;
        this.recomendacao = recomendacao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getRecomendacao() {
        return recomendacao;
    }
}
