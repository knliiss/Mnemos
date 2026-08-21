import { useTranslation } from "react-i18next"
import type { IdentitySearchResponse } from "@/shared/api/moderationApi"
import { Glyph } from "../../shared/MnemosIcon"

type BadgeAssignmentSearchProps = {
  query: string
  normalizedQuery: string
  results: IdentitySearchResponse[]
  searchFailed: boolean
  selectedUser: IdentitySearchResponse | null
  busy: boolean
  onQueryChange(value: string): void
  onSelectUser(identity: IdentitySearchResponse): void
}

export function BadgeAssignmentSearch({
  query,
  normalizedQuery,
  results,
  searchFailed,
  selectedUser,
  busy,
  onQueryChange,
  onSelectUser,
}: BadgeAssignmentSearchProps) {
  const { t } = useTranslation()

  return (
    <section className="staff-form-block">
      <header>
        <small>{t("badgeAdmin.assignmentEyebrow")}</small>
        <strong>{t("badgeAdmin.assignment")}</strong>
      </header>

      <label className="staff-search">
        <Glyph name="search" size={18} />
        <input
          value={query}
          disabled={busy}
          minLength={2}
          maxLength={64}
          onChange={(event) => onQueryChange(event.target.value)}
          placeholder={t("moderation.searchPlaceholder")}
        />
      </label>

      {results.length ? (
        <section className="staff-search-results">
          <div>
            {results.map((identity) => (
              <button
                type="button"
                key={identity.userId}
                disabled={busy}
                className={selectedUser?.userId === identity.userId ? "is-selected" : ""}
                onClick={() => onSelectUser(identity)}
              >
                <span className="staff-search-results__avatar">
                  {identity.username.slice(0, 1).toUpperCase()}
                </span>
                <span>
                  <strong>@{identity.username}</strong>
                  <small>{identity.telegramId}</small>
                </span>
                <span className="staff-search-results__accounts">
                  {t("moderation.accountCount", { count: identity.accounts.length })}
                </span>
              </button>
            ))}
          </div>
        </section>
      ) : searchFailed ? (
        <p className="inline-hint">{t("common.loadErrorTitle")}</p>
      ) : normalizedQuery.length >= 2 ? (
        <p className="inline-hint">{t("moderation.nothingFound")}</p>
      ) : null}
    </section>
  )
}
