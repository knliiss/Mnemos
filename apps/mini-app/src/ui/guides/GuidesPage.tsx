import { useEffect, useMemo, useState, type ReactNode } from "react"
import { useTranslation } from "react-i18next"
import {
  GUIDE_LOCATION_ATLAS_PARTS,
  GUIDE_LOCATIONS,
  GUIDE_NPC_ROLE_KEYS,
  type GuideLocation,
} from "./guideCatalog"
import { locationMatchesQuery, useSaoGuideLocalization } from "./guideLocalization"
import { guideUiText } from "./guideUiText"
import "./guides.css"

type Props = {
  back: () => void
}

export function GuidesPage({ back }: Props) {
  const { i18n } = useTranslation()
  const ui = guideUiText(i18n.resolvedLanguage)
  const localization = useSaoGuideLocalization(i18n.resolvedLanguage)
  const atlasUrl = useGuideAtlasUrl()
  const [query, setQuery] = useState("")
  const [selectedLocationId, setSelectedLocationId] = useState<number | null>(null)
  const selectedLocation =
    GUIDE_LOCATIONS.find((location) => location.id === selectedLocationId) ?? null

  const locations = useMemo(
    () =>
      GUIDE_LOCATIONS.filter((location) =>
        locationMatchesQuery(location, query, localization.catalogs),
      ),
    [localization.catalogs, query],
  )

  if (selectedLocation) {
    return (
      <GuideLocationDetail
        location={selectedLocation}
        text={localization.text}
        ui={ui}
        atlasUrl={atlasUrl}
        back={() => setSelectedLocationId(null)}
      />
    )
  }

  return (
    <div className="screen guides-screen">
      <GuideHeader title={ui.title} back={back} backLabel={ui.back} />

      <section className="guides-hero">
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

        <div className="guides-hero__meta">
          <b>{GUIDE_LOCATIONS.length}</b>
          <span>{ui.locations}</span>
        </div>
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
        <b>{Object.keys(localization.catalogs).length}</b>
      </div>

      {locations.length ? (
        <section className="guides-location-grid" aria-label={ui.locations}>
          {locations.map((location) => (
            <GuideLocationCard
              key={location.id}
              location={location}
              text={localization.text}
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

type GuideUi = ReturnType<typeof guideUiText>

type GuideLocationCardProps = {
  location: GuideLocation
  text: (key: string) => string
  ui: GuideUi
  atlasUrl: string | null
  onOpen: () => void
}

function GuideLocationCard({
  location,
  text,
  ui,
  atlasUrl,
  onOpen,
}: GuideLocationCardProps) {
  const creatureCount = location.mobKeys.length + location.cometMobKeys.length

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

function GuideLocationDetail({
  location,
  text,
  ui,
  atlasUrl,
  back,
}: GuideLocationDetailProps) {
  const creatureCount = location.mobKeys.length + location.cometMobKeys.length

  return (
    <div className="screen guides-screen guide-location-detail">
      <GuideHeader title={text(location.nameKey)} back={back} backLabel={ui.back} />

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
