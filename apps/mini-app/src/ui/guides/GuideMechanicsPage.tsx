import { useMemo, useState } from "react"
import {
  GUIDE_MECHANIC_CHAPTER_KEYS,
  mechanicMatchesQuery,
  verifiedGuideMechanics,
  type GuideMechanic,
  type GuideMechanicChapter,
} from "./guideMechanics"
import type { SaoCatalogs } from "./guideLocalization"
import { guideUiText } from "./guideUiText"

type Props = {
  ui: ReturnType<typeof guideUiText>
  catalogs: SaoCatalogs
  text: (key: string) => string
  back: () => void
}

const CHAPTERS: GuideMechanicChapter[] = [
  "FIRST_STEPS",
  "GROWTH",
  "GEAR",
  "QUESTS",
  "RAIDS",
  "DUNGEONS",
  "ENDGAME",
]

export function GuideMechanicsPage({ ui, catalogs, text, back }: Props) {
  const [query, setQuery] = useState("")
  const verifiedMechanics = useMemo(() => verifiedGuideMechanics(catalogs), [catalogs])
  const mechanics = useMemo(
    () =>
      query.trim()
        ? verifiedMechanics.filter((mechanic) => mechanicMatchesQuery(mechanic, query, catalogs))
        : verifiedMechanics,
    [catalogs, query, verifiedMechanics],
  )
  const grouped = useMemo(
    () =>
      CHAPTERS.map((chapter) => ({
        chapter,
        mechanics: mechanics.filter((mechanic) => mechanic.chapter === chapter),
      })).filter((group) => group.mechanics.length > 0),
    [mechanics],
  )

  return (
    <div className="screen guides-screen guide-mechanics-screen">
      <GuideHeader title={ui.mechanics} back={back} backLabel={ui.back} />

      <section className="guide-context-hero guide-context-hero--mechanics">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          <i>FIELD</i>
        </div>
        <span>
          <small>{ui.mechanicsEyebrow}</small>
          <strong>{ui.mechanics}</strong>
          <p>{ui.mechanicsCopy}</p>
        </span>
      </section>

      <label className="guides-search">
        <span className="guides-search__mark" aria-hidden="true" />
        <input
          type="search"
          value={query}
          onInput={(event) => setQuery(event.currentTarget.value)}
          placeholder={ui.mechanicsSearchPlaceholder}
          aria-label={ui.mechanicsSearchPlaceholder}
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

      {grouped.length ? (
        <div className="guide-mechanics-chapters">
          {grouped.map(({ chapter, mechanics: chapterMechanics }) => (
            <section key={chapter} className="guide-mechanics-chapter">
              <header>
                <small>SAO FIELD NOTES</small>
                <strong>{text(GUIDE_MECHANIC_CHAPTER_KEYS[chapter])}</strong>
                <b>{chapterMechanics.length}</b>
              </header>

              <div className="guide-mechanics-list">
                {chapterMechanics.map((mechanic) => (
                  <GuideMechanicCard key={mechanic.id} mechanic={mechanic} text={text} ui={ui} />
                ))}
              </div>
            </section>
          ))}
        </div>
      ) : (
        <section className="guides-empty">
          <img src="/ui-illustrations/account-guides.svg" alt="" aria-hidden="true" />
          <strong>{ui.noResults}</strong>
          <small>{ui.mechanicsNoResultsCopy}</small>
        </section>
      )}
    </div>
  )
}

function GuideMechanicCard({
  mechanic,
  text,
  ui,
}: {
  mechanic: GuideMechanic
  text: (key: string) => string
  ui: ReturnType<typeof guideUiText>
}) {
  return (
    <article className="guide-mechanic-card">
      <span className="guide-mechanic-card__marker" aria-hidden="true">
        <i />
        <b>{mechanic.id.slice(0, 2).toUpperCase()}</b>
      </span>

      <div className="guide-mechanic-card__body">
        <strong>{text(mechanic.titleKey)}</strong>
        <p>{text(mechanic.textKey)}</p>

        <dl>
          <div>
            <dt>{ui.mechanicsAction}</dt>
            <dd>{text(mechanic.hintKey)}</dd>
          </div>
          <div>
            <dt>{ui.mechanicsWhere}</dt>
            <dd>{text(mechanic.whereKey)}</dd>
          </div>
        </dl>
      </div>
    </article>
  )
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
        DOC
      </span>
    </header>
  )
}
