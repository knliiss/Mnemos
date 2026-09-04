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

const firstUser = {
  userId: "019c1129-ef54-7000-8000-000000000601",
  username: "knalis",
  telegramId: 123456,
  activeAccountId: null,
  accountCount: 2,
  createdAt: "2026-09-03T18:00:00Z",
}

const secondUser = {
  userId: "019c1129-ef54-7000-8000-000000000602",
  username: "knalis_alt",
  telegramId: 654321,
  activeAccountId: null,
  accountCount: 1,
  createdAt: "2026-09-03T18:01:00Z",
}

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
      content: [firstUser],
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
        userId: firstUser.userId,
        username: firstUser.username,
      }),
    )
    expect(screen.queryByPlaceholderText(/uuid/i)).toBeNull()
  })

  it("supports arrow-key navigation and Enter selection", async () => {
    const onSelect = vi.fn()
    mocks.searchUserDirectory.mockResolvedValue({
      content: [firstUser, secondUser],
      page: 0,
      size: 6,
      totalElements: 2,
      totalPages: 1,
    })

    render(
      <AdminUserPicker
        label="Transfer ownership"
        placeholder="Search owner"
        selected={null}
        onSelect={onSelect}
      />,
    )

    const input = screen.getByPlaceholderText("Search owner")
    fireEvent.change(input, { target: { value: "kna" } })

    await screen.findByText("@knalis_alt")

    fireEvent.keyDown(input, { key: "ArrowDown" })
    fireEvent.keyDown(input, { key: "Enter" })

    expect(onSelect).toHaveBeenCalledWith(
      expect.objectContaining({
        userId: secondUser.userId,
        username: secondUser.username,
      }),
    )
  })
})
