import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    // In dev, /api goes to the catalog-service port-forward (localhost:8080), the same path
    // nginx proxies in the cluster, so the browser never needs CORS.
    proxy: {
      // Everything the gateway or Keycloak serves; run the compose stack (port 3000) behind it.
      '^/(api|oauth2|login|logout|auth)(/|$)': { target: process.env.API_URL ?? 'http://localhost:3000', changeOrigin: false },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['src/test-setup.ts'],
  },
})
