# Hub de PDAs

SPA/PWA desktop-first do GerenciamentoIT para conferência e operação dos pools de PDAs.

O Hub utiliza a API local real para identificar o usuário e carregar os contextos operacionais autorizados. Conferência, ações rápidas e sincronização offline permanecem para as próximas etapas do MVP.

## Tecnologias

- React;
- TypeScript;
- Vite;
- vite-plugin-pwa;
- CSS responsivo sem biblioteca visual externa.

## Requisitos

- Node.js 20.19 ou superior;
- npm 10 ou superior;
- API GerenciamentoIT em `http://localhost:8080/api`;
- MySQL usado pela API.

## Execução local

Primeiro, inicie a API na raiz do repositório:

```bash
docker compose up --build
```

Em outro terminal:

```bash
cd front/hub-pdas
cp .env.example .env
npm install
npm run dev
```

No Windows PowerShell, use:

```powershell
Copy-Item .env.example .env
npm install
npm run dev
```

Abra o endereço informado pelo Vite, normalmente:

```text
http://localhost:5173
```

## Primeiro contexto local

O bootstrap cria o usuário `ADMIN-LOCAL`, mas não inventa setores ou turnos de negócio. Em um banco novo, crie ao menos um setor e um turno pela API antes de entrar no Hub.

Exemplo no PowerShell:

```powershell
$session = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/sessoes" `
  -ContentType "application/json" `
  -Body '{"matricula":"ADMIN-LOCAL","origemAplicacao":"HUB_PDA"}'

$headers = @{ Authorization = "Bearer $($session.token)" }

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/setores" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"codigo":"RECEBIMENTO","nome":"Recebimento","cotaPdas":12}'

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/api/v1/turnos" `
  -Headers $headers `
  -ContentType "application/json" `
  -Body '{"codigo":"T1","nome":"Turno 1","horaInicio":"06:00:00","horaFim":"14:00:00"}'
```

Depois, informe `ADMIN-LOCAL` na tela de identificação. Como esse usuário possui escopo global, a API disponibilizará as combinações ativas de setor e turno.

## Fluxo real disponível

1. O campo de matrícula permanece focado para aceitar um crachá configurado como teclado.
2. O Hub cria a sessão com `POST /api/v1/sessoes` e origem `HUB_PDA`.
3. O token opaco é mantido em `sessionStorage`, somente durante a sessão do navegador.
4. O Hub carrega `GET /api/v1/me/contextos-operacionais`.
5. Um único contexto é selecionado automaticamente; múltiplos contextos abrem a tela de seleção.
6. O logout revoga a sessão na API e limpa o estado local.
7. Erros de matrícula, sessão expirada, API indisponível e ausência de contexto são mostrados na interface.

## Configuração

Crie um `.env` a partir do `.env.example`.

```text
VITE_API_BASE_URL=http://localhost:8080/api
```

O endereço da API não fica fixado nos componentes. Uma configuração específica de produção será fornecida pelo ambiente quando o MVP estiver pronto para hospedagem.

## PWA local

O Service Worker é gerado no build de produção.

```bash
npm run build
npm run preview
```

A fila offline de operações de negócio ainda não está implementada. Sem API, uma nova identificação real não pode ser concluída.

## Validação

```bash
npm run lint
npm run build
```

Na API:

```bash
./mvnw verify
```

## Escopo atual

Incluído:

- estrutura React/TypeScript/Vite;
- fundação SPA/PWA;
- identificação real pela API;
- restauração e revogação da sessão;
- contexto operacional universal;
- seleção de contexto;
- tratamento de carregamento, vazio e erro;
- indicador online/offline;
- layout principal para PCs;
- adaptação para tablet em modo paisagem;
- configuração da API local.

Fora desta entrega:

- conferência por bipagem;
- consulta e ações rápidas sobre ativos;
- persistência em IndexedDB;
- fila offline;
- sincronização de operações;
- hospedagem.
