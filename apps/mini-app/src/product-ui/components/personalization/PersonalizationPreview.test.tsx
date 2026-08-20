// @vitest-environment jsdom

import { render, screen } from "@testing-library/react"
import { describe, expect, it, vi } from "vitest"
import type { Personalization } from "../../model"
import { PersonalizationDetails } from "./PersonalizationPreview"

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

describe("PersonalizationDetails", () => {
  it("does not hydrate missing API content from a magic personalization id", () => {
    const item: Personalization = {
      id: "p-1",
      type: "NOTIFICATION_PACK",
      target: "USER",
      name: "Unavailable pack",
      creator: "@creator",
      currency: "PRIME",
      price: 100,
      status: "PUBLISHED",
      owned: false,
      active: false,
      preview: "Unavailable pack",
    }

    render(<PersonalizationDetails item={item} />)

    expect(screen.getByText("personalization.preview.missingTitle")).toBeTruthy()
    expect(screen.getByText("personalization.preview.missingCopy")).toBeTruthy()
    expect(screen.queryByText("Нові події")).toBeNull()
  })
})
