import { describe, expect, it } from "vitest"
import { resolveErrorPresentation } from "./ErrorNotifications"

describe("resolveErrorPresentation", () => {
  it("uses the dedicated role access presentation", () => {
    const result = resolveErrorPresentation({
      code: "ROLE_ACCESS_DENIED",
      statusCode: 403,
    })

    expect(result).toMatchObject({
      tone: "access",
      mascot: "access",
      eyebrow: "РОЛІ",
      title: "Недостатньо прав",
    })
  })

  it.each([
    [400, "validation"],
    [403, "access"],
    [404, "not-found"],
    [409, "conflict"],
    [429, "waiting"],
    [503, "waiting"],
    [500, "internal"],
  ] as const)("maps unknown HTTP %s to %s", (statusCode, expectedTone) => {
    const result = resolveErrorPresentation({
      code: "SOMETHING_UNKNOWN",
      statusCode,
    })

    expect(result.tone).toBe(expectedTone)
    expect(result.mascot).toBe(expectedTone)
  })

  it("uses the code family before the generic HTTP status fallback", () => {
    const result = resolveErrorPresentation({
      code: "FUTURE_RESOURCE_NOT_FOUND",
      statusCode: 500,
    })

    expect(result.tone).toBe("not-found")
  })

  it("falls back to internal for an unknown error without metadata", () => {
    const result = resolveErrorPresentation({})

    expect(result).toMatchObject({
      tone: "internal",
      mascot: "internal",
      title: "Невідома помилка",
    })
  })
})
