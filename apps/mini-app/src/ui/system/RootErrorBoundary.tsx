import { Component, type ErrorInfo, type ReactNode } from "react"
import { handleError } from "@/shared/errors/errorHandler"
import { i18n } from "@/shared/i18n"
import { ErrorMascot } from "../components/errors/ErrorMascot"
import "./service-unavailable.css"

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
      <main className="service-unavailable" role="alert" aria-live="assertive">
        <section className="service-unavailable__panel">
          <div className="service-unavailable__illustration" aria-hidden="true">
            <ErrorMascot variant="internal" />
          </div>

          <div className="service-unavailable__copy">
            <span className="service-unavailable__eyebrow">
              {i18n.t("errorNotifications.tones.internal.eyebrow")}
            </span>
            <h1>{i18n.t("errorNotifications.tones.internal.title")}</h1>
            <p>{i18n.t("errorBoundary.message")}</p>
          </div>

          <button
            type="button"
            className="service-unavailable__retry"
            onClick={() => window.location.reload()}
          >
            {i18n.t("errorBoundary.reload")}
          </button>
        </section>
      </main>
    )
  }
}
