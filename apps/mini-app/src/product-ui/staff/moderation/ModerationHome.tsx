import { motion } from "framer-motion"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  searchIdentities,
  type IdentitySearchResponse,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function ModerationHome({ navigate, back }: Props) {
  const { t } = useTranslation()
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<IdentitySearchResponse[]>([])
  const [loading, setLoading] = useState(false)
  const normalized = query.trim()
  const visibleResults = normalized.length >= 2 ? results : []

  useEffect(() => {
    if (normalized.length < 2) {
      return
    }

    const timer = window.setTimeout(() => {
      setLoading(true)

      void searchIdentities(normalized)
        .then(setResults)
        .catch(handleError)
        .finally(() => setLoading(false))
    }, 250)

    return () => window.clearTimeout(timer)
  }, [normalized])

  return (
    <div className="screen moderation-screen">
      <PageHeader title={t("moderation.title")} back={back} />

      <section className="moderation-hero">
        <span className="moderation-hero__icon">
          <Glyph name="moderation" size={23} />
        </span>
        <span>
          <small>INTERNAL</small>
          <strong>{t("moderation.heroTitle")}</strong>
          <p>{t("moderation.heroDescription")}</p>
        </span>
      </section>

      <label className="staff-search">
        <Glyph name="search" size={19} />
        <input
          autoFocus
          value={query}
          minLength={2}
          maxLength={64}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={t("moderation.searchPlaceholder")}
        />
        {query ? (
          <button aria-label={t("moderation.clear")} onClick={() => setQuery("")}>
            <Glyph name="close" size={14} />
          </button>
        ) : null}
      </label>

      {!normalized ? (
        <section className="moderation-help">
          <span>{t("moderation.searchableBy")}</span>
          <div>
            <i>Username</i>
            <i>Telegram ID</i>
            <i>User ID</i>
            <i>Cristalix</i>
          </div>
        </section>
      ) : null}

      {normalized.length === 1 ? (
        <p className="moderation-empty">{t("moderation.oneMoreCharacter")}</p>
      ) : null}

      {normalized.length >= 2 && loading ? <FeatureLoading /> : null}

      {!loading && visibleResults.length ? (
        <section className="staff-search-results">
          <header>
            <strong>{t("moderation.results")}</strong>
            <small>{visibleResults.length}</small>
          </header>

          <div>
            {visibleResults.map((identity) => (
              <motion.button
                key={identity.userId}
                whileTap={{ scale: 0.985 }}
                onClick={() =>
                  navigate({
                    id: "moderation-user",
                    params: { userId: identity.userId },
                  })
                }
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
                <b>›</b>
              </motion.button>
            ))}
          </div>
        </section>
      ) : null}

      {!loading && normalized.length >= 2 && !visibleResults.length ? (
        <p className="moderation-empty">{t("moderation.nothingFound")}</p>
      ) : null}
    </div>
  )
}
