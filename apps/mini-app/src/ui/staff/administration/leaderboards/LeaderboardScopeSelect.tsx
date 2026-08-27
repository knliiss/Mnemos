import type { KeyboardEvent } from "react"
import { useEffect, useId, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type {
  LeaderboardExclusionScope,
  LeaderboardExclusionSubjectType,
} from "@/shared/api/leaderboardExclusionApi"
import { Glyph } from "../../../shared/MnemosIcon"
import "./leaderboard-scope-select.css"

const accountScopes: LeaderboardExclusionScope[] = ["ALL", "COINS"]
const playerScopes: LeaderboardExclusionScope[] = ["ALL", "DROPS", "BOOSTERS"]

type Props = {
  subjectType: LeaderboardExclusionSubjectType
  value: LeaderboardExclusionScope
  onChange(value: LeaderboardExclusionScope): void
}

export function LeaderboardScopeSelect({ subjectType, value, onChange }: Props) {
  const { t } = useTranslation()
  const [open, setOpen] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const triggerRef = useRef<HTMLButtonElement>(null)
  const optionRefs = useRef<Array<HTMLButtonElement | null>>([])
  const listboxId = useId()
  const options = subjectType === "ACCOUNT" ? accountScopes : playerScopes
  const selectedIndex = Math.max(0, options.indexOf(value))

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

    const handleKeyDown = (event: globalThis.KeyboardEvent) => {
      if (event.key !== "Escape") {
        return
      }

      setOpen(false)
      triggerRef.current?.focus()
    }

    document.addEventListener("pointerdown", handlePointerDown)
    document.addEventListener("keydown", handleKeyDown)

    window.requestAnimationFrame(() => {
      optionRefs.current[selectedIndex]?.focus()
    })

    return () => {
      document.removeEventListener("pointerdown", handlePointerDown)
      document.removeEventListener("keydown", handleKeyDown)
    }
  }, [open, selectedIndex])

  function select(option: LeaderboardExclusionScope) {
    onChange(option)
    setOpen(false)
    window.requestAnimationFrame(() => triggerRef.current?.focus())
  }

  function moveFocus(index: number) {
    const normalizedIndex = (index + options.length) % options.length
    optionRefs.current[normalizedIndex]?.focus()
  }

  function handleOptionKeyDown(index: number, event: KeyboardEvent<HTMLButtonElement>) {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault()
      select(options[index])
      return
    }

    if (event.key === "ArrowDown") {
      event.preventDefault()
      moveFocus(index + 1)
      return
    }

    if (event.key === "ArrowUp") {
      event.preventDefault()
      moveFocus(index - 1)
      return
    }

    if (event.key === "Home") {
      event.preventDefault()
      moveFocus(0)
      return
    }

    if (event.key === "End") {
      event.preventDefault()
      moveFocus(options.length - 1)
      return
    }

    if (event.key === "Tab") {
      setOpen(false)
    }
  }

  return (
    <div className="leaderboard-scope-field">
      <span>{t("leaderboardExclusions.scope")}</span>

      <div ref={rootRef} className={`leaderboard-scope-select${open ? " is-open" : ""}`}>
        <button
          ref={triggerRef}
          type="button"
          className="leaderboard-scope-select__trigger"
          aria-haspopup="listbox"
          aria-expanded={open}
          aria-controls={open ? listboxId : undefined}
          aria-label={t("leaderboardExclusions.scope")}
          onClick={() => setOpen((current) => !current)}
          onKeyDown={(event) => {
            if (event.key !== "ArrowDown" && event.key !== "ArrowUp") {
              return
            }

            event.preventDefault()
            setOpen(true)
          }}
        >
          <span>{t(`leaderboardExclusions.scopes.${value}`)}</span>
          <i className="leaderboard-scope-select__chevron" aria-hidden="true" />
        </button>

        {open ? (
          <div
            id={listboxId}
            className="leaderboard-scope-select__listbox"
            role="listbox"
            aria-label={t("leaderboardExclusions.scope")}
          >
            {options.map((option, index) => {
              const selected = option === value

              return (
                <button
                  key={option}
                  ref={(node) => {
                    optionRefs.current[index] = node
                  }}
                  type="button"
                  role="option"
                  tabIndex={-1}
                  aria-selected={selected}
                  className={selected ? "is-selected" : undefined}
                  onClick={() => select(option)}
                  onKeyDown={(event) => handleOptionKeyDown(index, event)}
                >
                  <span>{t(`leaderboardExclusions.scopes.${option}`)}</span>
                  {selected ? <Glyph name="check" size={14} /> : null}
                </button>
              )
            })}
          </div>
        ) : null}
      </div>
    </div>
  )
}
