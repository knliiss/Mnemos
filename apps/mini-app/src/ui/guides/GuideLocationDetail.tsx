import { useMemo, useState, type ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { PageHeader } from "../shared/Primitives"
import { GUIDE_NPC_ROLE_KEYS, type GuideLocation } from "./guideCatalog"
import { GuideDropArt } from "./GuideDropArt"
import {
  GUIDE_DUMP_LOCATION_BY_ID,
  type GuideDumpCase,
  type GuideDumpDrop,
  type GuideDumpMob,
} from "./guideLocationDumpData"
import { guideCaseEconomy } from "./guideLocationEconomyMath"
import { guideLocationUiText } from "./guideLocationUiText"
import { localizationKeyExists, type SaoCatalogs } from "./guideLocalization"
import { guideMobArtworkStyle } from "./guideMobArtwork"
import { guideNpcArtworkStyle } from "./guideNpcArtwork"
import { GUIDE_REBIRTH_STEPS, type GuideRebirthBoostStat } from "./guideRebirthData"
import {
  getGuideSourceItemStats,
  getGuideSourceMobStats,
  type GuideSourceMobStats,
} from "./guideSourceStats"
import { guideUiText } from "./guideUiText"

type GuideUi = ReturnType<typeof guideUiText>

type Props = {
  location: GuideLocation
  catalogs: SaoCatalogs
  text: (key: string) => string
  ui: GuideUi
  back: () => void
}

type DropSelection = {
  drop: GuideDumpDrop
  sourceName: string
  sourceType: "case" | "mob"
}

export function GuideLocationDetail({ location, catalogs, text, ui, back }: Props) {
  const { i18n } = useTranslation()
  const locationUi = guideLocationUiText(i18n.resolvedLanguage)
  const dump = GUIDE_DUMP_LOCATION_BY_ID.get(location.id)
  const rebirthSteps = useMemo(
    () => GUIDE_REBIRTH_STEPS.filter((step) => step.location === location.id),
    [location.id],
  )
  const [expandedMobKey, setExpandedMobKey] = useState<string | null>(null)
  const [selectedDrop, setSelectedDrop] = useState<DropSelection | null>(null)

  const cases = dump?.cases ?? []
  const mobs = dump?.mobs ?? []
  const detailsAvailable = dump?.detailsAvailable ?? false
  const confirmedNpcs = location.npcs.filter((npc) => localizationKeyExists(npc.nameKey, catalogs))

  if (selectedDrop) {
    return (
      <GuideDropDetail
        selection={selectedDrop}
        text={text}
        locationUi={locationUi}
        back={() => setSelectedDrop(null)}
      />
    )
  }

  return (
    <div className="screen guides-screen guide-location-detail guide-location-detail--redesign">
      <PageHeader
        title={text(location.nameKey)}
        eyebrow={locationLabel(location.id, locationUi)}
        back={back}
      />

      <section className="guide-location-hero guide-location-hero--redesign">
        <img src={location.artworkUrl} alt="" aria-hidden="true" />
        <span className="guide-location-hero__shade" />
        <div className="guide-location-hero__copy">
          <small>{locationLabel(location.id, locationUi)}</small>
          <strong>{text(location.nameKey)}</strong>
          <p>{text(location.descriptionKey)}</p>
        </div>
      </section>

      <section
        className="guide-location-summary guide-location-summary--redesign"
        aria-label={text(location.nameKey)}
      >
        <GuideSummaryFact label={locationUi.purchasePrice} value={dump?.purchasePrice || "—"} />
        <GuideSummaryFact label={locationUi.cases} value={locationUi.count(cases.length)} />
        <GuideSummaryFact label={locationUi.mobs} value={locationUi.count(mobs.length)} />
      </section>

      {!detailsAvailable ? (
        <section className="guides-empty guides-empty--source">
          <img src="/ui-illustrations/guides-cartographer.svg" alt="" aria-hidden="true" />
          <strong>{locationUi.detailsUnavailable}</strong>
          <small>{locationUi.detailsUnavailableCopy}</small>
        </section>
      ) : (
        <>
          {cases.length ? (
            <GuideLocationSection title={locationUi.cases} count={cases.length}>
              <div className="guide-case-list guide-case-list--redesign">
                {cases.map((caseData, index) => (
                  <GuideCaseCard
                    key={`${caseData.key}-${index}`}
                    caseData={caseData}
                    text={text}
                    locationUi={locationUi}
                    onSelectDrop={(drop) =>
                      setSelectedDrop({
                        drop,
                        sourceName: text(caseData.key),
                        sourceType: "case",
                      })
                    }
                  />
                ))}
              </div>
            </GuideLocationSection>
          ) : null}

          {rebirthSteps.length ? (
            <GuideLocationSection title={locationUi.rebirth} count={rebirthSteps.length}>
              <div className="guide-location-rebirth-list guide-location-rebirth-list--redesign">
                {rebirthSteps.map((step) => (
                  <article
                    key={step.rank}
                    className="guide-location-rebirth-card guide-location-rebirth-card--redesign"
                  >
                    <header>
                      <span className="guide-location-rebirth-card__rank">#{step.rank}</span>
                      <span>
                        <strong>{text(`sao.rebirth.${step.rank}.name`)}</strong>
                        <small>→ {text(`sao.rebirth.${step.nextRank}.name`)}</small>
                      </span>
                    </header>

                    <div className="guide-location-rebirth-card__facts">
                      <span>
                        <small>{locationUi.requirement}</small>
                        <b>{step.requirement}</b>
                      </span>
                      <span>
                        <small>{locationUi.growth}</small>
                        <b>{step.growth}</b>
                      </span>
                    </div>

                    <div className="guide-location-rebirth-card__boosts">
                      <small>{locationUi.boosts}</small>
                      {step.boosts.length ? (
                        <span>
                          {step.boosts.map((boost) => (
                            <i key={`${step.rank}-${boost.stat}`}>
                              {boostLabel(boost.stat, ui)} +{boost.amount}%
                            </i>
                          ))}
                        </span>
                      ) : (
                        <em>{locationUi.noBoosts}</em>
                      )}
                    </div>
                  </article>
                ))}
              </div>
            </GuideLocationSection>
          ) : null}

          {confirmedNpcs.length ? (
            <GuideLocationSection title={locationUi.npcs} count={confirmedNpcs.length}>
              <div className="guide-location-npc-list guide-location-npc-list--redesign">
                {confirmedNpcs.map((npc) => {
                  const artworkStyle = guideNpcArtworkStyle(location.id, npc.role)
                  const roleKeys = GUIDE_NPC_ROLE_KEYS[npc.role]

                  return (
                    <article
                      key={npc.nameKey}
                      className="guide-location-npc-card guide-location-npc-card--redesign"
                    >
                      {artworkStyle ? (
                        <span
                          className="guide-location-npc-card__portrait"
                          style={artworkStyle}
                          aria-hidden="true"
                        />
                      ) : (
                        <span className="guide-location-npc-card__fallback" aria-hidden="true">
                          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
                        </span>
                      )}
                      <span>
                        <strong>{text(npc.nameKey)}</strong>
                        <small>{text(roleKeys.role)}</small>
                        <p>{text(roleKeys.description)}</p>
                      </span>
                    </article>
                  )
                })}
              </div>
            </GuideLocationSection>
          ) : null}

          {mobs.length ? (
            <GuideLocationSection title={locationUi.mobs} count={mobs.length}>
              <div className="guide-mob-list guide-mob-list--redesign">
                {mobs.map((mob) => {
                  const expanded = expandedMobKey === mob.key

                  return (
                    <GuideMobCard
                      key={mob.key}
                      mob={mob}
                      text={text}
                      expanded={expanded}
                      locationUi={locationUi}
                      onToggle={() => setExpandedMobKey(expanded ? null : mob.key)}
                      onSelectDrop={(drop) =>
                        setSelectedDrop({
                          drop,
                          sourceName: text(mob.key),
                          sourceType: "mob",
                        })
                      }
                    />
                  )
                })}
              </div>
            </GuideLocationSection>
          ) : null}
        </>
      )}
    </div>
  )
}

function GuideSummaryFact({ label, value }: { label: string; value: string }) {
  return (
    <span className="guide-location-summary__fact">
      <small>{label}</small>
      <strong>{value}</strong>
    </span>
  )
}

function GuideLocationSection({
  title,
  count,
  children,
}: {
  title: string
  count: number
  children: ReactNode
}) {
  return (
    <section className="guide-location-section guide-location-section--redesign">
      <header>
        <strong>{title}</strong>
        <b>{count}</b>
      </header>
      {children}
    </section>
  )
}

function GuideCaseCard({
  caseData,
  text,
  locationUi,
  onSelectDrop,
}: {
  caseData: GuideDumpCase
  text: (key: string) => string
  locationUi: ReturnType<typeof guideLocationUiText>
  onSelectDrop: (drop: GuideDumpDrop) => void
}) {
  const economy = caseData.kind === "NORMAL" ? guideCaseEconomy(caseData.price) : null

  return (
    <article
      className={`guide-case-card guide-case-card--${caseData.kind.toLowerCase()} guide-case-card--redesign`}
    >
      <header className="guide-case-card__header">
        <span>
          <small>{caseKindLabel(caseData.kind, locationUi)}</small>
          <strong>{text(caseData.key)}</strong>
        </span>
        <span className="guide-case-card__price">
          <small>{locationUi.casePrice}</small>
          <b>{caseData.price || "—"}</b>
        </span>
      </header>

      {economy ? (
        <section className="guide-case-economy guide-case-economy--redesign">
          <header>
            <strong>{locationUi.economy}</strong>
            <small>{locationUi.economyNote}</small>
          </header>
          <div>
            <GuideEconomyValue
              label={locationUi.fifteenMinutesAndDarkness}
              value={economy.fifteenMinutesAndDarkness}
            />
            <GuideEconomyValue label={locationUi.explosion} value={economy.explosion} />
            <GuideEconomyValue label={locationUi.oneHour} value={economy.oneHour} />
            <GuideEconomyValue label={locationUi.eightHours} value={economy.eightHours} />
            <GuideEconomyValue label={locationUi.twelveHours} value={economy.twelveHours} />
          </div>
        </section>
      ) : null}

      <section className="guide-drop-grid-section">
        <header>
          <strong>{locationUi.drops}</strong>
          <small>{caseData.drops.length}</small>
        </header>
        <GuideDropGrid drops={caseData.drops} text={text} onSelectDrop={onSelectDrop} />
      </section>
    </article>
  )
}

function GuideEconomyValue({ label, value }: { label: string; value: string }) {
  return (
    <span className="guide-case-economy__value">
      <small>{label}</small>
      <strong>{value}</strong>
    </span>
  )
}

function GuideMobCard({
  mob,
  text,
  expanded,
  locationUi,
  onToggle,
  onSelectDrop,
}: {
  mob: GuideDumpMob
  text: (key: string) => string
  expanded: boolean
  locationUi: ReturnType<typeof guideLocationUiText>
  onToggle: () => void
  onSelectDrop: (drop: GuideDumpDrop) => void
}) {
  const artworkStyle = guideMobArtworkStyle(mob.key)
  const sourceStats = getGuideSourceMobStats(mob.key)

  return (
    <article className={`guide-mob-card guide-mob-card--redesign ${expanded ? "is-expanded" : ""}`}>
      <button
        type="button"
        className={`guide-mob-card__summary ${artworkStyle ? "" : "guide-mob-card__summary--no-art"}`}
        onClick={onToggle}
      >
        {artworkStyle ? (
          <span
            className="guide-mob-card__portrait guide-mob-card__portrait--model"
            style={artworkStyle}
            aria-hidden="true"
          />
        ) : (
          <span className="guide-mob-card__fallback" aria-hidden="true">
            <img src="/ui-illustrations/guides-explorer.svg" alt="" />
          </span>
        )}
        <span className="guide-mob-card__copy">
          <strong>{text(mob.key)}</strong>
          <small>
            {sourceStats
              ? `${locationUi.health}: ${sourceStats.health}`
              : `${locationUi.drops}: ${mob.drops.length}`}
          </small>
        </span>
        <span className="guide-mob-card__toggle" aria-hidden="true" />
      </button>

      {expanded ? (
        <div className="guide-mob-card__drops">
          {sourceStats ? <GuideMobFacts stats={sourceStats} locationUi={locationUi} /> : null}
          <GuideDropGrid drops={mob.drops} text={text} onSelectDrop={onSelectDrop} />
        </div>
      ) : null}
    </article>
  )
}

function GuideMobFacts({
  stats,
  locationUi,
}: {
  stats: GuideSourceMobStats
  locationUi: ReturnType<typeof guideLocationUiText>
}) {
  return (
    <div className="guide-mob-card__facts guide-mob-card__facts--redesign">
      <GuideSourceFact label={locationUi.spawnType} value={locationUi.mobSpawn[stats.spawn]} />
      <GuideSourceFact label={locationUi.health} value={stats.health} />
      <GuideSourceFact label={locationUi.moneyDrop} value={stats.moneyDrop} />
      <GuideSourceFact
        label={locationUi.killTime}
        value={locationUi.formatKillTime(stats.killTime)}
      />
      <GuideSourceFact label={locationUi.kills} value={stats.kills} />
    </div>
  )
}

function GuideSourceFact({ label, value }: { label: string; value: string }) {
  return (
    <span>
      <small>{label}</small>
      <strong>{value || "—"}</strong>
    </span>
  )
}

function GuideDropGrid({
  drops,
  text,
  onSelectDrop,
}: {
  drops: GuideDumpDrop[]
  text: (key: string) => string
  onSelectDrop: (drop: GuideDumpDrop) => void
}) {
  return (
    <div className="guide-drop-grid guide-drop-grid--redesign">
      {drops.map((drop, index) => (
        <button
          key={`${drop.key}-${index}`}
          type="button"
          className="guide-drop-card guide-drop-card--redesign"
          data-rarity={drop.rarity.toLowerCase()}
          onClick={() => onSelectDrop(drop)}
        >
          <GuideDropArt localizationKey={drop.key} />
          <span>
            <strong>{text(drop.key)}</strong>
            <small>{rarityLabel(drop.rarity, text)}</small>
          </span>
          {drop.quantity > 1 ? <b>×{drop.quantity}</b> : null}
        </button>
      ))}
    </div>
  )
}

function GuideDropDetail({
  selection,
  text,
  locationUi,
  back,
}: {
  selection: DropSelection
  text: (key: string) => string
  locationUi: ReturnType<typeof guideLocationUiText>
  back: () => void
}) {
  const sourceLabel = selection.sourceType === "case" ? locationUi.sourceCase : locationUi.sourceMob
  const loreKey = selection.drop.key.replace(/\.name$/, ".lore")
  const lore = text(loreKey)
  const hasLore = lore !== loreKey
  const sourceStats = getGuideSourceItemStats(selection.drop.key)

  return (
    <div className="screen guides-screen guide-drop-detail-screen">
      <PageHeader title={text(selection.drop.key)} eyebrow={locationUi.itemDetails} back={back} />

      <section className="guide-drop-detail-hero" data-rarity={selection.drop.rarity.toLowerCase()}>
        <div className="guide-drop-detail-hero__art">
          <GuideDropArt localizationKey={selection.drop.key} size="detail" />
        </div>
        <span>
          <small>{rarityLabel(selection.drop.rarity, text)}</small>
          <strong>{text(selection.drop.key)}</strong>
          {hasLore ? <p>{lore}</p> : null}
        </span>
      </section>

      <section className="guide-drop-detail-facts">
        <GuideSourceFact label={sourceLabel} value={selection.sourceName} />
        <GuideSourceFact label={locationUi.quantity} value={`×${selection.drop.quantity}`} />
      </section>

      {sourceStats.length ? (
        <section className="guide-drop-detail-stats">
          <header>
            <strong>{locationUi.itemStats}</strong>
            <b>{sourceStats.length}</b>
          </header>
          <div>
            {sourceStats.map((stat) => (
              <GuideSourceFact
                key={stat.type}
                label={locationUi.itemStat[stat.type]}
                value={stat.value}
              />
            ))}
          </div>
        </section>
      ) : null}
    </div>
  )
}

function caseKindLabel(
  kind: GuideDumpCase["kind"],
  locationUi: ReturnType<typeof guideLocationUiText>,
) {
  if (kind === "DONATE") {
    return locationUi.donateCase
  }

  if (kind === "SPECIAL") {
    return locationUi.specialCase
  }

  return locationUi.normalCase
}

function rarityLabel(rarity: GuideDumpDrop["rarity"], text: (key: string) => string) {
  if (rarity === "UNKNOWN") {
    return "—"
  }

  return text(`sao.enum.rarity.${rarity.toLowerCase()}.name`)
}

function locationLabel(locationId: number, locationUi: ReturnType<typeof guideLocationUiText>) {
  return locationId > 0 ? locationUi.locationNumber(locationId) : locationUi.specialZone
}

function boostLabel(stat: GuideRebirthBoostStat, ui: GuideUi) {
  const labels: Record<GuideRebirthBoostStat, string> = {
    POWER: ui.boostPower,
    DAMAGE: ui.boostDamage,
    LUCK: ui.boostLuck,
    MONEY: ui.boostMoney,
    ATTACK_SPEED: ui.boostAttackSpeed,
    RELIC_EXP: ui.boostRelicExp,
  }

  return labels[stat]
}
