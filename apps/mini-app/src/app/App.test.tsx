// @vitest-environment jsdom

import { afterEach, describe, expect, it } from "vitest"
import { initialStack } from "@/ui/navigation/routeRegistry"

afterEach(() => {
  window.history.replaceState({}, "", "/")
})

describe("product route bootstrap", () => {
  it("starts on the account root without an explicit screen", () => {
    window.history.replaceState({}, "", "/")

    expect(initialStack()).toEqual([{ id: "account" }])
  })

  it("keeps a root route as a single stack entry", () => {
    window.history.replaceState({}, "", "/?screen=bets")

    expect(initialStack()).toEqual([{ id: "bets", params: undefined }])
  })

  it("restores a nested route with its product-area root", () => {
    window.history.replaceState({}, "", "/?screen=event-detail&p.eventId=event-42")

    expect(initialStack()).toEqual([
      { id: "events" },
      {
        id: "event-detail",
        params: { eventId: "event-42" },
      },
    ])
  })

  it("routes unknown screens to the Mnemos 404 state", () => {
    window.history.replaceState({}, "", "/?screen=totally-lost")

    expect(initialStack()).toEqual([
      {
        id: "account",
        params: {
          notFound: "true",
          requested: "totally-lost",
        },
      },
    ])
  })
})
