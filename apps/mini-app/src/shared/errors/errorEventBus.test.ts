import { describe, expect, it, vi } from "vitest"
import { errorEvents } from "./errorEventBus"

describe("error event bus", () => {
  it("publishes structured error payloads to active listeners", () => {
    const listener = vi.fn()
    const unsubscribe = errorEvents.subscribe(listener)
    const payload = {
      code: "ROLE_ACCESS_DENIED",
      statusCode: 403,
      requestId: "request-1",
    }

    errorEvents.publish(payload)
    unsubscribe()

    expect(listener).toHaveBeenCalledOnce()
    expect(listener).toHaveBeenCalledWith(payload)
  })

  it("stops publishing after unsubscribe", () => {
    const listener = vi.fn()
    const unsubscribe = errorEvents.subscribe(listener)

    unsubscribe()
    errorEvents.publish({ code: "INTERNAL_SERVER_ERROR", statusCode: 500 })

    expect(listener).not.toHaveBeenCalled()
  })
})
