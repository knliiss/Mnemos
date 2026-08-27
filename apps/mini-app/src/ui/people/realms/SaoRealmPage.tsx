import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"
import type { Route } from "../../model"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import { SaoRealmCard } from "./SaoRealmCard"
import { useSaoRealmDirectory } from "./data/useSaoRealmDirectory"
import "./realms.css"
import "./sao-realm-polish.css"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function SaoRealmPage({ navigate, back }: Props) {
  const { t } = useTranslation()
  const directory = useSaoRealmDirectory()
  const [query, setQuery] = useState("")
  const normalizedQuery = query.trim().toLocaleLowerCase()

  const realms = useMemo(() => {
    if (!directory.snapshot || !normalizedQuery) {
      return directory.snapshot?.realms ?? []
    }

    return directory.snapshot.realms
      .map((realm) => ({
        ...realm,
        players: realm.players.filter((player) =>
          player.nickname.toLocaleLowerCase().includes(normalizedQuery),
        ),
      }))
      .filter((realm) => realm.players.length > 0)
  }, [directory.snapshot, normalizedQuery])

  return (
    <div className="screen sao-realms-screen">
      <PageHeader title={t("people.realms.title")} back={back} />

      <section className="sao-realms-hero">
        <div className="sao-realms-hero__mascot" aria-hidden="true">
          <span className="sao-realms-hero__orbit sao-realms-hero__orbit--outer" />
          <span className="sao-realms-hero__orbit sao-realms-hero__orbit--inner" />
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          <i>SAO</i>
        </div>
        <span>
          <small>{t("people.realms.eyebrow")}</small>
          <strong>{t("people.realms.heroTitle")}</strong>
          <p>{t("people.realms.heroCopy")}</p>
        </span>
        <b>{directory.snapshot?.trackedOnline ?? 0}</b>
      </section>

      <label className="sao-realms-search">
        <Glyph name="search" size={18} />
        <input
          type="search"
          value={query}
          onInput={(event) => setQuery(event.currentTarget.value)}
          placeholder={t("people.realms.searchPlaceholder")}
          autoCapitalize="off"
          autoComplete="off"
          autoCorrect="off"
          spellCheck={false}
          enterKeyHint="search"
        />
        {query ? (
          <button type="button" onClick={() => setQuery("")} aria-label={t("common.clear")}>
            <Glyph name="close" size={14} />
          </button>
        ) : null}
      </label>

      <div className="sao-realms-meta">
        <span>
          {directory.snapshot
            ? t("people.realms.updated", {
                time: new Date(directory.snapshot.generatedAt).toLocaleTimeString([], {
                  hour: "2-digit",
                  minute: "2-digit",
                  second: "2-digit",
                }),
              })
            : t("people.realms.loading")}
        </span>
        <button
          type="button"
          className={`sao-realms-refresh ${directory.refreshing ? "is-refreshing" : ""}`}
          aria-label={t("people.realms.refresh")}
          title={t("people.realms.refresh")}
          onClick={directory.refresh}
          disabled={directory.refreshing}
        >
          <RefreshIcon />
        </button>
      </div>

      {directory.loading && !directory.snapshot ? (
        <FeatureLoading label={t("people.realms.loading")} />
      ) : null}

      {directory.error && !directory.snapshot ? (
        <section className="sao-realms-state">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" aria-hidden="true" />
          <strong>{t("common.loadErrorTitle")}</strong>
          <small>{t("common.loadErrorCopy")}</small>
          <button type="button" onClick={directory.retry}>
            {t("common.retry")}
          </button>
        </section>
      ) : null}

      {directory.snapshot && !realms.length ? (
        <section className="sao-realms-state">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" aria-hidden="true" />
          <strong>
            {normalizedQuery ? t("people.realms.noSearchResults") : t("people.realms.emptyTitle")}
          </strong>
          <small>
            {normalizedQuery ? t("people.realms.noSearchCopy") : t("people.realms.emptyCopy")}
          </small>
        </section>
      ) : null}

      {realms.length ? (
        <section className="sao-realm-grid">
          {realms.map((realm) => (
            <SaoRealmCard
              key={realm.name}
              realm={realm}
              forceExpanded={Boolean(normalizedQuery)}
              navigate={navigate}
            />
          ))}
        </section>
      ) : null}
    </div>
  )
}

function RefreshIcon() {
  return (
    <svg
      className="sao-realms-refresh__icon"
      width="16"
      height="16"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.7"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M21 12a9 9 0 0 0-15.9-5.7L3 8" />
      <path d="M3 3v5h5" />
      <path d="M3 12a9 9 0 0 0 15.9 5.7L21 16" />
      <path d="M21 21v-5h-5" />
    </svg>
  )
}