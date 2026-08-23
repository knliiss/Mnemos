import { useEffect, useId, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"
import type { CollectorSort } from "./collectorAdminPresentation"
import "./collector-sort.css"

const sortOptions: CollectorSort[] = ["STATUS", "RECENT", "NAME"]

type CollectorSortMenuProps = {
  value: CollectorSort
  onChange(value: CollectorSort): void
}

export function CollectorSortMenu({ value, onChange }: CollectorSortMenuProps) {
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

  const select = (option: CollectorSort) => {
    onChange(option)
    setOpen(false)
  }

  return (
    <div ref={rootRef} className={`collector-admin-sort${open ? " is-open" : ""}`}>
      <span>{t("collectorAdmin.sortLabel")}</span>

      <div className="collector-admin-sort__control">
        <button
          type="button"
          className="collector-admin-sort__trigger"
          aria-haspopup="menu"
          aria-expanded={open}
          aria-controls={open ? menuId : undefined}
          onClick={() => setOpen((current) => !current)}
        >
          <span>{t(`collectorAdmin.sort.${value.toLowerCase()}`)}</span>
          <i className="collector-admin-sort__chevron" aria-hidden="true" />
        </button>

        {open ? (
          <div
            id={menuId}
            className="collector-admin-sort__menu"
            role="menu"
            aria-label={t("collectorAdmin.sortLabel")}
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
                  <span>{t(`collectorAdmin.sort.${option.toLowerCase()}`)}</span>
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
