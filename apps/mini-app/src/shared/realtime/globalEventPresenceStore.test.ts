import { describe, expect, it } from "vitest"
import {
  createActiveGlobalEvent,
  GLOBAL_EVENT_LIFETIME_MS,
  mergeActiveGlobalEvents,
  type ActiveGlobalEvent,
} from "./globalEventPresenceStore"

const NOW = Date.parse("2026-08-25T16:00:00.000Z")

function activeEvent(
  eventId: string,
  type: ActiveGlobalEvent["type"],
  startedAtOffsetMs: number,
): ActiveGlobalEvent {
  const startedAt = new Date(NOW + startedAtOffsetMs).toISOString()

  return {
    eventId,
    type,
    startedAt,
    expiresAt: new Date(NOW + startedAtOffsetMs + GLOBAL_EVENT_LIFETIME_MS).toISOString(),
  }
}

describe("global event presence", () => {
  it("keeps at most three simultaneous events ordered by newest start", () => {
    const events = mergeActiveGlobalEvents(
      [],
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

  it("does not let older feed hydration replace a newer realtime event of the same type", () => {
    const realtimeEvent = activeEvent("new-moon", "MOON", -20_000)
    const olderFeedEvent = activeEvent("old-moon", "MOON", -70_000)

    const events = mergeActiveGlobalEvents([realtimeEvent], [olderFeedEvent], NOW)

    expect(events).toEqual([realtimeEvent])
  })

  it("removes events whose active lifetime has already ended", () => {
    const expired = activeEvent("expired", "DARKNESS", -GLOBAL_EVENT_LIFETIME_MS)
    const active = activeEvent("active", "ECLIPSE", -30_000)

    const events = mergeActiveGlobalEvents([expired, active], [], NOW)

    expect(events).toEqual([active])
  })

  it("derives the same five minute lifetime used by realtime game event broadcasts", () => {
    const startedAt = "2026-08-25T15:59:30.000Z"
    const event = createActiveGlobalEvent("moon", "MOON", startedAt)

    expect(event?.expiresAt).toBe("2026-08-25T16:04:30.000Z")
  })
})
