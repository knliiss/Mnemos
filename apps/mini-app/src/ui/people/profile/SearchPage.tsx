import { useTranslation } from "react-i18next"
import { CristalixHead } from "../../components/profile/CristalixHead"
import type { Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { FeatureLoading } from "../../system/FeatureLoading"
import { SaoRealmEntryCard } from "../realms/SaoRealmEntryCard"
import { useProfileSearch } from "./data/useProfileSearch"
import { nicknameStyle, profileSkinUrl } from "./profilePresentation"
import "./search.css"

type Props = {
  navigate: (route: Route) => void
}

export function SearchPage({ navigate }: Props) {
  const { t } = useTranslation()
  const search = useProfileSearch()

  return (
    <div className="screen search-root-screen root-screen">
      <header className="root-title-block">
        <h1>{t("people.search.title")}</h1>
        <p>{t("people.search.description")}</p>
      </header>

      <SaoRealmEntryCard navigate={navigate} />

      <label className="search-root-input">
        <Glyph name="search" size={20} />
        <input
          value={search.query}
          maxLength={64}
          onChange={(event) => search.changeQuery(event.target.value)}
          placeholder={t("people.search.placeholder")}
          autoCapitalize="off"
          autoCorrect="off"
        />
        {search.query ? (
          <button
            type="button"
            onClick={() => search.changeQuery("")}
            aria-label={t("people.search.clear")}
          >
            <Glyph name="close" size={15} />
          </button>
        ) : null}
      </label>

      {!search.normalizedQuery ? (
        <section className="search-root-empty">
          <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
          <strong>{t("people.search.emptyTitle")}</strong>
          <small>{t("people.search.emptyCopy")}</small>
        </section>
      ) : null}

      {search.normalizedQuery.length === 1 ? (
        <p className="search-root-hint">{t("people.search.oneMore")}</p>
      ) : null}

      {search.searching ? <FeatureLoading label={t("people.search.searching")} /> : null}

      {search.normalizedQuery.length >= 2 && !search.searching && search.searchFailed ? (
        <div className="search-root-hint" role="status">
          <span>{t("common.loadErrorTitle")}</span>
          <button type="button" onClick={search.retry}>
            {t("common.retry")}
          </button>
        </div>
      ) : null}

      {search.normalizedQuery.length >= 2 &&
      !search.searching &&
      !search.searchFailed &&
      !search.results.length ? (
        <p className="search-root-hint">{t("people.search.nothingFound")}</p>
      ) : null}

      {!search.searchFailed && search.results.length ? (
        <section className="search-result-panel">
          <div className="search-result-panel__head">
            <span>{t("people.search.results")}</span>
            <b>{search.results.length}</b>
          </div>

          {search.results.map((profile) => (
            <button
              key={profile.accountId}
              type="button"
              onClick={() =>
                navigate({
                  id: "public-profile",
                  params: { accountId: profile.accountId },
                })
              }
            >
              <CristalixHead skinUrl={profileSkinUrl(profile.cristalixUuid)} size={46} />
              <span>
                <strong style={nicknameStyle(profile)}>{profile.cristalixNickname}</strong>
                <small>{t("people.search.commentsCount", { count: profile.comments })}</small>
              </span>
              <span className="search-result-stats">
                <b>↑ {profile.likes}</b>
                <i>›</i>
              </span>
            </button>
          ))}
        </section>
      ) : null}
    </div>
  )
}
