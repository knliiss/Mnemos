import { useMemo, useState, type ReactNode } from "react"
import { PetInventoryArt } from "../components/events/PetInventoryArt"
import { resolveSaoPetInventoryTextureUrl } from "../components/events/saoPetInventoryArtwork"
import { GUIDE_LOCATIONS, GUIDE_NPC_ROLE_KEYS, type GuideLocation } from "./guideCatalog"
import { GUIDE_LOCATION_ECONOMY } from "./guideLocationEconomy"
import { localizationKeyExists, locationMatchesQuery, type SaoCatalogs } from "./guideLocalization"
import { guideMaterialArtworkStyle } from "./guideMaterialArtwork"
import { GUIDE_REBIRTH_STEPS } from "./guideRebirthData"
import { guideUiText } from "./guideUiText"
import { guideEntityIndex, guideWeaponArtworkStyle } from "./guideWeaponArtwork"

type GuideUi = ReturnType<typeof guideUiText>

type Props = {
  ui: GuideUi
  catalogs: SaoCatalogs
  text: (key: string) => string
  initialLocationId?: number | null
  back: () => void
}

export function GuideLocationsPage({ ui, catalogs, text, initialLocationId, back }: Props) {
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
        catalogs={catalogs}
        text={text}
        ui={ui}
        back={() => setSelectedLocationId(null)}
      />
    )
  }

  return (
    <div className="screen guides-screen guide-locations-screen">
      <GuideHeader title={ui.locations} back={back} backLabel={ui.back} sigil="LOC" />

      <section className="guide-context-hero guide-context-hero--cartographer">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/guides-cartographer.svg" alt="" />
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
          aria-label={ui.searchPlaceholder}
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
              catalogs={catalogs}
              text={text}
              ui={ui}
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
  catalogs: SaoCatalogs
  text: (key: string) => string
  ui: GuideUi
  onOpen: () => void
}

function GuideLocationCard({ location, catalogs, text, ui, onOpen }: GuideLocationCardProps) {
  const entities = confirmedLocationEntities(location, catalogs)
  const creatureCount = entities.mobKeys.length + entities.cometMobKeys.length
  const petCount = entities.petKeys.length + entities.donatePetKeys.length
  const economy = GUIDE_LOCATION_ECONOMY[location.id]

  return (
    <button type="button" className="guide-location-card" onClick={onOpen}>
      <span className="guide-location-card__visual">
        <span
          className="guide-location-artwork"
          style={{ backgroundImage: `url(${location.artworkUrl})` }}
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
          {entities.npcs.length ? <i>{ui.npcCount(entities.npcs.length)}</i> : null}
          {creatureCount ? <i>{ui.creaturesCount(creatureCount)}</i> : null}
          {entities.weaponKeys.length ? (
            <i>
              {text("sao.ui.weapon")}: {entities.weaponKeys.length}
            </i>
          ) : null}
          {petCount ? (
            <i>
              {text("sao.ui.pet")}: {petCount}
            </i>
          ) : null}
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
  catalogs: SaoCatalogs
  text: (key: string) => string
  ui: GuideUi
  back: () => void
}

function GuideLocationDetail({ location, catalogs, text, ui, back }: GuideLocationDetailProps) {
  const entities = confirmedLocationEntities(location, catalogs)
  const creatureCount = entities.mobKeys.length + entities.cometMobKeys.length
  const economy = GUIDE_LOCATION_ECONOMY[location.id]
  const rebirthSteps = GUIDE_REBIRTH_STEPS.filter((step) => step.location === location.id)

  return (
    <div className="screen guides-screen guide-location-detail">
      <GuideHeader title={text(location.nameKey)} back={back} backLabel={ui.back} sigil="LOC" />

      <section className="guide-location-hero">
        <span
          className="guide-location-artwork guide-location-artwork--hero"
          style={{ backgroundImage: `url(${location.artworkUrl})` }}
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
          <b>{entities.npcs.length}</b>
          <small>{ui.npc}</small>
        </span>
        <span>
          <b>{entities.mobKeys.length}</b>
          <small>{ui.mobs}</small>
        </span>
        <span>
          <b>{entities.weaponKeys.length}</b>
          <small>{text("sao.ui.weapon")}</small>
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
                    {text(`sao.rebirth.${step.rank}.name`)} →{" "}
                    {text(`sao.rebirth.${step.nextRank}.name`)}
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

      {entities.npcs.length ? (
        <GuideSection title={ui.npc} count={entities.npcs.length}>
          <div className="guide-npc-grid guide-npc-grid--ingame">
            {entities.npcs.map((npc) => {
              const roleKeys = GUIDE_NPC_ROLE_KEYS[npc.role]

              return (
                <article key={npc.nameKey} className="guide-npc-card guide-npc-card--ingame">
                  <span className="guide-npc-card__gameplate" aria-hidden="true">
                    <i />
                    <small>NPC</small>
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

      {entities.mobKeys.length ? (
        <GuideSection title={ui.mobs} count={entities.mobKeys.length}>
          <GuideEntityList keys={entities.mobKeys} text={text} codePrefix="MOB" />
        </GuideSection>
      ) : null}

      {entities.cometMobKeys.length ? (
        <GuideSection title={ui.cometMobs} count={entities.cometMobKeys.length} accent>
          <div className="guide-comet-list">
            {entities.cometMobKeys.map((key) => (
              <span key={key} className="guide-comet-card">
                <i aria-hidden="true">✦</i>
                <strong>{text(key)}</strong>
              </span>
            ))}
          </div>
        </GuideSection>
      ) : null}

      {entities.weaponKeys.length ? (
        <GuideSection title={text("sao.ui.weapon")} count={entities.weaponKeys.length}>
          <GuideEntityList
            keys={entities.weaponKeys}
            text={text}
            codePrefix={`L${location.id}`}
            artwork="weapon"
          />
        </GuideSection>
      ) : null}

      {entities.petKeys.length ? (
        <GuideSection title={text("sao.ui.pet")} count={entities.petKeys.length}>
          <GuideEntityList keys={entities.petKeys} text={text} codePrefix="PET" artwork="pet" />
        </GuideSection>
      ) : null}

      {entities.donatePetKeys.length ? (
        <GuideSection title={ui.premiumPets} count={entities.donatePetKeys.length} accent>
          <GuideEntityList
            keys={entities.donatePetKeys}
            text={text}
            codePrefix="PETD"
            artwork="pet"
          />
        </GuideSection>
      ) : null}

      {entities.materialKeys.length ? (
        <GuideSection title={ui.reward} count={entities.materialKeys.length} accent>
          <GuideMaterialList keys={entities.materialKeys} catalogs={catalogs} text={text} />
        </GuideSection>
      ) : null}

      {!creatureCount && !entities.npcs.length ? (
        <section className="guide-location-special-note">
          <img src="/ui-illustrations/account-guides.svg" alt="" aria-hidden="true" />
          <small>{ui.noStaticPopulation}</small>
        </section>
      ) : null}
    </div>
  )
}

function confirmedLocationEntities(location: GuideLocation, catalogs: SaoCatalogs) {
  const confirmedKeys = (keys: string[]) =>
    keys.filter((key) => localizationKeyExists(key, catalogs))

  return {
    npcs: location.npcs.filter((npc) => localizationKeyExists(npc.nameKey, catalogs)),
    mobKeys: confirmedKeys(location.mobKeys),
    cometMobKeys: confirmedKeys(location.cometMobKeys),
    weaponKeys: confirmedKeys(location.weaponKeys),
    petKeys: confirmedKeys(location.petKeys),
    donatePetKeys: confirmedKeys(location.donatePetKeys),
    materialKeys: confirmedKeys(location.materialKeys),
  }
}

type GuideEntityArtwork = "weapon" | "pet"

function GuideEntityList({
  keys,
  text,
  codePrefix,
  artwork,
}: {
  keys: string[]
  text: (key: string) => string
  codePrefix: string
  artwork?: GuideEntityArtwork
}) {
  const illustrated = Boolean(artwork)

  return (
    <div className={`guide-entity-list ${illustrated ? "guide-entity-list--illustrated" : ""}`}>
      {keys.map((key, index) => {
        const entityIndex = guideEntityIndex(key, index + 1)
        const weaponArtworkStyle = artwork === "weapon" ? guideWeaponArtworkStyle(key) : undefined
        const petTextureUrl = artwork === "pet" ? resolveSaoPetInventoryTextureUrl(key) : null

        return (
          <span key={key} className="guide-entity-row">
            {weaponArtworkStyle ? (
              <span
                className="guide-entity-row__art guide-entity-row__art--weapon"
                style={weaponArtworkStyle}
                aria-hidden="true"
              >
                <b>{String(entityIndex).padStart(2, "0")}</b>
              </span>
            ) : petTextureUrl ? (
              <span className="guide-entity-row__art guide-entity-row__art--pet" aria-hidden="true">
                <PetInventoryArt textureUrl={petTextureUrl} />
                <b>{String(entityIndex).padStart(2, "0")}</b>
              </span>
            ) : (
              <i>{String(entityIndex).padStart(2, "0")}</i>
            )}
            <span>
              <strong>{text(key)}</strong>
              <small>
                {codePrefix}-{String(entityIndex).padStart(2, "0")}
              </small>
            </span>
          </span>
        )
      })}
    </div>
  )
}

function GuideMaterialList({
  keys,
  catalogs,
  text,
}: {
  keys: string[]
  catalogs: SaoCatalogs
  text: (key: string) => string
}) {
  return (
    <div className="guide-entity-list guide-entity-list--illustrated">
      {keys.map((key, index) => {
        const loreKey = key.replace(/\.name$/, ".lore")
        const hasLore = loreKey !== key && localizationKeyExists(loreKey, catalogs)
        const entityIndex = guideEntityIndex(key, index + 1)
        const artworkStyle = guideMaterialArtworkStyle(key)

        return (
          <span key={key} className="guide-entity-row">
            {artworkStyle ? (
              <span
                className="guide-entity-row__art guide-entity-row__art--weapon"
                style={artworkStyle}
                aria-hidden="true"
              >
                <b>{String(entityIndex).padStart(2, "0")}</b>
              </span>
            ) : (
              <i>{String(entityIndex).padStart(2, "0")}</i>
            )}
            <span>
              <strong>{text(key)}</strong>
              {hasLore ? <small>{text(loreKey)}</small> : null}
            </span>
          </span>
        )
      })}
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
