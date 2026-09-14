import { describe, expect, it } from "vitest"
import type { FeedEvent } from "../../model"
import { eventToFeedRouteParams, readEventFeedRouteParams } from "./eventFeedRouteParams"

function event(overrides: Partial<FeedEvent>): FeedEvent {
  return {
    id: "event-1",
    kind: "ITEM_DROP",
    title: "Test item",
    detail: "Player",
    occurredAt: "2026-09-14T12:00:00Z",
    relative: "2026-09-14T12:00:00Z",
    ...overrides,
  }
}

describe("event feed route params", () => {
  it("copies the useful drop filters into the feed route", () => {
    expect(
      eventToFeedRouteParams(
        event({
          kind: "ITEM_DROP",
          meta: "PET",
          rarity: "SECRET",
        }),
      ),
    ).toEqual({
      filter: "ITEM_DROP",
      itemType: "PET",
      rarity: "SECRET",
    })
  })

  it("copies a raid location only when the event has exactly one location", () => {
    expect(
      eventToFeedRouteParams(
        event({
          kind: "RAID",
          title: "RAID",
          detail: "7",
        }),
      ),
    ).toEqual({ filter: "RAID", raidLocation: "7" })

    expect(
      eventToFeedRouteParams(
        event({
          kind: "RAID",
          title: "RAID",
          detail: "7 · 12",
        }),
      ),
    ).toEqual({ filter: "RAID" })
  })

  it("restores only filters that belong to the selected event kind", () => {
    expect(
      readEventFeedRouteParams({
        filter: "BOOSTER",
        boosterType: "LUCK",
        rarity: "SECRET",
        itemType: "PET",
        query: "player",
      }),
    ).toEqual({
      filter: "BOOSTER",
      rarity: "ALL",
      itemType: "ALL",
      boosterType: "LUCK",
      globalType: "ALL",
      raidLocation: null,
      query: "player",
    })
  })

  it("falls back safely when route params are invalid", () => {
    expect(
      readEventFeedRouteParams({
        filter: "NOPE",
        raidLocation: "-3",
        rarity: "COMMON",
      }),
    ).toEqual({
      filter: "ALL",
      rarity: "ALL",
      itemType: "ALL",
      boosterType: "ALL",
      globalType: "ALL",
      raidLocation: null,
      query: "",
    })
  })
})
