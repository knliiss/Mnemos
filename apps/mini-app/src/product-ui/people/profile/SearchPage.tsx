import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  searchPublicProfiles,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { handleError } from "@/shared/errors/errorHandler"
import { CristalixHead } from "../../components/CristalixHead"
import type { Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { nicknameStyle, profileSkinUrl } from "./profilePresentation"

type Props = {
  navigate: (route: Route) => void
}

export function SearchPage({ navigate }: Props) {
  const { t } = useTranslation()
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<PublicProfileResponse[]>([])
  const [searching, setSearching] = useState(false)
  const normalizedQuery = query.trim()

  const runSearch = useCallback(async (value: string) => {
    setSearching(true)

    try {
      const page = await searchPublicProfiles(value)
      setResults(page.content)
    } catch (error) {
      handleError(error)
      setResults([])
    } finally {
      setSearching(false)
    }
  }, [])

  useEffect(() => {
    if (normalizedQuery.length < 2) {
      return
    }

    const timeout = window.setTimeout(() => {
      void runSearch(normalizedQuery)
    }, 260)

    return () => window.clearTimeout(timeout)
  }, [normalizedQuery, runSearch])

  const changeQuery = (value: string) => {
    setQuery(value)

    if (value.trim().length < 2) {
      setResults([])
      setSearching(false)
    }
  }

  return (
    <div className="screen search-root-screen root-screen">
      <header className="root-title-block">
        <h1>{t("people.search.title")}</h1>
        <p>{t("people.search.description")}</p>
      </header>

      <label className="search-root-input">
        <Glyph name="search" size={20} />
        <input
          value={query}
          onChange={(event) => changeQuery(event.target.value)}
          placeholder="Cristalix nickname"
          autoCapitalize="off"
          autoCorrect="off"
        />
        {query ? (
          <button type="button" onClick={() => changeQuery("")} aria-label={t("people.search.clear")}>
            <Glyph name="close" size={15} />
          </button>
        ) : null}
      </label>

      {!normalizedQuery ? (
        <section className="search-root-empty">
          <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
          <strong>{t("people.search.emptyTitle")}</strong>
          <small>{t("people.search.emptyCopy")}</small>
        </section>
      ) : null}

      {normalizedQuery.length === 1 ? (
        <p className="search-root-hint">{t("people.search.oneMore")}</p>
      ) : null}

      {searching ? <p className="search-root-hint">{t("people.search.searching")}</p> : null}

      {normalizedQuery.length >= 2 && !searching && !results.length ? (
        <p className="search-root-hint">{t("people.search.nothingFound")}</p>
      ) : null}

      {results.length ? (
        <section className="search-result-panel">
          <div className="search-result-panel__head">
            <span>{t("people.search.results")}</span>
            <b>{results.length}</b>
          </div>

          {results.map((profile) => (
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
