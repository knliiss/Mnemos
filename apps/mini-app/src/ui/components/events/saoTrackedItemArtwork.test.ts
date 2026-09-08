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

  it("maps every verified location weapon through the shared inventory atlas", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.l1_8.name")).toEqual({
      column: 7,
      row: 0,
      atlas: {
        path: "/guides/entities/weapon-atlas.webp",
        columns: 12,
        rows: 28,
      },
    })

    expect(resolveSaoItemAtlasSprite("sao.item.l23_1.name")).toEqual({
      column: 0,
      row: 22,
      atlas: {
        path: "/guides/entities/weapon-atlas.webp",
        columns: 12,
        rows: 28,
      },
    })

    expect(resolveSaoItemAtlasSprite("sao.item.l23_10.name")).toEqual({
      column: 9,
      row: 22,
      atlas: {
        path: "/guides/entities/weapon-atlas.webp",
        columns: 12,
        rows: 28,
      },
    })
  })

  it("does not invent sparse early-location weapon artwork", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.l1_3.name")).toBeNull()
    expect(resolveSaoItemAtlasSprite("sao.item.l2_4.name")).toBeNull()
  })

  it("rejects unknown and non-item localization keys", () => {
    expect(resolveSaoItemAtlasSprite("sao.event.darkness.name")).toBeNull()
    expect(resolveSaoItemAtlasSprite("sao.item.unknown.name")).toBeNull()
  })
})
