package dominio;

import excecao.DadosInvalidosException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Utilitário dos testes: cria um histórico em ordem cronológica a partir de uma lista de valores. */
public final class Historicos {

    private Historicos() {
    }

    public static List<Medicao> de(String sensorId, TipoSensor tipo, double... valores) throws DadosInvalidosException {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 16, 8, 0);
        List<Medicao> historico = new ArrayList<>();
        for (int i = 0; i < valores.length; i++) {
            historico.add(new Medicao((long) i + 1, sensorId, tipo, valores[i], inicio.plusMinutes(10L * i)));
        }
        return historico;
    }
}
