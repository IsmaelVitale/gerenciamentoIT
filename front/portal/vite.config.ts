import { resolve } from 'node:path'
import { defineConfig } from 'vite'

const apiProxy = {
  target: 'http://localhost:8080',
  changeOrigin: true,
}

export default defineConfig({
  server: {
    port: 5174,
    strictPort: true,
    proxy: {
      '/api': apiProxy,
    },
  },
  preview: {
    port: 4174,
    strictPort: true,
    proxy: {
      '/api': apiProxy,
    },
  },
  build: {
    rollupOptions: {
      input: {
        portal: resolve(__dirname, 'index.html'),
        hubPdas: resolve(__dirname, 'apps/hub-pdas.html'),
        gestaoOperacional: resolve(__dirname, 'apps/gestao-operacional.html'),
        gestaoAtivos: resolve(__dirname, 'apps/gestao-ativos.html'),
        atendimentoItsm: resolve(__dirname, 'apps/atendimento-itsm.html'),
        plannerTi: resolve(__dirname, 'apps/planner-ti.html'),
      },
    },
  },
})
