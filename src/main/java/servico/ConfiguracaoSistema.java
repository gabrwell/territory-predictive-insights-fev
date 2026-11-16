package servico;

import dominio.Limiares;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * SINGLETON: guarda a configuração do sistema (limiares, janelas das previsões,
 * estratégias ativas e endereço do banco).
 *
 * Por que Singleton aqui: os limiares precisam ser UMA verdade só. Se o menu alterasse
 * uma cópia e o serviço de alertas lesse outra, o sistema avisaria com base em limites velhos.
 * O construtor é privado: a única forma de obter a configuração é getInstancia().
 */
public final class ConfiguracaoSistema {

    private static ConfiguracaoSistema instancia;

    private final Map<TipoSensor, Limiares> limiares = new EnumMap<>(TipoSensor.class);
    private int janelaMediaMovel;
    private int janelaComparacao;
    private List<TipoEstrategia> estrategiasAtivas;
    private String urlBanco;

    private ConfiguracaoSistema() {
        restaurarPadroes();
    }

    public static synchronized ConfiguracaoSistema getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracaoSistema();
        }
        return instancia;
    }

    /** Volta tudo aos valores de fábrica (também usado pelos testes para isolar cada caso). */
    public void restaurarPadroes() {
        // Referências aproximadas de intensidade de chuva: >10 mm/h moderada, >25 forte, >50 muito forte.
        limiares.put(TipoSensor.PLUVIOMETRO, limiarPadrao(10.0, 25.0, 50.0));
        // Altura da água no córrego (m) em relação ao leito; a margem transborda por volta de 4 m.
        limiares.put(TipoSensor.NIVEL_CORREGO, limiarPadrao(2.0, 3.0, 4.0));
        // Saturação do solo (%): acima de 85% quase toda chuva vira enxurrada.
        limiares.put(TipoSensor.UMIDADE_SOLO, limiarPadrao(70.0, 85.0, 95.0));
        janelaMediaMovel = 5;
        janelaComparacao = 3;
        estrategiasAtivas = List.of(TipoEstrategia.MEDIA_MOVEL, TipoEstrategia.COMPARACAO);
        urlBanco = "jdbc:h2:./dados/territorio";
    }

    private static Limiares limiarPadrao(double atencao, double alerta, double critico) {
        try {
            return new Limiares(atencao, alerta, critico);
        } catch (DadosInvalidosException e) {
            // Só acontece se alguém digitar um padrão errado no código: falha alto, não engole.
            throw new IllegalStateException("Limiares padrão inválidos: " + e.getMessage(), e);
        }
    }

    public Limiares getLimiares(TipoSensor tipo) {
        return limiares.get(tipo);
    }

    public void definirLimiares(TipoSensor tipo, Limiares novos) throws DadosInvalidosException {
        if (tipo == null || novos == null) {
            throw new DadosInvalidosException("Limiar inválido: tipo de sensor e limiares são obrigatórios.");
        }
        if (novos.getCritico() > tipo.getValorMaximo()) {
            throw new DadosInvalidosException(String.format(
                    "Limiar inválido: CRÍTICO (%s) acima do máximo que o sensor de %s consegue medir (%s).",
                    tipo.formatar(novos.getCritico()), tipo.getDescricaoMinuscula(),
                    tipo.formatar(tipo.getValorMaximo())));
        }
        limiares.put(tipo, novos);
    }

    public int getJanelaMediaMovel() {
        return janelaMediaMovel;
    }

    public void setJanelaMediaMovel(int janela) throws DadosInvalidosException {
        validarJanela(janela);
        this.janelaMediaMovel = janela;
    }

    public int getJanelaComparacao() {
        return janelaComparacao;
    }

    public void setJanelaComparacao(int janela) throws DadosInvalidosException {
        validarJanela(janela);
        this.janelaComparacao = janela;
    }

    private static void validarJanela(int janela) throws DadosInvalidosException {
        if (janela < 2 || janela > 50) {
            throw new DadosInvalidosException("Janela de previsão inválida (" + janela + "): use de 2 a 50 leituras.");
        }
    }

    /** Quantas leituras buscar no banco para que TODAS as estratégias tenham dados suficientes. */
    public int getQuantidadeHistoricoParaPrevisao() {
        return Math.max(janelaMediaMovel, janelaComparacao * 2);
    }

    public List<TipoEstrategia> getEstrategiasAtivas() {
        return estrategiasAtivas;
    }

    public void setEstrategiasAtivas(List<TipoEstrategia> estrategias) throws DadosInvalidosException {
        if (estrategias == null || estrategias.isEmpty()) {
            throw new DadosInvalidosException("É preciso manter pelo menos uma estratégia de previsão ativa.");
        }
        this.estrategiasAtivas = List.copyOf(estrategias);
    }

    public String getUrlBanco() {
        return urlBanco;
    }
}
