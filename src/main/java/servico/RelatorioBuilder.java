package servico;

import dominio.Alerta;
import dominio.ComponenteTerritorial;
import dominio.DadosTerritorio;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.Sensor;
import dominio.StatusComponente;
import dominio.TipoSensor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * BUILDER: monta o relatório de status completo passo a passo.
 *
 * Por que Builder aqui: o relatório tem muitas partes opcionais (componentes,
 * sensores, previsões, alertas). Um construtor com 7 parâmetros seria ilegível e
 * obrigaria a passar null para o que não existe. Com o builder, o menu adiciona só
 * o que tem e chama construir() no fim.
 */
public class RelatorioBuilder {

    private static final String LINHA = "=".repeat(78);
    private static final String NL = System.lineSeparator();

    private DadosTerritorio territorio;
    private final List<String> componentes = new ArrayList<>();
    private final List<String> sensores = new ArrayList<>();
    private final List<AnalisePrevisao> analises = new ArrayList<>();
    private final List<Alerta> alertasRecentes = new ArrayList<>();
    private int totalMedicoes = -1;
    private LocalDateTime geradoEm = LocalDateTime.now();

    public RelatorioBuilder comTerritorio(DadosTerritorio territorio) {
        this.territorio = territorio;
        return this;
    }

    public RelatorioBuilder comComponente(ComponenteTerritorial componente) {
        StatusComponente status = componente.verificarStatus();
        componentes.add(String.format("%s - %s (%s): %s", componente.getId(), componente.getNome(),
                componente.getRegiao(), status.getDescricao()));
        return this;
    }

    public RelatorioBuilder comSensor(Sensor sensor, int leiturasNoBanco) {
        sensores.add(String.format("%-7s %-18s %-14s %d leitura(s) no banco", sensor.getId(),
                sensor.getTipo().getDescricao(), sensor.estaFuncionando() ? "funcionando" : "EM MANUTENÇÃO",
                leiturasNoBanco));
        return this;
    }

    public RelatorioBuilder comAnalises(List<AnalisePrevisao> novasAnalises) {
        analises.addAll(novasAnalises);
        return this;
    }

    public RelatorioBuilder comAlertasRecentes(List<Alerta> alertas) {
        alertasRecentes.addAll(alertas);
        return this;
    }

    public RelatorioBuilder comTotalMedicoes(int total) {
        this.totalMedicoes = total;
        return this;
    }

    public RelatorioBuilder geradoEm(LocalDateTime momento) {
        this.geradoEm = momento;
        return this;
    }

    public Relatorio construir() {
        if (territorio == null) {
            throw new IllegalStateException("O relatório precisa dos dados do território (chame comTerritorio).");
        }
        NivelAlerta maisGrave = analises.stream()
                .flatMap(a -> a.getAlerta().stream())
                .map(Alerta::getNivel)
                .max(Comparator.naturalOrder())
                .orElse(null);

        StringBuilder sb = new StringBuilder();
        sb.append(LINHA).append(NL)
          .append(" STATUS COMPLETO - ").append(territorio.getNomeTerritorio()).append(NL)
          .append(" Gerado em ").append(geradoEm.format(Medicao.FORMATO_DATA)).append(NL)
          .append(LINHA).append(NL);

        sb.append("Território: ").append(territorio).append(NL);
        if (totalMedicoes >= 0) {
            sb.append("Medições no banco: ").append(totalMedicoes).append(NL);
        }
        sb.append(NL).append("Limiares em vigor:").append(NL);
        for (TipoSensor tipo : TipoSensor.values()) {
            sb.append(String.format("  %-18s %s%n", tipo.getDescricao(),
                    territorio.consultarLimiares(tipo).formatar(tipo)));
        }

        secao(sb, "Pontos monitorados", componentes);
        secao(sb, "Sensores", sensores);

        sb.append(NL).append("Previsões atuais:").append(NL);
        if (analises.isEmpty()) {
            sb.append("  (nenhuma previsão calculada)").append(NL);
        }
        for (AnalisePrevisao analise : analises) {
            sb.append(analise.formatar()).append(NL);
        }

        sb.append(NL).append("Últimos alertas registrados:").append(NL);
        if (alertasRecentes.isEmpty()) {
            sb.append("  (nenhum alerta emitido até agora)").append(NL);
        }
        for (Alerta alerta : alertasRecentes) {
            sb.append("  ").append(alerta).append(NL);
        }

        sb.append(NL).append("SITUAÇÃO GERAL: ")
          .append(maisGrave == null ? "sem alerta previsto no momento." : maisGrave.getRotulo()
                  + " - " + maisGrave.getRecomendacao())
          .append(NL).append(LINHA);

        return new Relatorio(geradoEm, maisGrave, sb.toString());
    }

    private static void secao(StringBuilder sb, String titulo, List<String> linhas) {
        if (linhas.isEmpty()) {
            return;
        }
        sb.append(NL).append(titulo).append(':').append(NL);
        for (String linha : linhas) {
            sb.append("  ").append(linha).append(NL);
        }
    }
}
