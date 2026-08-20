import { Component, type ErrorInfo, type ReactNode } from "react"
import { i18n } from "@/shared/i18n"
import { handleError } from "./errorHandler"

interface State {
  error: Error | null
}
export class RootErrorBoundary extends Component<{ children: ReactNode }, State> {
  state: State = { error: null }
  static getDerivedStateFromError(error: Error): State {
    return { error }
  }
  componentDidCatch(error: Error, info: ErrorInfo) {
    handleError(error)
    console.error(info)
  }
  render() {
    return this.state.error ? (
      <div className="fatal-error" role="alert">
        <h1>Mnemos</h1>
        <p>{i18n.t("errorBoundary.message")}</p>
        <button type="button" onClick={() => location.reload()}>
          {i18n.t("errorBoundary.reload")}
        </button>
      </div>
    ) : (
      this.props.children
    )
  }
}
