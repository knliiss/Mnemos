import { describe, expect, it } from "vitest"
import {
  clearTelegramKeycloakSession,
  loadTelegramKeycloakSession,
  persistTelegramKeycloakSession,
} from "@/features/auth/api/telegramKeycloakSessionStorage"

describe("Telegram Keycloak session storage", () => {
  it("persists and restores a Telegram-bound Keycloak token pair", async () => {
    const secureStorage = createSecureStorage()
    const telegram = createTelegramWebApp(secureStorage)
    const session = {
      telegramUserId: 931_458_258,
      token: "access-token",
      refreshToken: "refresh-token",
    }

    await persistTelegramKeycloakSession(telegram, session)

    expect(await loadTelegramKeycloakSession(telegram)).toEqual(session)
  })

  it("removes malformed persisted sessions", async () => {
    const secureStorage = createSecureStorage('{"token":"missing-fields"}')
    const telegram = createTelegramWebApp(secureStorage)

    expect(await loadTelegramKeycloakSession(telegram)).toBeNull()
    expect(secureStorage.value()).toBeNull()
  })

  it("clears a persisted session", async () => {
    const secureStorage = createSecureStorage("stored-session")
    const telegram = createTelegramWebApp(secureStorage)

    await clearTelegramKeycloakSession(telegram)

    expect(secureStorage.value()).toBeNull()
  })

  it("falls back when Telegram secure storage is unavailable", async () => {
    const telegram = createTelegramWebApp()

    expect(await loadTelegramKeycloakSession(telegram)).toBeNull()
  })
})

function createTelegramWebApp(secureStorage?: TelegramSecureStorage): TelegramWebApp {
  return {
    initData: "",
    platform: "android",
    SecureStorage: secureStorage,
    ready: () => undefined,
    expand: () => undefined,
    close: () => undefined,
    setHeaderColor: () => undefined,
    setBackgroundColor: () => undefined,
  }
}

function createSecureStorage(initialValue: string | null = null): TelegramSecureStorage & {
  value(): string | null
} {
  let storedValue = initialValue

  const storage: TelegramSecureStorage & { value(): string | null } = {
    setItem(_key, value, callback) {
      storedValue = value
      callback?.(null, true)
      return storage
    },
    getItem(_key, callback) {
      callback(null, storedValue, false)
      return storage
    },
    removeItem(_key, callback) {
      storedValue = null
      callback?.(null, true)
      return storage
    },
    value() {
      return storedValue
    },
  }

  return storage
}
