import { Component, type ErrorInfo, type ReactNode } from "react"
import { i18n } from "@/shared/i18n"
import { handleError } from "./errorHandler"

type RootErrorBoundaryProps = {
  children: ReactNode
}

type RootErrorBoundaryState = {
  error: Error | null
}

export class RootErrorBoundary extends Component<RootErrorBoundaryProps, RootErrorBoundaryState> {
  state: RootErrorBoundaryState = {
    error: null,
  }

  static getDerivedStateFromError(error: Error): RootErrorBoundaryState {
    return {
      error,
    }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    handleError(error)
    console.error(info)
  }

  render() {
    if (!this.state.error) {
      return this.props.children
    }

    return (
      <div className="fatal-error" role="alert">
        <h1>Mnemos</h1>
        <p>{i18n.t("errorBoundary.message")}</p>
        <button type="button" onClick={() => window.location.reload()}>
          {i18n.t("errorBoundary.reload")}
        </button>
      </div>
    )
  }
}
