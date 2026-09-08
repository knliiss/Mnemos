import { describe, expect, it } from "vitest"
import { resolveSaoItemAtlasSprite } from "./saoTrackedItemArtwork"

describe("SAO tracked item artwork", () => {
  it("maps regular tracked items to the original atlas", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.d1_324.name")).toEqual({
      column: 0,
      row: 3,
      atlas: {
        path: "/event-icons/sao-tracked-items.webp",
        columns: 16,
        rows: 11,
      },
    })
  })

  it("keeps the reused blazing ring artwork in the original atlas", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.d3_504.name")).toEqual({
      column: 5,
      row: 10,
      atlas: {
        path: "/event-icons/sao-tracked-items.webp",
        columns: 16,
        rows: 11,
      },
    })
  })

  it("maps every source-backed aura through the complete RP atlas", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.auraAwareness.name")).toEqual({
      column: 0,
      row: 0,
      atlas: {
        path: "/guides/entities/aura-inventory-atlas.webp",
        columns: 8,
        rows: 5,
      },
    })
    expect(resolveSaoItemAtlasSprite("sao.item.auraWater.name")).toEqual({
      column: 2,
      row: 4,
      atlas: {
        path: "/guides/entities/aura-inventory-atlas.webp",
        columns: 8,
        rows: 5,
      },
    })
  })

  it("uses rendered JSON model artwork when the resource pack defines a model", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.l1_8.name")).toEqual({
      column: 4,
      row: 2,
      atlas: {
        path: "/event-icons/sao-tracked-item-models-inventory-v2.webp",
        columns: 8,
        rows: 8,
      },
    })
  })

  it("keeps untracked location weapons out of the ordinary feed resolver", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.l23_1.name")).toBeNull()
  })

  it("rejects unknown and non-item localization keys", () => {
    expect(resolveSaoItemAtlasSprite("sao.event.darkness.name")).toBeNull()
    expect(resolveSaoItemAtlasSprite("sao.item.unknown.name")).toBeNull()
  })
})
