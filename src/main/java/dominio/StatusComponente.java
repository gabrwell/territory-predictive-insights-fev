package dominio;

public enum StatusComponente {

    NAO_VERIFICADO("Não verificado"),
    OPERANTE("Operante: todos os sensores respondendo"),
    DEGRADADO("Degradado: parte dos sensores fora de operação"),
    INOPERANTE("Inoperante: nenhum sensor respondendo");

    private final String descricao;

    StatusComponente(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
