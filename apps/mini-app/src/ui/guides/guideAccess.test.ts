import { describe, expect, it } from "vitest"
import { canAccessGuides } from "./guideAccess"

describe("guide access", () => {
  it("allows only existing admin system roles", () => {
    expect(canAccessGuides(["ADMIN"])).toBe(true)
    expect(canAccessGuides(["SUPER_ADMIN"])).toBe(true)
    expect(canAccessGuides(["USER"])).toBe(false)
    expect(canAccessGuides(["MODERATOR"])).toBe(false)
    expect(canAccessGuides(["PERSONALIZATION_CREATOR"])).toBe(false)
    expect(canAccessGuides(["PERSONALIZATION_MODERATOR"])).toBe(false)
  })
})
