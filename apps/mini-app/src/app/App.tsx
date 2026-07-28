import {BrowserRouter} from "react-router-dom"
import {AppProvider} from "@/app/providers/AppProvider"
import {AppRouter} from "@/app/router/AppRouter"
import {AuthGate} from "@/features/auth/ui/AuthGate"
import {NotificationHost} from "@/shared/notifications/NotificationHost"
import {RootErrorBoundary} from "@/shared/errors/RootErrorBoundary"

export function App() {
    return <>
        <NotificationHost/>
        <RootErrorBoundary>
            <AuthGate>
                <AppProvider>
                    <BrowserRouter><AppRouter/></BrowserRouter>
                </AppProvider>
            </AuthGate>
        </RootErrorBoundary>
    </>
}
