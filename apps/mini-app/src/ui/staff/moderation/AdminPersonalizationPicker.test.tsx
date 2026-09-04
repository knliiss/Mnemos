// @vitest-environment jsdom

import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import type { PersonalizationResponse } from "@/shared/api/personalizationApi"
import { AdminPersonalizationPicker } from "./AdminPersonalizationPicker"

const mocks = vi.hoisted(() => ({
  handleError: vi.fn(),
  listPersonalizations: vi.fn(),
}))

vi.mock("react-i18next", () => ({
  useTranslation: () => ({
    t: (key: string, fallback?: string) => fallback ?? key,
  }),
}))

vi.mock("@/shared/api/personalizationApi", () => ({
  listPersonalizations: mocks.listPersonalizations,
}))

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: mocks.handleError,
}))

const firstPersonalization = createNickStyle(
  "019c11d1-bb2a-7000-8000-000000000201",
  "Aurora",
  "aurora",
)
const secondPersonalization = createNickStyle(
  "019c11d1-bb2a-7000-8000-000000000202",
  "Lime Pulse",
  "lime-pulse",
)

beforeEach(() => {
  Object.values(mocks).forEach((mock) => mock.mockReset())
})

afterEach(() => {
  cleanup()
})

describe("AdminPersonalizationPicker", () => {
  it("loads the catalog lazily and supports keyboard selection", async () => {
    const onSelect = vi.fn()
    mockCatalog([firstPersonalization, secondPersonalization])

    render(<AdminPersonalizationPicker selected={null} onSelect={onSelect} />)

    const input = screen.getByRole("combobox")

    expect(mocks.listPersonalizations).not.toHaveBeenCalled()

    fireEvent.focus(input)

    await screen.findByText("Lime Pulse")

    fireEvent.keyDown(input, { key: "ArrowDown" })
    fireEvent.keyDown(input, { key: "Enter" })

    expect(onSelect).toHaveBeenCalledWith(secondPersonalization)
  })

  it("closes the catalog when focus leaves the picker", async () => {
    mockCatalog([firstPersonalization])

    render(
      <div>
        <AdminPersonalizationPicker selected={null} onSelect={vi.fn()} />
        <button type="button">Outside</button>
      </div>,
    )

    const input = screen.getByRole("combobox")
    const outside = screen.getByRole("button", { name: "Outside" })

    fireEvent.focus(input)
    await screen.findByText("Aurora")

    fireEvent.blur(input, { relatedTarget: outside })

    expect(screen.queryByRole("listbox")).toBeNull()
  })

  it("closes the catalog when the user clicks outside", async () => {
    mockCatalog([firstPersonalization])

    render(<AdminPersonalizationPicker selected={null} onSelect={vi.fn()} />)

    const input = screen.getByRole("combobox")

    fireEvent.focus(input)
    await screen.findByText("Aurora")

    fireEvent.pointerDown(document.body)

    expect(screen.queryByRole("listbox")).toBeNull()
  })

  it("can retry catalog loading after an initial failure", async () => {
    let calls = 0

    mocks.listPersonalizations.mockImplementation(async (type: string) => {
      calls += 1

      if (calls <= 6) {
        throw new Error("temporary catalog failure")
      }

      return page(type === "NICK_STYLE" ? [firstPersonalization] : [])
    })

    render(<AdminPersonalizationPicker selected={null} onSelect={vi.fn()} />)

    const input = screen.getByRole("combobox")
    fireEvent.focus(input)

    await waitFor(() => expect(mocks.handleError).toHaveBeenCalledTimes(1))

    fireEvent.blur(input)
    fireEvent.focus(input)

    expect(await screen.findByText("Aurora")).toBeDefined()
    expect(mocks.listPersonalizations).toHaveBeenCalledTimes(12)
  })
})

function mockCatalog(items: PersonalizationResponse[]) {
  mocks.listPersonalizations.mockImplementation(async (type: string) =>
    page(type === "NICK_STYLE" ? items : []),
  )
}

function page(items: PersonalizationResponse[]) {
  return {
    items,
    page: 0,
    size: 100,
    totalItems: items.length,
    totalPages: items.length > 0 ? 1 : 0,
    hasNextPage: false,
  }
}

function createNickStyle(id: string, name: string, catalogKey: string): PersonalizationResponse {
  return {
    id,
    creatorUserId: null,
    catalogKey,
    type: "NICK_STYLE",
    origin: "SYSTEM",
    visibility: "PUBLIC",
    name,
    content: {
      colors: [
        { color: "#CBE86E", position: 0 },
        { color: "#6EB8E8", position: 1 },
      ],
      angleDegrees: 90,
      glow: null,
    },
    price: null,
    status: "PUBLISHED",
    rejectionReason: null,
    createdAt: "2026-09-03T18:00:00Z",
    updatedAt: "2026-09-03T18:00:00Z",
  }
}
