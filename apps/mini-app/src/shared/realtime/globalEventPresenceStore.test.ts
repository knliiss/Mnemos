import { describe, expect, it } from "vitest"
import {
  normalizeActiveGlobalEvents,
  type ActiveGlobalEvent,
} from "./globalEventPresenceStore"

const NOW = Date.parse("2026-08-25T16:00:00.000Z")

function activeEvent(
  eventId: string,
  type: ActiveGlobalEvent["type"],
  startedAtOffsetMs: number,
  endsAtOffsetMs = 120_000,
): ActiveGlobalEvent {
  return {
    eventId,
    type,
    startedAt: new Date(NOW + startedAtOffsetMs).toISOString(),
    endsAt: new Date(NOW + endsAtOffsetMs).toISOString(),
  }
}

describe("global event presence", () => {
  it("keeps at most three simultaneous events ordered by newest start", () => {
    const events = normalizeActiveGlobalEvents(
      [
        activeEvent("darkness", "DARKNESS", -60_000),
        activeEvent("moon", "MOON", -50_000),
        activeEvent("eclipse", "ECLIPSE", -40_000),
        activeEvent("explosion", "EXPLOSION", -30_000),
      ],
      NOW,
    )

    expect(events.map((event) => event.eventId)).toEqual(["explosion", "eclipse", "moon"])
  })

  it("keeps the newest event when a snapshot contains the same type more than once", () => {
    const oldMoon = activeEvent("old-moon", "MOON", -70_000)
    const newMoon = activeEvent("new-moon", "MOON", -20_000)

    const events = normalizeActiveGlobalEvents([oldMoon, newMoon], NOW)

    expect(events).toEqual([newMoon])
  })

  it("removes events whose server supplied end time has passed", () => {
    const expired = activeEvent("expired", "DARKNESS", -60_000, 0)
    const active = activeEvent("active", "ECLIPSE", -30_000, 90_000)

    const events = normalizeActiveGlobalEvents([expired, active], NOW)

    expect(events).toEqual([active])
  })

  it("preserves the authoritative server end time without deriving a client lifetime", () => {
    const event = activeEvent("moon", "MOON", -30_000, 173_000)

    const events = normalizeActiveGlobalEvents([event], NOW)

    expect(events[0]?.endsAt).toBe(new Date(NOW + 173_000).toISOString())
  })
})
