package dominio;

import excecao.DadosInvalidosException;

/**
 * Qualquer coisa do território que pode ser monitorada (um ponto de alagamento,
 * uma estação, um equipamento público). Cada tipo concreto decide como
 * verificar o próprio status.
 */
public abstract class ComponenteTerritorial {

    private final String id;
    private String nome;
    private String regiao;
    private StatusComponente status;

    protected ComponenteTerritorial(String id, String nome, String regiao) throws DadosInvalidosException {
        if (id == null || id.isBlank()) {
            throw new DadosInvalidosException("Componente territorial sem identificador.");
        }
        this.id = id;
        setNome(nome);
        setRegiao(regiao);
        this.status = StatusComponente.NAO_VERIFICADO;
    }

    /** Verifica e atualiza o status do componente. */
    public abstract StatusComponente verificarStatus();

    protected void atualizarStatus(StatusComponente novoStatus) {
        this.status = novoStatus;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) throws DadosInvalidosException {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("O nome do componente " + id + " não pode ficar vazio.");
        }
        this.nome = nome.trim();
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("A região do componente " + id + " não pode ficar vazia.");
        }
        this.regiao = regiao.trim();
    }

    public StatusComponente getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s) | status: %s", id, nome, regiao, status.getDescricao());
    }
}
