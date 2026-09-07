import { describe, expect, it } from "vitest"
import { guideSourceItemArtworkStyle } from "./guideSourceItemArtwork"

const SOURCE_BACKED_DUNGEON_ITEMS = [
  "corrupt_shard",
  "star_dust",
  "vibrant_shard",
  "arcane_shard",
  "fragment_aura_chest_key",
  "rare_shard",
  "aura_chest_key",
  "book_improvement",
] as const

describe("guideSourceItemArtworkStyle", () => {
  it.each(SOURCE_BACKED_DUNGEON_ITEMS)("resolves source-backed item %s", (itemId) => {
    const style = guideSourceItemArtworkStyle(`sao.item.${itemId}.name`, 56)

    expect(style?.backgroundImage).toBe("url(/guides/entities/source-item-atlas.webp)")
  })

  it("does not invent artwork for unresolved items", () => {
    expect(guideSourceItemArtworkStyle("sao.item.enchant_swap.name", 56)).toBeUndefined()
    expect(guideSourceItemArtworkStyle("sao.item.gang_flag.name", 56)).toBeUndefined()
  })
})
