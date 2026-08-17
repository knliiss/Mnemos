import { StrictMode } from "react"
import { createRoot } from "react-dom/client"
import AppFinal from "./AppFinal"
import "./styles.css"
import "./feature-details.css"
import "./event-details.css"
import "./account-details.css"
import "./bets-details.css"
import "./skin-details.css"
import "./visual-system.css"
import "./redesign-2026.css"
import "./final-revision.css"
import "./final-fixes.css"

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <AppFinal />
  </StrictMode>,
)
