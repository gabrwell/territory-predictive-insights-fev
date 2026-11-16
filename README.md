# Territory Predictive Insights — Chuva intensa e risco de alagamento

Global Solution 2º/2026 · Programação Orientada a Objetos · Tema **CitizenTech**

Plataforma de console em Java que lê sensores de um ponto vulnerável a alagamento, grava o histórico em banco de dados (H2 via JDBC), prevê a evolução de cada sensor com duas estratégias e emite alertas **ATENÇÃO / ALERTA / CRÍTICO** explicando o porquê de cada um.

## Integrantes

| Nome | RM | Responsabilidade |
|------|----|------------------|
| Pedro Henrique dos Santos Cardoso | 563268 | Domínio e exceções (`dominio/`, `excecao/`) |
| Gabriel Gibin Leoncio | 565462 | Persistência (`persistencia/`, `schema.sql`) |
| Rai Augusto Ribeiro | 562870 | Previsões, alertas, padrões e testes (`servico/`, `src/test`) |

---

## 1. Público beneficiado

**Quem sofre o problema:** famílias que moram nas partes baixas de bairros cortados por córregos, onde uma chuva forte de poucas horas basta para a água invadir as casas, arrastar carros e bloquear as ruas.

**Quem usa o sistema:** a associação de moradores, o núcleo comunitário de Defesa Civil (NUDEC) ou a equipe de zeladoria da subprefeitura.

**Qual decisão o sistema apoia:** *"preciso avisar os moradores, deixar abrigo e rotas prontos ou acionar a Defesa Civil agora, enquanto ainda dá tempo?"*. O sistema junta a chuva, o nível do córrego e a saturação do solo, projeta as próximas horas e diz qual limite deve ser ultrapassado e qual ação é recomendada.

O território é descrito por **região e equipamento** (bairro fictício "Jardim das Águas", Zona Leste, ponto PT-01). Nenhum dado pessoal é usado.

---

## 2. Como rodar

**Pré-requisito:** JDK 17 ou superior (testado com JDK 25). **Não é preciso instalar Maven nem banco**: o projeto traz o Maven Wrapper e o H2 é baixado como dependência.

```bash
# Windows
mvnw.cmd clean package
java -jar target/territory-predictive-insights.jar

# Linux / macOS
./mvnw clean package
java -jar target/territory-predictive-insights.jar
```

`package` compila, **roda os 35 testes** e gera o `.jar` executável. Só os testes: `mvnw.cmd test`.

**No IntelliJ:** abrir a pasta como projeto Maven e executar `SistemaMonitoramento.main()` (a pasta de trabalho deve ser a raiz do projeto).

### Banco de dados

- H2 **em arquivo**: `jdbc:h2:./dados/territorio`. A pasta `dados/` é criada sozinha na primeira execução, então **o histórico sobrevive ao reiniciar o programa**.
- As tabelas são criadas automaticamente pelo script [`src/main/resources/schema.sql`](src/main/resources/schema.sql) (`CREATE TABLE IF NOT EXISTS`).
- Para começar do zero, basta apagar a pasta `dados/`.

### Roteiro sugerido para a correção

1. **Opção 1** → informe `8` ciclos. Aparecem leituras `[GRAVADA]`, leituras `[REJEITADA]` (valor impossível → `DadosInvalidosException`) e `[FALHA]` (sensor sem resposta → `FonteIndisponivelException`).
2. **Opção 2** → previsões das duas estratégias para cada sensor, com valor previsto, como foi calculado e o que significa. Alertas são gravados no banco.
3. **Opção 3 → 1 / 2** → histórico por sensor e alertas emitidos. **Opção 3 → 3** exporta CSV (bônus).
4. **Opção 4 → 4** → alterar limiares (código de acesso: **`1234`**; teste também um código errado e limiares fora de ordem).
5. **Opção 4 → 6** → coloque um sensor em manutenção e rode a opção 1 de novo: o ponto fica **DEGRADADO**.
6. **Opção 5** → status completo (relatório montado pelo Builder).
7. Feche (0), abra de novo e veja na opção 3 que tudo continua lá.

> As leituras são simuladas com `Random` e mudam a cada execução. Se nenhum alerta aparecer, faça mais ciclos de leitura ou baixe os limiares de chuva pela opção 4.

---

## 3. Estrutura

```
src/main/java/
├── dominio/       ComponenteTerritorial (abstrata), PontoMonitoramento, StatusComponente,
│                  Sensor (interface), SensorSimulado (abstrata), Pluviometro,
│                  SensorNivelCorrego, SensorUmidadeSolo, TipoSensor, DadosTerritorio,
│                  Limiares, EstrategiaPrevisao (abstrata), PrevisaoMediaMovel,
│                  PrevisaoComparacao, ResultadoPrevisao, Medicao, Alerta, NivelAlerta
├── excecao/       DadosInvalidosException, FonteIndisponivelException, AcessoNegadoException
├── persistencia/  ConexaoBanco, MedicaoDAO, AlertaDAO              (JDBC + PreparedStatement)
├── servico/       ServicoMonitoramento, FabricaPrevisao, ConfiguracaoSistema, TipoEstrategia,
│                  RelatorioBuilder, Relatorio, AnalisePrevisao, ExportadorCsv
└── SistemaMonitoramento.java   (menu principal com Scanner)
src/main/resources/schema.sql
src/test/java/
├── dominio/       MedicaoTest, PrevisaoMediaMovelTest, PrevisaoComparacaoTest, LimiaresTest,
│                  SensorTest, DadosTerritorioTest, Historicos (utilitário)
└── servico/       ServicoMonitoramentoTest (Mockito), ConfiguracaoEFabricaTest
```

---

## 4. Onde está cada requisito

| Requisito | Onde |
|-----------|------|
| `ComponenteTerritorial` abstrata (id, nome, região, status) + `verificarStatus()` abstrato | `dominio/ComponenteTerritorial.java`; filha concreta `PontoMonitoramento` (OPERANTE / DEGRADADO / INOPERANTE conforme os sensores) |
| Interface `Sensor` (ler valor, verificar funcionamento, retornar tipo) com 3 implementações | `Sensor` → `SensorSimulado` → `Pluviometro` (mm/h), `SensorNivelCorrego` (m), `SensorUmidadeSolo` (%) |
| `DadosTerritorio` com atributos privados e código de acesso para alterar limiares | `DadosTerritorio.alterarLimiares(codigo, tipo, limiares)` lança `AcessoNegadoException` com código errado; setters validam população e domicílios |
| `EstrategiaPrevisao` + 2 filhas sobrescrevendo `prever()` e usando `super` | `PrevisaoMediaMovel` e `PrevisaoComparacao` chamam `super(...)` no construtor e `super.prever(historico)` para reaproveitar a validação do histórico |
| Escolha da estratégia sem `if tipo ==` / `instanceof` | `FabricaPrevisao` usa um `EnumMap<TipoEstrategia, Supplier<...>>`; o serviço só chama `estrategia.prever(...)` (polimorfismo) |
| `DadosInvalidosException` | `Medicao` (fora da faixa, negativa), `Limiares` (limiar inválido), `DadosTerritorio` (população negativa), `ConfiguracaoSistema`, `EstrategiaPrevisao` (histórico misturado) |
| `FonteIndisponivelException` | `SensorSimulado.lerValor()` (falha/manutenção) e DAOs (banco inacessível ou registro corrompido) |
| Captura no menu com mensagem clara, nenhum `catch` vazio | `SistemaMonitoramento`: cada `catch` imprime o que aconteceu e o que foi (ou não) feito |
| Persistência JDBC com `PreparedStatement` e try-with-resources | `MedicaoDAO`, `AlertaDAO`; nenhum SQL concatena dados; todo `Connection`/`PreparedStatement`/`ResultSet` está em try-with-resources |
| Gravar cada medição e cada alerta; listar histórico por sensor e alertas | `ServicoMonitoramento.registrarLeitura` / `rodarPrevisoesEAlertas`; menu opção 3 |
| Padrões criacionais | Factory (`FabricaPrevisao`), Singleton (`ConfiguracaoSistema`), Builder (`RelatorioBuilder`) — ver seção 5 |
| Alertas ATENÇÃO / ALERTA / CRÍTICO explicando sensor, valor previsto e limite | `ServicoMonitoramento.avaliarAlerta` monta o motivo; `NivelAlerta` traz a recomendação de ação |
| Testes: ≥5 AAA, `assertThrows`, Mockito | 35 testes; `assertThrows` em `MedicaoTest`, `LimiaresTest`, `SensorTest`, etc.; mocks de `MedicaoDAO`/`AlertaDAO` em `ServicoMonitoramentoTest` |
| Bônus | Exportação do histórico consultado para CSV (menu 3 → 3, pasta `exportacoes/`) |

---

## 5. Padrões criacionais: por que ali e não em outro lugar

**Factory: `FabricaPrevisao`.** Cada estratégia precisa de parâmetros que vêm da configuração (tamanho da janela). Sem a fábrica, o `ServicoMonitoramento` teria um `if (tipo == MEDIA_MOVEL) new PrevisaoMediaMovel(...) else if ...` e precisaria conhecer todas as classes concretas. A fábrica registra um `Supplier` por `TipoEstrategia` num `EnumMap`: o serviço pede `criarEstrategiasAtivas()` e trabalha só com o tipo abstrato `EstrategiaPrevisao`. Para criar uma terceira estratégia basta registrar uma linha na fábrica, sem mexer no serviço. Não usamos Factory para `Medicao` porque ela tem um único jeito de ser criada: o construtor com validação já basta.

**Singleton: `ConfiguracaoSistema`.** Os limiares precisam ser **uma única verdade**: se o menu alterasse uma cópia e o serviço de alertas lesse outra, o sistema avisaria com base em limites antigos. O construtor é privado e o acesso é por `getInstancia()`. Mesmo assim, o serviço **recebe** a configuração pelo construtor, em vez de chamar `getInstancia()` lá dentro, para continuar testável. Nos testes, `restaurarPadroes()` isola um caso do outro. Não fizemos os DAOs como Singleton porque eles não guardam estado e, sendo instâncias comuns, podem ser trocados por mocks.

**Builder: `RelatorioBuilder`.** O relatório de status completo tem muitas partes opcionais (componentes, sensores, previsões, alertas, total de medições). Um construtor com sete parâmetros seria ilegível e obrigaria a passar `null`. Com o builder, o menu encadeia `comTerritorio(...).comComponente(...).comSensor(...)` e chama `construir()`, que valida o mínimo obrigatório (o território).

---

## 6. Decisões da equipe

- **Recorte:** clima local (chuva intensa e alagamento), com três sensores complementares. A chuva sozinha não diz tudo: 30 mm/h sobre solo seco é bem diferente de 30 mm/h sobre solo encharcado com o córrego já alto.
- **JDBC puro + H2 em arquivo** em vez de Spring Boot: menos "mágica" para explicar na conversa de correção, SQL visível e controle explícito dos recursos. Usamos H2 em arquivo (e não em memória) para cumprir o requisito de os dados sobreviverem ao reinício.
- **Exceções verificadas (checked):** `DadosInvalidosException` e `FonteIndisponivelException` estendem `Exception`, então o compilador obriga quem chama a tratar ou repassar o erro e nada passa despercebido. Erros de programação (ex.: janela < 1 no construtor da estratégia) usam `IllegalArgumentException`/`IllegalStateException`.
- **Banco fora do ar é `FonteIndisponivelException`:** o banco também é uma fonte de dados. O DAO embrulha a `SQLException` mantendo a causa original.
- **A validação mora na `Medicao`:** o sensor simulado às vezes devolve valores impossíveis (ruído elétrico, reflexo). O sensor não os corrige; quem barra é o construtor de `Medicao`, então **nenhuma medição inválida existe como objeto** e nada inválido chega ao banco.
- **`super.prever()` faz o trabalho comum:** valida o histórico, decide se há leituras suficientes (senão o sistema se cala) e devolve a previsão mais ingênua ("persistência"). As filhas reaproveitam isso e refinam.
- **Status completo não grava alertas:** usa `preverSensor()` (só calcula). Apenas a opção 2 emite e grava, para o relatório não duplicar alertas.
- **`AcessoNegadoException` (terceira exceção):** código de acesso errado não é "dado inválido", é falta de permissão, por isso ganhou uma classe própria.
- **Limites conhecidos:** cada execução da opção 2 grava de novo os alertas que continuam valendo (é um registro de emissões, não de "situações abertas"). Os limiares alterados pelo menu valem só até o programa ser fechado.

---

## 7. Reflexão crítica

**Quando o sistema deve se calar?** Quando não há histórico suficiente. A média móvel exige 5 leituras e a comparação exige 6 (dois períodos de 3). Abaixo disso o sistema responde "previsão suspensa" e **não emite alerta, mesmo que o último valor seja altíssimo**: com 2 ou 3 leituras não dá para separar uma tendência real de um pico isolado ou de um defeito do sensor. Ele também deve se calar diante de dados que não fazem sentido: leituras negativas ou acima da faixa física são rejeitadas e não entram no cálculo. Há um ponto que o sistema ainda não trata: um sensor em manutenção mantém no banco leituras cada vez mais antigas, e a previsão continua usando esse histórico velho. Uma evolução natural seria descartar leituras com mais de X horas.

**O que os dados não representam?** Um pluviômetro mede a chuva **num ponto**. Em temporais de verão, a duas quadras dali pode estar caindo o dobro ou nada. Um sensor de nível no córrego não enxerga um bueiro entupido na rua de cima, que alaga independentemente do córrego. Os dados também não mostram **quem** está exposto: uma casa térrea com idosos e acamados corre um risco muito diferente do de um sobrado, mas o sistema só sabe "340 domicílios em área de risco". As previsões (média e tendência) são estatística simples sobre o passado recente e não conhecem frentes frias, a maré ou a chuva que já caiu rio acima. Por fim, nossos valores são **simulados**: os limiares são referências razoáveis, mas teriam de ser calibrados com o histórico real do bairro.

**Por que o alerta apoia a decisão em vez de decidir?** Porque quem decide tem informações que o sistema não tem: viu a rua, sabe que o bueiro foi limpo ontem, conhece a família que precisa de ajuda para sair. Evacuar tem custo (gente deixando a casa, perda de confiança se o alarme for falso) e não evacuar também (vidas). Essa ponderação é humana e tem responsável. Por isso o alerta mostra **o porquê** (sensor, valor previsto, limite estourado, estratégia usada) e uma **recomendação**, nunca uma ordem. Assim a pessoa pode concordar, desconfiar ou procurar mais informação. Um sistema que agisse sozinho e errasse ensinaria a comunidade a ignorá-lo, e o próximo alerta verdadeiro chegaria a ouvidos que já não acreditam nele.

---

## 8. Testes

35 testes JUnit 5 no padrão **Arrange / Act / Assert**, sem banco e sem digitação no console:

- `MedicaoTest`: medição válida; chuva negativa, umidade > 100% e sensor vazio lançam `DadosInvalidosException` (`assertThrows`).
- `PrevisaoMediaMovelTest`: média das últimas N (7 leituras → usa as 5 últimas = 40,0); silêncio com poucas leituras; histórico misturado ou nulo.
- `PrevisaoComparacaoTest`: tendência de alta (10 → 20 projeta 30); queda não gera valor negativo; exige dois períodos; as duas estratégias dão resultados diferentes no mesmo histórico (polimorfismo).
- `LimiaresTest`, `SensorTest`, `DadosTerritorioTest`, `ConfiguracaoEFabricaTest`: classificação por nível, sensor com falha/manutenção (`FonteIndisponivelException`), código de acesso errado, Singleton e Factory.
- `ServicoMonitoramentoTest` (**Mockito**): `MedicaoDAO` e `AlertaDAO` mockados. Verifica que chuva de 60 mm/h gera alerta **CRÍTICO** com limite 50 gravado via `alertaDAO.salvar`, que chuva fraca **não** grava alerta, que poucas leituras não alertam e que leitura inválida **não** chega ao `medicaoDAO.salvar`.

Os testes conferem valores calculados: se a conta da média ou da tendência errar, eles falham.

---

## 9. Uso de IA

**Ferramenta:** Claude (Anthropic), usado pelo Claude Code.

**Onde usamos:**

- **Código-fonte:** o projeto (classes de domínio, exceções, DAOs, serviços, padrões criacionais e menu) foi gerado pela IA a partir do enunciado da GS. A IA também propôs as escolhas de arquitetura (recorte de chuva e alagamento, JDBC + H2 em arquivo, três sensores), que a equipe aceitou.
- **Testes:** os testes unitários (JUnit 5 + Mockito) foram escritos pela IA junto com o código.
- **Documentação:** este README foi escrito pela IA, incluindo a justificativa dos padrões e a reflexão crítica.

**Como validamos:**

- Compilamos o projeto e rodamos os 35 testes (`mvnw.cmd clean package`), todos passando.
- Executamos o menu seguindo o roteiro da seção 2: leituras gravadas, rejeitadas e com falha; previsões das duas estratégias; alertas; código de acesso errado; sensor em manutenção; relatório; exportação CSV. Fechamos e reabrimos o programa para confirmar que o histórico continua no banco.
- Conferimos os critérios de avaliação do enunciado um a um (tabela da seção 4).
