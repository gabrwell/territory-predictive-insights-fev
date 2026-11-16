package servico;

import dominio.EstrategiaPrevisao;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * FACTORY: cria as estratégias de previsão a partir da configuração.
 *
 * Por que Factory aqui: cada estratégia precisa de parâmetros que vêm do
 * ConfiguracaoSistema (tamanho da janela). Sem a fábrica, o serviço teria
 * "if (tipo == MEDIA_MOVEL) new ... else if ..." espalhado. Com ela, a escolha
 * é uma consulta num mapa: para criar uma estratégia nova basta registrar uma
 * linha no construtor, sem mexer no serviço.
 */
public class FabricaPrevisao {

    private final ConfiguracaoSistema configuracao;
    private final Map<TipoEstrategia, Supplier<EstrategiaPrevisao>> criadores = new EnumMap<>(TipoEstrategia.class);

    public FabricaPrevisao(ConfiguracaoSistema configuracao) {
        this.configuracao = configuracao;
        criadores.put(TipoEstrategia.MEDIA_MOVEL,
                () -> new PrevisaoMediaMovel(configuracao.getJanelaMediaMovel()));
        criadores.put(TipoEstrategia.COMPARACAO,
                () -> new PrevisaoComparacao(configuracao.getJanelaComparacao()));
    }

    public EstrategiaPrevisao criar(TipoEstrategia tipo) {
        Supplier<EstrategiaPrevisao> criador = criadores.get(tipo);
        if (criador == null) {
            throw new IllegalArgumentException("Nenhuma estratégia registrada para " + tipo + ".");
        }
        return criador.get();
    }

    /** Cria as estratégias que estão ativas na configuração, na ordem configurada. */
    public List<EstrategiaPrevisao> criarEstrategiasAtivas() {
        return configuracao.getEstrategiasAtivas().stream().map(this::criar).toList();
    }
}
