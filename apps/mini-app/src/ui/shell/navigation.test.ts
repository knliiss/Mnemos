import { describe, expect, it } from "vitest"
import { canRenderWithoutActiveAccount } from "@/ui/shell/navigation"

describe("active account route gating", () => {
  it("keeps user-scoped wallet and personalization routes available", () => {
    expect(canRenderWithoutActiveAccount({ id: "user-wallet" })).toBe(true)
    expect(canRenderWithoutActiveAccount({ id: "personalization-store" })).toBe(true)
    expect(canRenderWithoutActiveAccount({ id: "personalization-store-category" })).toBe(true)
    expect(canRenderWithoutActiveAccount({ id: "personalization-detail" })).toBe(true)
    expect(
      canRenderWithoutActiveAccount({
        id: "personalization-owned",
        params: { target: "USER" },
      }),
    ).toBe(true)
  })

  it("still requires an active account for account-scoped management", () => {
    expect(canRenderWithoutActiveAccount({ id: "account-wallet" })).toBe(false)
    expect(canRenderWithoutActiveAccount({ id: "roles" })).toBe(false)
    expect(
      canRenderWithoutActiveAccount({
        id: "personalization-owned",
        params: { target: "ACCOUNT" },
      }),
    ).toBe(false)
  })
})
