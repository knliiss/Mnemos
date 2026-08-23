import { describe, expect, it } from "vitest"
import { getTelegramInitDataState, getTelegramUserId } from "@/features/auth/api/telegramInitData"

describe("getTelegramInitDataState", () => {
  const nowMilliseconds = 2_000_000 * 1000

  it("accepts initData inside the freshness window", () => {
    const initData = `auth_date=${2_000_000 - 299}&hash=test`

    expect(getTelegramInitDataState(initData, 300, nowMilliseconds)).toBe("fresh")
  })

  it("rejects initData older than the configured freshness window", () => {
    const initData = `auth_date=${2_000_000 - 301}&hash=test`

    expect(getTelegramInitDataState(initData, 300, nowMilliseconds)).toBe("expired")
  })

  it("accepts small positive clock skew", () => {
    const initData = `auth_date=${2_000_000 + 30}&hash=test`

    expect(getTelegramInitDataState(initData, 300, nowMilliseconds)).toBe("fresh")
  })

  it("rejects excessive future clock skew", () => {
    const initData = `auth_date=${2_000_000 + 31}&hash=test`

    expect(getTelegramInitDataState(initData, 300, nowMilliseconds)).toBe("expired")
  })

  it("rejects missing and malformed auth dates before redirecting to Keycloak", () => {
    expect(getTelegramInitDataState("hash=test", 300, nowMilliseconds)).toBe("missing-auth-date")
    expect(getTelegramInitDataState("auth_date=nope&hash=test", 300, nowMilliseconds)).toBe(
      "invalid-auth-date",
    )
  })
})

describe("getTelegramUserId", () => {
  it("reads the Telegram user id from initData", () => {
    const user = encodeURIComponent(JSON.stringify({ id: 931_458_258, username: "knaliz" }))
    const initData = `user=${user}&auth_date=2000000&hash=test`

    expect(getTelegramUserId(initData)).toBe(931_458_258)
  })

  it("rejects missing, malformed, and unsafe Telegram user ids", () => {
    const malformedUser = encodeURIComponent("not-json")
    const unsafeUser = encodeURIComponent(JSON.stringify({ id: Number.MAX_SAFE_INTEGER + 1 }))

    expect(getTelegramUserId("auth_date=2000000&hash=test")).toBeNull()
    expect(getTelegramUserId(`user=${malformedUser}&auth_date=2000000&hash=test`)).toBeNull()
    expect(getTelegramUserId(`user=${unsafeUser}&auth_date=2000000&hash=test`)).toBeNull()
  })
})
