import { describe, expect, it } from "vitest"
import { GUIDE_LOCATIONS } from "./guideCatalog"

describe("guide location artwork", () => {
  it("maps both special zones and all 28 numbered locations to unique WebPs", () => {
    const urls = GUIDE_LOCATIONS.map((location) => location.artworkUrl)

    expect(urls).toHaveLength(30)
    expect(new Set(urls)).toHaveLength(30)
    expect(urls.every((url) => url.endsWith(".webp"))).toBe(true)
    expect(urls.every((url) => !url.includes(".b64"))).toBe(true)
  })

  it("keeps Dungeon Hall and Starter Village outside numbered banner names", () => {
    const dungeonHall = GUIDE_LOCATIONS.find((location) => location.id === -1)
    const starterVillage = GUIDE_LOCATIONS.find((location) => location.id === 0)

    expect(dungeonHall?.artworkUrl).toBe("/guides/locations/location-dungeon-hall.webp")
    expect(starterVillage?.artworkUrl).toBe("/guides/locations/location-00-starter-village.webp")
  })
})
