import { describe, expect, it } from "vitest"
import { resolveErrorPresentation } from "./errorPresentation"

describe("resolveErrorPresentation", () => {
  it("uses localized access presentation for a known backend code", () => {
    const result = resolveErrorPresentation(
      {
        code: "ROLE_ACCESS_DENIED",
        statusCode: 403,
      },
      "uk",
    )

    expect(result).toMatchObject({
      tone: "access",
      mascot: "access",
      eyebrow: "ДОСТУП",
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
    const result = resolveErrorPresentation(
      {
        code: "SOMETHING_UNKNOWN",
        statusCode,
      },
      "uk",
    )

    expect(result.tone).toBe(expectedTone)
    expect(result.mascot).toBe(expectedTone)
  })

  it("uses the code family before the generic HTTP status fallback", () => {
    const result = resolveErrorPresentation(
      {
        code: "FUTURE_RESOURCE_NOT_FOUND",
        statusCode: 500,
      },
      "uk",
    )

    expect(result.tone).toBe("not-found")
  })

  it("falls back to internal for an unknown error without metadata", () => {
    const result = resolveErrorPresentation({}, "uk")

    expect(result).toMatchObject({
      tone: "internal",
      mascot: "internal",
      title: "Невідома помилка",
    })
  })

  it("localizes presentation without changing the backend code contract", () => {
    const payload = { code: "ROLE_ACCESS_DENIED", statusCode: 403 }

    expect(resolveErrorPresentation(payload, "en").title).toBe("Insufficient permissions")
    expect(resolveErrorPresentation(payload, "ru").title).toBe("Недостаточно прав")
    expect(payload.code).toBe("ROLE_ACCESS_DENIED")
  })
})
