import { getTelegramUserId } from "@/features/auth/api/telegramInitData"

const TELEGRAM_USER_ID_STORAGE_KEY = "mnemos_telegram_user_id"
const STORAGE_OPERATION_TIMEOUT_MS = 750

export async function resolveTelegramSessionUserId(
  telegram: TelegramWebApp,
): Promise<number | null> {
  const initDataUserId = getTelegramUserId(telegram.initData)

  if (initDataUserId !== null) {
    await storeTelegramUserId(telegram.DeviceStorage, initDataUserId)
    return initDataUserId
  }

  return readTelegramUserId(telegram.DeviceStorage)
}

async function storeTelegramUserId(
  storage: TelegramDeviceStorage | undefined,
  telegramUserId: number,
): Promise<void> {
  if (!storage) {
    return
  }

  await new Promise<void>((resolve) => {
    let completed = false

    const finish = () => {
      if (completed) {
        return
      }

      completed = true
      clearTimeout(timeout)
      resolve()
    }
    const timeout = setTimeout(finish, STORAGE_OPERATION_TIMEOUT_MS)

    try {
      storage.setItem(TELEGRAM_USER_ID_STORAGE_KEY, telegramUserId.toString(), finish)
    } catch {
      finish()
    }
  })
}

async function readTelegramUserId(
  storage: TelegramDeviceStorage | undefined,
): Promise<number | null> {
  if (!storage) {
    return null
  }

  return new Promise<number | null>((resolve) => {
    let completed = false

    const finish = (value: number | null) => {
      if (completed) {
        return
      }

      completed = true
      clearTimeout(timeout)
      resolve(value)
    }
    const timeout = setTimeout(() => finish(null), STORAGE_OPERATION_TIMEOUT_MS)

    try {
      storage.getItem(TELEGRAM_USER_ID_STORAGE_KEY, (error, value) => {
        if (error || !value) {
          finish(null)
          return
        }

        finish(parseTelegramUserId(value))
      })
    } catch {
      finish(null)
    }
  })
}

function parseTelegramUserId(value: string): number | null {
  if (!/^\d+$/.test(value)) {
    return null
  }

  const parsed = Number(value)

  return Number.isSafeInteger(parsed) && parsed > 0 ? parsed : null
}
