import { describe, expect, it } from "vitest"
import { parseRealtimeEvent } from "./realtimeEventParser"

describe("moderation sanction realtime events", () => {
  it("accepts an activated ban for the affected user", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000401",
        type: "moderation.sanction.activated",
        occurredAt: "2026-09-03T04:00:00Z",
        operationId: null,
        payload: {
          sanctionId: "019c1129-ef54-7000-8000-000000000402",
          userId: "019c1129-ef54-7000-8000-000000000403",
          type: "BAN",
          status: "ACTIVE",
        },
      }),
    )

    expect(event?.type).toBe("moderation.sanction.activated")
  })

  it("accepts an activated notification suspension", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000411",
        type: "moderation.sanction.activated",
        occurredAt: "2026-09-03T04:00:00Z",
        operationId: null,
        payload: {
          sanctionId: "019c1129-ef54-7000-8000-000000000412",
          userId: "019c1129-ef54-7000-8000-000000000413",
          type: "NOTIFICATION_SUSPENSION",
          status: "ACTIVE",
        },
      }),
    )

    expect(event?.type).toBe("moderation.sanction.activated")
  })

  it.each(["REVOKED", "EXPIRED"])("accepts a %s sanction deactivation", (status) => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000421",
        type: "moderation.sanction.deactivated",
        occurredAt: "2026-09-03T04:00:00Z",
        operationId: null,
        payload: {
          sanctionId: "019c1129-ef54-7000-8000-000000000422",
          userId: "019c1129-ef54-7000-8000-000000000423",
          type: "BAN",
          status,
        },
      }),
    )

    expect(event?.type).toBe("moderation.sanction.deactivated")
  })

  it("rejects an activated sanction with a terminal status", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000431",
        type: "moderation.sanction.activated",
        occurredAt: "2026-09-03T04:00:00Z",
        operationId: null,
        payload: {
          sanctionId: "019c1129-ef54-7000-8000-000000000432",
          userId: "019c1129-ef54-7000-8000-000000000433",
          type: "BAN",
          status: "REVOKED",
        },
      }),
    )

    expect(event).toBeNull()
  })

  it("rejects a sanction event with an unsupported type", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000441",
        type: "moderation.sanction.activated",
        occurredAt: "2026-09-03T04:00:00Z",
        operationId: null,
        payload: {
          sanctionId: "019c1129-ef54-7000-8000-000000000442",
          userId: "019c1129-ef54-7000-8000-000000000443",
          type: "UNKNOWN",
          status: "ACTIVE",
        },
      }),
    )

    expect(event).toBeNull()
  })

  it("rejects sanction events carrying an operation id", () => {
    const event = parseRealtimeEvent(
      JSON.stringify({
        id: "019c1129-ef54-7000-8000-000000000451",
        type: "moderation.sanction.deactivated",
        occurredAt: "2026-09-03T04:00:00Z",
        operationId: "019c1129-ef54-7000-8000-000000000452",
        payload: {
          sanctionId: "019c1129-ef54-7000-8000-000000000453",
          userId: "019c1129-ef54-7000-8000-000000000454",
          type: "BAN",
          status: "EXPIRED",
        },
      }),
    )

    expect(event).toBeNull()
  })
})
