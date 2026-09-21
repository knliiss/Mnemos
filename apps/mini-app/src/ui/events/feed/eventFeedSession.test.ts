// @vitest-environment jsdom

import { beforeEach, describe, expect, it } from "vitest"
import { persistEventFeedState, readPersistedEventFeedState } from "./eventFeedSession"

describe("event feed session state", () => {
  beforeEach(() => {
    window.sessionStorage.clear()
  })

  it("restores search and filters for the same account", () => {
    persistEventFeedState("account-1", {
      filter: "ITEM_DROP",
      rarity: "SECRET",
      itemType: "SWORD",
      boosterType: "ALL",
      globalType: "ALL",
      raidLocation: null,
      query: "mnNick4 лесной меч",
      sort: "OLDEST",
      wipeId: "wipe-7",
    })

    expect(readPersistedEventFeedState("account-1")).toEqual({
      filter: "ITEM_DROP",
      rarity: "SECRET",
      itemType: "SWORD",
      boosterType: "ALL",
      globalType: "ALL",
      raidLocation: null,
      query: "mnNick4 лесной меч",
      sort: "OLDEST",
      wipeId: "wipe-7",
    })
  })

  it("does not leak feed state between accounts", () => {
    persistEventFeedState("account-1", {
      filter: "ALL",
      rarity: "ALL",
      itemType: "ALL",
      boosterType: "ALL",
      globalType: "ALL",
      raidLocation: null,
      query: "player-one",
      sort: "NEWEST",
      wipeId: null,
    })

    expect(readPersistedEventFeedState("account-2")).toBeNull()
  })
})
