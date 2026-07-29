# Hub de PDAs

SPA/PWA desktop-first do GerenciamentoIT para conferência e operação dos pools de PDAs.

Esta primeira entrega é visual e executável. Ela utiliza um contexto simulado para permitir a validação das telas enquanto os endpoints específicos do Hub ainda não existem na API.

## Tecnologias

- React;
- TypeScript;
- Vite;
- vite-plugin-pwa;
- CSS responsivo sem biblioteca visual externa.

## Requisitos

- Node.js 20.19 ou superior;
- npm 10 ou superior;
- API GerenciamentoIT local para as integrações futuras.

## Execução local

Na raiz do repositório:

```bash
cd front/hub-pdas
cp .env.example .env
npm install
npm run dev
```

Abra o endereço informado pelo Vite, normalmente:

```text
http://localhost:5173
```

No Windows PowerShell, caso `cp` não esteja disponível:

```powershell
Copy-Item .env.example .env
```

## Visualização do fluxo

1. A tela inicial mantém o campo de matrícula focado para aceitar leitor de crachá configurado como teclado.
2. No modo simulado, digite qualquer matrícula e pressione `Enter` ou clique em `Continuar`.
3. A tela inicial operacional mostrará um contexto simulado de Recebimento / Turno T1.
4. As ações ainda não integradas exibem um aviso sem persistir mudanças.
5. `Sair e bloquear o terminal` retorna para a identificação.

## Configuração

Crie um `.env` a partir do `.env.example`.

```text
VITE_API_BASE_URL=http://localhost:8080/api
VITE_SIMULATION_MODE=true
```

| Variável | Finalidade |
| --- | --- |
| `VITE_API_BASE_URL` | Endereço da API local Spring Boot |
| `VITE_SIMULATION_MODE` | Mantém as telas independentes dos endpoints ainda não implementados |

O endereço da API não deve ser fixado nos componentes. Quando houver hospedagem, uma configuração específica de produção será fornecida pelo ambiente.

## PWA local

O Service Worker é gerado no build de produção.

```bash
npm run build
npm run preview
```

Abra o endereço de preview no navegador para validar manifesto, cache do app shell e instalação local. A fila offline de operações de negócio ainda não está implementada.

## Validação

```bash
npm run lint
npm run build
```

## Escopo atual

Incluído:

- estrutura React/TypeScript/Vite;
- fundação SPA/PWA;
- tela de identificação;
- foco no leitor de crachá;
- tela inicial operacional;
- indicador online/offline;
- layout principal para PCs;
- adaptação para tablet em modo paisagem;
- configuração da API local;
- modo simulado.

Fora desta entrega:

- autenticação real;
- conferência por bipagem;
- consulta e ações rápidas sobre PDAs;
- persistência em IndexedDB;
- fila offline;
- sincronização;
- integração com os endpoints do Hub;
- hospedagem.
