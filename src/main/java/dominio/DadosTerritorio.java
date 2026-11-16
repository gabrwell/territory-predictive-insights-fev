package dominio;

import excecao.AcessoNegadoException;
import excecao.DadosInvalidosException;
import servico.ConfiguracaoSistema;

import java.util.Objects;

/**
 * Dados gerais do território monitorado. Todos os atributos são privados e
 * só mudam por métodos que validam o valor. Os limiares só podem ser alterados
 * por quem conhece o código de acesso da coordenação.
 *
 * O território é descrito por região e equipamentos, nunca por pessoas.
 */
public class DadosTerritorio {

    private String nomeTerritorio;
    private String regiao;
    private int populacaoEstimada;
    private int domiciliosEmAreaDeRisco;
    private final String codigoAcesso;
    private final ConfiguracaoSistema configuracao;

    public DadosTerritorio(String nomeTerritorio, String regiao, int populacaoEstimada,
                           int domiciliosEmAreaDeRisco, String codigoAcesso,
                           ConfiguracaoSistema configuracao) throws DadosInvalidosException {
        if (codigoAcesso == null || codigoAcesso.length() < 4) {
            throw new DadosInvalidosException("O código de acesso precisa ter pelo menos 4 caracteres.");
        }
        this.codigoAcesso = codigoAcesso;
        this.configuracao = Objects.requireNonNull(configuracao, "configuracao");
        setNomeTerritorio(nomeTerritorio);
        setRegiao(regiao);
        setPopulacaoEstimada(populacaoEstimada);
        setDomiciliosEmAreaDeRisco(domiciliosEmAreaDeRisco);
    }

    /**
     * Altera os limiares de um tipo de sensor. Exige o código de acesso.
     * Os limiares continuam morando no ConfiguracaoSistema (fonte única);
     * esta classe é a "porta trancada" para alterá-los.
     */
    public void alterarLimiares(String codigoInformado, TipoSensor tipo, Limiares novosLimiares)
            throws AcessoNegadoException, DadosInvalidosException {
        if (!codigoAcesso.equals(codigoInformado)) {
            throw new AcessoNegadoException("Código de acesso incorreto: os limiares de "
                    + tipo.getDescricaoMinuscula() + " NÃO foram alterados.");
        }
        configuracao.definirLimiares(tipo, novosLimiares);
    }

    public Limiares consultarLimiares(TipoSensor tipo) {
        return configuracao.getLimiares(tipo);
    }

    public String getNomeTerritorio() {
        return nomeTerritorio;
    }

    public void setNomeTerritorio(String nomeTerritorio) throws DadosInvalidosException {
        if (nomeTerritorio == null || nomeTerritorio.isBlank()) {
            throw new DadosInvalidosException("O nome do território não pode ficar vazio.");
        }
        this.nomeTerritorio = nomeTerritorio.trim();
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) throws DadosInvalidosException {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("A região do território não pode ficar vazia.");
        }
        this.regiao = regiao.trim();
    }

    public int getPopulacaoEstimada() {
        return populacaoEstimada;
    }

    public void setPopulacaoEstimada(int populacaoEstimada) throws DadosInvalidosException {
        if (populacaoEstimada < 0) {
            throw new DadosInvalidosException("População negativa (" + populacaoEstimada
                    + ") não é um valor válido para o território.");
        }
        this.populacaoEstimada = populacaoEstimada;
    }

    public int getDomiciliosEmAreaDeRisco() {
        return domiciliosEmAreaDeRisco;
    }

    public void setDomiciliosEmAreaDeRisco(int domiciliosEmAreaDeRisco) throws DadosInvalidosException {
        if (domiciliosEmAreaDeRisco < 0) {
            throw new DadosInvalidosException("Número de domicílios em área de risco não pode ser negativo ("
                    + domiciliosEmAreaDeRisco + ").");
        }
        this.domiciliosEmAreaDeRisco = domiciliosEmAreaDeRisco;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) | população estimada: %d | domicílios em área de risco: %d",
                nomeTerritorio, regiao, populacaoEstimada, domiciliosEmAreaDeRisco);
    }
}
