import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const apiTarget = process.env.GREYHAVEN_API_PROXY ?? 'http://localhost:8080'

const apiProxy = {
  '/api': {
    target: apiTarget,
    changeOrigin: true,
  },
  '/actuator': {
    target: apiTarget,
    changeOrigin: true,
  },
}

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: apiProxy,
  },
  preview: {
    port: 4173,
    proxy: apiProxy,
  },
})
