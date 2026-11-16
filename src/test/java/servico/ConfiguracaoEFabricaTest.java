package servico;

import dominio.EstrategiaPrevisao;
import dominio.Limiares;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfiguracaoEFabricaTest {

    private ConfiguracaoSistema configuracao;

    @BeforeEach
    void preparar() {
        configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes();
    }

    @Test
    void singletonDeveDevolverSempreAMesmaInstancia() {
        // Act
        ConfiguracaoSistema outra = ConfiguracaoSistema.getInstancia();

        // Assert
        assertSame(configuracao, outra);
    }

    @Test
    void naoDeveAceitarLimiarAcimaDoQueOSensorConsegueMedir() throws DadosInvalidosException {
        // Arrange: umidade não passa de 100%
        Limiares impossiveis = new Limiares(90, 110, 130);

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> configuracao.definirLimiares(TipoSensor.UMIDADE_SOLO, impossiveis));
    }

    @Test
    void fabricaDeveCriarEstrategiasAtivasComJanelaDaConfiguracao() throws DadosInvalidosException {
        // Arrange
        configuracao.setJanelaMediaMovel(7);
        FabricaPrevisao fabrica = new FabricaPrevisao(configuracao);

        // Act
        List<EstrategiaPrevisao> estrategias = fabrica.criarEstrategiasAtivas();

        // Assert
        assertEquals(2, estrategias.size());
        PrevisaoMediaMovel media = assertInstanceOf(PrevisaoMediaMovel.class, estrategias.get(0));
        assertEquals(7, media.getJanela());
        assertInstanceOf(PrevisaoComparacao.class, estrategias.get(1));
    }

    @Test
    void fabricaDeveRespeitarEstrategiasAtivasConfiguradas() throws DadosInvalidosException {
        // Arrange
        configuracao.setEstrategiasAtivas(List.of(TipoEstrategia.COMPARACAO));
        FabricaPrevisao fabrica = new FabricaPrevisao(configuracao);

        // Act
        List<EstrategiaPrevisao> estrategias = fabrica.criarEstrategiasAtivas();

        // Assert
        assertEquals(1, estrategias.size());
        assertInstanceOf(PrevisaoComparacao.class, estrategias.get(0));
    }
}
