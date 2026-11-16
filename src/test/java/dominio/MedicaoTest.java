package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 10, 0);

    @Test
    void deveCriarMedicaoValidaDentroDaFaixa() throws DadosInvalidosException {
        // Arrange
        double chuva = 32.5;

        // Act
        Medicao medicao = new Medicao("PLU-01", TipoSensor.PLUVIOMETRO, chuva, AGORA);

        // Assert
        assertEquals("PLU-01", medicao.getSensorId());
        assertEquals(32.5, medicao.getValor());
        assertNull(medicao.getId(), "medição nova ainda não tem id do banco");
    }

    @Test
    void deveRejeitarChuvaNegativa() {
        // Arrange
        double chuvaNegativa = -3.0;

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("PLU-01", TipoSensor.PLUVIOMETRO, chuvaNegativa, AGORA));

        // Assert
        assertTrue(erro.getMessage().contains("negativa"), erro.getMessage());
        assertTrue(erro.getMessage().contains("PLU-01"), erro.getMessage());
    }

    @Test
    void deveRejeitarUmidadeAcimaDeCemPorCento() {
        // Arrange
        double umidadeImpossivel = 120.0;

        // Act + Assert
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("SOL-01", TipoSensor.UMIDADE_SOLO, umidadeImpossivel, AGORA));
        assertTrue(erro.getMessage().contains("fora da faixa"), erro.getMessage());
    }

    @Test
    void deveRejeitarMedicaoSemSensor() {
        // Arrange
        String sensorVazio = "  ";

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao(sensorVazio, TipoSensor.NIVEL_CORREGO, 1.0, AGORA));
    }

    @Test
    void comIdDevePreservarOsDadosEAtribuirId() throws DadosInvalidosException {
        // Arrange
        Medicao nova = new Medicao("NIV-01", TipoSensor.NIVEL_CORREGO, 2.4, AGORA);

        // Act
        Medicao gravada = nova.comId(42L);

        // Assert
        assertEquals(42L, gravada.getId());
        assertEquals(2.4, gravada.getValor());
        assertEquals(AGORA, gravada.getDataHora());
    }
}
