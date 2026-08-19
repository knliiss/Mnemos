import { AppProvider } from "@/app/providers/AppProvider"
import { RealtimeProvider } from "@/app/providers/RealtimeProvider"
import { AuthGate } from "@/features/auth/ui/AuthGate"
import ProductApp from "@/product-ui/ProductApp"
import { ProductDataProvider } from "@/product-ui/data/ProductDataProvider"
import { ProductLanguageSync } from "@/product-ui/data/ProductLanguageSync"
import "@/product-ui/product.css"
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
                  <ProductApp />
                </ProductLanguageSync>
              </ProductDataProvider>
            </RealtimeProvider>
          </AppProvider>
        </AuthGate>
      </RootErrorBoundary>
    </>
  )
}
