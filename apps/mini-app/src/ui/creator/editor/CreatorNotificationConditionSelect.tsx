import { useEffect, useId, useRef, useState } from "react"
import { Glyph } from "../../shared/MnemosIcon"

type Option = {
  value: string
  label: string
}

type Props = {
  value: string
  options: Option[]
  disabled?: boolean
  ariaLabel: string
  onChange: (value: string) => void
}

export function CreatorNotificationConditionSelect({
  value,
  options,
  disabled = false,
  ariaLabel,
  onChange,
}: Props) {
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const menuId = useId()
  const selected = options.find((option) => option.value === value) ?? options[0]

  useEffect(() => {
    if (!open) {
      return
    }

    const handlePointerDown = (event: PointerEvent) => {
      const target = event.target

      if (!(target instanceof Node) || rootRef.current?.contains(target)) {
        return
      }

      setOpen(false)
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setOpen(false)
      }
    }

    document.addEventListener("pointerdown", handlePointerDown)
    document.addEventListener("keydown", handleKeyDown)

    return () => {
      document.removeEventListener("pointerdown", handlePointerDown)
      document.removeEventListener("keydown", handleKeyDown)
    }
  }, [open])

  const select = (nextValue: string) => {
    onChange(nextValue)
    setOpen(false)
  }

  return (
    <div
      ref={rootRef}
      className={`creator-condition-select${open ? " is-open" : ""}`}
    >
      <button
        type="button"
        className="creator-condition-select__trigger"
        disabled={disabled || options.length === 0}
        aria-label={ariaLabel}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={open ? menuId : undefined}
        onClick={() => setOpen((current) => !current)}
      >
        <span>{selected?.label ?? "—"}</span>
        <i aria-hidden="true" />
      </button>

      {open ? (
        <div
          id={menuId}
          className="creator-condition-select__menu"
          role="menu"
          aria-label={ariaLabel}
        >
          {options.map((option) => {
            const active = option.value === value

            return (
              <button
                key={option.value}
                type="button"
                role="menuitemradio"
                aria-checked={active}
                className={active ? "is-active" : undefined}
                onClick={() => select(option.value)}
              >
                <span>{option.label}</span>
                {active ? <Glyph name="check" size={14} /> : null}
              </button>
            )
          })}
        </div>
      ) : null}
    </div>
  )
}
