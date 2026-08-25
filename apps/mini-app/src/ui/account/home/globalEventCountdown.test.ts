import { describe, expect, it } from "vitest"
import { formatGlobalEventRemainingTime } from "./globalEventCountdown"

describe("global event countdown", () => {
  const now = Date.parse("2026-08-26T00:00:00.000Z")

  it("formats remaining minutes and seconds", () => {
    const endsAt = new Date(now + 8 * 60_000 + 7_000).toISOString()

    expect(formatGlobalEventRemainingTime(endsAt, now)).toBe("08:07")
  })

  it("formats hours when the event lasts longer than an hour", () => {
    const endsAt = new Date(now + 2 * 3_600_000 + 5 * 60_000 + 9_000).toISOString()

    expect(formatGlobalEventRemainingTime(endsAt, now)).toBe("2:05:09")
  })

  it("never displays a negative countdown", () => {
    const endsAt = new Date(now - 1_000).toISOString()

    expect(formatGlobalEventRemainingTime(endsAt, now)).toBe("00:00")
  })
})
