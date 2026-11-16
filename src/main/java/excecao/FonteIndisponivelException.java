package excecao;

/**
 * Lançada quando uma fonte de dados falha: sensor sem resposta,
 * sensor em manutenção ou banco de dados inacessível.
 */
public class FonteIndisponivelException extends Exception {

    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FonteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
