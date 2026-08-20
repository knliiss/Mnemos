import { AppProvider } from "@/app/providers/AppProvider"
import { RealtimeProvider } from "@/app/providers/RealtimeProvider"
import { AuthGate } from "@/features/auth/ui/AuthGate"
import { ProductDataProvider } from "@/ui/data/ProductDataProvider"
import { ProductLanguageSync } from "@/ui/data/ProductLanguageSync"
import { ProductShell } from "@/ui/shell/ProductShell"
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
                <ProductLanguageSync>
                  <ProductShell />
                </ProductLanguageSync>
              </ProductDataProvider>
            </RealtimeProvider>
          </AppProvider>
        </AuthGate>
      </RootErrorBoundary>
    </>
  )
}
