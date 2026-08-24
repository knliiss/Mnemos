const KEYCLOAK_SESSION_STORAGE_KEY = "mnemos_keycloak_session"
const STORAGE_OPERATION_TIMEOUT_MS = 750
const MAX_TOKEN_LENGTH = 32_768

export type StoredTelegramKeycloakSession = {
  telegramUserId: number
  token: string
  refreshToken: string
}

export async function loadTelegramKeycloakSession(
  telegram: TelegramWebApp,
): Promise<StoredTelegramKeycloakSession | null> {
  const storage = telegram.SecureStorage

  if (!storage) {
    return null
  }

  const serialized = await getSecureItem(storage, KEYCLOAK_SESSION_STORAGE_KEY)

  if (!serialized) {
    return null
  }

  const session = parseStoredSession(serialized)

  if (!session) {
    await removeSecureItem(storage, KEYCLOAK_SESSION_STORAGE_KEY)
  }

  return session
}

export async function persistTelegramKeycloakSession(
  telegram: TelegramWebApp,
  session: StoredTelegramKeycloakSession,
): Promise<void> {
  const storage = telegram.SecureStorage

  if (!storage || !isValidStoredSession(session)) {
    return
  }

  await setSecureItem(storage, KEYCLOAK_SESSION_STORAGE_KEY, JSON.stringify(session))
}

export async function clearTelegramKeycloakSession(telegram: TelegramWebApp): Promise<void> {
  const storage = telegram.SecureStorage

  if (!storage) {
    return
  }

  await removeSecureItem(storage, KEYCLOAK_SESSION_STORAGE_KEY)
}

function parseStoredSession(serialized: string): StoredTelegramKeycloakSession | null {
  try {
    const parsed: unknown = JSON.parse(serialized)

    if (!isValidStoredSession(parsed)) {
      return null
    }

    return parsed
  } catch {
    return null
  }
}

function isValidStoredSession(value: unknown): value is StoredTelegramKeycloakSession {
  if (!value || typeof value !== "object") {
    return false
  }

  const session = value as Partial<StoredTelegramKeycloakSession>

  return (
    typeof session.telegramUserId === "number" &&
    Number.isSafeInteger(session.telegramUserId) &&
    session.telegramUserId > 0 &&
    isValidToken(session.token) &&
    isValidToken(session.refreshToken)
  )
}

function isValidToken(value: unknown): value is string {
  return typeof value === "string" && value.length > 0 && value.length <= MAX_TOKEN_LENGTH
}

function getSecureItem(storage: TelegramSecureStorage, key: string): Promise<string | null> {
  return new Promise((resolve) => {
    let completed = false

    const finish = (value: string | null) => {
      if (completed) {
        return
      }

      completed = true
      clearTimeout(timeout)
      resolve(value)
    }
    const timeout = setTimeout(() => finish(null), STORAGE_OPERATION_TIMEOUT_MS)

    try {
      storage.getItem(key, (error, value) => {
        finish(error ? null : (value ?? null))
      })
    } catch {
      finish(null)
    }
  })
}

function setSecureItem(storage: TelegramSecureStorage, key: string, value: string): Promise<void> {
  return new Promise((resolve) => {
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
      storage.setItem(key, value, finish)
    } catch {
      finish()
    }
  })
}

function removeSecureItem(storage: TelegramSecureStorage, key: string): Promise<void> {
  return new Promise((resolve) => {
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
      storage.removeItem(key, finish)
    } catch {
      finish()
    }
  })
}
