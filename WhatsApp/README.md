# Bot do WhatsApp

Servico Node.js que abre chamados na API central do GerenciamentoIT por meio de uma conversa direta no WhatsApp.

## Estado da integracao

O backend de chamados e o contrato real estao implementados. O bot continua iniciando com `SIMULATION_MODE=true` por seguranca; para gravar chamados, execute a API e configure `SIMULATION_MODE=false`.

Fluxo:

1. o usuario envia `chamado <descricao>`;
2. o bot solicita a matricula;
3. a matricula cria uma sessao provisoria com origem `WHATSAPP`;
4. a API abre o chamado para o usuario autenticado;
5. a sessao temporaria e revogada;
6. o bot devolve o protocolo.

O telefone e armazenado apenas como contato. A identidade e definida pela sessao autenticada e nunca pelo numero recebido no payload.

## Contrato utilizado

Criacao da sessao:

```http
POST /api/v1/sessoes
Content-Type: application/json
```

```json
{
  "matricula": "123456",
  "origemAplicacao": "WHATSAPP"
}
```

Abertura idempotente:

```http
POST /api/v1/chamados
Authorization: Bearer <token>
Idempotency-Key: <identificador-da-mensagem>
Content-Type: application/json
```

```json
{
  "descricao": "Leitor de codigo de barras travado",
  "telefoneContato": "5511999999999",
  "identificadorExterno": "identificador-unico-da-mensagem"
}
```

A API retorna o mesmo chamado quando a mesma mensagem e reprocessada.

## Requisitos

- Node.js 20 ou superior;
- Chromium e bibliotecas exigidas pelo Puppeteer;
- telefone com WhatsApp para escanear o QR Code;
- API GerenciamentoIT acessivel quando o modo de simulacao estiver desativado.

## Execucao

```bash
cd WhatsApp
cp .env.example .env
npm install
npm start
```

Na primeira execucao, escaneie o QR Code exibido no terminal. A sessao do WhatsApp fica persistida no caminho definido por `WHATSAPP_SESSION_PATH`.

Para ativar a integracao real:

```env
SIMULATION_MODE=false
API_BASE_URL=http://localhost:8080/api
```

## Comandos

```text
chamado <descricao>
!chamado <descricao>
cancelar
ajuda
menu
```

Chamados somente sao aceitos em conversas diretas. Comandos enviados em grupos sao recusados para reduzir exposicao de dados pessoais e tecnicos.

## Variaveis

| Variavel | Padrao | Finalidade |
| --- | --- | --- |
| `API_BASE_URL` | `http://localhost:8080/api` | URL base da API, incluindo o contexto `/api` |
| `API_TIMEOUT_MS` | `10000` | timeout das requisicoes |
| `SIMULATION_MODE` | `true` | impede gravacoes reais enquanto o ambiente e validado |
| `PENDING_TICKET_TTL_MS` | `300000` | prazo para o usuario informar a matricula |
| `WHATSAPP_SESSION_PATH` | `.wwebjs_auth` | diretorio local da sessao do WhatsApp Web |

## Seguranca e operacao

A autenticacao somente por matricula e provisoria e segue o estado atual da API. Antes de producao, deve ser substituida ou reforcada por senha, SSO, cracha seguro, OTP ou outro fator.

A integracao usa `whatsapp-web.js`, que automatiza o WhatsApp Web e nao e a API oficial da Meta. Para uso corporativo em producao, avalie a WhatsApp Business Platform e defina consentimento, retencao de mensagens, limites de envio, monitoramento e recuperacao da sessao.
