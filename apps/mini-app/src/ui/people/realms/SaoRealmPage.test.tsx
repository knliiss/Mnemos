// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { SaoRealmPage } from "./SaoRealmPage"

const state = vi.hoisted(() => ({
  refresh: vi.fn(),
  retry: vi.fn(),
  refreshing: false,
}))

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

vi.mock("../../shared/Primitives", () => ({
  PageHeader: () => null,
}))

vi.mock("../../system/FeatureLoading", () => ({
  FeatureLoading: () => null,
}))

vi.mock("./SaoRealmCard", () => ({
  SaoRealmCard: () => null,
}))

vi.mock("./data/useSaoRealmDirectory", () => ({
  useSaoRealmDirectory: () => ({
    snapshot: {
      generatedAt: "2026-08-27T16:00:00Z",
      trackedOnline: 1,
      realms: [
        {
          name: "SAO-1",
          number: 1,
          playerCount: 1,
          players: [],
        },
      ],
    },
    loading: false,
    refreshing: state.refreshing,
    error: false,
    retry: state.retry,
    refresh: state.refresh,
  }),
}))

describe("SaoRealmPage", () => {
  beforeEach(() => {
    state.refresh.mockReset()
    state.retry.mockReset()
    state.refreshing = false
  })

  afterEach(() => {
    cleanup()
  })

  it("uses the current Mnemos cat artwork without the legacy realm mascot", () => {
    const { container } = render(<SaoRealmPage navigate={vi.fn()} back={vi.fn()} />)
    const catArtwork = container.querySelectorAll(
      'img[src="/ui-illustrations/mnemos-mascot-cat.svg"]',
    )

    expect(catArtwork).toHaveLength(1)
    expect(
      container.querySelector('img[src="/ui-illustrations/mnemos-mascot-realms.svg"]'),
    ).toBeNull()
  })

  it("uses a circular-arrow refresh icon and invokes refresh", () => {
    render(<SaoRealmPage navigate={vi.fn()} back={vi.fn()} />)

    const refresh = screen.getByRole("button", { name: "people.realms.refresh" })

    expect(refresh.textContent).toBe("")
    expect(refresh.getAttribute("title")).toBe("people.realms.refresh")
    expect(refresh.querySelector(".sao-realms-refresh__icon")).not.toBeNull()
    expect(refresh.querySelector(".glyph")).toBeNull()

    fireEvent.click(refresh)

    expect(state.refresh).toHaveBeenCalledTimes(1)
  })

  it("accepts search input and clears the current query", () => {
    render(<SaoRealmPage navigate={vi.fn()} back={vi.fn()} />)

    const input = screen.getByPlaceholderText("people.realms.searchPlaceholder") as HTMLInputElement

    fireEvent.input(input, { target: { value: "Crafter" } })

    expect(input.value).toBe("Crafter")

    fireEvent.click(screen.getByRole("button", { name: "common.clear" }))

    expect(input.value).toBe("")
  })

  it("disables and marks the refresh icon while refreshing", () => {
    state.refreshing = true

    render(<SaoRealmPage navigate={vi.fn()} back={vi.fn()} />)

    const refresh = screen.getByRole("button", { name: "people.realms.refresh" })

    expect((refresh as HTMLButtonElement).disabled).toBe(true)
    expect(refresh.className).toContain("is-refreshing")
  })
})