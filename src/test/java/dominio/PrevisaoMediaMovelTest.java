package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoMediaMovelTest {

    @Test
    void deveCalcularMediaDasUltimasNLeituras() throws DadosInvalidosException {
        // Arrange: 7 leituras, a janela pega só as 5 últimas (20, 30, 40, 50, 60)
        List<Medicao> historico = Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 1, 2, 20, 30, 40, 50, 60);
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(5);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertTrue(resultado.isConfiavel());
        assertEquals(40.0, resultado.getValorPrevisto(), 0.0001);
        assertEquals(5, resultado.getLeiturasUsadas());
    }

    @Test
    void deveSeCalarComMenosLeiturasQueAJanela() throws DadosInvalidosException {
        // Arrange
        List<Medicao> historico = Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 80, 90, 95);
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(5);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertFalse(resultado.isConfiavel(), "com 3 de 5 leituras a previsão não deve ser confiável");
        assertTrue(Double.isNaN(resultado.getValorPrevisto()));
        assertTrue(resultado.getExplicacao().contains("3 leitura(s)"));
    }

    @Test
    void deveRejeitarHistoricoQueMisturaSensores() throws DadosInvalidosException {
        // Arrange
        List<Medicao> historico = new ArrayList<>(Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 10, 10, 10, 10));
        historico.addAll(Historicos.de("PLU-02", TipoSensor.PLUVIOMETRO, 10));
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(5);

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> estrategia.prever(historico));
    }

    @Test
    void deveRejeitarHistoricoNulo() {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(5);

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> estrategia.prever(null));
    }
}
