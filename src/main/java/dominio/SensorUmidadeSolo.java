package dominio;

import java.util.Random;

/** Mede a saturação do solo (%). Solo encharcado não absorve mais chuva e aumenta o risco. */
public class SensorUmidadeSolo extends SensorSimulado {

    public SensorUmidadeSolo(String id, Random random) {
        this(id, random, 55.0, 0.05, 0.05);
    }

    public SensorUmidadeSolo(String id, Random random, double valorInicial,
                             double probabilidadeFalha, double probabilidadeLeituraEspuria) {
        super(id, TipoSensor.UMIDADE_SOLO, random, valorInicial, probabilidadeFalha, probabilidadeLeituraEspuria);
    }

    @Override
    protected double proximoValor(double valorAnterior, Random random) {
        double variacao = random.nextGaussian() * 4.0 + 1.0;
        return Math.max(20.0, Math.min(100.0, valorAnterior + variacao));
    }

    /** Defeito de calibração devolve saturação acima de 100%. */
    @Override
    protected double gerarLeituraEspuria(Random random) {
        return 100.0 + 5 + random.nextInt(40);
    }
}
