import { describe, expect, it } from "vitest"
import { resolveSaoPetInventoryTextureUrl } from "./saoPetInventoryArtwork"

describe("SAO PET inventory artwork", () => {
  it("maps a canonical PET localization key to its inventory head texture", () => {
    expect(resolveSaoPetInventoryTextureUrl("sao.item.pet_l9_4.name")).toBe(
      "https://rustorage.c7x.dev/butowka/sao/global/img/pet/pet_l9_4.png",
    )
  })

  it("rejects non-PET and malformed localization keys", () => {
    expect(resolveSaoPetInventoryTextureUrl("sao.item.l9_4.name")).toBeNull()
    expect(resolveSaoPetInventoryTextureUrl("sao.item.pet_bad.name")).toBeNull()
    expect(resolveSaoPetInventoryTextureUrl("sao.event.pet_l9_4.name")).toBeNull()
  })
})
