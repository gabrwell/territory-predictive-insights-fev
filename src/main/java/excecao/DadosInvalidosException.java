package excecao;

/**
 * Lançada quando um dado do território não pode ser aceito:
 * medição fora da faixa física do sensor, população negativa,
 * limiar inválido, histórico inconsistente etc.
 */
public class DadosInvalidosException extends Exception {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
