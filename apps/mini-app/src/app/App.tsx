import { AppProvider } from "@/app/providers/AppProvider"
import { RealtimeProvider } from "@/app/providers/RealtimeProvider"
import { AuthGate } from "@/features/auth/ui/AuthGate"
import AppFinal from "@/product-ui/AppFinal"
import { ProductDataProvider } from "@/product-ui/data/ProductDataProvider"
import "@/product-ui/productStyles"
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
                <AppFinal />
              </ProductDataProvider>
            </RealtimeProvider>
          </AppProvider>
        </AuthGate>
      </RootErrorBoundary>
    </>
  )
}
