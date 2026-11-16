package dominio;

import excecao.FonteIndisponivelException;

/**
 * Contrato de qualquer sensor do território.
 * Quem usa o sensor não precisa saber se é pluviômetro, régua de nível ou sensor de solo.
 */
public interface Sensor {

    String getId();

    /** Retorna o tipo do sensor (o que ele mede e em qual unidade). */
    TipoSensor getTipo();

    /** Lê o valor atual. Lança FonteIndisponivelException se o equipamento não responder. */
    double lerValor() throws FonteIndisponivelException;

    /** Verifica se o sensor está apto a fazer leituras. */
    boolean estaFuncionando();

    /** Coloca o sensor em manutenção (true) ou o devolve à operação (false). */
    void alterarManutencao(boolean emManutencao);
}
