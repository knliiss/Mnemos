// @vitest-environment jsdom

import { fireEvent, render, screen } from "@testing-library/react"
import { describe, expect, it, vi } from "vitest"
import { RangeControl } from "./RangeControl"

describe("RangeControl", () => {
  it("renders controlled value and emits numeric changes", () => {
    const onChange = vi.fn()

    render(<RangeControl value={25} min={0} max={100} ariaLabel="Intensity" onChange={onChange} />)

    const input = screen.getByRole("slider", { name: "Intensity" }) as HTMLInputElement

    expect(input.value).toBe("25")
    expect(input.parentElement?.style.getPropertyValue("--range-progress")).toBe("25%")

    fireEvent.change(input, { target: { value: "42" } })

    expect(onChange).toHaveBeenCalledWith(42)
  })

  it("clamps visual progress to the available range", () => {
    const onChange = vi.fn()
    const { rerender } = render(
      <RangeControl value={-10} min={0} max={100} ariaLabel="Scale" onChange={onChange} />,
    )

    const input = screen.getByRole("slider", { name: "Scale" })

    expect(input.parentElement?.style.getPropertyValue("--range-progress")).toBe("0%")

    rerender(<RangeControl value={140} min={0} max={100} ariaLabel="Scale" onChange={onChange} />)

    expect(input.parentElement?.style.getPropertyValue("--range-progress")).toBe("100%")
  })
})
