package dominio;

import java.util.Random;

/** Régua/sensor ultrassônico que mede a altura da água no córrego, em metros. */
public class SensorNivelCorrego extends SensorSimulado {

    public SensorNivelCorrego(String id, Random random) {
        this(id, random, 1.0, 0.05, 0.05);
    }

    public SensorNivelCorrego(String id, Random random, double valorInicial,
                              double probabilidadeFalha, double probabilidadeLeituraEspuria) {
        super(id, TipoSensor.NIVEL_CORREGO, random, valorInicial, probabilidadeFalha, probabilidadeLeituraEspuria);
    }

    @Override
    protected double proximoValor(double valorAnterior, Random random) {
        double variacao = random.nextGaussian() * 0.35 + 0.05; // leve tendência de subida em época de chuva
        return Math.max(0.2, Math.min(6.0, valorAnterior + variacao));
    }

    /** Sensor ultrassônico com reflexo devolve uma altura absurda. */
    @Override
    protected double gerarLeituraEspuria(Random random) {
        return 99.9;
    }
}
