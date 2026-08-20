import { AppProvider } from "@/app/providers/AppProvider"
import { RealtimeProvider } from "@/app/providers/RealtimeProvider"
import { AuthGate } from "@/features/auth/ui/AuthGate"
import { ProductDataProvider } from "@/ui/data/ProductDataProvider"
import { LanguageSync } from "@/ui/data/LanguageSync"
import { AppShell } from "@/ui/shell/AppShell"
import "@/ui/styles.css"
import { RootErrorBoundary } from "@/shared/errors/RootErrorBoundary"
import { NotificationHost } from "@/shared/notifications/NotificationHost"

export function App() {
  return (
    <>
      <NotificationHost />
      <RootErrorBoundary>
        <AuthGate>
          <AppProvider>
            <RealtimeProvider>
              <ProductDataProvider>
                <LanguageSync>
                  <AppShell />
                </LanguageSync>
              </ProductDataProvider>
            </RealtimeProvider>
          </AppProvider>
        </AuthGate>
      </RootErrorBoundary>
    </>
  )
}
