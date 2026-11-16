package servico;

import dominio.Alerta;
import dominio.Historicos;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.Sensor;
import dominio.TipoSensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa a lógica de alerta SEM banco: MedicaoDAO e AlertaDAO são mocks do Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoDAO medicaoDAO;
    @Mock
    private AlertaDAO alertaDAO;
    @Mock
    private Sensor pluviometro;

    private ServicoMonitoramento servico;

    @BeforeEach
    void preparar() {
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes(); // chuva: ATENÇÃO 10 | ALERTA 25 | CRÍTICO 50 ; janelas 5 e 3
        servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, configuracao, new FabricaPrevisao(configuracao));
        // lenient: nem todo teste chega a consultar id/tipo (ex.: quando o sensor falha antes)
        lenient().when(pluviometro.getId()).thenReturn("PLU-01");
        lenient().when(pluviometro.getTipo()).thenReturn(TipoSensor.PLUVIOMETRO);
    }

    @Test
    void chuvaForteNoHistoricoDeveGerarEGravarAlertaCritico() throws Exception {
        // Arrange: média das 5 últimas = 60 mm/h (acima do CRÍTICO de 50)
        List<Medicao> historico = Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 60, 60, 60, 60, 60, 60);
        when(medicaoDAO.buscarUltimasPorSensor(eq("PLU-01"), anyInt())).thenReturn(historico);
        when(alertaDAO.salvar(any(Alerta.class))).thenAnswer(inv -> ((Alerta) inv.getArgument(0)).comId(1L));

        // Act
        List<AnalisePrevisao> analises = servico.rodarPrevisoesEAlertas(pluviometro);

        // Assert: as duas estratégias preveem 60 -> 2 alertas CRÍTICOS gravados
        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaDAO, times(2)).salvar(captor.capture());
        Alerta primeiro = captor.getAllValues().get(0);
        assertEquals(NivelAlerta.CRITICO, primeiro.getNivel());
        assertEquals(60.0, primeiro.getValorPrevisto(), 0.0001);
        assertEquals(50.0, primeiro.getLimiteUltrapassado(), 0.0001);
        assertTrue(primeiro.getMotivo().contains("PLU-01"), "o alerta precisa dizer qual sensor");
        assertTrue(analises.get(0).getAlerta().isPresent());
    }

    @Test
    void chuvaFracaNaoDeveGerarAlerta() throws Exception {
        // Arrange: tudo abaixo de 10 mm/h
        List<Medicao> historico = Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 2, 3, 2, 3, 2, 3);
        when(medicaoDAO.buscarUltimasPorSensor(eq("PLU-01"), anyInt())).thenReturn(historico);

        // Act
        List<AnalisePrevisao> analises = servico.rodarPrevisoesEAlertas(pluviometro);

        // Assert
        verify(alertaDAO, never()).salvar(any());
        assertTrue(analises.stream().allMatch(a -> a.getAlerta().isEmpty()));
        assertTrue(analises.get(0).getSignificado().startsWith("Situação normal"));
    }

    @Test
    void poucasLeiturasNaoDevemGerarAlertaMesmoComValoresAltos() throws Exception {
        // Arrange: só 3 leituras (mínimo é 5): o sistema deve se calar
        List<Medicao> historico = Historicos.de("PLU-01", TipoSensor.PLUVIOMETRO, 90, 95, 100);
        when(medicaoDAO.buscarUltimasPorSensor(eq("PLU-01"), anyInt())).thenReturn(historico);

        // Act
        List<AnalisePrevisao> analises = servico.rodarPrevisoesEAlertas(pluviometro);

        // Assert
        verify(alertaDAO, never()).salvar(any());
        assertTrue(analises.stream().noneMatch(a -> a.getResultado().isConfiavel()));
    }

    @Test
    void leituraInvalidaDoSensorNaoDeveSerGravada() throws Exception {
        // Arrange: sensor devolve chuva negativa
        when(pluviometro.lerValor()).thenReturn(-5.0);

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> servico.registrarLeitura(pluviometro));
        verify(medicaoDAO, never()).salvar(any());
    }

    @Test
    void falhaDoSensorDevePropagarFonteIndisponivel() throws Exception {
        // Arrange
        when(pluviometro.lerValor()).thenThrow(new FonteIndisponivelException("sem resposta"));

        // Act + Assert
        assertThrows(FonteIndisponivelException.class, () -> servico.registrarLeitura(pluviometro));
        verify(medicaoDAO, never()).salvar(any());
    }

    @Test
    void leituraValidaDeveSerGravadaNoBanco() throws Exception {
        // Arrange
        when(pluviometro.lerValor()).thenReturn(12.5);
        when(medicaoDAO.salvar(any(Medicao.class))).thenAnswer(inv -> ((Medicao) inv.getArgument(0)).comId(7L));

        // Act
        Medicao gravada = servico.registrarLeitura(pluviometro);

        // Assert
        verify(medicaoDAO, times(1)).salvar(any(Medicao.class));
        assertEquals(7L, gravada.getId());
        assertEquals(12.5, gravada.getValor());
    }
}
