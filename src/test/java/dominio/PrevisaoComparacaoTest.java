package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoComparacaoTest {

    @Test
    void deveProjetarTendenciaDeAlta() throws DadosInvalidosException {
        // Arrange: período anterior média 10, atual média 20 -> projeção 20 + (20 - 10) = 30
        List<Medicao> historico = Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 10, 10, 10, 20, 20, 20);
        PrevisaoComparacao estrategia = new PrevisaoComparacao(3);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertTrue(resultado.isConfiavel());
        assertEquals(30.0, resultado.getValorPrevisto(), 0.0001);
        assertTrue(resultado.getExplicacao().contains("ALTA"), resultado.getExplicacao());
    }

    @Test
    void naoDevePreverValorNegativoNaQueda() throws DadosInvalidosException {
        // Arrange: anterior 30, atual 5 -> 5 + (5 - 30) = -20, que fisicamente vira 0 mm/h
        List<Medicao> historico = Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 30, 30, 30, 5, 5, 5);
        PrevisaoComparacao estrategia = new PrevisaoComparacao(3);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertEquals(0.0, resultado.getValorPrevisto(), 0.0001);
    }

    @Test
    void deveExigirDoisPeriodosCompletos() throws DadosInvalidosException {
        // Arrange: janela 3 exige 6 leituras; há só 5
        List<Medicao> historico = Historicos.de("NIV-01", TipoSensor.NIVEL_CORREGO, 1, 1, 1, 2, 2);
        PrevisaoComparacao estrategia = new PrevisaoComparacao(3);

        // Act
        ResultadoPrevisao resultado = estrategia.prever(historico);

        // Assert
        assertFalse(resultado.isConfiavel());
    }

    @Test
    void estrategiasDiferentesDevemDarResultadosDiferentesNoMesmoHistorico() throws DadosInvalidosException {
        // Arrange: polimorfismo - o mesmo histórico, duas formas de prever
        List<Medicao> historico = Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 10, 10, 10, 20, 20, 20);
        EstrategiaPrevisao media = new PrevisaoMediaMovel(5);
        EstrategiaPrevisao comparacao = new PrevisaoComparacao(3);

        // Act
        double previstoMedia = media.prever(historico).getValorPrevisto();
        double previstoComparacao = comparacao.prever(historico).getValorPrevisto();

        // Assert: média das 5 últimas = (10+10+20+20+20)/5 = 16; comparação = 30
        assertEquals(16.0, previstoMedia, 0.0001);
        assertNotEquals(previstoMedia, previstoComparacao);
    }
}
