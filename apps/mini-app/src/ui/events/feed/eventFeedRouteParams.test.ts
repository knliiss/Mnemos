import { describe, expect, it } from "vitest"
import type { FeedEvent } from "../../model"
import { eventToFeedRouteChoices, readEventFeedRouteParams } from "./eventFeedRouteParams"

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
  it("offers player and exact drop searches separately", () => {
    expect(eventToFeedRouteChoices(event({}))).toEqual([
      {
        id: "player:player",
        type: "PLAYER",
        value: "Player",
        params: { query: "Player" },
      },
      {
        id: "item:test item",
        type: "ITEM",
        value: "Test item",
        params: { filter: "ITEM_DROP", query: "Test item" },
      },
    ])
  })

  it("offers player and booster searches separately", () => {
    expect(
      eventToFeedRouteChoices(
        event({
          kind: "BOOSTER",
          title: "LUCK",
          detail: "BoostPlayer",
        }),
      ),
    ).toEqual([
      {
        id: "player:boostplayer",
        type: "PLAYER",
        value: "BoostPlayer",
        params: { query: "BoostPlayer" },
      },
      {
        id: "booster:LUCK",
        type: "BOOSTER",
        value: "LUCK",
        params: { filter: "BOOSTER", boosterType: "LUCK" },
      },
    ])
  })

  it("turns every raid location into its own feed choice", () => {
    expect(
      eventToFeedRouteChoices(
        event({
          kind: "RAID",
          title: "RAID",
          detail: "7 · 12 · 7",
        }),
      ),
    ).toEqual([
      {
        id: "raid-location:7",
        type: "RAID_LOCATION",
        value: "7",
        location: 7,
        params: { filter: "RAID", raidLocation: "7" },
      },
      {
        id: "raid-location:12",
        type: "RAID_LOCATION",
        value: "12",
        location: 12,
        params: { filter: "RAID", raidLocation: "12" },
      },
    ])
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
