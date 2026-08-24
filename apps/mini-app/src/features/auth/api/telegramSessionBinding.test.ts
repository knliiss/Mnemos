import { describe, expect, it } from "vitest"
import { resolveTelegramSessionUserId } from "@/features/auth/api/telegramSessionBinding"

describe("resolveTelegramSessionUserId", () => {
  it("stores and returns the user id from fresh Telegram initData", async () => {
    const deviceStorage = createDeviceStorage()
    const user = encodeURIComponent(JSON.stringify({ id: 931_458_258 }))
    const telegram = createTelegramWebApp(`user=${user}&auth_date=2000000&hash=test`, deviceStorage)

    const userId = await resolveTelegramSessionUserId(telegram)

    expect(userId).toBe(931_458_258)
    expect(deviceStorage.value()).toBe("931458258")
  })

  it("restores the user id from Telegram device storage when initData is unavailable", async () => {
    const deviceStorage = createDeviceStorage("931458258")
    const telegram = createTelegramWebApp("", deviceStorage)

    const userId = await resolveTelegramSessionUserId(telegram)

    expect(userId).toBe(931_458_258)
  })

  it("rejects malformed persisted Telegram user ids", async () => {
    const deviceStorage = createDeviceStorage("not-a-user-id")
    const telegram = createTelegramWebApp("", deviceStorage)

    const userId = await resolveTelegramSessionUserId(telegram)

    expect(userId).toBeNull()
  })

  it("returns null when Telegram device storage is unavailable", async () => {
    const telegram = createTelegramWebApp("")

    const userId = await resolveTelegramSessionUserId(telegram)

    expect(userId).toBeNull()
  })
})

function createTelegramWebApp(
  initData: string,
  deviceStorage?: TelegramDeviceStorage,
): TelegramWebApp {
  return {
    initData,
    platform: "android",
    DeviceStorage: deviceStorage,
    ready: () => undefined,
    expand: () => undefined,
    close: () => undefined,
    setHeaderColor: () => undefined,
    setBackgroundColor: () => undefined,
  }
}

function createDeviceStorage(initialValue: string | null = null): TelegramDeviceStorage & {
  value(): string | null
} {
  let storedValue = initialValue

  const storage: TelegramDeviceStorage & { value(): string | null } = {
    setItem(_key, value, callback) {
      storedValue = value
      callback?.(null, true)
      return storage
    },
    getItem(_key, callback) {
      callback(null, storedValue)
      return storage
    },
    value() {
      return storedValue
    },
  }

  return storage
}
