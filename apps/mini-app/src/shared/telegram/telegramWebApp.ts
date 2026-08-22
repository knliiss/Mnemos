type TelegramWebApp = {
  initData?: string
  platform?: string
  ready?: () => void
  expand?: () => void
  requestFullscreen?: () => void
  disableVerticalSwipes?: () => void
  setHeaderColor?: (color: string) => void
  setBackgroundColor?: (color: string) => void
  setBottomBarColor?: (color: string) => void
  isVersionAtLeast?: (version: string) => boolean
}

declare global {
  interface Window {
    Telegram?: {
      WebApp?: TelegramWebApp
    }
  }
}

const TELEGRAM_BACKGROUND = "#050604"
const TELEGRAM_CHROME = "#07090b"

function isTelegramRuntime(webApp: TelegramWebApp): boolean {
  return Boolean(
    webApp.initData ||
      (webApp.platform && webApp.platform !== "unknown") ||
      window.location.hash.includes("tgWebAppVersion="),
  )
}

function supports(webApp: TelegramWebApp, version: string): boolean {
  return webApp.isVersionAtLeast?.(version) ?? false
}

export function initializeTelegramWebApp(): void {
  const webApp = window.Telegram?.WebApp

  if (!webApp || !isTelegramRuntime(webApp)) {
    return
  }

  document.documentElement.classList.add("telegram-mini-app")

  webApp.ready?.()
  webApp.expand?.()
  webApp.setHeaderColor?.(TELEGRAM_CHROME)
  webApp.setBackgroundColor?.(TELEGRAM_BACKGROUND)

  if (supports(webApp, "7.10")) {
    webApp.setBottomBarColor?.(TELEGRAM_BACKGROUND)
  }

  if (supports(webApp, "7.7")) {
    webApp.disableVerticalSwipes?.()
  }

  if (supports(webApp, "8.0")) {
    try {
      webApp.requestFullscreen?.()
    } catch {
      // Telegram can reject fullscreen on clients that advertise support but do not allow it yet.
      // expand() above remains the safe fallback.
    }
  }
}
