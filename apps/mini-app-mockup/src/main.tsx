import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import App from "./App"
import "./styles.css"
import "./feature-details.css"
import "./event-details.css"
import "./account-details.css"
import "./visual-system.css"

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
