// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { AdminUserPicker } from "./AdminUserPicker"

const mocks = vi.hoisted(() => ({
  searchUserDirectory: vi.fn(),
}))

vi.mock("@/shared/api/moderationApi", () => ({
  searchUserDirectory: mocks.searchUserDirectory,
}))

beforeEach(() => {
  mocks.searchUserDirectory.mockReset()
})

afterEach(() => {
  cleanup()
})

describe("AdminUserPicker", () => {
  it("searches identities and returns the selected user without exposing a raw UUID field", async () => {
    const onSelect = vi.fn()
    mocks.searchUserDirectory.mockResolvedValue({
      content: [
        {
          userId: "019c1129-ef54-7000-8000-000000000601",
          username: "knalis",
          telegramId: 123456,
          activeAccountId: null,
          accountCount: 2,
          createdAt: "2026-09-03T18:00:00Z",
        },
      ],
      page: 0,
      size: 6,
      totalElements: 1,
      totalPages: 1,
    })

    render(
      <AdminUserPicker
        label="Add member"
        placeholder="Search user"
        selected={null}
        onSelect={onSelect}
      />,
    )

    const input = screen.getByPlaceholderText("Search user")
    fireEvent.change(input, { target: { value: "kna" } })

    await waitFor(
      () => {
        expect(mocks.searchUserDirectory).toHaveBeenCalledWith({
          query: "kna",
          page: 0,
          size: 6,
          order: "NEWEST",
        })
      },
      { timeout: 1000 },
    )

    fireEvent.click(await screen.findByText("@knalis"))

    expect(onSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        userId: "019c1129-ef54-7000-8000-000000000601",
        username: "knalis",
      }),
    )
    expect(screen.queryByPlaceholderText(/uuid/i)).toBeNull()
  })
})
