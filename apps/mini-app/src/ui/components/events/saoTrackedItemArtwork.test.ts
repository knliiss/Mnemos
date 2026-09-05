import { describe, expect, it } from "vitest"
import { resolveSaoItemAtlasSprite, SAO_ITEM_ATLAS_PATH } from "./saoTrackedItemArtwork"

describe("SAO tracked item artwork", () => {
  it("maps canonical localization keys to their atlas cells", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.d1_324.name")).toEqual({
      column: 0,
      row: 3,
    })
  })

  it("maps the reused blazing ring artwork added at the end of the atlas", () => {
    expect(resolveSaoItemAtlasSprite("sao.item.d3_504.name")).toEqual({
      column: 5,
      row: 10,
    })
  })

  it("rejects unknown and non-item localization keys", () => {
    expect(resolveSaoItemAtlasSprite("sao.event.darkness.name")).toBeNull()
    expect(resolveSaoItemAtlasSprite("sao.item.unknown.name")).toBeNull()
  })

  it("uses the rebuilt WebP atlas", () => {
    expect(SAO_ITEM_ATLAS_PATH).toBe("/event-icons/sao-tracked-items.webp")
  })
})
