import { defineConfig, loadEnv } from "vite"
import react from "@vitejs/plugin-react"

const DEFAULT_DEV_ALLOWED_HOST = "app.knalis.rest"

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "")
  const devAllowedHost = env.VITE_DEV_ALLOWED_HOST?.trim() || DEFAULT_DEV_ALLOWED_HOST

  return {
    plugins: [react()],
    resolve: { alias: { "@": "/src" } },
    server: { allowedHosts: [devAllowedHost] },
  }
})
