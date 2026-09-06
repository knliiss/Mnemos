import { describe, expect, it } from "vitest"
import { guideMaterialArtworkStyle } from "./guideMaterialArtwork"

describe("guide material artwork", () => {
  it("maps upgrade materials and comet shards to the confirmed resource-pack atlas", () => {
    expect(guideMaterialArtworkStyle("sao.item.weapon_upgrade_material_11.name")).toMatchObject({
      backgroundPosition: "0px 0px",
    })
    expect(guideMaterialArtworkStyle("sao.item.comet_shard_11_1.name")).toMatchObject({
      backgroundPosition: "-38px 0px",
    })
    expect(guideMaterialArtworkStyle("sao.item.weapon_upgrade_material_28.name")).toMatchObject({
      backgroundPosition: "0px -646px",
    })
    expect(guideMaterialArtworkStyle("sao.item.comet_shard_28_1.name")).toMatchObject({
      backgroundPosition: "-38px -646px",
    })
  })

  it("rejects unrelated and out-of-range localization keys", () => {
    expect(guideMaterialArtworkStyle("sao.item.weapon_upgrade_material_10.name")).toBeUndefined()
    expect(guideMaterialArtworkStyle("sao.item.comet_shard_29_1.name")).toBeUndefined()
    expect(guideMaterialArtworkStyle("sao.item.l11_1.name")).toBeUndefined()
  })
})
