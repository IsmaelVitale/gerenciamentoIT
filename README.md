# GerenciamentoIT

Documento mestre do produto, das regras de negocio e da arquitetura planejada.

> Este README existe para preservar o contexto completo do projeto entre conversas, desenvolvedores e futuras etapas de implementacao. Ele deve ser atualizado sempre que uma regra relevante for alterada.

## 1. Visao do produto

O GerenciamentoIT sera uma plataforma centralizada para:

- gestao operacional de PDAs e outros ativos;
- controle do inventario fisico da T.I.;
- abertura e acompanhamento de chamados;
- atendimento ITSM pela T.I.;
- organizacao interna do trabalho tecnico em Kanban;
- auditoria de movimentacoes, decisoes e responsabilidades.

A plataforma tera uma unica API central e um unico banco de dados. Os diferentes aplicativos serao interfaces especializadas sobre a mesma fonte de verdade.

```text
Frontends e integracoes
        ↓
API central Spring Boot
        ↓
MySQL
```

Nenhum frontend podera implementar sozinho uma regra critica de negocio. A interface pode ocultar ou exibir funcionalidades, mas a autorizacao definitiva sempre pertence a API.

## 2. Estado atual do repositorio

A fundacao da nova API foi implementada e integrada a `main`.

Tecnologias atuais:

- Java 21;
- Spring Boot 4.1;
- Spring Web;
- Spring Security;
- Spring Data JPA;
- Hibernate;
- Bean Validation;
- MySQL;
- Maven;
- OpenAPI/Swagger;
- Docker Compose;
- GitHub Actions.

A API usa `/api/v1` como prefixo funcional e `/api` como contexto da aplicacao.

A primeira etapa implementada inclui:

- sessoes opacas por matricula;
- usuarios, papeis, permissoes e escopos;
- setores e turnos;
- tipos de ativo;
- cadastro e liberacao inicial de ativos;
- numero de serie obrigatorio;
- patrimonio opcional;
- auditoria;
- historico inicial de movimentacoes;
- criacao automatica do banco e do schema.

Os frontends antigos permanecem apenas como referencia visual e ainda nao consomem os novos contratos.

## 3. Principio central de autorizacao

A API nao deve decidir o que pode acontecer apenas pelo aplicativo que realizou a chamada.

A decisao deve considerar:

```text
Usuario autenticado
+ papel
+ permissoes
+ setor
+ turno
+ recurso
+ acao solicitada
```

O aplicativo de origem sera apenas um metadado de auditoria, por exemplo:

- Hub de PDAs;
- Gestao Operacional;
- Portal de Chamados;
- Gestao de Ativos;
- ITSM;
- Planner;
- WhatsApp futuramente.

Um usuario nao podera contornar uma regra chamando diretamente um endpoint usado por outro aplicativo.

## 4. Atores e responsabilidades

### 4.1 Usuario comum

Escopo restrito a si mesmo.

Pode:

- abrir chamados;
- consultar os proprios chamados;
- responder mensagens publicas da T.I.;
- acompanhar resolucao e fechamento.

Nao participa do controle de PDAs. Mesmo que tenha cadastro, esse cadastro existe principalmente para abertura e acompanhamento de chamados.

### 4.2 Lider de setor

Escopo restrito aos setores e turnos sob sua responsabilidade.

Pode:

- criar usuarios comuns do proprio setor;
- conferir o pool de PDAs;
- visualizar o historico dos dois turnos do setor;
- marcar uma PDA como indisponivel;
- autorizar emprestimos temporarios entre setores;
- devolver emprestimos;
- encaminhar equipamentos para manutencao;
- visualizar chamados operacionais da equipe, respeitando futuras regras de confidencialidade.

Nao pode:

- fazer transferencia permanente entre setores;
- regularizar divergencias de forma definitiva;
- criar supervisores;
- administrar o ciclo patrimonial completo dos ativos.

### 4.3 Supervisor

Possui visao global da operacao.

Pode:

- criar e administrar lideres;
- administrar setores operacionais;
- definir a distribuicao permanente de PDAs;
- acompanhar todos os pools;
- visualizar divergencias globais;
- regularizar divergencias;
- extrair relatorios operacionais;
- visualizar chamados da operacao, respeitando regras de confidencialidade.

### 4.4 Analista de T.I.

Possui visao tecnica e administrativa sobre ativos, chamados e tarefas.

Pode:

- cadastrar e editar ativos;
- receber ativos em manutencao;
- registrar diagnosticos e reparos;
- atender chamados;
- assumir tarefas;
- criar tarefas independentes ou auxiliares;
- reabrir tarefas internas;
- administrar inventario tecnico conforme permissoes.

O analista nao participa da logistica diaria dos pools como lider ou supervisor, salvo se possuir tambem outro vinculo de acesso.

### 4.5 Gestor de T.I. ou administrador do sistema

Possui escopo global administrativo.

Pode:

- criar supervisores e analistas;
- conceder papeis e permissoes elevadas;
- configurar parametros sistemicos;
- autorizar baixas patrimoniais;
- executar correcoes administrativas excepcionais;
- administrar acessos globais.

Mesmo que inicialmente uma pessoa acumule funcoes, `ANALISTA_TI` e `GESTOR_TI` sao papeis conceitualmente separados.

## 5. Hierarquia de criacao de acessos

Regra inicial:

```text
Lider       → cria Usuario comum do proprio setor
Supervisor  → cria Lider
Gestor TI   → cria Supervisor e Analista TI
```

Nenhum papel pode conceder silenciosamente um nivel igual ou superior ao proprio sem permissao especifica.

Toda alteracao de acesso deve ser auditada.

## 6. Ecossistema de interfaces

A solucao foi concebida com seis aplicacoes e uma integracao futura.

### 6.1 Hub de PDAs

Aplicacao de quiosque para o chao de fabrica.

Responsabilidades:

- identificacao do lider por matricula;
- conferencia de abertura do turno;
- conferencia facultativa de encerramento;
- bipagem em lote;
- visualizacao do estoque esperado;
- identificacao de faltas, extras e conflitos;
- operacoes rapidas sobre uma PDA;
- funcionamento offline com sincronizacao posterior.

### 6.2 Gestao Operacional

Uso exclusivo de lideres e supervisores.

Responsabilidades:

- administracao de usuarios operacionais;
- administracao de lideres;
- distribuicao de PDAs;
- acompanhamento de conferencias;
- acompanhamento de emprestimos;
- regularizacao de divergencias;
- relatorios por setor e turno.

### 6.3 Portal de Chamados

Interface do solicitante e da operacao.

Responsabilidades:

- abrir chamado;
- acompanhar andamento;
- trocar mensagens publicas;
- confirmar resolucao;
- reabrir antes do fechamento quando o problema persistir.

### 6.4 Gestao de Ativos da T.I.

Inventario global de equipamentos.

Abrange:

- PDAs;
- notebooks;
- celulares corporativos;
- coletores;
- impressoras;
- perifericos;
- futuros tipos de ativo.

Responsabilidades:

- cadastro;
- identificacao;
- condicao fisica;
- manutencao;
- garantia;
- baixa;
- historico patrimonial.

### 6.5 Atendimento ITSM

Painel interno robusto para analistas.

Responsabilidades:

- triagem;
- categorizacao;
- prioridade;
- SLA;
- atribuicao;
- atendimento;
- mensagens ao usuario;
- resolucao;
- fechamento;
- relatorios e filtros.

### 6.6 Planner da T.I.

Kanban interno da equipe tecnica.

Responsabilidades:

- quadros;
- colunas;
- tarefas;
- responsaveis;
- etiquetas;
- checklists;
- prazos;
- comentarios internos;
- tarefas independentes;
- tarefas ligadas a chamados.

### 6.7 WhatsApp

Integracao futura e fora do escopo atual.

As regras serao especificadas posteriormente. A direcao inicial e validar o usuario por matricula, sem senha nesta primeira fase, mas nenhum fluxo definitivo de WhatsApp deve ser assumido ainda.

## 7. Identificacao e autenticacao

A autenticacao inicial e provisoria e usa matricula, sem senha.

Fluxo:

```http
POST /api/v1/sessoes
```

```json
{
  "matricula": "ADMIN-LOCAL",
  "origemAplicacao": "GESTAO_ATIVOS"
}
```

A API devolve um token temporario. As proximas chamadas usam:

```http
Authorization: Bearer <token>
```

A matricula nao deve ser enviada em cada comando como prova de identidade. A API descobre o autor pela sessao.

A estrutura deve permitir futuramente senha, SSO, cracha seguro ou outro fator sem alterar as regras de negocio.

## 8. Regras gerais dos ativos

### 8.1 Identificadores

Todo ativo possui:

```text
ID interno UUID: obrigatorio e imutavel
Numero de serie: obrigatorio e unico
Patrimonio: opcional e unico quando preenchido
```

O numero de serie nao e chave primaria. O UUID interno protege relacionamentos contra correcoes cadastrais.

Patrimonio vazio deve ser normalizado para `null`.

### 8.2 Dimensoes separadas

Um unico campo `status` nao e suficiente.

O sistema deve distinguir:

- situacao patrimonial;
- disponibilidade;
- alocacao permanente;
- custodia fisica atual;
- movimentacao temporaria;
- condicao tecnica.

Exemplo:

```text
Situacao patrimonial: ATIVO
Alocacao permanente: Recebimento
Custodia atual: Expedicao
Movimentacao: EMPRESTADA
Disponibilidade: DISPONIVEL
```

### 8.3 Modalidades de uso

#### Pool setorial

Usado pelas PDAs.

- pertence ao setor;
- nao pertence ao operador;
- e conferido por turno;
- pode ser emprestado;
- fica sob responsabilidade operacional dos lideres.

#### Custodia individual

Pode ser usada para notebooks, celulares e outros equipamentos entregues nominalmente.

Nao se aplica as PDAs.

#### Ativo fixo em local

Usado para impressoras, estacoes e dispositivos instalados.

#### Estoque da T.I.

Usado para equipamentos novos, reparados, devolvidos, reservados ou ainda sem distribuicao.

## 9. Ciclo de vida dos ativos

### 9.1 Cadastro

Todo ativo entra pelo painel da T.I.

Dados minimos:

- tipo;
- numero de serie;
- patrimonio opcional;
- fabricante;
- modelo;
- observacoes;
- origem do equipamento.

O ativo nasce em preparacao e indisponivel.

### 9.2 Preparacao e liberacao

A T.I. valida:

- numero de serie;
- patrimonio, quando existir;
- funcionamento;
- etiqueta;
- acessorios relevantes;
- condicao fisica.

Depois, o ativo e liberado para estoque tecnico.

### 9.3 Distribuicao permanente

A distribuicao permanente para um setor e decisao do supervisor.

Fluxo:

1. supervisor seleciona a PDA;
2. define o setor de destino;
3. registra a transferencia;
4. equipamento fica aguardando recebimento;
5. lider do destino confirma fisicamente por bipagem;
6. PDA passa a integrar oficialmente o pool.

A responsabilidade fisica so muda quando quem recebe confirma.

### 9.4 Pool setorial

Uma PDA no pool:

- entra na quantidade esperada;
- deve aparecer nas conferencias;
- pertence ao setor, nao ao operador;
- fica sob responsabilidade dos lideres do setor.

### 9.5 Indisponibilidade

O lider pode marcar uma PDA como indisponivel.

Ela continua:

- pertencendo ao setor;
- fisicamente esperada;
- obrigatoria nas conferencias;
- sob responsabilidade do lider.

A indisponibilidade exige motivo.

### 9.6 Envio para manutencao

O lider pode encaminhar uma PDA para manutencao.

Ao fazer isso:

- ela sai do pool operacional do setor;
- deixa de ser esperada nas conferencias futuras;
- fica aguardando recebimento pela T.I.;
- o ultimo responsavel continua identificado ate a T.I. confirmar o recebimento.

Depois do reparo, a PDA volta ao estoque da T.I. sem retornar automaticamente ao setor anterior. O supervisor decide a proxima alocacao.

### 9.7 Baixa definitiva

A baixa pode ocorrer por:

- dano irreparavel;
- perda confirmada;
- furto;
- descarte;
- obsolescencia;
- devolucao ao fornecedor;
- fim de vida util.

O ativo baixado nunca e apagado. Todo historico permanece.

## 10. Controle de PDAs por pool

Nao existe retirada individual por operador.

O operador comum so precisa de cadastro para abertura de chamados.

A responsabilidade da PDA e determinada por:

```text
Setor
+ turno
+ lider responsavel pela conferencia
```

O sistema nao rastreara qual operador utilizou uma PDA durante o turno nesta primeira concepcao.

## 11. Conferencia de turno

### 11.1 Abertura

Obrigatoria no inicio de cada turno.

Fluxo:

1. lider bipa a matricula;
2. sistema identifica setor e turno;
3. sistema mostra estoque esperado;
4. lider bipa todas as PDAs presentes;
5. sistema mostra o resumo;
6. lider envia a conferencia.

Enquanto a abertura nao for concluida, o painel mostra pendencia para lider e supervisor.

### 11.2 Encerramento

Facultativo no fim do turno.

Nao realizar o encerramento nao impede a operacao, mas aumenta a responsabilidade do lider anterior quando o turno seguinte encontra divergencias.

### 11.3 Comparacao entre turnos

A abertura do novo turno compara:

- encerramento anterior, quando existir;
- ultima conferencia valida;
- emprestimos;
- manutencoes;
- indisponibilidades;
- transferencias autorizadas.

O lider do turno seguinte bipa novamente todas as PDAs.

## 12. Resultados possiveis da bipagem

Uma leitura pode resultar em:

- confirmada;
- duplicada;
- nao cadastrada;
- de outro setor;
- em manutencao;
- baixada;
- emprestada;
- indisponivel;
- conflito de localizacao;
- ausente quando esperada e nao lida.

Erros nao precisam interromper toda a sessao. Um popup alerta durante a leitura e um resumo completo aparece antes do envio.

## 13. Divergencias

Uma divergencia representa diferenca entre o estado esperado e a realidade fisica.

### 13.1 PDA de outro setor

Quando uma PDA de outro setor e bipada sem emprestimo valido:

- nao e transferida automaticamente;
- passa a aparecer como `DIVERGENCIA`;
- deixa de contar como disponibilidade normal;
- o setor de origem fica preservado no historico;
- o setor onde foi encontrada e registrado;
- a ocorrencia fica fixada nos paineis dos lideres envolvidos e do supervisor.

### 13.2 PDA faltante

Quando uma PDA esperada nao e bipada:

- e registrada como ausente;
- deixa de contar como disponivel;
- gera divergencia;
- aparece de forma fixa;
- fica inicialmente associada a responsabilidade do turno anterior.

Ausente nao significa automaticamente perdida.

### 13.3 Prazo

Divergencias nao expiram automaticamente.

O supervisor decide quando e como regularizar.

O tempo em aberto e apenas informativo.

### 13.4 Regularizacao

O supervisor pode classificar como:

- erro de bipagem;
- equipamento encontrado;
- emprestimo valido;
- devolucao;
- transferencia permanente;
- manutencao;
- indisponibilidade;
- nao localizado;
- investigacao de perda;
- erro cadastral.

Toda regularizacao exige justificativa e auditoria.

## 14. Emprestimo temporario entre setores

O proprio lider do setor de origem pode autorizar.

Fluxo:

1. lider bipa a PDA;
2. escolhe `Emprestar para outro setor`;
3. informa destino e motivo;
4. confirma com a propria matricula;
5. PDA fica em transito;
6. lider do destino confirma o recebimento por bipagem.

Durante o emprestimo devem ser preservados:

- setor de origem;
- setor de destino;
- lider que autorizou;
- data e hora;
- motivo;
- previsao opcional de devolucao.

O emprestimo nao se transforma automaticamente em transferencia permanente.

A devolucao tambem exige confirmacao fisica do setor de origem.

## 15. Operacao iniciada pela bipagem da PDA

No estado inicial do quiosque:

```text
Bipar matricula → iniciar conferencia
Bipar PDA       → abrir menu de acoes
```

Possiveis acoes:

- emprestar;
- devolver emprestimo;
- enviar para manutencao;
- marcar indisponivel;
- tornar disponivel;
- consultar situacao;
- reimprimir etiqueta futuramente.

Qualquer acao que altere estado exige identificacao do lider responsavel.

## 16. Operacao offline do Hub

O Hub pode registrar operacoes sem comunicacao com a API.

Durante a indisponibilidade:

- mostra claramente que esta offline;
- informa a ultima sincronizacao;
- usa a ultima fotografia conhecida;
- permite bipagem;
- armazena localmente a sessao;
- gera identificador unico de operacao;
- nao apresenta o resultado como oficialmente confirmado pela central.

Quando a conexao volta:

1. operacoes sao enviadas na ordem original;
2. a API revalida tudo;
3. cada operacao pode ser aceita, rejeitada ou convertida em divergencia;
4. a fila local so e removida apos confirmacao;
5. conflitos nunca sao sobrescritos silenciosamente.

Exemplo: se dois Hubs offline registrarem a mesma PDA em setores diferentes, a PDA vira divergencia.

## 17. Chamados

Todos os canais criarao o mesmo objeto de chamado.

Dados funcionais esperados:

- solicitante;
- setor do solicitante no momento da abertura;
- canal de origem;
- categoria;
- descricao;
- impacto;
- urgencia;
- prioridade;
- status;
- tecnico responsavel;
- mensagens publicas;
- SLA;
- historico;
- tarefa principal vinculada.

O setor do chamado deve ser preservado historicamente mesmo que o usuario mude de setor depois.

## 18. Ciclo de vida dos chamados

Estados planejados:

```text
NOVO
EM_TRIAGEM
ATRIBUIDO
EM_ATENDIMENTO
AGUARDANDO_SOLICITANTE
AGUARDANDO_TERCEIRO
RESOLVIDO
FECHADO
CANCELADO
REABERTO
```

### Resolvido

A T.I. aplicou uma solucao.

### Fechado

A solucao foi aceita ou o prazo de validacao terminou.

Regra adotada:

- usuario pode confirmar e fechar imediatamente;
- sem contestacao, o chamado fecha automaticamente apos tres dias uteis;
- mensagem publica antes do fechamento reabre o chamado;
- comentarios internos da tarefa nao reiniciam esse prazo.

## 19. Confidencialidade dos chamados

A politica definitiva ainda nao foi aprovada.

O sistema deve reservar a classificacao `RESTRITO`.

Possiveis categorias futuras:

- seguranca da informacao;
- acessos;
- fraude;
- dados pessoais;
- desligamentos;
- credenciais.

Enquanto a regra nao for fechada, nao assumir automaticamente que toda categoria e confidencial.

## 20. Chamados e tarefas

Chamado e tarefa possuem significados diferentes.

### Chamado

Representa:

- problema do usuario;
- compromisso de atendimento;
- comunicacao externa;
- SLA;
- resultado entregue.

### Tarefa

Representa:

- trabalho interno;
- planejamento;
- execucao tecnica;
- checklist;
- comentarios internos;
- prazo interno.

## 21. Sincronismo chamado e tarefa

Todo chamado cria exatamente uma tarefa principal obrigatoria.

```text
1 chamado
=
1 tarefa principal
```

Somente a tarefa principal sincroniza:

- responsavel;
- inicio de atendimento;
- resolucao;
- reabertura;
- cancelamento.

### Criacao

Abrir um chamado cria a tarefa principal na coluna `A Fazer`.

Se a tarefa nao puder ser criada, o chamado tambem nao deve ser confirmado.

### Atribuicao

Atribuir pelo ITSM atualiza a tarefa principal.

Atribuir pelo Planner atualiza o chamado.

### Resolucao

Concluir a tarefa principal resolve o chamado.

Resolver o chamado conclui a tarefa principal.

### Fechamento

Fechar o chamado mantem a tarefa concluida e permite arquivamento.

### Reabertura

Reabrir o chamado reativa a mesma tarefa principal.

Um chamado fechado definitivamente nao deve ter sua tarefa principal reaberta diretamente; um novo chamado relacionado ou tarefa auxiliar deve ser criado.

### Cancelamento

Cancelar chamado cancela a tarefa principal. Cancelado nao equivale a concluido.

## 22. Tarefas auxiliares e independentes

A T.I. pode criar tarefas sem chamado.

Tambem pode ligar tarefas existentes a um chamado como auxiliares.

Um chamado pode ter:

```text
1 tarefa principal obrigatoria
+ 0 ou varias tarefas auxiliares
```

Tarefa auxiliar:

- nao altera status do chamado;
- nao altera responsavel principal;
- nao resolve;
- nao reabre;
- nao muda SLA.

Qualquer integrante da T.I. pode reabrir tarefas independentes ou auxiliares concluidas.

Chamados podem ser fechados mesmo com tarefas auxiliares abertas, mas o ITSM deve alertar.

## 23. Isolamento de comunicacao

### Conversa do chamado

Visivel ao solicitante e demais atores autorizados.

Contem:

- perguntas;
- respostas;
- anexos publicos;
- orientacoes;
- solucao apresentada.

### Comentarios da tarefa

Visiveis somente a T.I.

Contem:

- hipoteses tecnicas;
- discussao interna;
- passos executados;
- coordenacao da equipe;
- documentacao tecnica.

Comentarios internos nunca devem vazar ao solicitante.

## 24. Auditoria

Toda acao relevante deve registrar:

- usuario;
- papel utilizado;
- permissao;
- setor e escopo;
- aplicativo de origem;
- data e hora;
- recurso afetado;
- valor anterior;
- valor novo;
- justificativa;
- identificador de correlacao.

Exemplos obrigatorios:

- mudanca de acesso;
- transferencia;
- emprestimo;
- manutencao;
- regularizacao de divergencia;
- prioridade de chamado;
- atribuicao;
- resolucao;
- reabertura;
- baixa;
- correcao de identificacao.

Nenhuma movimentacao relevante deve ser apagada. Correcoes geram novos registros compensatorios.

## 25. Arquitetura da API

A direcao arquitetural e um monolito modular.

```text
com.gerenciamentoit
├── acesso
├── organizacao
├── ativos
├── operacaopda
├── chamados
├── tarefas
├── fluxoatendimento
├── auditoria
└── shared
```

Regras:

- cada modulo possui suas entidades, aplicacao, persistencia e web;
- nenhum modulo acessa diretamente o repositorio de outro;
- integracoes entre modulos usam interfaces publicas;
- `shared` contem apenas componentes realmente transversais;
- regras de negocio nao ficam nos controllers;
- acoes importantes usam comandos explicitos, nao atualizacoes genericas de status.

## 26. Banco de dados e Hibernate

Nao existe dependencia do banco antigo.

O banco novo e:

```text
gerenciamento_it
```

O MySQL precisa estar executando, mas o banco logico pode nao existir.

A URL usa:

```text
createDatabaseIfNotExist=true
```

Fluxo:

1. aplicacao conecta ao MySQL;
2. Connector/J cria o banco se necessario;
3. Hibernate cria ou atualiza tabelas, indices e relacionamentos;
4. bootstrap cria dados fundamentais.

Configuracao principal:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

Nao usar nesta fase:

- Flyway;
- Liquibase;
- `schema.sql`;
- banco legado.

O usuario MySQL precisa de permissoes para criar banco, tabelas, indices e alterar o schema.

Entidades mutaveis usam controle otimista com `@Version`.

Enums devem ser persistidos como texto, nunca por ordinal.

## 27. Inicializacao do ambiente

Na raiz:

```bash
cp .env.example .env
docker compose up --build
```

Servicos:

```text
API:     http://localhost:8080/api
Swagger: http://localhost:8080/api/swagger-ui.html
Health:  http://localhost:8080/api/actuator/health
MySQL:   localhost:3306
```

## 28. Bootstrap administrativo

Na primeira inicializacao, o sistema cria:

- tipos basicos de ativo;
- primeiro gestor da T.I.;
- atribuicao global `GESTOR_TI`.

Padrao local:

```text
Matricula: ADMIN-LOCAL
Papel: GESTOR_TI
```

Variaveis relevantes:

```text
BOOTSTRAP_ENABLED
BOOTSTRAP_ADMIN_MATRICULA
BOOTSTRAP_ADMIN_NOME
```

O bootstrap deve ser idempotente.

## 29. Convencoes da API

Prefixo:

```text
/api/v1
```

Rotas principais planejadas:

```text
/api/v1/sessoes
/api/v1/usuarios
/api/v1/setores
/api/v1/turnos
/api/v1/tipos-ativo
/api/v1/ativos
/api/v1/conferencias-pda
/api/v1/divergencias
/api/v1/chamados
/api/v1/tarefas
/api/v1/quadros
```

Acoes de negocio devem usar endpoints explicitos, por exemplo:

```text
POST /ativos/{id}/liberacoes
POST /ativos/{id}/emprestimos
POST /ativos/{id}/indisponibilidades
POST /ativos/{id}/encaminhamentos-manutencao
POST /ativos/{id}/baixas
```

## 30. Idempotencia e concorrencia

Operacoes do Hub precisam ter identificador unico.

Exemplo:

```http
Idempotency-Key: <identificador>
```

Objetivos:

- evitar processamento duplicado;
- permitir reenvio offline;
- devolver resultado anterior para a mesma operacao;
- detectar conflitos;
- impedir sobrescrita silenciosa.

Conflitos relevantes retornam `409 Conflict` e podem gerar divergencia operacional.

## 31. Regras consolidadas

1. Existe uma unica fonte de verdade.
2. Os aplicativos sao visoes especializadas do mesmo produto.
3. A API decide autorizacao por usuario, papel, permissao e escopo.
4. WhatsApp fica para uma etapa futura.
5. Operadores nao recebem PDAs individualmente.
6. PDAs pertencem a pools setoriais.
7. Abertura de turno exige conferencia.
8. Encerramento de turno e facultativo, mas gera responsabilidade.
9. PDA de outro setor sem emprestimo vira divergencia.
10. PDA faltante gera divergencia.
11. Divergencias nao expiram automaticamente.
12. Supervisor regulariza divergencias.
13. Lider pode autorizar emprestimo temporario.
14. Emprestimo preserva origem e registra destino.
15. Lider pode enviar PDA para manutencao.
16. Manutencao remove a PDA do pool do setor.
17. PDA indisponivel continua pertencendo ao setor.
18. Numero de serie e obrigatorio.
19. Patrimonio e opcional.
20. Todo chamado cria uma tarefa principal.
21. Tarefas auxiliares nao sincronizam status.
22. Comentarios tecnicos permanecem internos.
23. Chamado resolvido fecha automaticamente apos tres dias uteis sem contestacao.
24. Hub pode operar offline.
25. Conflitos offline geram divergencia.
26. Historico nunca e apagado silenciosamente.

## 32. Pontos ainda abertos

Ainda precisam de definicao futura:

- categorias confidenciais definitivas;
- regras detalhadas do WhatsApp;
- verificacao inicial do numero de telefone;
- politica completa de SLA;
- catalogo inicial de categorias de chamados;
- metricas e relatorios finais;
- desenho visual definitivo dos frontends;
- politica de anexos;
- regras de notificacao;
- politica de backup e recuperacao;
- regras de aprovacao patrimonial para baixa.

## 33. Ordem recomendada das proximas entregas

1. completar alocacao e ciclo de vida dos ativos;
2. implementar conferencia de PDA;
3. implementar emprestimos, indisponibilidades e manutencoes;
4. implementar divergencias e regularizacao;
5. implementar chamados;
6. implementar tarefas e Kanban;
7. implementar sincronismo chamado-tarefa;
8. construir os frontends especializados;
9. especificar e implementar WhatsApp.

## 34. Regra de manutencao deste documento

Toda mudanca de produto deve responder:

- qual regra anterior mudou;
- qual ator e afetado;
- qual fluxo e afetado;
- qual excecao foi criada;
- qual efeito existe sobre auditoria e historico.

Depois da decisao, este README deve ser atualizado para continuar funcionando como documento de contexto do projeto.

Para detalhes tecnicos da API ja implementada, consulte [`API/README.md`](API/README.md).
