package dominio;

import excecao.FonteIndisponivelException;

import java.util.Objects;
import java.util.Random;

/**
 * Base dos sensores simulados. Concentra o que é comum (falha de comunicação,
 * manutenção, leitura espúria) e deixa cada filho definir como o valor evolui.
 *
 * A simulação é um "passeio aleatório": cada leitura parte da anterior,
 * então há tendências de subida/descida, como na vida real.
 */
public abstract class SensorSimulado implements Sensor {

    private final String id;
    private final TipoSensor tipo;
    private final Random random;
    private final double probabilidadeFalha;
    private final double probabilidadeLeituraEspuria;
    private double valorAtual;
    private boolean emManutencao;

    protected SensorSimulado(String id, TipoSensor tipo, Random random, double valorInicial,
                             double probabilidadeFalha, double probabilidadeLeituraEspuria) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("O sensor precisa de um id.");
        }
        this.id = id;
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.random = Objects.requireNonNull(random, "random");
        this.valorAtual = tipo.limitarAFaixa(valorInicial);
        this.probabilidadeFalha = probabilidadeFalha;
        this.probabilidadeLeituraEspuria = probabilidadeLeituraEspuria;
    }

    @Override
    public final double lerValor() throws FonteIndisponivelException {
        if (emManutencao) {
            throw new FonteIndisponivelException("Sensor " + id + " (" + tipo.getDescricao()
                    + ") está em manutenção e não pode ser lido.");
        }
        if (random.nextDouble() < probabilidadeFalha) {
            throw new FonteIndisponivelException("Falha de comunicação com o sensor " + id + " ("
                    + tipo.getDescricao() + "): o equipamento não respondeu.");
        }
        if (random.nextDouble() < probabilidadeLeituraEspuria) {
            // Ruído elétrico/reflexo: o equipamento devolve um número impossível.
            // NÃO tratamos aqui: quem valida é a Medicao (e o valor é rejeitado).
            return gerarLeituraEspuria(random);
        }
        valorAtual = tipo.limitarAFaixa(proximoValor(valorAtual, random));
        return arredondar(valorAtual);
    }

    /** Calcula o próximo valor a partir do anterior (comportamento específico de cada sensor). */
    protected abstract double proximoValor(double valorAnterior, Random random);

    /** Valor impossível que o equipamento pode devolver com defeito. Padrão: negativo. */
    protected double gerarLeituraEspuria(Random random) {
        return -(1 + random.nextInt(20));
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TipoSensor getTipo() {
        return tipo;
    }

    @Override
    public boolean estaFuncionando() {
        return !emManutencao;
    }

    @Override
    public void alterarManutencao(boolean emManutencao) {
        this.emManutencao = emManutencao;
    }

    @Override
    public String toString() {
        return String.format("%s - %s [%s]", id, tipo.getDescricao(),
                emManutencao ? "EM MANUTENÇÃO" : "funcionando");
    }
}
