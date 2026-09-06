import { useMemo, useState } from "react"
import { GUIDE_LOCATIONS } from "./guideCatalog"
import {
  localizationKeyMatchesQuery,
  locationMatchesQuery,
  type SaoCatalogs,
} from "./guideLocalization"
import { GUIDE_MECHANICS, mechanicMatchesQuery } from "./guideMechanics"
import { guideUiText } from "./guideUiText"

export type GuideHubSection = "locations" | "rebirth" | "dungeons" | "cases" | "mechanics"

type Props = {
  ui: ReturnType<typeof guideUiText>
  catalogs: SaoCatalogs
  text: (key: string) => string
  back: () => void
  openSection: (section: GuideHubSection) => void
  openLocation: (locationId: number) => void
  openRank: (rank: number) => void
}

type HubSearchResult = {
  key: string
  kind: string
  title: string
  meta: string
  onOpen: () => void
}

const DUNGEONS = [
  { type: "EASY", searchAliases: ["LP"] },
  { type: "NORMAL", searchAliases: ["SP"] },
  { type: "HARD", searchAliases: ["SLP"] },
  { type: "INSANE", searchAliases: ["EP"] },
] as const

export function GuideHub({ ui, catalogs, text, back, openSection, openLocation, openRank }: Props) {
  const [query, setQuery] = useState("")
  const normalizedQuery = query.trim()
  const locationResults = useMemo(
    () =>
      normalizedQuery
        ? GUIDE_LOCATIONS.filter((location) =>
            locationMatchesQuery(location, query, catalogs),
          ).slice(0, 6)
        : [],
    [catalogs, normalizedQuery, query],
  )
  const rankResults = useMemo(() => {
    if (!normalizedQuery) {
      return []
    }

    return Array.from({ length: 68 }, (_, index) => index + 1)
      .filter((rank) =>
        localizationKeyMatchesQuery(`sao.rebirth.${rank}.name`, query, catalogs, [
          String(rank),
          `rank ${rank}`,
          `rb ${rank}`,
          `рб ${rank}`,
          `ранг ${rank}`,
        ]),
      )
      .slice(0, 6)
  }, [catalogs, normalizedQuery, query])
  const dungeonResults = useMemo(() => {
    if (!normalizedQuery) {
      return []
    }

    return DUNGEONS.filter(({ type, searchAliases }) =>
      localizationKeyMatchesQuery(
        `sao.dungeon.${type}.name`,
        query,
        catalogs,
        [...searchAliases],
      ),
    )
  }, [catalogs, normalizedQuery, query])
  const caseMatches = useMemo(
    () =>
      normalizedQuery
        ? ["sao.ui.case_pet_name", "sao.ui.case_aura_name", "sao.ui.case_shard_name"].some((key) =>
            localizationKeyMatchesQuery(key, query, catalogs, [
              "case",
              "cases",
              "кейс",
              "кейсы",
              "кейси",
            ]),
          )
        : false,
    [catalogs, normalizedQuery, query],
  )
  const mechanicResults = useMemo(
    () =>
      normalizedQuery
        ? GUIDE_MECHANICS.filter((mechanic) =>
            mechanicMatchesQuery(mechanic, query, catalogs),
          ).slice(0, 5)
        : [],
    [catalogs, normalizedQuery, query],
  )
  const searchResults = useMemo<HubSearchResult[]>(() => {
    const results: HubSearchResult[] = []

    for (const location of locationResults) {
      results.push({
        key: `location-${location.id}`,
        kind: ui.location,
        title: text(location.nameKey),
        meta: location.id > 0 ? `#${location.id}` : ui.special,
        onOpen: () => openLocation(location.id),
      })
    }

    for (const rank of rankResults) {
      results.push({
        key: `rank-${rank}`,
        kind: ui.rank,
        title: text(`sao.rebirth.${rank}.name`),
        meta: `#${rank}`,
        onOpen: () => openRank(rank),
      })
    }

    for (const dungeon of dungeonResults) {
      results.push({
        key: `dungeon-${dungeon.type}`,
        kind: ui.dungeons,
        title: text(`sao.dungeon.${dungeon.type}.name`),
        meta: ui.dungeonMilestones,
        onOpen: () => openSection("dungeons"),
      })
    }

    for (const mechanic of mechanicResults) {
      results.push({
        key: `mechanic-${mechanic.id}`,
        kind: ui.mechanics,
        title: text(mechanic.titleKey),
        meta: text(mechanic.whereKey),
        onOpen: () => openSection("mechanics"),
      })
    }

    if (caseMatches) {
      results.push({
        key: "cases",
        kind: ui.cases,
        title: ui.cases,
        meta: "25 → 5M",
        onOpen: () => openSection("cases"),
      })
    }

    return results.slice(0, 12)
  }, [
    caseMatches,
    dungeonResults,
    locationResults,
    mechanicResults,
    openLocation,
    openRank,
    openSection,
    rankResults,
    text,
    ui,
  ])

  return (
    <div className="screen guides-screen guides-hub-screen">
      <GuideHubHeader title={ui.title} back={back} backLabel={ui.back} />

      <section className="guides-hero guides-hero--hub">
        <div className="guides-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/guides-cartographer.svg" alt="" />
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
          {searchResults.map((result) => (
            <button key={result.key} type="button" onClick={result.onOpen}>
              <span className="guide-hub-search-results__kind">{result.kind}</span>
              <strong>{result.title}</strong>
              <small>{result.meta}</small>
            </button>
          ))}

          {!searchResults.length ? (
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
            illustration="/ui-illustrations/guides-cartographer.svg"
            onClick={() => openSection("locations")}
          />
          <GuideHubCard
            tone="rebirth"
            title={ui.rebirth}
            copy={ui.rebirthCopy}
            meta="1 → 68"
            illustration="/ui-illustrations/guides-trainer.svg"
            onClick={() => openSection("rebirth")}
          />
          <GuideHubCard
            tone="dungeons"
            title={ui.dungeons}
            copy={ui.dungeonsCopy}
            meta={`${DUNGEONS.length}`}
            illustration="/ui-illustrations/guides-explorer.svg"
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
          <GuideHubCard
            tone="mechanics"
            title={ui.mechanics}
            copy={ui.mechanicsCopy}
            meta={`${GUIDE_MECHANICS.length}`}
            illustration="/ui-illustrations/account-settings.svg"
            onClick={() => openSection("mechanics")}
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
