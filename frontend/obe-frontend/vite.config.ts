import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const projectRoot = path.resolve(process.cwd())

export default defineConfig({
  root: projectRoot,
  plugins: [vue()],
  resolve: { alias: { '@': path.resolve(projectRoot, 'src') } },
  server: { port: 5173 },
})
