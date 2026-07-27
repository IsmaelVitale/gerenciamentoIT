# Bot do WhatsApp

Servico Node.js que recebe comandos pelo WhatsApp e prepara a abertura de chamados na API central do GerenciamentoIT.

## Estado da integracao

O modulo de chamados ainda nao existe na API Spring Boot. Por isso, o bot inicia com `SIMULATION_MODE=true` e nao cria registros reais. Quando o endpoint estiver implementado, configure `SIMULATION_MODE=false`.

Contrato esperado:

```http
POST /api/api/v1/chamados
Content-Type: application/json
```

```json
{
  "telefoneUsuario": "5511999999999",
  "problema": "Leitor de codigo de barras travado",
  "origem": "WHATSAPP",
  "identificadorMensagem": "identificador-unico-da-mensagem"
}
```

A resposta deve conter `protocolo` ou `id`.

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

Na primeira execucao, escaneie o QR Code exibido no terminal. A sessao fica persistida no caminho definido por `WHATSAPP_SESSION_PATH`.

## Comandos

```text
chamado <descricao>
!chamado <descricao>
ajuda
menu
```

## Variaveis

| Variavel | Padrao | Finalidade |
| --- | --- | --- |
| `API_BASE_URL` | `http://localhost:8080/api` | URL base da API |
| `API_TIMEOUT_MS` | `10000` | timeout das requisicoes |
| `SIMULATION_MODE` | `true` | impede chamadas reais enquanto o backend nao existe |
| `WHATSAPP_SESSION_PATH` | `.wwebjs_auth` | diretorio local da sessao |

## Seguranca e operacao

A integracao usa `whatsapp-web.js`, que automatiza o WhatsApp Web e nao e a API oficial da Meta. Antes de uso corporativo em producao, avalie migrar para a WhatsApp Business Platform, definir consentimento, retencao de mensagens, limites de envio e vinculacao segura entre telefone e usuario.
