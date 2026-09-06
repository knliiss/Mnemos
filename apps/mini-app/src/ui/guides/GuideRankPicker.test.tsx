// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, describe, expect, it, vi } from "vitest"
import { GuideRankPicker } from "./GuideRankPicker"

afterEach(() => {
  cleanup()
})

function renderPicker(onChange = vi.fn()) {
  render(
    <GuideRankPicker
      label="Current rank"
      value={2}
      min={1}
      max={4}
      rankName={(rank) => `Rank ${rank}`}
      searchPlaceholder="Find rank"
      closeLabel="Close rank picker"
      onChange={onChange}
    />,
  )

  return onChange
}

describe("GuideRankPicker", () => {
  it("focuses search on open and restores trigger focus after Escape", async () => {
    renderPicker()

    const trigger = screen.getByRole("button", { name: /#2 Rank 2/ })
    fireEvent.click(trigger)

    const search = screen.getByRole("searchbox", { name: "Find rank" })

    await waitFor(() => {
      expect(document.activeElement).toBe(search)
    })

    fireEvent.keyDown(window, { key: "Escape" })

    await waitFor(() => {
      expect(screen.queryByRole("dialog", { name: "Current rank" })).toBeNull()
      expect(document.activeElement).toBe(trigger)
    })
  })

  it("filters ranks, selects one and restores trigger focus", async () => {
    const onChange = renderPicker()

    const trigger = screen.getByRole("button", { name: /#2 Rank 2/ })
    fireEvent.click(trigger)

    const search = screen.getByRole("searchbox", { name: "Find rank" })
    fireEvent.input(search, { target: { value: "Rank 3" } })

    expect(screen.queryByRole("option", { name: /#1 Rank 1/ })).toBeNull()

    const rankThree = screen.getByRole("option", { name: /#3 Rank 3/ })
    fireEvent.click(rankThree)

    expect(onChange).toHaveBeenCalledWith(3)

    await waitFor(() => {
      expect(screen.queryByRole("dialog", { name: "Current rank" })).toBeNull()
      expect(document.activeElement).toBe(trigger)
    })
  })
})
