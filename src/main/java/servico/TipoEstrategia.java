package servico;

/** Estratégias de previsão disponíveis (a FabricaPrevisao sabe criar cada uma). */
public enum TipoEstrategia {

    MEDIA_MOVEL("Média móvel das últimas leituras"),
    COMPARACAO("Comparação com o período anterior");

    private final String descricao;

    TipoEstrategia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
