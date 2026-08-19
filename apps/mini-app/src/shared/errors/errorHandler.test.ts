import { afterEach, describe, expect, it, vi } from "vitest"
import { ApiError, NetworkError } from "@/shared/api/httpClient"
import { errorEvents } from "./errorEventBus"
import { handleError } from "./errorHandler"

afterEach(() => {
  vi.restoreAllMocks()
})

describe("handleError", () => {
  it("publishes structured backend errors for the mascot popup system", () => {
    vi.spyOn(console, "error").mockImplementation(() => undefined)
    const listener = vi.fn()
    const unsubscribe = errorEvents.subscribe(listener)
    const error = new ApiError(403, {
      code: "ROLE_ACCESS_DENIED",
      message: "forbidden",
      statusCode: 403,
      requestId: "request-42",
    })

    handleError(error)
    unsubscribe()

    expect(listener).toHaveBeenCalledWith({
      code: "ROLE_ACCESS_DENIED",
      message: "forbidden",
      statusCode: 403,
      details: undefined,
      path: undefined,
      requestId: "request-42",
    })
  })

  it("classifies network failures as retryable service errors", () => {
    vi.spyOn(console, "error").mockImplementation(() => undefined)
    const listener = vi.fn()
    const unsubscribe = errorEvents.subscribe(listener)

    handleError(new NetworkError("connection failed"))
    unsubscribe()

    expect(listener).toHaveBeenCalledWith({
      code: "NETWORK_UNAVAILABLE",
      message: "connection failed",
      statusCode: 503,
    })
  })
})
