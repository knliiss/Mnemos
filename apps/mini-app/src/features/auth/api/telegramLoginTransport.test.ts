import { describe, expect, it } from "vitest"
import { createTelegramLoginPostRequest } from "@/features/auth/api/telegramLoginTransport"

describe("createTelegramLoginPostRequest", () => {
  it("moves the OIDC authorization request and Telegram initData into a POST body", () => {
    const loginUrl =
      "https://auth.knalis.rest/realms/mnemos/protocol/openid-connect/auth?client_id=mnemos-mini-app&state=test-state&code_challenge=test-challenge"
    const initData =
      "query_id=test-query&user=%7B%22id%22%3A123%7D&auth_date=2000000&hash=test-hash"

    const request = createTelegramLoginPostRequest(loginUrl, initData)
    const action = new URL(request.action)

    expect(action.search).toBe("")
    expect(action.hash).toBe("")
    expect(request.parameters.get("client_id")).toBe("mnemos-mini-app")
    expect(request.parameters.get("state")).toBe("test-state")
    expect(request.parameters.get("code_challenge")).toBe("test-challenge")
    expect(request.parameters.get("telegram_init_data")).toBe(initData)
  })

  it("does not carry a stale authorization URL fragment into the POST target", () => {
    const loginUrl =
      "https://auth.knalis.rest/realms/mnemos/protocol/openid-connect/auth?client_id=mnemos-mini-app#stale-fragment"
    const initData = "auth_date=2000000&hash=test-hash"

    const request = createTelegramLoginPostRequest(loginUrl, initData)
    const action = new URL(request.action)

    expect(action.hash).toBe("")
    expect(request.parameters.get("client_id")).toBe("mnemos-mini-app")
    expect(request.parameters.get("telegram_init_data")).toBe(initData)
  })
})
