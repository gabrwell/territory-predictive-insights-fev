import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Limiares;
import dominio.Medicao;
import dominio.Pluviometro;
import dominio.PontoMonitoramento;
import dominio.Sensor;
import dominio.SensorNivelCorrego;
import dominio.SensorUmidadeSolo;
import dominio.StatusComponente;
import dominio.TipoSensor;
import excecao.AcessoNegadoException;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.ConexaoBanco;
import persistencia.MedicaoDAO;
import servico.AnalisePrevisao;
import servico.ConfiguracaoSistema;
import servico.ExportadorCsv;
import servico.FabricaPrevisao;
import servico.RelatorioBuilder;
import servico.ServicoMonitoramento;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * Ponto de entrada: monta os objetos e roda o menu de console.
 * Todos os erros esperados são capturados aqui e viram mensagens claras para o usuário.
 */
public class SistemaMonitoramento {

    /** Código da coordenação para alterar limiares (fictício, apenas para a demonstração). */
    private static final String CODIGO_ACESSO = "1234";

    private final Scanner scanner;
    private final ServicoMonitoramento servico;
    private final PontoMonitoramento ponto;
    private final DadosTerritorio territorio;
    private final ExportadorCsv exportadorCsv = new ExportadorCsv();

    public SistemaMonitoramento(Scanner scanner, ServicoMonitoramento servico,
                                PontoMonitoramento ponto, DadosTerritorio territorio) {
        this.scanner = scanner;
        this.servico = servico;
        this.ponto = ponto;
        this.territorio = territorio;
    }

    public static void main(String[] args) {
        ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();
        ConexaoBanco conexao = new ConexaoBanco(configuracao.getUrlBanco());
        try {
            conexao.inicializarEsquema();
        } catch (FonteIndisponivelException e) {
            System.out.println("Não foi possível iniciar o sistema: " + e.getMessage());
            System.out.println("Verifique se outra instância do programa está aberta usando o mesmo banco.");
            return;
        }

        try {
            Random random = new Random();
            PontoMonitoramento ponto = new PontoMonitoramento("PT-01",
                    "Trecho baixo do córrego - Jardim das Águas", "Zona Leste");
            ponto.adicionarSensor(new Pluviometro("PLU-01", random));
            ponto.adicionarSensor(new SensorNivelCorrego("NIV-01", random));
            ponto.adicionarSensor(new SensorUmidadeSolo("SOL-01", random));

            DadosTerritorio territorio = new DadosTerritorio("Jardim das Águas (bairro fictício)",
                    "Zona Leste", 12_500, 340, CODIGO_ACESSO, configuracao);

            ServicoMonitoramento servico = new ServicoMonitoramento(new MedicaoDAO(conexao),
                    new AlertaDAO(conexao), configuracao, new FabricaPrevisao(configuracao));

            new SistemaMonitoramento(new Scanner(System.in), servico, ponto, territorio).executar();
        } catch (DadosInvalidosException e) {
            System.out.println("Configuração inicial do território inválida: " + e.getMessage());
        }
    }

    // ====================================================================== menu principal

    public void executar() {
        System.out.println("Banco de dados pronto. O histórico fica salvo na pasta ./dados.");
        boolean continuar = true;
        while (continuar) {
            exibirMenuPrincipal();
            try {
                int opcao = lerInteiro("Escolha uma opção: ");
                switch (opcao) {
                    case 1 -> verificarSensores();
                    case 2 -> rodarPrevisoes();
                    case 3 -> menuHistorico();
                    case 4 -> menuDadosTerritorio();
                    case 5 -> exibirStatusCompleto();
                    case 0 -> continuar = false;
                    default -> System.out.println("Opção inválida. Digite um número de 0 a 5.");
                }
            } catch (NoSuchElementException e) {
                System.out.println();
                System.out.println("Entrada encerrada (" + e.getClass().getSimpleName() + "). Saindo do sistema.");
                continuar = false;
            }
        }
        System.out.println("Até logo! O histórico continua salvo para a próxima execução.");
    }

    private void exibirMenuPrincipal() {
        System.out.println();
        System.out.println("=========== TERRITORY PREDICTIVE INSIGHTS ===========");
        System.out.println(" " + territorio.getNomeTerritorio() + " - risco de alagamento");
        System.out.println("=====================================================");
        System.out.println(" 1. Verificar sensores (ler e gravar no banco)");
        System.out.println(" 2. Rodar previsões e alertas");
        System.out.println(" 3. Ver histórico (medições e alertas)");
        System.out.println(" 4. Gerenciar dados do território");
        System.out.println(" 5. Status completo");
        System.out.println(" 0. Sair");
    }

    // ====================================================================== 1. sensores

    private void verificarSensores() {
        int ciclos = lerInteiroNaFaixa("Quantos ciclos de leitura deseja fazer? (1 a 24): ", 1, 24);
        int gravadas = 0;
        int rejeitadas = 0;
        int falhas = 0;
        for (int ciclo = 1; ciclo <= ciclos; ciclo++) {
            System.out.println("-- Ciclo " + ciclo + " --");
            for (Sensor sensor : ponto.getSensores()) {
                try {
                    Medicao medicao = servico.registrarLeitura(sensor);
                    System.out.println("  [GRAVADA]   " + medicao);
                    gravadas++;
                } catch (DadosInvalidosException e) {
                    System.out.println("  [REJEITADA] " + e.getMessage() + " A leitura não foi gravada.");
                    rejeitadas++;
                } catch (FonteIndisponivelException e) {
                    System.out.println("  [FALHA]     " + e.getMessage());
                    falhas++;
                }
            }
        }
        StatusComponente status = ponto.verificarStatus();
        System.out.printf("Resumo: %d gravada(s), %d rejeitada(s) por valor inválido, %d falha(s) de sensor.%n",
                gravadas, rejeitadas, falhas);
        System.out.println("Status do ponto " + ponto.getId() + ": " + status.getDescricao());
    }

    // ====================================================================== 2. previsões

    private void rodarPrevisoes() {
        System.out.println("Previsões com base no histórico gravado no banco:");
        int alertasEmitidos = 0;
        for (Sensor sensor : ponto.getSensores()) {
            try {
                List<AnalisePrevisao> analises = servico.rodarPrevisoesEAlertas(sensor);
                for (AnalisePrevisao analise : analises) {
                    System.out.println(analise.formatar());
                    if (analise.getAlerta().isPresent()) {
                        alertasEmitidos++;
                    }
                }
            } catch (FonteIndisponivelException e) {
                System.out.println("  Não foi possível prever para " + sensor.getId() + ": " + e.getMessage());
            } catch (DadosInvalidosException e) {
                System.out.println("  Histórico de " + sensor.getId() + " inconsistente: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println(alertasEmitidos == 0
                ? "Nenhum alerta emitido nesta rodada."
                : alertasEmitidos + " alerta(s) emitido(s) e gravado(s) no banco.");
    }

    // ====================================================================== 3. histórico

    private void menuHistorico() {
        System.out.println();
        System.out.println("--- Histórico ---");
        System.out.println(" 1. Medições de um sensor");
        System.out.println(" 2. Alertas já emitidos");
        System.out.println(" 3. Exportar medições de um sensor para CSV");
        System.out.println(" 0. Voltar");
        int opcao = lerInteiro("Opção: ");
        switch (opcao) {
            case 1 -> listarMedicoesDeSensor();
            case 2 -> listarAlertas();
            case 3 -> exportarCsv();
            case 0 -> { /* volta ao menu principal */ }
            default -> System.out.println("Opção inválida no menu de histórico.");
        }
    }

    private void listarMedicoesDeSensor() {
        Sensor sensor = escolherSensor();
        try {
            List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getId());
            if (historico.isEmpty()) {
                System.out.println("Nenhuma medição gravada para " + sensor.getId()
                        + ". Use a opção 1 do menu principal para ler os sensores.");
                return;
            }
            System.out.println("Histórico de " + sensor.getId() + " (" + historico.size() + " medições):");
            historico.forEach(m -> System.out.println("  " + m));
        } catch (FonteIndisponivelException e) {
            System.out.println("Não foi possível consultar o histórico: " + e.getMessage());
        }
    }

    private void listarAlertas() {
        try {
            List<Alerta> alertas = servico.listarAlertas();
            if (alertas.isEmpty()) {
                System.out.println("Nenhum alerta emitido até agora.");
                return;
            }
            System.out.println("Alertas emitidos (" + alertas.size() + "), do mais recente ao mais antigo:");
            alertas.forEach(a -> System.out.println("  " + a));
        } catch (FonteIndisponivelException e) {
            System.out.println("Não foi possível consultar os alertas: " + e.getMessage());
        }
    }

    private void exportarCsv() {
        Sensor sensor = escolherSensor();
        String carimbo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path arquivo = Path.of("exportacoes", "historico-" + sensor.getId() + "-" + carimbo + ".csv");
        try {
            List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getId());
            Path gerado = exportadorCsv.exportar(historico, arquivo);
            System.out.println(historico.size() + " medição(ões) exportada(s) para " + gerado);
        } catch (FonteIndisponivelException e) {
            System.out.println("Não foi possível ler o histórico para exportar: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Não foi possível gravar o arquivo CSV (" + arquivo + "): " + e.getMessage());
        }
    }

    // ====================================================================== 4. dados do território

    private void menuDadosTerritorio() {
        System.out.println();
        System.out.println("--- Dados do território ---");
        System.out.println(" 1. Ver dados e limiares");
        System.out.println(" 2. Alterar população estimada");
        System.out.println(" 3. Alterar domicílios em área de risco");
        System.out.println(" 4. Alterar limiares (exige código de acesso)");
        System.out.println(" 5. Registrar medição manual (leitura de campo)");
        System.out.println(" 6. Colocar/retirar sensor de manutenção");
        System.out.println(" 0. Voltar");
        int opcao = lerInteiro("Opção: ");
        try {
            switch (opcao) {
                case 1 -> exibirDadosTerritorio();
                case 2 -> {
                    territorio.setPopulacaoEstimada(lerInteiro("Nova população estimada: "));
                    System.out.println("População atualizada: " + territorio.getPopulacaoEstimada());
                }
                case 3 -> {
                    territorio.setDomiciliosEmAreaDeRisco(lerInteiro("Domicílios em área de risco: "));
                    System.out.println("Atualizado: " + territorio.getDomiciliosEmAreaDeRisco() + " domicílio(s).");
                }
                case 4 -> alterarLimiares();
                case 5 -> registrarMedicaoManual();
                case 6 -> alternarManutencao();
                case 0 -> { /* volta ao menu principal */ }
                default -> System.out.println("Opção inválida no menu de dados do território.");
            }
        } catch (DadosInvalidosException e) {
            System.out.println("Dado não aceito: " + e.getMessage());
        } catch (AcessoNegadoException e) {
            System.out.println("Acesso negado: " + e.getMessage());
        } catch (FonteIndisponivelException e) {
            System.out.println("Fonte de dados indisponível: " + e.getMessage());
        }
    }

    private void exibirDadosTerritorio() {
        System.out.println(territorio);
        System.out.println("Ponto monitorado: " + ponto);
        System.out.println("Limiares:");
        for (TipoSensor tipo : TipoSensor.values()) {
            System.out.printf("  %-18s %s%n", tipo.getDescricao(), territorio.consultarLimiares(tipo).formatar(tipo));
        }
    }

    private void alterarLimiares() throws AcessoNegadoException, DadosInvalidosException {
        TipoSensor tipo = escolherTipoSensor();
        System.out.println("Atuais: " + territorio.consultarLimiares(tipo).formatar(tipo));
        double atencao = lerDouble("Novo limite de ATENÇÃO (" + tipo.getUnidade() + "): ");
        double alerta = lerDouble("Novo limite de ALERTA (" + tipo.getUnidade() + "): ");
        double critico = lerDouble("Novo limite CRÍTICO (" + tipo.getUnidade() + "): ");
        Limiares novos = new Limiares(atencao, alerta, critico);
        String codigo = lerTexto("Código de acesso da coordenação: ");
        territorio.alterarLimiares(codigo, tipo, novos);
        System.out.println("Limiares atualizados: " + territorio.consultarLimiares(tipo).formatar(tipo));
    }

    private void registrarMedicaoManual() throws DadosInvalidosException, FonteIndisponivelException {
        Sensor sensor = escolherSensor();
        double valor = lerDouble("Valor medido em campo (" + sensor.getTipo().getUnidade() + "): ");
        Medicao gravada = servico.registrarMedicaoManual(sensor, valor);
        System.out.println("Medição gravada: " + gravada);
    }

    private void alternarManutencao() {
        Sensor sensor = escolherSensor();
        boolean entrarEmManutencao = sensor.estaFuncionando();
        sensor.alterarManutencao(entrarEmManutencao);
        System.out.println(entrarEmManutencao
                ? sensor.getId() + " colocado EM MANUTENÇÃO: leituras vão falhar até ele voltar."
                : sensor.getId() + " voltou à operação.");
        System.out.println("Status do ponto: " + ponto.verificarStatus().getDescricao());
    }

    // ====================================================================== 5. status completo

    private void exibirStatusCompleto() {
        RelatorioBuilder builder = new RelatorioBuilder()
                .comTerritorio(territorio)
                .comComponente(ponto);
        try {
            builder.comTotalMedicoes(servico.contarMedicoes());
            for (Sensor sensor : ponto.getSensores()) {
                builder.comSensor(sensor, servico.contarMedicoesPorSensor(sensor.getId()));
                builder.comAnalises(servico.preverSensor(sensor));
            }
            builder.comAlertasRecentes(servico.listarAlertasRecentes(5));
        } catch (FonteIndisponivelException e) {
            System.out.println("Aviso: parte do relatório ficou incompleta porque o banco não respondeu: "
                    + e.getMessage());
        } catch (DadosInvalidosException e) {
            System.out.println("Aviso: histórico inconsistente, previsões omitidas do relatório: " + e.getMessage());
        }
        System.out.println(builder.construir());
    }

    // ====================================================================== leitura de entrada

    private Sensor escolherSensor() {
        List<Sensor> sensores = ponto.getSensores();
        for (int i = 0; i < sensores.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, sensores.get(i));
        }
        int indice = lerInteiroNaFaixa("Sensor: ", 1, sensores.size());
        return sensores.get(indice - 1);
    }

    private TipoSensor escolherTipoSensor() {
        TipoSensor[] tipos = TipoSensor.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.printf("  %d. %s (%s)%n", i + 1, tipos[i].getDescricao(), tipos[i].getUnidade());
        }
        return tipos[lerInteiroNaFaixa("Tipo de sensor: ", 1, tipos.length) - 1];
    }

    private String lerTexto(String rotulo) {
        System.out.print(rotulo);
        return scanner.nextLine().trim();
    }

    private int lerInteiro(String rotulo) {
        while (true) {
            String entrada = lerTexto(rotulo);
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("'" + entrada + "' não é um número inteiro. Tente novamente.");
            }
        }
    }

    private int lerInteiroNaFaixa(String rotulo, int minimo, int maximo) {
        while (true) {
            int valor = lerInteiro(rotulo);
            if (valor >= minimo && valor <= maximo) {
                return valor;
            }
            System.out.printf("Digite um número entre %d e %d.%n", minimo, maximo);
        }
    }

    private double lerDouble(String rotulo) {
        while (true) {
            String entrada = lerTexto(rotulo).replace(',', '.');
            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                System.out.println("'" + entrada + "' não é um número válido (ex.: 12,5). Tente novamente.");
            }
        }
    }
}
