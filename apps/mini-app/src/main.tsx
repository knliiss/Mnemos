import {StrictMode} from "react"
import {createRoot} from "react-dom/client"
import "@/shared/i18n"
import "@/shared/styles/index.css"
import {App} from "@/app/App"
import {installGlobalErrorHandlers} from "@/shared/errors/errorHandler"

installGlobalErrorHandlers()
createRoot(document.getElementById("root")!).render(<StrictMode><App/></StrictMode>)
