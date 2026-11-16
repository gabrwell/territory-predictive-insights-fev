package dominio;

import excecao.DadosInvalidosException;

import java.util.Optional;

/**
 * Os três limites de um tipo de sensor (ATENÇÃO &lt; ALERTA &lt; CRÍTICO).
 * Objeto imutável: para mudar um limiar, cria-se um novo Limiares (já validado).
 */
public final class Limiares {

    private final double atencao;
    private final double alerta;
    private final double critico;

    public Limiares(double atencao, double alerta, double critico) throws DadosInvalidosException {
        if (Double.isNaN(atencao) || Double.isNaN(alerta) || Double.isNaN(critico)) {
            throw new DadosInvalidosException("Limiar inválido: os valores precisam ser números.");
        }
        if (atencao < 0 || alerta < 0 || critico < 0) {
            throw new DadosInvalidosException("Limiar inválido: nenhum limite pode ser negativo.");
        }
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(String.format(
                    "Limiar inválido: é preciso ATENÇÃO < ALERTA < CRÍTICO (recebido %.1f / %.1f / %.1f).",
                    atencao, alerta, critico));
        }
        this.atencao = atencao;
        this.alerta = alerta;
        this.critico = critico;
    }

    /** Devolve o nível mais grave alcançado pelo valor, ou vazio se estiver abaixo de ATENÇÃO. */
    public Optional<NivelAlerta> classificar(double valor) {
        if (valor >= critico) {
            return Optional.of(NivelAlerta.CRITICO);
        }
        if (valor >= alerta) {
            return Optional.of(NivelAlerta.ALERTA);
        }
        if (valor >= atencao) {
            return Optional.of(NivelAlerta.ATENCAO);
        }
        return Optional.empty();
    }

    public double limiteDo(NivelAlerta nivel) {
        return switch (nivel) {
            case ATENCAO -> atencao;
            case ALERTA -> alerta;
            case CRITICO -> critico;
        };
    }

    public double getAtencao() {
        return atencao;
    }

    public double getAlerta() {
        return alerta;
    }

    public double getCritico() {
        return critico;
    }

    public String formatar(TipoSensor tipo) {
        return String.format("ATENÇÃO >= %s | ALERTA >= %s | CRÍTICO >= %s",
                tipo.formatar(atencao), tipo.formatar(alerta), tipo.formatar(critico));
    }
}
