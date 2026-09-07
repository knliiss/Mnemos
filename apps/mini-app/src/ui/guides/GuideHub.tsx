import { useMemo, useState } from "react"
import { PageHeader } from "../shared/Primitives"
import { GUIDE_LOCATIONS } from "./guideCatalog"
import { GUIDE_DUMP_LOCATIONS } from "./guideLocationDumpData"
import {
  localizationKeyMatchesQuery,
  locationMatchesQuery,
  type SaoCatalogs,
} from "./guideLocalization"
import { mechanicMatchesQuery, verifiedGuideMechanics } from "./guideMechanics"
import { GUIDE_REBIRTH_STEPS } from "./guideRebirthData"
import { GuideSearch } from "./GuideSearch"
import { guideUiText } from "./guideUiText"

export type GuideHubSection = "locations" | "dungeons" | "mechanics"

type Props = {
  ui: ReturnType<typeof guideUiText>
  catalogs: SaoCatalogs
  text: (key: string) => string
  back: () => void
  openSection: (section: GuideHubSection) => void
  openLocation: (locationId: number) => void
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

export function GuideHub({ ui, catalogs, text, back, openSection, openLocation }: Props) {
  const [query, setQuery] = useState("")
  const normalizedQuery = query.trim()
  const verifiedMechanics = useMemo(() => verifiedGuideMechanics(catalogs), [catalogs])
  const locationResults = useMemo(
    () =>
      normalizedQuery
        ? GUIDE_LOCATIONS.filter((location) =>
            locationMatchesQuery(location, query, catalogs),
          ).slice(0, 8)
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
      .map((rank) => ({ rank, locationId: locationForRank(rank) }))
      .filter(
        (result): result is { rank: number; locationId: number } => result.locationId !== null,
      )
      .slice(0, 6)
  }, [catalogs, normalizedQuery, query])
  const caseResults = useMemo(() => {
    if (!normalizedQuery) {
      return []
    }

    return GUIDE_DUMP_LOCATIONS.flatMap((location) =>
      location.cases.flatMap((caseData) =>
        localizationKeyMatchesQuery(caseData.key, query, catalogs, [
          "case",
          "cases",
          "кейс",
          "кейсы",
          "кейси",
        ])
          ? [{ locationId: location.id, key: caseData.key }]
          : [],
      ),
    ).slice(0, 6)
  }, [catalogs, normalizedQuery, query])
  const dungeonResults = useMemo(() => {
    if (!normalizedQuery) {
      return []
    }

    return DUNGEONS.filter(({ type, searchAliases }) =>
      localizationKeyMatchesQuery(`sao.dungeon.${type}.name`, query, catalogs, searchAliases),
    )
  }, [catalogs, normalizedQuery, query])
  const mechanicResults = useMemo(
    () =>
      normalizedQuery
        ? verifiedMechanics
            .filter((mechanic) => mechanicMatchesQuery(mechanic, query, catalogs))
            .slice(0, 5)
        : [],
    [catalogs, normalizedQuery, query, verifiedMechanics],
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

    for (const { rank, locationId } of rankResults) {
      results.push({
        key: `rank-${rank}`,
        kind: ui.rebirth,
        title: text(`sao.rebirth.${rank}.name`),
        meta: `${ui.location} #${locationId}`,
        onOpen: () => openLocation(locationId),
      })
    }

    for (const caseResult of caseResults) {
      results.push({
        key: `${caseResult.locationId}-${caseResult.key}`,
        kind: ui.cases,
        title: text(caseResult.key),
        meta:
          caseResult.locationId > 0
            ? `${ui.location} #${caseResult.locationId}`
            : text(`sao.location.${caseResult.locationId}.name`),
        onOpen: () => openLocation(caseResult.locationId),
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

    return results.slice(0, 14)
  }, [
    caseResults,
    dungeonResults,
    locationResults,
    mechanicResults,
    openLocation,
    openSection,
    rankResults,
    text,
    ui,
  ])

  return (
    <div className="screen guides-screen guides-hub-screen guides-hub-screen--redesign">
      <PageHeader title={ui.title} eyebrow={ui.eyebrow} back={back} />

      <section className="guides-hub-intro guides-hub-intro--redesign">
        <span className="guides-hub-intro__copy">
          <small>{ui.eyebrow}</small>
          <strong>{ui.heroTitle}</strong>
          <p>{ui.heroCopy}</p>
        </span>

        <div className="guides-hub-intro__facts">
          <span>
            <b>{GUIDE_LOCATIONS.length}</b>
            <small>{ui.locations}</small>
          </span>
          <span>
            <b>{DUNGEONS.length}</b>
            <small>{ui.dungeons}</small>
          </span>
          <span>
            <b>{verifiedMechanics.length}</b>
            <small>{ui.mechanics}</small>
          </span>
        </div>
      </section>

      <GuideSearch
        value={query}
        placeholder={ui.searchPlaceholder}
        clearLabel={ui.clearSearch}
        onChange={setQuery}
      />

      {normalizedQuery ? (
        <section className="guide-hub-search-results guide-hub-search-results--redesign">
          {searchResults.map((result) => (
            <button key={result.key} type="button" onClick={result.onOpen}>
              <span className="guide-hub-search-results__kind">{result.kind}</span>
              <strong>{result.title}</strong>
              <small>{result.meta}</small>
            </button>
          ))}

          {!searchResults.length ? (
            <div className="guides-empty guides-empty--search">
              <strong>{ui.noResults}</strong>
              <small>{ui.noResultsCopy}</small>
            </div>
          ) : null}
        </section>
      ) : (
        <section className="guide-hub-grid guide-hub-grid--redesign">
          <GuideHubCard
            tone="locations"
            title={ui.locations}
            copy={ui.locationsCopy}
            meta={`${GUIDE_LOCATIONS.length}`}
            onClick={() => openSection("locations")}
          />
          <GuideHubCard
            tone="dungeons"
            title={ui.dungeons}
            copy={ui.dungeonsCopy}
            meta={`${DUNGEONS.length}`}
            onClick={() => openSection("dungeons")}
          />
          {verifiedMechanics.length ? (
            <GuideHubCard
              tone="mechanics"
              title={ui.mechanics}
              copy={ui.mechanicsCopy}
              meta={`${verifiedMechanics.length}`}
              onClick={() => openSection("mechanics")}
            />
          ) : null}
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
  onClick,
}: {
  tone: GuideHubSection
  title: string
  copy: string
  meta: string
  onClick: () => void
}) {
  return (
    <button
      type="button"
      className={`guide-hub-card guide-hub-card--${tone} guide-hub-card--redesign`}
      onClick={onClick}
    >
      <span className="guide-hub-card__meta">{meta}</span>
      <span className="guide-hub-card__copy">
        <strong>{title}</strong>
        <small>{copy}</small>
      </span>
      <span className="guide-hub-card__open" aria-hidden="true" />
    </button>
  )
}

function locationForRank(rank: number) {
  if (rank === 68) {
    return 28
  }

  const step = GUIDE_REBIRTH_STEPS.find((candidate) => candidate.rank === rank)
  return step?.location ?? null
}
