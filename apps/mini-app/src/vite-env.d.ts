/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_KEYCLOAK_URL: string
  readonly VITE_KEYCLOAK_REALM: string
  readonly VITE_KEYCLOAK_CLIENT_ID: string
  readonly VITE_API_URL: string
  readonly VITE_TELEGRAM_INIT_DATA_MAX_AGE_SECONDS?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

interface Window {
  Telegram?: {
    WebApp: TelegramWebApp
  }
}

interface TelegramWebApp {
  initData: string
  initDataUnsafe?: {
    user?: {
      language_code?: string
    }
  }
  colorScheme?: "light" | "dark"

  ready(): void

  expand(): void

  close(): void

  setHeaderColor(color: string): void

  setBackgroundColor(color: string): void

  BackButton?: {
    show(): void
    hide(): void
    onClick(callback: () => void): void
    offClick(callback: () => void): void
  }

  HapticFeedback?: {
    impactOccurred(style: "light" | "medium" | "heavy"): void
    notificationOccurred(type: "error" | "success" | "warning"): void
  }
}
