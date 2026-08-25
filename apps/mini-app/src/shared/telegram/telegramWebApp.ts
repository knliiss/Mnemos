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

function safeInsetValue(value: number | undefined): number {
  return typeof value === "number" && Number.isFinite(value) && value >= 0 ? value : 0
}

function applyInsetVariables(prefix: "safe" | "content-safe", inset?: TelegramSafeAreaInset): void {
  if (!inset) {
    return
  }

  const root = document.documentElement

  root.style.setProperty(`--tg-${prefix}-area-inset-top`, `${safeInsetValue(inset.top)}px`)
  root.style.setProperty(`--tg-${prefix}-area-inset-right`, `${safeInsetValue(inset.right)}px`)
  root.style.setProperty(`--tg-${prefix}-area-inset-bottom`, `${safeInsetValue(inset.bottom)}px`)
  root.style.setProperty(`--tg-${prefix}-area-inset-left`, `${safeInsetValue(inset.left)}px`)
}

function syncTelegramSafeArea(webApp: TelegramWebApp): void {
  applyInsetVariables("safe", webApp.safeAreaInset)
  applyInsetVariables("content-safe", webApp.contentSafeAreaInset)
}

export function initializeTelegramWebApp(): void {
  const webApp = window.Telegram?.WebApp

  if (!webApp || !isTelegramRuntime(webApp)) {
    return
  }

  document.documentElement.classList.add("telegram-mini-app")

  webApp.ready()
  webApp.expand()
  webApp.setHeaderColor(TELEGRAM_CHROME)
  webApp.setBackgroundColor(TELEGRAM_BACKGROUND)

  const refreshSafeArea = () => syncTelegramSafeArea(webApp)

  refreshSafeArea()
  webApp.onEvent?.("safeAreaChanged", refreshSafeArea)
  webApp.onEvent?.("contentSafeAreaChanged", refreshSafeArea)

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

  window.requestAnimationFrame(refreshSafeArea)
}
