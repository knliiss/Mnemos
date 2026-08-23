import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { ApiError, NetworkError } from "@/shared/api/httpClient"
import { i18n } from "@/shared/i18n"
import { AppError } from "./AppError"
import { errorEvents } from "./errorEventBus"
import { handleError } from "./errorHandler"

beforeEach(async () => {
  await i18n.changeLanguage("en")
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe("handleError", () => {
  it("publishes structured backend errors without exposing technical metadata as UI text", () => {
    vi.spyOn(console, "error").mockImplementation(() => undefined)
    const listener = vi.fn()
    const unsubscribe = errorEvents.subscribe(listener)
    const error = new ApiError(403, {
      code: "ROLE_ACCESS_DENIED",
      message: "forbidden",
      statusCode: 403,
      path: "/api/v1/accounts/roles",
      requestId: "request-42",
    })

    const normalized = handleError(error)
    unsubscribe()

    expect(normalized.message).toBe("Your role is lower in the hierarchy.")
    expect(normalized.message).not.toContain("HTTP")
    expect(normalized.message).not.toContain("ROLE_ACCESS_DENIED")
    expect(normalized.message).not.toContain("/api/v1/accounts/roles")
    expect(listener).toHaveBeenCalledWith({
      code: "ROLE_ACCESS_DENIED",
      message: "forbidden",
      statusCode: 403,
      details: undefined,
      path: "/api/v1/accounts/roles",
      requestId: "request-42",
    })
  })

  it("uses a localized status fallback for an unknown backend code", () => {
    vi.spyOn(console, "error").mockImplementation(() => undefined)

    const normalized = handleError(
      new ApiError(409, {
        code: "FUTURE_CONFLICT",
        message: "database state mismatch",
        statusCode: 409,
      }),
    )

    expect(normalized.message).toBe("Refresh the page and try again.")
    expect(normalized.message).not.toContain("database state mismatch")
  })

  it("classifies network failures as retryable service errors without leaking request details", () => {
    vi.spyOn(console, "error").mockImplementation(() => undefined)
    const listener = vi.fn()
    const unsubscribe = errorEvents.subscribe(listener)

    const normalized = handleError(new NetworkError("GET https://internal.example failed"))
    unsubscribe()

    expect(normalized.message).toBe("The service or operation is currently busy.")
    expect(normalized.message).not.toContain("internal.example")
    expect(listener).toHaveBeenCalledWith({
      code: "NETWORK_UNAVAILABLE",
      message: "The service or operation is currently busy.",
      statusCode: 503,
    })
  })

  it("keeps explicit client validation messages intact", () => {
    vi.spyOn(console, "error").mockImplementation(() => undefined)
    const error = new AppError("validation", "Enter a whole number of COIN.")

    const normalized = handleError(error)

    expect(normalized).toBe(error)
    expect(normalized.message).toBe("Enter a whole number of COIN.")
  })
})
