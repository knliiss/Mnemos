import { useMemo, useState } from "react"
import { GUIDE_LOCATIONS } from "./guideCatalog"
import { locationMatchesQuery, type SaoCatalogs } from "./guideLocalization"
import { GUIDE_RANK_NAMES_RU } from "./guideRanks"
import { guideUiText } from "./guideUiText"

export type GuideHubSection = "locations" | "rebirth" | "dungeons" | "cases"

type Props = {
  ui: ReturnType<typeof guideUiText>
  catalogs: SaoCatalogs
  text: (key: string) => string
  back: () => void
  openSection: (section: GuideHubSection) => void
  openLocation: (locationId: number) => void
  openRank: (rank: number) => void
}

export function GuideHub({
  ui,
  catalogs,
  text,
  back,
  openSection,
  openLocation,
  openRank,
}: Props) {
  const [query, setQuery] = useState("")
  const normalizedQuery = query.trim().toLocaleLowerCase()
  const locationResults = useMemo(
    () =>
      normalizedQuery
        ? GUIDE_LOCATIONS.filter((location) => locationMatchesQuery(location, query, catalogs)).slice(
            0,
            6,
          )
        : [],
    [catalogs, normalizedQuery, query],
  )
  const rankResults = useMemo(() => {
    if (!normalizedQuery) {
      return []
    }

    return Object.entries(GUIDE_RANK_NAMES_RU)
      .map(([rank, name]) => ({ rank: Number(rank), name }))
      .filter(({ rank, name }) => {
        const searchable = `${rank} ${name}`.toLocaleLowerCase()
        return searchable.includes(normalizedQuery)
      })
      .slice(0, 6)
  }, [normalizedQuery])
  const hasSearchResults = locationResults.length > 0 || rankResults.length > 0

  return (
    <div className="screen guides-screen guides-hub-screen">
      <GuideHubHeader title={ui.title} back={back} backLabel={ui.back} />

      <section className="guides-hero guides-hero--hub">
        <div className="guides-hero__mascot" aria-hidden="true">
          <span className="guides-hero__map-lines" />
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          <span className="guides-hero__compass">N</span>
          <span className="guides-hero__scroll">SAO</span>
        </div>

        <div className="guides-hero__copy">
          <small>{ui.eyebrow}</small>
          <strong>{ui.heroTitle}</strong>
          <p>{ui.heroCopy}</p>
        </div>
      </section>

      <label className="guides-search guides-search--hub">
        <span className="guides-search__mark" aria-hidden="true" />
        <input
          type="search"
          value={query}
          onInput={(event) => setQuery(event.currentTarget.value)}
          placeholder={ui.searchPlaceholder}
          autoCapitalize="off"
          autoComplete="off"
          autoCorrect="off"
          spellCheck={false}
          enterKeyHint="search"
        />
        {query ? (
          <button type="button" onClick={() => setQuery("")} aria-label={ui.clearSearch}>
            ×
          </button>
        ) : null}
      </label>

      {normalizedQuery ? (
        <section className="guide-hub-search-results">
          {locationResults.map((location) => (
            <button key={`location-${location.id}`} type="button" onClick={() => openLocation(location.id)}>
              <span className="guide-hub-search-results__kind">{ui.location}</span>
              <strong>{text(location.nameKey)}</strong>
              <small>{location.id > 0 ? `#${location.id}` : ui.special}</small>
            </button>
          ))}

          {rankResults.map(({ rank, name }) => (
            <button key={`rank-${rank}`} type="button" onClick={() => openRank(rank)}>
              <span className="guide-hub-search-results__kind">{ui.rank}</span>
              <strong>{name}</strong>
              <small>#{rank}</small>
            </button>
          ))}

          {!hasSearchResults ? (
            <div className="guide-hub-search-results__empty">
              <strong>{ui.noResults}</strong>
              <small>{ui.noResultsCopy}</small>
            </div>
          ) : null}
        </section>
      ) : (
        <section className="guide-hub-grid">
          <GuideHubCard
            tone="locations"
            title={ui.locations}
            copy={ui.locationsCopy}
            meta={`${GUIDE_LOCATIONS.length}`}
            illustration="/ui-illustrations/account-guides.svg"
            onClick={() => openSection("locations")}
          />
          <GuideHubCard
            tone="rebirth"
            title={ui.rebirth}
            copy={ui.rebirthCopy}
            meta="1 → 68"
            illustration="/ui-illustrations/admin-leaderboards.svg"
            onClick={() => openSection("rebirth")}
          />
          <GuideHubCard
            tone="dungeons"
            title={ui.dungeons}
            copy={ui.dungeonsCopy}
            meta="LP · SP · SLP · EP"
            illustration="/ui-illustrations/admin-bets.svg"
            onClick={() => openSection("dungeons")}
          />
          <GuideHubCard
            tone="cases"
            title={ui.cases}
            copy={ui.casesCopy}
            meta="25 → 5M"
            illustration="/ui-illustrations/account-activity.svg"
            onClick={() => openSection("cases")}
          />
        </section>
      )}
    </div>
  )
}

function GuideHubCard({
  tone,
  title,
  copy,
  meta,
  illustration,
  onClick,
}: {
  tone: GuideHubSection
  title: string
  copy: string
  meta: string
  illustration: string
  onClick: () => void
}) {
  return (
    <button type="button" className={`guide-hub-card guide-hub-card--${tone}`} onClick={onClick}>
      <span className="guide-hub-card__art" aria-hidden="true">
        <img src={illustration} alt="" />
        <i>{meta}</i>
      </span>
      <span className="guide-hub-card__copy">
        <strong>{title}</strong>
        <small>{copy}</small>
      </span>
      <span className="guide-hub-card__open" aria-hidden="true">
        ›
      </span>
    </button>
  )
}

function GuideHubHeader({
  title,
  back,
  backLabel,
}: {
  title: string
  back: () => void
  backLabel: string
}) {
  return (
    <header className="guides-page-header">
      <button type="button" onClick={back} aria-label={backLabel}>
        <span aria-hidden="true">‹</span>
      </button>
      <strong>{title}</strong>
      <span className="guides-page-header__sigil" aria-hidden="true">
        SAO
      </span>
    </header>
  )
}
