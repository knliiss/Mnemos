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

interface TelegramDeviceStorage {
  setItem(
    key: string,
    value: string,
    callback?: (error: string | null, stored?: boolean) => void,
  ): TelegramDeviceStorage

  getItem(
    key: string,
    callback: (error: string | null, value?: string | null) => void,
  ): TelegramDeviceStorage
}

interface TelegramSecureStorage {
  setItem(
    key: string,
    value: string,
    callback?: (error: string | null, stored?: boolean) => void,
  ): TelegramSecureStorage

  getItem(
    key: string,
    callback: (error: string | null, value?: string | null, canRestore?: boolean) => void,
  ): TelegramSecureStorage

  removeItem(
    key: string,
    callback?: (error: string | null, removed?: boolean) => void,
  ): TelegramSecureStorage
}

interface TelegramSafeAreaInset {
  top: number
  right: number
  bottom: number
  left: number
}

type TelegramSafeAreaEvent = "safeAreaChanged" | "contentSafeAreaChanged"

interface TelegramWebApp {
  initData: string
  initDataUnsafe?: {
    user?: {
      language_code?: string
    }
  }
  colorScheme?: "light" | "dark"
  platform?: string
  safeAreaInset?: TelegramSafeAreaInset
  contentSafeAreaInset?: TelegramSafeAreaInset
  DeviceStorage?: TelegramDeviceStorage
  SecureStorage?: TelegramSecureStorage

  ready(): void

  expand(): void

  close(): void

  requestFullscreen?(): void

  disableVerticalSwipes?(): void

  setHeaderColor(color: string): void

  setBackgroundColor(color: string): void

  setBottomBarColor?(color: string): void

  isVersionAtLeast?(version: string): boolean

  onEvent?(event: TelegramSafeAreaEvent, callback: () => void): void

  offEvent?(event: TelegramSafeAreaEvent, callback: () => void): void

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
