package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Base de todas as estratégias de previsão.
 *
 * O prever() desta classe faz o trabalho comum a todas:
 *  1. valida o histórico (não nulo, de um único sensor);
 *  2. "cala" o sistema se houver menos leituras que o mínimo;
 *  3. devolve a previsão mais ingênua possível (persistência: "vai continuar como está agora").
 *
 * As filhas sobrescrevem prever(), chamam super.prever() para reaproveitar
 * essa validação e refinam o cálculo.
 */
public abstract class EstrategiaPrevisao {

    private final String nome;
    private final int minimoLeituras;

    protected EstrategiaPrevisao(String nome, int minimoLeituras) {
        if (minimoLeituras < 1) {
            throw new IllegalArgumentException("Uma estratégia precisa de pelo menos 1 leitura.");
        }
        this.nome = nome;
        this.minimoLeituras = minimoLeituras;
    }

    /**
     * @param historico leituras de UM sensor em ordem cronológica (mais antiga primeiro)
     */
    public ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException {
        if (historico == null) {
            throw new DadosInvalidosException("Histórico nulo: não há como prever sem dados.");
        }
        validarSensorUnico(historico);
        if (historico.size() < minimoLeituras) {
            return ResultadoPrevisao.insuficiente(nome, historico.size(), minimoLeituras);
        }
        Medicao ultima = historico.get(historico.size() - 1);
        return new ResultadoPrevisao(nome, ultima.getValor(), 1, true,
                "Persistência: o próximo valor tende a repetir a última leitura.");
    }

    private static void validarSensorUnico(List<Medicao> historico) throws DadosInvalidosException {
        if (historico.isEmpty()) {
            return;
        }
        String sensor = historico.get(0).getSensorId();
        for (Medicao m : historico) {
            if (!m.getSensorId().equals(sensor)) {
                throw new DadosInvalidosException("Histórico inconsistente: mistura os sensores "
                        + sensor + " e " + m.getSensorId() + ".");
            }
        }
    }

    /** Média dos valores entre os índices [inicio, fim). */
    protected static double media(List<Medicao> historico, int inicio, int fim) {
        double soma = 0.0;
        for (int i = inicio; i < fim; i++) {
            soma += historico.get(i).getValor();
        }
        return soma / (fim - inicio);
    }

    /** Garante que a previsão respeite a faixa física do sensor (ex.: umidade nunca passa de 100%). */
    protected static double limitarAFaixa(List<Medicao> historico, double valor) {
        return historico.get(0).getTipo().limitarAFaixa(valor);
    }

    public String getNome() {
        return nome;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }
}
