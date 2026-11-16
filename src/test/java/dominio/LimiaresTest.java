package dominio;

import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimiaresTest {

    @Test
    void deveClassificarCadaFaixaNoNivelCorreto() throws DadosInvalidosException {
        // Arrange
        Limiares limiares = new Limiares(10, 25, 50);

        // Act
        Optional<NivelAlerta> normal = limiares.classificar(9.9);
        Optional<NivelAlerta> atencao = limiares.classificar(10.0);
        Optional<NivelAlerta> alerta = limiares.classificar(30.0);
        Optional<NivelAlerta> critico = limiares.classificar(50.0);

        // Assert
        assertTrue(normal.isEmpty());
        assertEquals(NivelAlerta.ATENCAO, atencao.orElseThrow());
        assertEquals(NivelAlerta.ALERTA, alerta.orElseThrow());
        assertEquals(NivelAlerta.CRITICO, critico.orElseThrow());
    }

    @Test
    void deveRejeitarLimiaresForaDeOrdem() {
        // Arrange: ALERTA menor que ATENÇÃO
        double atencao = 30;
        double alerta = 20;
        double critico = 50;

        // Act + Assert
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Limiares(atencao, alerta, critico));
        assertTrue(erro.getMessage().contains("Limiar inválido"));
    }

    @Test
    void deveRejeitarLimiarNegativo() {
        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> new Limiares(-1, 5, 10));
    }
}
