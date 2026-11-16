package dominio;

import excecao.DadosInvalidosException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Ponto vulnerável a alagamento (ex.: trecho baixo próximo a um córrego)
 * com um conjunto de sensores instalados.
 */
public class PontoMonitoramento extends ComponenteTerritorial {

    private final List<Sensor> sensores = new ArrayList<>();

    public PontoMonitoramento(String id, String nome, String regiao) throws DadosInvalidosException {
        super(id, nome, regiao);
    }

    public void adicionarSensor(Sensor sensor) throws DadosInvalidosException {
        if (sensor == null) {
            throw new DadosInvalidosException("Não é possível instalar um sensor nulo no ponto " + getId() + ".");
        }
        if (buscarSensor(sensor.getId()).isPresent()) {
            throw new DadosInvalidosException("Já existe um sensor com id " + sensor.getId()
                    + " no ponto " + getId() + ".");
        }
        sensores.add(sensor);
    }

    public Optional<Sensor> buscarSensor(String sensorId) {
        return sensores.stream().filter(s -> s.getId().equalsIgnoreCase(sensorId)).findFirst();
    }

    public List<Sensor> getSensores() {
        return Collections.unmodifiableList(sensores);
    }

    @Override
    public StatusComponente verificarStatus() {
        long funcionando = sensores.stream().filter(Sensor::estaFuncionando).count();
        StatusComponente novoStatus;
        if (sensores.isEmpty() || funcionando == 0) {
            novoStatus = StatusComponente.INOPERANTE;
        } else if (funcionando == sensores.size()) {
            novoStatus = StatusComponente.OPERANTE;
        } else {
            novoStatus = StatusComponente.DEGRADADO;
        }
        atualizarStatus(novoStatus);
        return novoStatus;
    }
}
