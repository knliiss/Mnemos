// @vitest-environment jsdom

import { render, screen } from "@testing-library/react"
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { handleError } from "@/shared/errors/errorHandler"
import { RootErrorBoundary } from "./RootErrorBoundary"

vi.mock("@/shared/errors/errorHandler", () => ({
  handleError: vi.fn(),
}))

function BrokenChild(): never {
  throw new Error("render failed")
}

describe("RootErrorBoundary", () => {
  beforeEach(() => {
    vi.mocked(handleError).mockReset()
    vi.spyOn(console, "error").mockImplementation(() => undefined)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it("replaces a crashed subtree with the Mnemos internal error surface", () => {
    render(
      <RootErrorBoundary>
        <BrokenChild />
      </RootErrorBoundary>,
    )

    const alert = screen.getByRole("alert")

    expect(alert.querySelector(".error-mascot--internal")).not.toBeNull()
    expect(screen.getByRole("button")).not.toBeNull()
    expect(handleError).toHaveBeenCalledTimes(1)
  })
})
