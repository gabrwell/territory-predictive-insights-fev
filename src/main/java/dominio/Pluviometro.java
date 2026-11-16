package dominio;

import java.util.Random;

/** Mede a intensidade da chuva em mm/h. */
public class Pluviometro extends SensorSimulado {

    public Pluviometro(String id, Random random) {
        this(id, random, 5.0, 0.05, 0.05);
    }

    public Pluviometro(String id, Random random, double valorInicial,
                       double probabilidadeFalha, double probabilidadeLeituraEspuria) {
        super(id, TipoSensor.PLUVIOMETRO, random, valorInicial, probabilidadeFalha, probabilidadeLeituraEspuria);
    }

    @Override
    protected double proximoValor(double valorAnterior, Random random) {
        double variacao = random.nextGaussian() * 6.0;
        if (random.nextDouble() < 0.15) {
            variacao += 15.0; // pancada de chuva repentina
        }
        return Math.max(0.0, Math.min(120.0, valorAnterior + variacao));
    }
}
