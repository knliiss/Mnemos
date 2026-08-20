import { describe, expect, it } from "vitest"
import {
  toProductAccount,
  toProductFeedEvent,
  toProductSubscription,
} from "./productMappers"

describe("product mappers", () => {
  it("maps account identity, activation and Cristalix skin source", () => {
    const response = {
      id: "account-1",
      cristalixUuid: "00000000-0000-0000-0000-000000000123",
      cristalixNickname: "knalis",
      owner: true,
      memberCount: 4,
      maxMembers: 8,
      status: "ACTIVE",
    } as Parameters<typeof toProductAccount>[0]

    const result = toProductAccount(response, 1250)

    expect(result).toMatchObject({
      id: "account-1",
      nickname: "knalis",
      owner: true,
      active: true,
      memberCount: 4,
      maxMembers: 8,
      coin: 1250,
    })
    expect(result.skinUrl).toBe(
      "https://webdata.c7x.dev/textures/skin/00000000-0000-0000-0000-000000000123",
    )
  })

  it("maps an item drop without losing searchable event metadata", () => {
    const response = {
      type: "ITEM_DROP",
      eventId: "event-1",
      itemName: "Eternal Blade",
      droppedFor: "knalis",
      occurredAt: "2026-08-19T20:00:00Z",
      itemType: "SWORD",
      itemRarity: "MYTHICAL",
    } as Parameters<typeof toProductFeedEvent>[0]

    const result = toProductFeedEvent(response)

    expect(result).toEqual({
      id: "event-1",
      kind: "ITEM_DROP",
      title: "Eternal Blade",
      detail: "knalis",
      occurredAt: "2026-08-19T20:00:00Z",
      relative: "2026-08-19T20:00:00Z",
      meta: "SWORD",
      rarity: "MYTHICAL",
    })
  })

  it("maps raid locations into a stable feed representation", () => {
    const response = {
      type: "RAID",
      eventId: "raid-1",
      occurredAt: "2026-08-19T20:05:00Z",
      locations: [2, 4, 7],
    } as Parameters<typeof toProductFeedEvent>[0]

    const result = toProductFeedEvent(response)

    expect(result).toMatchObject({
      id: "raid-1",
      kind: "RAID",
      detail: "2 · 4 · 7",
      meta: "3 locations",
    })
  })

  it("maps nested notification subscription details", () => {
    const booster = {
      id: "subscription-1",
      type: "BOOSTER",
      createdAt: "2026-08-20T00:00:00Z",
      details: {
        boosterType: "LUCK",
      },
    } as Parameters<typeof toProductSubscription>[0]

    expect(toProductSubscription(booster)).toEqual({
      id: "subscription-1",
      type: "BOOSTER",
      boosterType: "LUCK",
    })
  })
})
