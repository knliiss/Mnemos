// @vitest-environment jsdom

import { afterEach, describe, expect, it } from "vitest"
import { installViewportZoomLock } from "./viewportZoom"

let cleanup: (() => void) | null = null

afterEach(() => {
  cleanup?.()
  cleanup = null
})

describe("viewport zoom lock", () => {
  it("prevents WebKit gesture zoom events", () => {
    cleanup = installViewportZoomLock()
    const event = new Event("gesturestart", { cancelable: true })

    document.dispatchEvent(event)

    expect(event.defaultPrevented).toBe(true)
  })

  it("prevents modifier keyboard zoom shortcuts", () => {
    cleanup = installViewportZoomLock()
    const event = new KeyboardEvent("keydown", {
      key: "+",
      ctrlKey: true,
      cancelable: true,
    })

    window.dispatchEvent(event)

    expect(event.defaultPrevented).toBe(true)
  })

  it("keeps ordinary keyboard input available", () => {
    cleanup = installViewportZoomLock()
    const event = new KeyboardEvent("keydown", {
      key: "a",
      cancelable: true,
    })

    window.dispatchEvent(event)

    expect(event.defaultPrevented).toBe(false)
  })
})
