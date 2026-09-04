import { useEffect, useId, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { searchUserDirectory, type UserIdentityDirectoryResponse } from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { Glyph } from "../../shared/MnemosIcon"
import "./moderation-admin-user-picker.css"

type Props = {
  label: string
  placeholder: string
  disabled?: boolean
  excludedUserIds?: string[]
  selected: UserIdentityDirectoryResponse | null
  onSelect(user: UserIdentityDirectoryResponse | null): void
}

export function AdminUserPicker({
  label,
  placeholder,
  disabled = false,
  excludedUserIds = [],
  selected,
  onSelect,
}: Props) {
  const { t } = useTranslation()
  const listboxId = useId()
  const rootRef = useRef<HTMLDivElement>(null)
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<UserIdentityDirectoryResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)
  const excluded = useMemo(() => new Set(excludedUserIds), [excludedUserIds])
  const pickerOpen = open && !disabled

  useEffect(() => {
    const normalized = query.trim()

    if (disabled || selected || normalized.length < 2) {
      return
    }

    let active = true
    const timeout = window.setTimeout(() => {
      setLoading(true)

      void searchUserDirectory({
        query: normalized,
        page: 0,
        size: 6,
        order: "NEWEST",
      })
        .then((page) => {
          if (active) {
            const nextResults = page.content.filter((candidate) => !excluded.has(candidate.userId))

            setResults(nextResults)
            setActiveIndex(nextResults.length > 0 ? 0 : -1)
            setOpen(true)
          }
        })
        .catch((error: unknown) => {
          if (active) {
            setResults([])
            setActiveIndex(-1)
            handleError(error)
          }
        })
        .finally(() => {
          if (active) {
            setLoading(false)
          }
        })
    }, 220)

    return () => {
      active = false
      window.clearTimeout(timeout)
    }
  }, [disabled, excluded, query, selected])

  useEffect(() => {
    if (!pickerOpen) {
      return
    }

    const closeOnOutsidePointer = (event: PointerEvent) => {
      const target = event.target

      if (target instanceof Node && rootRef.current?.contains(target)) {
        return
      }

      setOpen(false)
      setActiveIndex(-1)
    }

    document.addEventListener("pointerdown", closeOnOutsidePointer)

    return () => {
      document.removeEventListener("pointerdown", closeOnOutsidePointer)
    }
  }, [pickerOpen])

  const closePicker = () => {
    setOpen(false)
    setActiveIndex(-1)
  }

  const clear = () => {
    onSelect(null)
    setQuery("")
    setResults([])
    setLoading(false)
    closePicker()
  }

  const selectCandidate = (candidate: UserIdentityDirectoryResponse) => {
    onSelect(candidate)
    setQuery("")
    setResults([])
    closePicker()
  }

  const changeQuery = (value: string) => {
    setQuery(value)
    setOpen(true)
    setActiveIndex(-1)

    if (value.trim().length < 2) {
      setResults([])
      setLoading(false)
    }
  }

  const moveActiveOption = (direction: -1 | 1) => {
    if (results.length === 0) {
      return
    }

    setOpen(true)
    setActiveIndex((current) => {
      if (current < 0) {
        return direction > 0 ? 0 : results.length - 1
      }

      return (current + direction + results.length) % results.length
    })
  }

  const activeOptionId =
    pickerOpen && activeIndex >= 0 && results[activeIndex]
      ? `${listboxId}-option-${results[activeIndex].userId}`
      : undefined

  return (
    <div
      ref={rootRef}
      className="admin-user-picker"
      onBlur={(event) => {
        const nextFocus = event.relatedTarget

        if (!(nextFocus instanceof Node) || !event.currentTarget.contains(nextFocus)) {
          closePicker()
        }
      }}
    >
      <span className="admin-user-picker__label">{label}</span>

      {selected ? (
        <div className="admin-user-picker__selected">
          <span className="admin-user-picker__avatar" aria-hidden="true">
            <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
          </span>
          <span>
            <strong>@{selected.username}</strong>
            <small>
              {selected.telegramId} ·{" "}
              {t("moderation.accountCount", { count: selected.accountCount })}
            </small>
          </span>
          <button
            type="button"
            disabled={disabled}
            aria-label={t("moderation.clear")}
            onClick={clear}
          >
            <Glyph name="close" size={13} />
          </button>
        </div>
      ) : (
        <div className="admin-user-picker__control">
          <Glyph name="search" size={16} />
          <input
            type="search"
            role="combobox"
            value={query}
            disabled={disabled}
            autoComplete="off"
            spellCheck={false}
            placeholder={placeholder}
            aria-autocomplete="list"
            aria-expanded={pickerOpen && query.trim().length >= 2}
            aria-controls={listboxId}
            aria-activedescendant={activeOptionId}
            onFocus={() => setOpen(true)}
            onChange={(event) => changeQuery(event.target.value)}
            onKeyDown={(event) => {
              if (event.key === "ArrowDown") {
                event.preventDefault()
                moveActiveOption(1)
              } else if (event.key === "ArrowUp") {
                event.preventDefault()
                moveActiveOption(-1)
              } else if (event.key === "Enter" && activeIndex >= 0) {
                const candidate = results[activeIndex]

                if (candidate) {
                  event.preventDefault()
                  selectCandidate(candidate)
                }
              } else if (event.key === "Escape") {
                closePicker()
              }
            }}
          />
          {loading ? <span className="admin-user-picker__loading" aria-hidden="true" /> : null}
          {query ? (
            <button type="button" aria-label={t("moderation.clear")} onClick={clear}>
              <Glyph name="close" size={12} />
            </button>
          ) : null}
        </div>
      )}

      {!selected && pickerOpen && query.trim().length >= 2 ? (
        <div id={listboxId} className="admin-user-picker__results" role="listbox">
          {loading && results.length === 0 ? (
            <div className="admin-user-picker__state">{t("common.loading")}</div>
          ) : results.length === 0 ? (
            <div className="admin-user-picker__state">
              <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
              <span>{t("moderation.nothingFound")}</span>
            </div>
          ) : (
            results.map((candidate, index) => (
              <button
                type="button"
                role="option"
                tabIndex={-1}
                id={`${listboxId}-option-${candidate.userId}`}
                key={candidate.userId}
                className={activeIndex === index ? "is-keyboard-active" : ""}
                aria-selected={activeIndex === index}
                onMouseEnter={() => setActiveIndex(index)}
                onClick={() => selectCandidate(candidate)}
              >
                <span className="admin-user-picker__avatar" aria-hidden="true">
                  <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
                </span>
                <span>
                  <strong>@{candidate.username}</strong>
                  <small>{candidate.telegramId}</small>
                </span>
                <em>{t("moderation.accountCount", { count: candidate.accountCount })}</em>
              </button>
            ))
          )}
        </div>
      ) : null}
    </div>
  )
}
