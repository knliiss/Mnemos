import { AppProvider } from "@/app/providers/AppProvider"
import { RealtimeProvider } from "@/app/providers/RealtimeProvider"
import { AuthGate } from "@/features/auth/ui/AuthGate"
import { RootErrorBoundary } from "@/shared/errors/RootErrorBoundary"
import { NotificationHost } from "@/shared/notifications/NotificationHost"
import { LanguageSync } from "@/ui/data/LanguageSync"
import { MnemosDataProvider } from "@/ui/data/MnemosDataProvider"
import { AppShell } from "@/ui/shell/AppShell"

export function App() {
  return (
    <>
      <NotificationHost />
      <RootErrorBoundary>
        <AuthGate>
          <AppProvider>
            <RealtimeProvider>
              <MnemosDataProvider>
                <LanguageSync>
                  <AppShell />
                </LanguageSync>
              </MnemosDataProvider>
            </RealtimeProvider>
          </AppProvider>
        </AuthGate>
      </RootErrorBoundary>
    </>
  )
}
