import { describe, expect, it } from "vitest"
import { i18n } from "@/shared/i18n"
import { betAdminRaceStatusLabel, betAdminRoundStatusLabel } from "./betAdminPresentation"

describe("betAdminPresentation", () => {
  it("renders event round status using the active locale contract", () => {
    const translate = i18n.getFixedT("uk")

    expect(betAdminRoundStatusLabel(translate, "AWAITING_ADMIN_CONFIRMATION")).toBe(
      "Очікуємо підтвердження",
    )
  })

  it("renders item race status using the active locale contract", () => {
    const translate = i18n.getFixedT("en")

    expect(betAdminRaceStatusLabel(translate, "REFUNDED")).toBe("Refunded")
  })
})
