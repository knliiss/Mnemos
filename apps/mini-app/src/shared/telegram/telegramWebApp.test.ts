// @vitest-environment jsdom

import { afterEach, describe, expect, it, vi } from "vitest"
import { initializeTelegramWebApp } from "./telegramWebApp"

const safeAreaVariables = [
  "--tg-safe-area-inset-top",
  "--tg-safe-area-inset-right",
  "--tg-safe-area-inset-bottom",
  "--tg-safe-area-inset-left",
  "--tg-content-safe-area-inset-top",
  "--tg-content-safe-area-inset-right",
  "--tg-content-safe-area-inset-bottom",
  "--tg-content-safe-area-inset-left",
]

afterEach(() => {
  delete window.Telegram
  document.documentElement.classList.remove("telegram-mini-app")

  safeAreaVariables.forEach((name) => document.documentElement.style.removeProperty(name))

  vi.restoreAllMocks()
})

describe("Telegram Web App bootstrap", () => {
  it("binds Telegram safe-area values to CSS variables and refreshes them on events", () => {
    const callbacks = new Map<TelegramSafeAreaEvent, () => void>()
    const requestAnimationFrame = vi
      .spyOn(window, "requestAnimationFrame")
      .mockImplementation((callback) => {
        callback(0)
        return 1
      })
    const webApp = {
      initData: "query_id=test",
      platform: "ios",
      safeAreaInset: { top: 47, right: 0, bottom: 34, left: 0 },
      contentSafeAreaInset: { top: 96, right: 0, bottom: 34, left: 0 },
      ready: vi.fn(),
      expand: vi.fn(),
      close: vi.fn(),
      setHeaderColor: vi.fn(),
      setBackgroundColor: vi.fn(),
      isVersionAtLeast: vi.fn(() => false),
      onEvent: vi.fn((event: TelegramSafeAreaEvent, callback: () => void) => {
        callbacks.set(event, callback)
      }),
    } satisfies TelegramWebApp

    window.Telegram = { WebApp: webApp }

    initializeTelegramWebApp()

    expect(document.documentElement.classList.contains("telegram-mini-app")).toBe(true)
    expect(document.documentElement.style.getPropertyValue("--tg-safe-area-inset-top")).toBe("47px")
    expect(
      document.documentElement.style.getPropertyValue("--tg-content-safe-area-inset-top"),
    ).toBe("96px")

    webApp.contentSafeAreaInset = { top: 112, right: 2, bottom: 38, left: 2 }
    callbacks.get("contentSafeAreaChanged")?.()

    expect(
      document.documentElement.style.getPropertyValue("--tg-content-safe-area-inset-top"),
    ).toBe("112px")
    expect(
      document.documentElement.style.getPropertyValue("--tg-content-safe-area-inset-bottom"),
    ).toBe("38px")
    expect(requestAnimationFrame).toHaveBeenCalledOnce()
  })
})
