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
    <section className="staff-form-block badge-assignment-search">
      <header>
        <small>{t("badgeAdmin.assignmentEyebrow")}</small>
        <strong>{t("badgeAdmin.assignment")}</strong>
      </header>

      <label className="staff-search">
        <Glyph name="search" size={18} />
        <input
          type="search"
          value={query}
          disabled={busy}
          minLength={2}
          maxLength={64}
          autoComplete="off"
          spellCheck={false}
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
                <span className="staff-search-results__avatar staff-search-results__avatar--mnemos">
                  <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" aria-hidden="true" />
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
        <div className="badge-assignment-empty is-error">
          <img src="/ui-illustrations/mnemos-mascot-404.svg" alt="" aria-hidden="true" />
          <span>{t("common.loadErrorTitle")}</span>
        </div>
      ) : normalizedQuery.length >= 2 ? (
        <div className="badge-assignment-empty">
          <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
          <span>{t("moderation.nothingFound")}</span>
        </div>
      ) : null}
    </section>
  )
}
