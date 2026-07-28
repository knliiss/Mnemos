import {Component, type ErrorInfo, type ReactNode} from "react"
import {handleError} from "./errorHandler"

interface State { error: Error | null }
export class RootErrorBoundary extends Component<{children: ReactNode}, State> {
    state: State = {error: null}
    static getDerivedStateFromError(error: Error): State { return {error} }
    componentDidCatch(error: Error, info: ErrorInfo) { handleError(error); console.error(info) }
    render() {
        return this.state.error ? <div className="fatal-error"><h1>Mnemos</h1><p>{this.state.error.message}</p><button onClick={() => location.reload()}>Reload</button></div> : this.props.children
    }
}
