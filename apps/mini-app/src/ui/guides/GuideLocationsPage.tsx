import { useEffect, useMemo, useState, type ReactNode } from "react"
import {
  GUIDE_LOCATION_ATLAS_PARTS,
  GUIDE_LOCATIONS,
  GUIDE_NPC_ROLE_KEYS,
  type GuideLocation,
} from "./guideCatalog"
import { GUIDE_LOCATION_ECONOMY } from "./guideLocationEconomy"
import { locationMatchesQuery, type SaoCatalogs } from "./guideLocalization"
import { GUIDE_REBIRTH_STEPS } from "./guideRebirthData"
import { guideRankName } from "./guideRanks"
import { guideUiText } from "./guideUiText"

type GuideUi = ReturnType<typeof guideUiText>

type Props = {
  ui: GuideUi
  catalogs: SaoCatalogs
  text: (key: string) => string
  initialLocationId?: number | null
  back: () => void
}

export function GuideLocationsPage({ ui, catalogs, text, initialLocationId, back }: Props) {
  const atlasUrl = useGuideAtlasUrl()
  const [query, setQuery] = useState("")
  const [selectedLocationId, setSelectedLocationId] = useState<number | null>(
    initialLocationId ?? null,
  )
  const selectedLocation =
    GUIDE_LOCATIONS.find((location) => location.id === selectedLocationId) ?? null
  const locations = useMemo(
    () => GUIDE_LOCATIONS.filter((location) => locationMatchesQuery(location, query, catalogs)),
    [catalogs, query],
  )

  if (selectedLocation) {
    return (
      <GuideLocationDetail
        location={selectedLocation}
        text={text}
        ui={ui}
        atlasUrl={atlasUrl}
        back={() => setSelectedLocationId(null)}
      />
    )
  }

  return (
    <div className="screen guides-screen guide-locations-screen">
      <GuideHeader title={ui.locations} back={back} backLabel={ui.back} sigil="LOC" />

      <section className="guide-context-hero guide-context-hero--cartographer">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          <i>MAP</i>
        </div>
        <span>
          <small>{ui.eyebrow}</small>
          <strong>{ui.locations}</strong>
          <p>{ui.locationsCopy}</p>
        </span>
      </section>

      <label className="guides-search">
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

      <div className="guides-index-meta">
        <span>{ui.liveLanguages}</span>
        <b>{Object.keys(catalogs).length}</b>
      </div>

      {locations.length ? (
        <section className="guides-location-grid" aria-label={ui.locations}>
          {locations.map((location) => (
            <GuideLocationCard
              key={location.id}
              location={location}
              text={text}
              ui={ui}
              atlasUrl={atlasUrl}
              onOpen={() => setSelectedLocationId(location.id)}
            />
          ))}
        </section>
      ) : (
        <section className="guides-empty">
          <img src="/ui-illustrations/account-guides.svg" alt="" aria-hidden="true" />
          <strong>{ui.noResults}</strong>
          <small>{ui.noResultsCopy}</small>
        </section>
      )}
    </div>
  )
}

type GuideLocationCardProps = {
  location: GuideLocation
  text: (key: string) => string
  ui: GuideUi
  atlasUrl: string | null
  onOpen: () => void
}

function GuideLocationCard({ location, text, ui, atlasUrl, onOpen }: GuideLocationCardProps) {
  const creatureCount = location.mobKeys.length + location.cometMobKeys.length
  const economy = GUIDE_LOCATION_ECONOMY[location.id]

  return (
    <button type="button" className="guide-location-card" onClick={onOpen}>
      <span className="guide-location-card__visual">
        <span
          className="guide-location-artwork"
          style={guideArtworkStyle(location, atlasUrl)}
          aria-hidden="true"
        />
        <span className="guide-location-card__number">
          {location.id > 0 ? `#${location.id}` : ui.special}
        </span>
      </span>

      <span className="guide-location-card__body">
        <span className="guide-location-card__copy">
          <strong>{text(location.nameKey)}</strong>
          <small>{text(location.descriptionKey)}</small>
        </span>

        <span className="guide-location-card__stats">
          {location.npcs.length ? <i>{ui.npcCount(location.npcs.length)}</i> : null}
          {creatureCount ? <i>{ui.creaturesCount(creatureCount)}</i> : null}
          {economy ? (
            <i>
              {ui.firstCase}: {economy.caseOne}
            </i>
          ) : null}
        </span>

        <span className="guide-location-card__open">{ui.open}</span>
      </span>
    </button>
  )
}

type GuideLocationDetailProps = {
  location: GuideLocation
  text: (key: string) => string
  ui: GuideUi
  atlasUrl: string | null
  back: () => void
}

function GuideLocationDetail({ location, text, ui, atlasUrl, back }: GuideLocationDetailProps) {
  const creatureCount = location.mobKeys.length + location.cometMobKeys.length
  const economy = GUIDE_LOCATION_ECONOMY[location.id]
  const rebirthSteps = GUIDE_REBIRTH_STEPS.filter((step) => step.location === location.id)

  return (
    <div className="screen guides-screen guide-location-detail">
      <GuideHeader title={text(location.nameKey)} back={back} backLabel={ui.back} sigil="LOC" />

      <section className="guide-location-hero">
        <span
          className="guide-location-artwork guide-location-artwork--hero"
          style={guideArtworkStyle(location, atlasUrl)}
          aria-hidden="true"
        />
        <span className="guide-location-hero__shade" />
        <div className="guide-location-hero__copy">
          <small>{location.id > 0 ? `${ui.location} #${location.id}` : ui.special}</small>
          <strong>{text(location.nameKey)}</strong>
          <p>{text(location.descriptionKey)}</p>
        </div>
      </section>

      <section className="guide-location-facts">
        <span>
          <b>{location.npcs.length}</b>
          <small>{ui.npc}</small>
        </span>
        <span>
          <b>{location.mobKeys.length}</b>
          <small>{ui.mobs}</small>
        </span>
        <span>
          <b>{location.cometMobKeys.length}</b>
          <small>{ui.cometMobs}</small>
        </span>
      </section>

      {economy ? <GuideLocationEconomyPanel economy={economy} ui={ui} /> : null}

      {rebirthSteps.length ? (
        <GuideSection title={ui.rebirthOnLocation} count={rebirthSteps.length}>
          <div className="guide-location-rebirth-list">
            {rebirthSteps.map((step) => (
              <article key={step.rank} className="guide-location-rebirth-row">
                <span>
                  <small>
                    #{step.rank} → #{step.nextRank}
                  </small>
                  <strong>
                    {guideRankName(step.rank)} → {guideRankName(step.nextRank)}
                  </strong>
                </span>
                <span>
                  <i>{step.requirement}</i>
                  <b>{step.growth}</b>
                </span>
              </article>
            ))}
          </div>
        </GuideSection>
      ) : null}

      {location.npcs.length ? (
        <GuideSection title={ui.npc} count={location.npcs.length}>
          <div className="guide-npc-grid">
            {location.npcs.map((npc) => {
              const roleKeys = GUIDE_NPC_ROLE_KEYS[npc.role]

              return (
                <article key={npc.nameKey} className="guide-npc-card">
                  <span className="guide-npc-card__portrait" aria-hidden="true">
                    <i />
                    <b>{text(npc.nameKey).slice(0, 1)}</b>
                  </span>
                  <span className="guide-npc-card__copy">
                    <strong>{text(npc.nameKey)}</strong>
                    <em>&lt;{text(roleKeys.role)}&gt;</em>
                    <small>{text(roleKeys.description)}</small>
                  </span>
                </article>
              )
            })}
          </div>
        </GuideSection>
      ) : null}

      {location.mobKeys.length ? (
        <GuideSection title={ui.mobs} count={location.mobKeys.length}>
          <div className="guide-mob-list">
            {location.mobKeys.map((key, index) => (
              <span key={key} className="guide-mob-row">
                <i>{String(index + 1).padStart(2, "0")}</i>
                <strong>{text(key)}</strong>
              </span>
            ))}
          </div>
        </GuideSection>
      ) : null}

      {location.cometMobKeys.length ? (
        <GuideSection title={ui.cometMobs} count={location.cometMobKeys.length} accent>
          <div className="guide-comet-list">
            {location.cometMobKeys.map((key) => (
              <span key={key} className="guide-comet-card">
                <i aria-hidden="true">✦</i>
                <strong>{text(key)}</strong>
              </span>
            ))}
          </div>
        </GuideSection>
      ) : null}

      {!creatureCount && !location.npcs.length ? (
        <section className="guide-location-special-note">
          <img src="/ui-illustrations/account-guides.svg" alt="" aria-hidden="true" />
          <small>{ui.noStaticPopulation}</small>
        </section>
      ) : null}
    </div>
  )
}

function GuideLocationEconomyPanel({
  economy,
  ui,
}: {
  economy: (typeof GUIDE_LOCATION_ECONOMY)[number]
  ui: GuideUi
}) {
  const rows = [
    [ui.firstCase, economy.caseOne],
    economy.caseTwo ? [ui.secondCase, economy.caseTwo] : null,
    economy.darkness ? [ui.darkness, economy.darkness] : null,
    economy.fifteenMinutes ? [ui.fifteenMinutes, economy.fifteenMinutes] : null,
    [ui.darknessExplosion, economy.darknessExplosion],
    [ui.oneHour, economy.oneHour],
    [ui.eightHours, economy.eightHours],
    [ui.twelveHours, economy.twelveHours],
  ].filter((row): row is [string, string] => Boolean(row))

  return (
    <GuideSection title={ui.economy} count={rows.length} accent>
      <div className="guide-location-economy-grid">
        {rows.map(([label, value]) => (
          <span key={label} className="guide-location-economy-row">
            <small>{label}</small>
            <strong>{value}</strong>
          </span>
        ))}
      </div>
    </GuideSection>
  )
}

function GuideSection({
  title,
  count,
  accent = false,
  children,
}: {
  title: string
  count: number
  accent?: boolean
  children: ReactNode
}) {
  return (
    <section className={`guide-section ${accent ? "guide-section--accent" : ""}`}>
      <header>
        <span>
          <small>SAO</small>
          <strong>{title}</strong>
        </span>
        <b>{count}</b>
      </header>
      {children}
    </section>
  )
}

function guideArtworkStyle(location: GuideLocation, atlasUrl: string | null) {
  return {
    backgroundImage: atlasUrl ? `url(${atlasUrl})` : undefined,
    backgroundPosition: location.artworkPosition,
    backgroundSize: "500% 600%",
  }
}

function useGuideAtlasUrl() {
  const [atlasUrl, setAtlasUrl] = useState<string | null>(null)

  useEffect(() => {
    let objectUrl: string | null = null
    let active = true

    void Promise.all(
      GUIDE_LOCATION_ATLAS_PARTS.map(async (part) => {
        const response = await fetch(part)

        if (!response.ok) {
          throw new Error("atlas")
        }

        return response.text()
      }),
    )
      .then((parts) => {
        const base64 = parts.join("")

        if (!active) {
          return
        }

        const binary = atob(base64.trim())
        const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0))
        objectUrl = URL.createObjectURL(new Blob([bytes], { type: "image/webp" }))
        setAtlasUrl(objectUrl)
      })
      .catch(() => undefined)

    return () => {
      active = false

      if (objectUrl) {
        URL.revokeObjectURL(objectUrl)
      }
    }
  }, [])

  return atlasUrl
}

function GuideHeader({
  title,
  back,
  backLabel,
  sigil,
}: {
  title: string
  back: () => void
  backLabel: string
  sigil: string
}) {
  return (
    <header className="guides-page-header">
      <button type="button" onClick={back} aria-label={backLabel}>
        <span aria-hidden="true">‹</span>
      </button>
      <strong>{title}</strong>
      <span className="guides-page-header__sigil" aria-hidden="true">
        {sigil}
      </span>
    </header>
  )
}
