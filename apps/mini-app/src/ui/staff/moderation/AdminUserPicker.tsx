import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  searchUserDirectory,
  type UserIdentityDirectoryResponse,
} from "@/shared/api/moderationApi"
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
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<UserIdentityDirectoryResponse[]>([])
  const [loading, setLoading] = useState(false)
  const [open, setOpen] = useState(false)
  const excluded = useMemo(() => new Set(excludedUserIds), [excludedUserIds])

  useEffect(() => {
    const normalized = query.trim()

    if (disabled || selected || normalized.length < 2) {
      setResults([])
      setLoading(false)
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
            setResults(page.content.filter((candidate) => !excluded.has(candidate.userId)))
            setOpen(true)
          }
        })
        .catch((error: unknown) => {
          if (active) {
            setResults([])
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

  const clear = () => {
    onSelect(null)
    setQuery("")
    setResults([])
    setOpen(false)
  }

  return (
    <div className="admin-user-picker">
      <span className="admin-user-picker__label">{label}</span>

      {selected ? (
        <div className="admin-user-picker__selected">
          <span className="admin-user-picker__avatar" aria-hidden="true">
            <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
          </span>
          <span>
            <strong>@{selected.username}</strong>
            <small>
              {selected.telegramId} · {t("moderation.accountCount", { count: selected.accountCount })}
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
            value={query}
            disabled={disabled}
            autoComplete="off"
            spellCheck={false}
            placeholder={placeholder}
            onFocus={() => setOpen(true)}
            onChange={(event) => setQuery(event.target.value)}
          />
          {loading ? <span className="admin-user-picker__loading" aria-hidden="true" /> : null}
          {query ? (
            <button type="button" aria-label={t("moderation.clear")} onClick={clear}>
              <Glyph name="close" size={12} />
            </button>
          ) : null}
        </div>
      )}

      {!selected && open && query.trim().length >= 2 ? (
        <div className="admin-user-picker__results">
          {loading && results.length === 0 ? (
            <div className="admin-user-picker__state">{t("common.loading")}</div>
          ) : results.length === 0 ? (
            <div className="admin-user-picker__state">
              <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
              <span>{t("moderation.nothingFound")}</span>
            </div>
          ) : (
            results.map((candidate) => (
              <button
                type="button"
                key={candidate.userId}
                onClick={() => {
                  onSelect(candidate)
                  setQuery("")
                  setResults([])
                  setOpen(false)
                }}
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
