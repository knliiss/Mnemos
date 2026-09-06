import { describe, expect, it } from "vitest"
import { GUIDE_LOCATIONS } from "./guideCatalog"

describe("guide location artwork", () => {
  it("maps starter village and all 28 numbered locations to individual WebPs", () => {
    const urls = GUIDE_LOCATIONS.map((location) => location.artworkUrl)

    expect(urls).toHaveLength(29)
    expect(new Set(urls)).toHaveLength(29)
    expect(urls.every((url) => url.endsWith(".webp"))).toBe(true)
    expect(urls.every((url) => !url.includes(".b64"))).toBe(true)
  })

  it("keeps starter village outside the numbered banner names", () => {
    const starterVillage = GUIDE_LOCATIONS.find((location) => location.id === 0)

    expect(starterVillage?.artworkUrl).toBe("/guides/locations/location-00-starter-village.webp")
    expect(GUIDE_LOCATIONS.some((location) => location.id === -1)).toBe(false)
  })
})
