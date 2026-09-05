import { describe, expect, it } from "vitest"
import {
  resolveSaoPetAtlasSprite,
  SAO_PET_ATLAS_ITEM_COUNT,
  SAO_PET_ATLAS_PATH,
} from "./saoPetArtwork"

describe("SAO pet artwork", () => {
  it("maps a canonical pet localization key to the rendered pet atlas", () => {
    const sprite = resolveSaoPetAtlasSprite("sao.item.pet_l9_4.name")

    expect(sprite).not.toBeNull()
    expect(sprite?.atlas.path).toBe(SAO_PET_ATLAS_PATH)
  })

  it("contains the current generated pet catalog", () => {
    expect(SAO_PET_ATLAS_ITEM_COUNT).toBeGreaterThan(0)
  })

  it("rejects unknown and non-pet localization keys", () => {
    expect(resolveSaoPetAtlasSprite("sao.item.pet_unknown.name")).toBeNull()
    expect(resolveSaoPetAtlasSprite("sao.item.l1_8.name")).toBeNull()
    expect(resolveSaoPetAtlasSprite("sao.event.darkness.name")).toBeNull()
  })
})
