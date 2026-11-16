package excecao;

/**
 * Lançada quando alguém tenta alterar os limiares do território
 * sem informar o código de acesso correto.
 */
public class AcessoNegadoException extends Exception {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
