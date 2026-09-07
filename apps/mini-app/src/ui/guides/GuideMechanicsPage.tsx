import { useMemo, useState } from "react"
import { PageHeader } from "../shared/Primitives"
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
      <PageHeader title={ui.mechanics} eyebrow={ui.mechanicsEyebrow} back={back} />

      <section className="guide-context-hero guide-context-hero--mechanics">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
        </div>
        <span>
          <strong>{ui.mechanics}</strong>
          <p>{ui.mechanicsCopy}</p>
        </span>
      </section>

      <label className="guides-search guides-search--app-pattern">
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
                <small>{ui.mechanicsEyebrow}</small>
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
    <article className="guide-mechanic-card guide-mechanic-card--content-first">
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
