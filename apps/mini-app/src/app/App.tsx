import { AppProvider } from "@/app/providers/AppProvider"
import { RealtimeProvider } from "@/app/providers/RealtimeProvider"
import { AuthGate } from "@/features/auth/ui/AuthGate"
import { NotificationHost } from "@/shared/notifications/NotificationHost"
import { LanguageSync } from "@/ui/data/LanguageSync"
import { MnemosDataProvider } from "@/ui/data/MnemosDataProvider"
import { AppShell } from "@/ui/shell/AppShell"
import { RootErrorBoundary } from "@/ui/system/RootErrorBoundary"

export function App() {
  return (
    <>
      <NotificationHost />
      <RootErrorBoundary>
        <AuthGate>
          <RealtimeProvider>
            <AppProvider>
              <MnemosDataProvider>
                <LanguageSync>
                  <AppShell />
                </LanguageSync>
              </MnemosDataProvider>
            </AppProvider>
          </RealtimeProvider>
        </AuthGate>
      </RootErrorBoundary>
    </>
  )
}
