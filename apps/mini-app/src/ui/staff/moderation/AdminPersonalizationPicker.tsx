import { useEffect, useId, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { listPersonalizations, type PersonalizationResponse } from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { PersonalizationType } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import "./moderation-admin-personalization-picker.css"

const PERSONALIZATION_TYPES: PersonalizationType[] = [
  "NOTIFICATION_PACK",
  "NICK_STYLE",
  "PROFILE_THEME",
  "PROFILE_BACKGROUND",
  "AVATAR_FRAME",
  "PROFILE_EFFECT",
]

type Props = {
  disabled?: boolean
  excludedIds?: string[]
  selected: PersonalizationResponse | null
  onSelect(personalization: PersonalizationResponse | null): void
}

export function AdminPersonalizationPicker({
  disabled = false,
  excludedIds = [],
  selected,
  onSelect,
}: Props) {
  const { t } = useTranslation()
  const listboxId = useId()
  const rootRef = useRef<HTMLDivElement>(null)
  const [query, setQuery] = useState("")
  const [items, setItems] = useState<PersonalizationResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [loaded, setLoaded] = useState(false)
  const [open, setOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)
  const loadRevision = useRef(0)
  const loadingRef = useRef(false)
  const excluded = useMemo(() => new Set(excludedIds), [excludedIds])

  useEffect(
    () => () => {
      loadRevision.current += 1
    },
    [],
  )

  useEffect(() => {
    if (!open) {
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
  }, [open])

  useEffect(() => {
    if (disabled) {
      setOpen(false)
      setActiveIndex(-1)
    }
  }, [disabled])

  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return items
      .filter((item) => !excluded.has(item.id))
      .filter((item) => {
        if (!normalized) {
          return true
        }

        return [item.name, item.type, item.catalogKey ?? "", item.id].some((value) =>
          value.toLowerCase().includes(normalized),
        )
      })
      .slice(0, 12)
  }, [excluded, items, query])

  const loadCatalog = async () => {
    if (loaded || loadingRef.current || disabled) {
      return
    }

    loadingRef.current = true
    setLoading(true)
    const revision = loadRevision.current + 1
    loadRevision.current = revision

    try {
      const pages = await Promise.all(
        PERSONALIZATION_TYPES.map((type) => listPersonalizations(type, undefined, 0, 100)),
      )

      if (revision === loadRevision.current) {
        const nextItems = pages.flatMap((page) => page.items)

        setItems(nextItems)
        setLoaded(true)
        setActiveIndex(nextItems.length > 0 ? 0 : -1)
      }
    } catch (error) {
      if (revision === loadRevision.current) {
        handleError(error)
      }
    } finally {
      loadingRef.current = false

      if (revision === loadRevision.current) {
        setLoading(false)
      }
    }
  }

  const openPicker = () => {
    setOpen(true)
    void loadCatalog()
  }

  const closePicker = () => {
    setOpen(false)
    setActiveIndex(-1)
  }

  const clear = () => {
    onSelect(null)
    setQuery("")
    closePicker()
  }

  const selectItem = (item: PersonalizationResponse) => {
    onSelect(item)
    setQuery("")
    closePicker()
  }

  const moveActiveOption = (direction: -1 | 1) => {
    if (visible.length === 0) {
      openPicker()
      return
    }

    setOpen(true)
    setActiveIndex((current) => {
      if (current < 0 || current >= visible.length) {
        return direction > 0 ? 0 : visible.length - 1
      }

      return (current + direction + visible.length) % visible.length
    })
  }

  const activeItem = open && activeIndex >= 0 ? visible[activeIndex] : undefined
  const activeOptionId = activeItem ? `${listboxId}-option-${activeItem.id}` : undefined

  if (selected) {
    return (
      <div className="admin-personalization-picker">
        <span className="admin-personalization-picker__label">
          {t("moderation.personalization", "Personalization")}
        </span>
        <div className="admin-personalization-picker__selected">
          <span className="admin-personalization-picker__mark" aria-hidden="true">
            <Glyph name="spark" size={15} />
          </span>
          <span>
            <strong>{selected.name}</strong>
            <small>
              {selected.type.replaceAll("_", " ")} · {selected.status}
            </small>
          </span>
          <button
            type="button"
            disabled={disabled}
            aria-label={t("moderation.clear")}
            onClick={clear}
          >
            <Glyph name="close" size={12} />
          </button>
        </div>
      </div>
    )
  }

  return (
    <div
      ref={rootRef}
      className="admin-personalization-picker"
      onBlur={(event) => {
        const nextFocus = event.relatedTarget

        if (!(nextFocus instanceof Node) || !event.currentTarget.contains(nextFocus)) {
          closePicker()
        }
      }}
    >
      <span className="admin-personalization-picker__label">
        {t("moderation.personalization", "Personalization")}
      </span>
      <div className="admin-personalization-picker__control">
        <Glyph name="search" size={16} />
        <input
          type="search"
          role="combobox"
          value={query}
          disabled={disabled}
          autoComplete="off"
          spellCheck={false}
          placeholder={t(
            "moderation.personalizationPickerPlaceholder",
            "Search catalog by name or type",
          )}
          aria-autocomplete="list"
          aria-expanded={open}
          aria-controls={listboxId}
          aria-activedescendant={activeOptionId}
          onFocus={openPicker}
          onChange={(event) => {
            setQuery(event.target.value)
            setActiveIndex(0)
            openPicker()
          }}
          onKeyDown={(event) => {
            if (event.key === "ArrowDown") {
              event.preventDefault()
              moveActiveOption(1)
            } else if (event.key === "ArrowUp") {
              event.preventDefault()
              moveActiveOption(-1)
            } else if (event.key === "Enter" && activeItem) {
              event.preventDefault()
              selectItem(activeItem)
            } else if (event.key === "Escape") {
              closePicker()
            }
          }}
        />
        {loading ? (
          <span className="admin-personalization-picker__loading" aria-hidden="true" />
        ) : null}
        {query ? (
          <button type="button" aria-label={t("moderation.clear")} onClick={clear}>
            <Glyph name="close" size={12} />
          </button>
        ) : null}
      </div>

      {open ? (
        <div id={listboxId} className="admin-personalization-picker__results" role="listbox">
          {loading && visible.length === 0 ? (
            <div className="admin-personalization-picker__state">{t("common.loading")}</div>
          ) : visible.length === 0 ? (
            <div className="admin-personalization-picker__state">
              <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
              <strong>{t("moderation.nothingFound")}</strong>
              <small>
                {t(
                  "moderation.personalizationPickerEmpty",
                  "No matching catalog personalization is available.",
                )}
              </small>
            </div>
          ) : (
            visible.map((item, index) => (
              <button
                type="button"
                role="option"
                tabIndex={-1}
                id={`${listboxId}-option-${item.id}`}
                key={item.id}
                className={activeIndex === index ? "is-keyboard-active" : ""}
                aria-selected={activeIndex === index}
                onMouseEnter={() => setActiveIndex(index)}
                onClick={() => selectItem(item)}
              >
                <span className="admin-personalization-picker__mark" aria-hidden="true">
                  <Glyph name="spark" size={14} />
                </span>
                <span>
                  <strong>{item.name}</strong>
                  <small>{item.type.replaceAll("_", " ")}</small>
                </span>
                <em className={`is-${item.status.toLowerCase()}`}>{item.status}</em>
              </button>
            ))
          )}
        </div>
      ) : null}
    </div>
  )
}
