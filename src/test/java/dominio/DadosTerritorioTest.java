package dominio;

import excecao.AcessoNegadoException;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servico.ConfiguracaoSistema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DadosTerritorioTest {

    private ConfiguracaoSistema configuracao;
    private DadosTerritorio territorio;

    @BeforeEach
    void preparar() throws DadosInvalidosException {
        configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes();
        territorio = new DadosTerritorio("Bairro Teste", "Zona Leste", 1000, 50, "1234", configuracao);
    }

    @Test
    void codigoErradoNaoDeveAlterarLimiares() throws DadosInvalidosException {
        // Arrange
        Limiares originais = configuracao.getLimiares(TipoSensor.PLUVIOMETRO);
        Limiares novos = new Limiares(1, 2, 3);

        // Act
        assertThrows(AcessoNegadoException.class,
                () -> territorio.alterarLimiares("0000", TipoSensor.PLUVIOMETRO, novos));

        // Assert
        assertEquals(originais.getCritico(), configuracao.getLimiares(TipoSensor.PLUVIOMETRO).getCritico());
    }

    @Test
    void codigoCorretoDeveAlterarLimiaresNaConfiguracao() throws Exception {
        // Arrange
        Limiares novos = new Limiares(5, 15, 30);

        // Act
        territorio.alterarLimiares("1234", TipoSensor.PLUVIOMETRO, novos);

        // Assert
        assertEquals(30.0, configuracao.getLimiares(TipoSensor.PLUVIOMETRO).getCritico());
    }

    @Test
    void naoDeveAceitarPopulacaoNegativa() {
        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> territorio.setPopulacaoEstimada(-10));
        assertEquals(1000, territorio.getPopulacaoEstimada());
    }
}
