// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { LeaderboardScopeSelect } from "./LeaderboardScopeSelect"

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string) => key,
  }),
}))

describe("LeaderboardScopeSelect", () => {
  beforeEach(() => {
    vi.stubGlobal("requestAnimationFrame", (callback: FrameRequestCallback) => {
      callback(0)
      return 1
    })
  })

  afterEach(() => {
    cleanup()
    vi.unstubAllGlobals()
  })

  it("renders an accessible custom listbox and supports keyboard selection", () => {
    const onChange = vi.fn()
    const { container } = render(
      <LeaderboardScopeSelect subjectType="ACCOUNT" value="COINS" onChange={onChange} />,
    )

    const trigger = screen.getByRole("button", { name: "leaderboardExclusions.scope" })

    expect(container.querySelector("select")).toBeNull()
    expect(trigger.getAttribute("aria-haspopup")).toBe("listbox")

    fireEvent.click(trigger)

    const options = screen.getAllByRole("option")

    expect(options).toHaveLength(2)
    expect(options[1].getAttribute("aria-selected")).toBe("true")
    expect(document.activeElement).toBe(options[1])

    fireEvent.keyDown(options[1], { key: "ArrowUp" })

    expect(document.activeElement).toBe(options[0])

    fireEvent.keyDown(options[0], { key: "Enter" })

    expect(onChange).toHaveBeenCalledWith("ALL")
  })

  it("shows only player-compatible scopes for player exclusions", () => {
    render(<LeaderboardScopeSelect subjectType="PLAYER" value="DROPS" onChange={vi.fn()} />)

    fireEvent.click(screen.getByRole("button", { name: "leaderboardExclusions.scope" }))

    const labels = screen.getAllByRole("option").map((option) => option.textContent)

    expect(labels).toContain("leaderboardExclusions.scopes.ALL")
    expect(labels).toContain("leaderboardExclusions.scopes.DROPS")
    expect(labels).toContain("leaderboardExclusions.scopes.BOOSTERS")
    expect(labels).not.toContain("leaderboardExclusions.scopes.COINS")
  })
})
