import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    // 개발 중에는 /api 요청을 스프링 서버(8080)로 넘김
    // → 브라우저 입장에서는 같은 주소라서 CORS 설정이 필요 없음
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
