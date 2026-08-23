import type { CSSProperties } from "react"
import "./range-control.css"

type RangeControlProps = {
  value: number
  min: number
  max: number
  step?: number
  disabled?: boolean
  ariaLabel?: string
  className?: string
  onChange(value: number): void
}

function progressFor(value: number, min: number, max: number): number {
  if (max <= min) {
    return 0
  }

  const progress = ((value - min) / (max - min)) * 100

  return Math.min(100, Math.max(0, progress))
}

export function RangeControl({
  value,
  min,
  max,
  step = 1,
  disabled = false,
  ariaLabel,
  className,
  onChange,
}: RangeControlProps) {
  const progress = progressFor(value, min, max)
  const style = {
    "--range-progress": `${progress}%`,
  } as CSSProperties

  return (
    <span className={`range-control ${className ?? ""}`} style={style}>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        disabled={disabled}
        aria-label={ariaLabel}
        onChange={(event) => onChange(Number(event.target.value))}
      />
    </span>
  )
}
