import { describe, expect, it } from "vitest"
import { guideMobArtworkStyle } from "./guideMobArtwork"

describe("Guide mob artwork", () => {
  it("maps early dump mobs to the current rendered model atlas", () => {
    expect(guideMobArtworkStyle("sao.mob.mob1_1.name")).toMatchObject({
      backgroundImage: "url(/guides/entities/mob-model-atlas-1.webp)",
      backgroundPosition: "0px 0px",
      backgroundSize: "896px 128px",
    })
  })

  it("maps location eight using the dump-backed five mob layout", () => {
    expect(guideMobArtworkStyle("sao.mob.mob8_5.name")).toMatchObject({
      backgroundImage: "url(/guides/entities/mob-model-atlas-2.webp)",
      backgroundPosition: "-256px 0px",
    })
  })

  it("maps the last verified current resource-pack mob", () => {
    expect(guideMobArtworkStyle("sao.mob.mob25_7.name")).toMatchObject({
      backgroundImage: "url(/guides/entities/mob-model-atlas-6.webp)",
      backgroundPosition: "-704px 0px",
      backgroundSize: "896px 64px",
    })
  })

  it("does not pretend old dump textures are current models", () => {
    expect(guideMobArtworkStyle("sao.mob.mob26_1.name")).toBeUndefined()
    expect(guideMobArtworkStyle("sao.mob.mob27_1.name")).toBeUndefined()
  })

  it("rejects non-mob and out-of-range keys", () => {
    expect(guideMobArtworkStyle("sao.item.l1_1.name")).toBeUndefined()
    expect(guideMobArtworkStyle("sao.mob.mob1_5.name")).toBeUndefined()
  })
})
