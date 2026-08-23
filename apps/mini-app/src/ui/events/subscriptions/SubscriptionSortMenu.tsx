import { useEffect, useId, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"
import type { SubscriptionSort } from "./subscriptionPresentation"
import "./subscription-sort.css"

const sortOptions: SubscriptionSort[] = ["NEWEST", "OLDEST", "TYPE", "NAME"]

type SubscriptionSortMenuProps = {
  value: SubscriptionSort
  onChange(value: SubscriptionSort): void
}

export function SubscriptionSortMenu({ value, onChange }: SubscriptionSortMenuProps) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const menuId = useId()

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

  const select = (option: SubscriptionSort) => {
    onChange(option)
    setOpen(false)
  }

  return (
    <div ref={rootRef} className={`subscription-sort${open ? " is-open" : ""}`}>
      <span>{t("events.subscriptions.sortLabel")}</span>

      <div className="subscription-sort__control">
        <button
          type="button"
          className="subscription-sort__trigger"
          aria-label={t("events.subscriptions.sortAria")}
          aria-haspopup="menu"
          aria-expanded={open}
          aria-controls={open ? menuId : undefined}
          onClick={() => setOpen((current) => !current)}
        >
          <span>{t(`events.subscriptions.sort.${value.toLowerCase()}`)}</span>
          <i className="subscription-sort__chevron" aria-hidden="true" />
        </button>

        {open ? (
          <div
            id={menuId}
            className="subscription-sort__menu"
            role="menu"
            aria-label={t("events.subscriptions.sortAria")}
          >
            {sortOptions.map((option) => {
              const active = option === value

              return (
                <button
                  key={option}
                  type="button"
                  role="menuitemradio"
                  aria-checked={active}
                  className={active ? "is-active" : undefined}
                  onClick={() => select(option)}
                >
                  <span>{t(`events.subscriptions.sort.${option.toLowerCase()}`)}</span>
                  {active ? <Glyph name="check" size={14} /> : null}
                </button>
              )
            })}
          </div>
        ) : null}
      </div>
    </div>
  )
}
