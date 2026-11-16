package dominio;

import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SensorTest {

    @Test
    void sensoresSaudaveisDevemLerValoresDentroDaFaixaFisica() throws FonteIndisponivelException {
        // Arrange: sem falhas nem leituras espúrias, semente fixa
        Random random = new Random(42);
        List<Sensor> sensores = List.of(
                new Pluviometro("PLU-01", random, 5, 0.0, 0.0),
                new SensorNivelCorrego("NIV-01", random, 1, 0.0, 0.0),
                new SensorUmidadeSolo("SOL-01", random, 50, 0.0, 0.0));

        // Act + Assert
        for (Sensor sensor : sensores) {
            for (int i = 0; i < 200; i++) {
                double valor = sensor.lerValor();
                assertTrue(sensor.getTipo().aceita(valor), sensor.getId() + " leu valor impossível: " + valor);
            }
        }
    }

    @Test
    void sensorComFalhaDeveLancarFonteIndisponivel() {
        // Arrange: probabilidade de falha = 100%
        Sensor sensor = new Pluviometro("PLU-01", new Random(1), 5, 1.0, 0.0);

        // Act + Assert
        FonteIndisponivelException erro = assertThrows(FonteIndisponivelException.class, sensor::lerValor);
        assertTrue(erro.getMessage().contains("PLU-01"));
    }

    @Test
    void sensorEmManutencaoNaoFuncionaENaoLe() {
        // Arrange
        Sensor sensor = new SensorNivelCorrego("NIV-01", new Random(1), 1, 0.0, 0.0);

        // Act
        sensor.alterarManutencao(true);

        // Assert
        assertFalse(sensor.estaFuncionando());
        assertThrows(FonteIndisponivelException.class, sensor::lerValor);
    }

    @Test
    void leituraEspuriaDoSensorDeveSerBarradaPelaMedicao() throws FonteIndisponivelException {
        // Arrange: sensor de solo que SEMPRE devolve leitura espúria (> 100%)
        Sensor sensor = new SensorUmidadeSolo("SOL-01", new Random(7), 50, 0.0, 1.0);
        double valorComDefeito = sensor.lerValor();

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> new Medicao(sensor.getId(), sensor.getTipo(),
                valorComDefeito, java.time.LocalDateTime.now()));
    }

    @Test
    void pontoDeveFicarDegradadoQuandoParteDosSensoresPara() throws DadosInvalidosException {
        // Arrange
        PontoMonitoramento ponto = new PontoMonitoramento("PT-01", "Ponto teste", "Zona Leste");
        Sensor chuva = new Pluviometro("PLU-01", new Random(1));
        Sensor nivel = new SensorNivelCorrego("NIV-01", new Random(1));
        ponto.adicionarSensor(chuva);
        ponto.adicionarSensor(nivel);

        // Act
        nivel.alterarManutencao(true);
        StatusComponente status = ponto.verificarStatus();

        // Assert
        assertEquals(StatusComponente.DEGRADADO, status);
        assertEquals(StatusComponente.DEGRADADO, ponto.getStatus());
    }

    @Test
    void naoDevePermitirDoisSensoresComMesmoId() throws DadosInvalidosException {
        // Arrange
        PontoMonitoramento ponto = new PontoMonitoramento("PT-01", "Ponto teste", "Zona Leste");
        ponto.adicionarSensor(new Pluviometro("PLU-01", new Random(1)));

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> ponto.adicionarSensor(new Pluviometro("PLU-01", new Random(2))));
    }
}
