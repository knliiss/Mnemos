import { describe, expect, it } from "vitest"
import { getTelegramInitDataState } from "@/features/auth/api/telegramInitData"

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
