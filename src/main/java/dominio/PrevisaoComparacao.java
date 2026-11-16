package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Compara o período atual (últimas N leituras) com o período anterior (N leituras antes delas)
 * e projeta a tendência: previsão = média atual + (média atual - média anterior).
 *
 * Diferente da média móvel, enxerga ACELERAÇÃO: se a chuva está aumentando,
 * a previsão fica acima da média atual.
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    private final int janela;

    public PrevisaoComparacao(int janela) {
        super("Comparação com período anterior (N=" + janela + ")", janela * 2);
        this.janela = janela;
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException {
        ResultadoPrevisao base = super.prever(historico);
        if (!base.isConfiavel()) {
            return base;
        }
        int fim = historico.size();
        double mediaAtual = media(historico, fim - janela, fim);
        double mediaAnterior = media(historico, fim - 2 * janela, fim - janela);
        double variacao = mediaAtual - mediaAnterior;
        double previsto = limitarAFaixa(historico, mediaAtual + variacao);

        TipoSensor tipo = historico.get(0).getTipo();
        String explicacao = String.format(
                "Período atual (média %s) vs. anterior (média %s): %s. Projetando a mesma variação, "
                        + "o próximo período deve ficar em torno de %s.",
                tipo.formatar(mediaAtual), tipo.formatar(mediaAnterior),
                descreverTendencia(variacao, mediaAnterior, tipo), tipo.formatar(previsto));
        return new ResultadoPrevisao(getNome(), previsto, janela * 2, true, explicacao);
    }

    private static String descreverTendencia(double variacao, double mediaAnterior, TipoSensor tipo) {
        String direcao = variacao > 0 ? "tendência de ALTA" : variacao < 0 ? "tendência de QUEDA" : "estável";
        if (mediaAnterior == 0.0) {
            return direcao + " (" + tipo.formatar(Math.abs(variacao)) + ")";
        }
        double percentual = Math.abs(variacao) / mediaAnterior * 100.0;
        return String.format("%s de %.0f%%", direcao, percentual);
    }

    public int getJanela() {
        return janela;
    }
}
