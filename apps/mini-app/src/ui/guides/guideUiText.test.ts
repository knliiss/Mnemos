import { describe, expect, it } from "vitest"
import { guideUiText } from "./guideUiText"

const LEGACY_DUNGEON_ABBREVIATIONS = ["LP", "SP", "SLP", "EP", "ЛП", "СП", "СЛП", "ЭП", "ЕП"]

describe("guide dungeon presentation", () => {
  it.each(["en", "ru", "uk"])("does not expose legacy dungeon abbreviations in %s UI text", (language) => {
    const ui = guideUiText(language)
    const visibleDungeonText = [
      ui.dungeonsCopy,
      ui.dungeonDifficultyRange,
      ui.dungeonEasy,
      ui.dungeonNormal,
      ui.dungeonHard,
      ui.dungeonInsane,
      ui.dungeonEasyCopy,
      ui.dungeonNormalCopy,
      ui.dungeonHardCopy,
      ui.dungeonInsaneCopy,
    ].join(" ")

    for (const abbreviation of LEGACY_DUNGEON_ABBREVIATIONS) {
      expect(visibleDungeonText).not.toMatch(new RegExp(`(^|\\s|[·/])${abbreviation}($|\\s|[·/])`))
    }
  })

  it("uses the requested Ukrainian difficulty names", () => {
    const ui = guideUiText("uk")

    expect(ui.dungeonEasy).toBe("Легке підземелля")
    expect(ui.dungeonNormal).toBe("Середнє підземелля")
    expect(ui.dungeonHard).toBe("Складне підземелля")
    expect(ui.dungeonInsane).toBe("Епічне підземелля")
  })
})
