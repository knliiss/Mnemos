import { describe, expect, it } from "vitest"
import { GUIDE_LOCATIONS } from "./guideCatalog"

describe("guide location artwork", () => {
  it("maps every guide location to one individual WebP", () => {
    const urls = GUIDE_LOCATIONS.map((location) => location.artworkUrl)

    expect(urls).toHaveLength(30)
    expect(new Set(urls)).toHaveLength(30)
    expect(urls.every((url) => url.endsWith(".webp"))).toBe(true)
    expect(urls.every((url) => !url.includes(".b64"))).toBe(true)
  })

  it("keeps special locations outside the numbered banner names", () => {
    const starterVillage = GUIDE_LOCATIONS.find((location) => location.id === 0)
    const dungeonHall = GUIDE_LOCATIONS.find((location) => location.id === -1)

    expect(starterVillage?.artworkUrl).toBe(
      "/guides/locations/location-00-starter-village.webp",
    )
    expect(dungeonHall?.artworkUrl).toBe(
      "/guides/locations/location-special-dungeon-hall.webp",
    )
  })
})
