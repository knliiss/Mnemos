import { describe, expect, it } from "vitest"
import { guideMobArtworkStyle } from "./guideMobArtwork"

describe("Guide mob artwork", () => {
  it("maps early dump mobs to the verified source artwork", () => {
    expect(guideMobArtworkStyle("sao.mob.mob1_1.name")).toMatchObject({
      backgroundImage:
        "url(https://storage.c7x.dev/butowka/sao/global/img/mob/mob1_1.png)",
      backgroundPosition: "center",
      backgroundSize: "contain",
    })
  })

  it("keeps source-specific artwork overrides from the dump", () => {
    expect(guideMobArtworkStyle("sao.mob.mob13_2.name")?.backgroundImage).toBe(
      "url(https://storage.c7x.dev/butowka/sao/global/img/mob/mob12_2.png)",
    )
  })

  it("covers source-backed mobs on locations 26 and 27", () => {
    expect(guideMobArtworkStyle("sao.mob.mob26_1.name")?.backgroundImage).toBe(
      "url(https://storage.c7x.dev/butowka/sao/global/img/mob/mob26_1.png)",
    )
    expect(guideMobArtworkStyle("sao.mob.mob27_7.name")?.backgroundImage).toBe(
      "url(https://storage.c7x.dev/butowka/sao/global/img/mob/mob27_7.png)",
    )
  })

  it("does not invent the broken Oceanid source portrait", () => {
    expect(guideMobArtworkStyle("sao.mob.mob17_7.name")).toBeUndefined()
  })

  it("rejects non-mob and out-of-range keys", () => {
    expect(guideMobArtworkStyle("sao.item.l1_1.name")).toBeUndefined()
    expect(guideMobArtworkStyle("sao.mob.mob1_5.name")).toBeUndefined()
    expect(guideMobArtworkStyle("sao.mob.mob28_1.name")).toBeUndefined()
  })
})
