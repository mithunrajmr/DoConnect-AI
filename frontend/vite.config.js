import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    proxy: {
      // Main backend REST API (Spring Boot, port 8080)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // Chat microservice REST endpoints (port 8090)
      '/chat-api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/chat-api/, '/api/chat'),
      },
      // Chat microservice WebSocket — proxied so the browser Origin header
      // always matches the chat service's allowed-origins list regardless
      // of which port Vite happens to bind to.
      '/ws-chat': {
        target: 'ws://localhost:8090',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ws-chat/, '/ws'),
      },
      // Main backend WebSocket for notifications (port 8080)
      '/ws-notify': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/ws-notify/, '/ws/notifications'),
      },
    },
  },
})
