// @vitest-environment jsdom

import { afterEach, beforeEach, describe, expect, it } from "vitest"
import type { Route } from "../model"
import { persistNavigationStack, restoreNavigationStack } from "./navigationSession"

describe("navigation session", () => {
  beforeEach(() => {
    window.sessionStorage.clear()
    window.history.replaceState(null, "", "/")
  })

  afterEach(() => {
    window.sessionStorage.clear()
    window.history.replaceState(null, "", "/")
  })

  it("restores the complete navigation stack after the shell is remounted", () => {
    const stack: Route[] = [
      { id: "events" },
      {
        id: "event-detail",
        params: {
          eventId: "event-1",
        },
      },
    ]

    persistNavigationStack(stack)

    expect(restoreNavigationStack()).toEqual(stack)
  })

  it("prefers an explicit deep link over a previously stored route", () => {
    persistNavigationStack([{ id: "events" }])
    window.history.replaceState(
      null,
      "",
      "/?screen=personalization-detail&p.personalizationId=personalization-1",
    )

    expect(restoreNavigationStack()).toEqual([
      { id: "personalization-store" },
      {
        id: "personalization-detail",
        params: {
          personalizationId: "personalization-1",
        },
      },
    ])
  })

  it("ignores invalid stored navigation data", () => {
    window.sessionStorage.setItem(
      "mnemos.navigation.stack.v1",
      JSON.stringify([{ id: "route-that-does-not-exist" }]),
    )

    expect(restoreNavigationStack()).toEqual([{ id: "account" }])
  })
})
