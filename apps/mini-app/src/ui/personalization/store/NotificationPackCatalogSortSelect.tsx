import { useEffect, useId, useRef, useState } from "react"

type SortOption<TValue extends string> = {
  value: TValue
  label: string
}

type NotificationPackCatalogSortSelectProps<TValue extends string> = {
  value: TValue
  options: SortOption<TValue>[]
  label: string
  onChange: (value: TValue) => void
}

export function NotificationPackCatalogSortSelect<TValue extends string>({
  value,
  options,
  label,
  onChange,
}: NotificationPackCatalogSortSelectProps<TValue>) {
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

  const select = (nextValue: TValue) => {
    onChange(nextValue)
    setOpen(false)
  }

  return (
    <div ref={rootRef} className={`notification-pack-catalog-sort-select${open ? " is-open" : ""}`}>
      <button
        type="button"
        className="notification-pack-catalog-sort-select__trigger"
        aria-haspopup="menu"
        aria-expanded={open}
        aria-controls={open ? menuId : undefined}
        onClick={() => setOpen((current) => !current)}
      >
        <span>
          <small>{label}</small>
          <strong>{selected?.label ?? "—"}</strong>
        </span>
        <i aria-hidden="true" />
      </button>

      {open ? (
        <div
          id={menuId}
          className="notification-pack-catalog-sort-select__menu"
          role="menu"
          aria-label={label}
        >
          {options.map((option) => {
            const active = option.value === value

            return (
              <button
                type="button"
                key={option.value}
                role="menuitemradio"
                aria-checked={active}
                className={active ? "is-active" : undefined}
                onClick={() => select(option.value)}
              >
                <span>{option.label}</span>
                {active ? <i aria-hidden="true">✓</i> : null}
              </button>
            )
          })}
        </div>
      ) : null}
    </div>
  )
}
