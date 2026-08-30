const TELEGRAM_BACKGROUND = "#060906"
const TELEGRAM_CHROME = "#0b0f0a"

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

function isMobileTelegramPlatform(platform: string | undefined): boolean {
  return platform === "android" || platform === "ios"
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

function syncTelegramRuntimeClasses(webApp: TelegramWebApp): void {
  const root = document.documentElement
  const isMobile = isMobileTelegramPlatform(webApp.platform)
  const isFullscreen = webApp.isFullscreen === true

  root.classList.toggle("telegram-platform-mobile", isMobile)
  root.classList.toggle("telegram-platform-desktop", !isMobile)
  root.classList.toggle("telegram-fullscreen", isFullscreen)
  root.classList.toggle("telegram-windowed", !isFullscreen)
}

function syncTelegramViewportState(webApp: TelegramWebApp): void {
  syncTelegramRuntimeClasses(webApp)
  applyInsetVariables("safe", webApp.safeAreaInset)
  applyInsetVariables("content-safe", webApp.contentSafeAreaInset)
}

export function initializeTelegramWebApp(): void {
  const webApp = window.Telegram?.WebApp

  if (!webApp || !isTelegramRuntime(webApp)) {
    return
  }

  const isMobile = isMobileTelegramPlatform(webApp.platform)

  document.documentElement.classList.add("telegram-mini-app")

  const refreshViewportState = () => syncTelegramViewportState(webApp)

  refreshViewportState()
  webApp.onEvent?.("safeAreaChanged", refreshViewportState)
  webApp.onEvent?.("contentSafeAreaChanged", refreshViewportState)
  webApp.onEvent?.("fullscreenChanged", refreshViewportState)

  webApp.ready()

  if (isMobile) {
    webApp.expand()
  }

  webApp.setHeaderColor(TELEGRAM_CHROME)
  webApp.setBackgroundColor(TELEGRAM_BACKGROUND)

  if (supports(webApp, "7.10")) {
    webApp.setBottomBarColor?.(TELEGRAM_BACKGROUND)
  }

  if (supports(webApp, "7.7")) {
    webApp.disableVerticalSwipes?.()
  }

  if (isMobile && supports(webApp, "8.0")) {
    try {
      webApp.requestFullscreen?.()
    } catch {
      // Telegram can reject fullscreen on clients that advertise support but do not allow it yet.
      // expand() above remains the safe fallback for mobile clients.
    }
  }

  window.requestAnimationFrame(refreshViewportState)
}
