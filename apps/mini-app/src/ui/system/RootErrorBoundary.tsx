import { Component, type ErrorInfo, type ReactNode } from "react"
import { i18n } from "@/shared/i18n"
import { handleError } from "@/shared/errors/errorHandler"
import { ErrorMascot } from "../components/errors/ErrorMascot"
import "./root-error.css"

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
      <main className="root-error-screen" role="alert" aria-live="assertive">
        <section className="root-error-screen__panel">
          <div className="root-error-screen__illustration" aria-hidden="true">
            <ErrorMascot variant="internal" />
          </div>

          <div className="root-error-screen__copy">
            <span>{i18n.t("errorNotifications.tones.internal.eyebrow")}</span>
            <h1>{i18n.t("errorNotifications.tones.internal.title")}</h1>
            <p>{i18n.t("errorBoundary.message")}</p>
          </div>

          <button type="button" onClick={() => window.location.reload()}>
            {i18n.t("errorBoundary.reload")}
          </button>
        </section>
      </main>
    )
  }
}
