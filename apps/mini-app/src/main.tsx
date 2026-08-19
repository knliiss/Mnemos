import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import { App } from "@/app/App"
import { installGlobalErrorHandlers } from "@/shared/errors/errorHandler"
import "@/shared/i18n"
import "@/shared/styles/index.css"

installGlobalErrorHandlers()

const root = document.getElementById("root")

if (!root) {
  throw new Error("Mini App root element is missing")
}

createRoot(root).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
