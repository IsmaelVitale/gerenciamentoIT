# Portal MPA do GerenciamentoIT

Portal temporário para navegar e acompanhar os frontends independentes do projeto durante o desenvolvimento do MVP.

## Aplicações exibidas

- Hub de PDAs — em teste e com atalho para a aplicação local.
- Gestão Operacional — planejada.
- Gestão de Ativos da T.I. — fundação disponível na API.
- Atendimento ITSM — fundação disponível na API.
- Planner da T.I. — planejado.

O portal não simula funcionalidades inexistentes. As páginas ainda não implementadas apresentam apenas o escopo, a base disponível e os próximos passos.

## Executar localmente

Com a API iniciada pelo IntelliJ na porta `8080`:

```powershell
cd .\front\portal
Copy-Item .env.example .env
npm install
npm run dev
```

Abra `http://localhost:5174`.

Para usar o atalho do Hub de PDAs, execute também a aplicação em `front/hub-pdas`, que usa por padrão `http://localhost:5173`.

## Variáveis

| Variável | Padrão | Uso |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `/api` | Endereço da API. Em desenvolvimento, o Vite encaminha para `http://localhost:8080`. |
| `VITE_HUB_PDAS_URL` | `http://localhost:5173` | Endereço local do Hub de PDAs. |

## Arquitetura

O portal usa Vite e TypeScript sem framework. Cada frontend possui um arquivo HTML de entrada próprio em `apps/`, caracterizando a navegação como MPA. O menu e os componentes visuais são compartilhados pelo arquivo `src/main.ts`.
