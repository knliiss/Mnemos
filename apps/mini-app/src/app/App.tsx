import {BrowserRouter} from "react-router-dom"
import {AppProvider} from "@/app/providers/AppProvider"
import {RealtimeProvider} from "@/app/providers/RealtimeProvider"
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
                    <RealtimeProvider>
                        <BrowserRouter><AppRouter/></BrowserRouter>
                    </RealtimeProvider>
                </AppProvider>
            </AuthGate>
        </RootErrorBoundary>
    </>
}
