import { describe, expect, it } from "vitest"
import {
  localizationKeyMatchesQuery,
  resolveSaoText,
  saoLanguageForUi,
  type SaoCatalogs,
} from "./guideLocalization"

describe("guide localization", () => {
  it("maps Mnemos UI languages to SAO catalog languages", () => {
    expect(saoLanguageForUi("uk")).toBe("uk_UA")
    expect(saoLanguageForUi("ru-RU")).toBe("ru_RU")
    expect(saoLanguageForUi("en-US")).toBe("en_US")
  })

  it("uses preferred, Russian, English and canonical-key fallback order", () => {
    const catalogs: SaoCatalogs = {
      uk_UA: { "sao.example.preferred": "Українська" },
      ru_RU: {
        "sao.example.russian": "Русский",
        "sao.example.english": "Русский приоритет",
      },
      en_US: { "sao.example.english": "English" },
    }

    expect(resolveSaoText("sao.example.preferred", "uk_UA", catalogs)).toBe("Українська")
    expect(resolveSaoText("sao.example.russian", "uk_UA", catalogs)).toBe("Русский")
    expect(resolveSaoText("sao.example.english", "uk_UA", catalogs)).toBe("Русский приоритет")
    expect(resolveSaoText("sao.example.missing", "uk_UA", catalogs)).toBe("sao.example.missing")
  })

  it("keeps critical interface and progression labels concrete before remote catalogs load", () => {
    const emptyCatalogs: SaoCatalogs = {}

    expect(resolveSaoText("sao.ui.weapon", "uk_UA", emptyCatalogs)).toBe("Зброя")
    expect(resolveSaoText("sao.ui.pet", "ru_RU", emptyCatalogs)).toBe("Питомцы")
    expect(resolveSaoText("sao.ui.weapon", "en_US", emptyCatalogs)).toBe("Weapons")
    expect(resolveSaoText("sao.rebirth.68.name", "en_US", emptyCatalogs)).toBe("Archangel")
  })

  it("matches aliases and values from every loaded SAO language", () => {
    const catalogs: SaoCatalogs = {
      en_US: { "sao.location.1.name": "Dark Forest" },
      ru_RU: { "sao.location.1.name": "Тёмный лес" },
      tr_TR: { "sao.location.1.name": "Karanlık Orman" },
    }

    expect(localizationKeyMatchesQuery("sao.location.1.name", "Karanlık", catalogs)).toBe(true)
    expect(localizationKeyMatchesQuery("sao.location.1.name", "темный", catalogs)).toBe(true)
    expect(
      localizationKeyMatchesQuery("sao.location.1.name", "first zone", catalogs, ["first zone"]),
    ).toBe(true)
  })
})
