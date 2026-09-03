import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  listPersonalizations,
  type PersonalizationResponse,
} from "@/shared/api/personalizationApi"
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
  const [query, setQuery] = useState("")
  const [items, setItems] = useState<PersonalizationResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [loaded, setLoaded] = useState(false)
  const [open, setOpen] = useState(false)
  const excluded = useMemo(() => new Set(excludedIds), [excludedIds])

  const loadCatalog = async () => {
    if (loaded || loading || disabled) {
      return
    }

    setLoading(true)

    try {
      const pages = await Promise.all(
        PERSONALIZATION_TYPES.map((type) => listPersonalizations(type, undefined, 0, 100)),
      )
      setItems(pages.flatMap((page) => page.items))
      setLoaded(true)
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (open && !loaded && !loading) {
      void loadCatalog()
    }
  })

  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return items
      .filter((item) => !excluded.has(item.id))
      .filter((item) => {
        if (!normalized) {
          return true
        }

        return [item.name, item.type, item.catalogKey ?? "", item.id]
          .some((value) => value.toLowerCase().includes(normalized))
      })
      .slice(0, 12)
  }, [excluded, items, query])

  const clear = () => {
    onSelect(null)
    setQuery("")
    setOpen(false)
  }

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
          <button type="button" disabled={disabled} aria-label={t("moderation.clear")} onClick={clear}>
            <Glyph name="close" size={12} />
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="admin-personalization-picker">
      <span className="admin-personalization-picker__label">
        {t("moderation.personalization", "Personalization")}
      </span>
      <div className="admin-personalization-picker__control">
        <Glyph name="search" size={16} />
        <input
          type="search"
          value={query}
          disabled={disabled}
          autoComplete="off"
          spellCheck={false}
          placeholder={t(
            "moderation.personalizationPickerPlaceholder",
            "Search catalog by name or type",
          )}
          onFocus={() => setOpen(true)}
          onChange={(event) => {
            setQuery(event.target.value)
            setOpen(true)
          }}
        />
        {loading ? <span className="admin-personalization-picker__loading" aria-hidden="true" /> : null}
        {query ? (
          <button type="button" aria-label={t("moderation.clear")} onClick={clear}>
            <Glyph name="close" size={12} />
          </button>
        ) : null}
      </div>

      {open ? (
        <div className="admin-personalization-picker__results">
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
            visible.map((item) => (
              <button
                type="button"
                key={item.id}
                onClick={() => {
                  onSelect(item)
                  setQuery("")
                  setOpen(false)
                }}
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
