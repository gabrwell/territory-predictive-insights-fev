package dominio;

import excecao.DadosInvalidosException;

import java.util.List;

/**
 * Previsão = média das últimas N leituras do banco.
 * Suaviza picos isolados: um valor alto sozinho não dispara alerta.
 */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private final int janela;

    public PrevisaoMediaMovel(int janela) {
        super("Média móvel (N=" + janela + ")", janela);
        this.janela = janela;
    }

    @Override
    public ResultadoPrevisao prever(List<Medicao> historico) throws DadosInvalidosException {
        ResultadoPrevisao base = super.prever(historico);
        if (!base.isConfiavel()) {
            return base;
        }
        int fim = historico.size();
        double media = media(historico, fim - janela, fim);
        TipoSensor tipo = historico.get(0).getTipo();
        String explicacao = String.format(
                "Média das últimas %d leituras gravadas: %s. É o nível esperado para as próximas horas "
                        + "se o padrão recente se mantiver.", janela, tipo.formatar(media));
        return new ResultadoPrevisao(getNome(), limitarAFaixa(historico, media), janela, true, explicacao);
    }

    public int getJanela() {
        return janela;
    }
}
