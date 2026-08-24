import { describe, expect, it } from "vitest"
import { createTelegramLoginRedirectUrl } from "@/features/auth/api/telegramLoginTransport"

describe("createTelegramLoginRedirectUrl", () => {
  it("keeps Telegram initData out of the authorization request query", () => {
    const loginUrl =
      "https://auth.knalis.rest/realms/mnemos/protocol/openid-connect/auth?client_id=mnemos-mini-app&state=test-state"
    const initData =
      "query_id=test-query&user=%7B%22id%22%3A123%7D&auth_date=2000000&hash=test-hash"

    const redirectUrl = new URL(createTelegramLoginRedirectUrl(loginUrl, initData))
    const fragmentParameters = new URLSearchParams(redirectUrl.hash.slice(1))

    expect(redirectUrl.searchParams.has("telegram_init_data")).toBe(false)
    expect(fragmentParameters.get("telegram_init_data")).toBe(initData)
  })

  it("replaces an existing fragment before carrying Telegram initData", () => {
    const loginUrl =
      "https://auth.knalis.rest/realms/mnemos/protocol/openid-connect/auth?client_id=mnemos-mini-app#stale-fragment"
    const initData = "auth_date=2000000&hash=test-hash"

    const redirectUrl = new URL(createTelegramLoginRedirectUrl(loginUrl, initData))
    const fragmentParameters = new URLSearchParams(redirectUrl.hash.slice(1))

    expect(fragmentParameters.size).toBe(1)
    expect(fragmentParameters.get("telegram_init_data")).toBe(initData)
  })
})
