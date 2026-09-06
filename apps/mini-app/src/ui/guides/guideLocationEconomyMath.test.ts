import { describe, expect, it } from "vitest"
import { guideCaseEconomy } from "./guideLocationEconomyMath"

describe("Guide location economy", () => {
  it("uses the confirmed 15 minute and Darkness multiplier", () => {
    expect(guideCaseEconomy("500")?.fifteenMinutesAndDarkness).toBe("4.5M")
    expect(guideCaseEconomy("50K")?.fifteenMinutesAndDarkness).toBe("450M")
  })

  it("prices the explosion as five normal minutes plus twenty minutes at +30%", () => {
    expect(guideCaseEconomy("500")?.explosion).toBe("9.3M")
  })

  it("derives longer periods from the same case opening rate", () => {
    const economy = guideCaseEconomy("780Sp")

    expect(economy?.oneHour).toBe("28.1No")
    expect(economy?.eightHours).toBe("224.6No")
    expect(economy?.twelveHours).toBe("337No")
  })

  it("rejects unsupported currencies instead of inventing a value", () => {
    expect(guideCaseEconomy("49 crystals")).toBeNull()
  })
})
