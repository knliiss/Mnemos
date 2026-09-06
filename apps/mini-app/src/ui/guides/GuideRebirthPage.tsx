import { useMemo, useState } from "react"
import { GUIDE_REBIRTH_STEPS, type GuideRebirthBoostStat } from "./guideRebirthData"
import { guideUiText } from "./guideUiText"

type Props = {
  ui: ReturnType<typeof guideUiText>
  text: (key: string) => string
  initialCurrentRank?: number | null
  back: () => void
}

export function GuideRebirthPage({ ui, text, initialCurrentRank, back }: Props) {
  const initialRank = Math.min(Math.max(initialCurrentRank ?? 1, 1), 67)
  const [currentRank, setCurrentRank] = useState(initialRank)
  const [targetRank, setTargetRank] = useState(Math.min(Math.max(initialRank + 1, 10), 68))
  const steps = useMemo(
    () =>
      GUIDE_REBIRTH_STEPS.filter((step) => step.rank >= currentRank && step.nextRank <= targetRank),
    [currentRank, targetRank],
  )
  const locationCount = new Set(steps.map((step) => step.location)).size
  const rankName = (rank: number) => text(`sao.rebirth.${rank}.name`)

  const changeCurrentRank = (rank: number) => {
    setCurrentRank(rank)
    setTargetRank((currentTarget) => Math.max(currentTarget, rank + 1))
  }

  return (
    <div className="screen guides-screen guide-rebirth-screen">
      <GuideSubpageHeader title={ui.rebirth} back={back} backLabel={ui.back} />

      <section className="guide-context-hero guide-context-hero--trainer">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/guides-trainer.svg" alt="" />
          <i>RB</i>
        </div>
        <span>
          <small>{ui.calculator}</small>
          <strong>{ui.rebirth}</strong>
          <p>{ui.calculatorCopy}</p>
        </span>
      </section>

      <section className="guide-rank-calculator" aria-label={ui.calculator}>
        <label>
          <small>{ui.currentRank}</small>
          <select
            value={currentRank}
            onChange={(event) => changeCurrentRank(Number(event.target.value))}
          >
            {rankOptions(1, 67).map((rank) => (
              <option key={rank} value={rank}>
                #{rank} · {rankName(rank)}
              </option>
            ))}
          </select>
        </label>

        <span className="guide-rank-calculator__arrow" aria-hidden="true">
          →
        </span>

        <label>
          <small>{ui.targetRank}</small>
          <select
            value={targetRank}
            onChange={(event) => setTargetRank(Number(event.target.value))}
          >
            {rankOptions(currentRank + 1, 68).map((rank) => (
              <option key={rank} value={rank}>
                #{rank} · {rankName(rank)}
              </option>
            ))}
          </select>
        </label>
      </section>

      <section className="guide-rank-summary">
        <span>
          <b>{steps.length}</b>
          <small>{ui.transitions}</small>
        </span>
        <span>
          <b>{locationCount}</b>
          <small>{ui.locationsUsed}</small>
        </span>
        <span>
          <b>#{targetRank}</b>
          <small>{rankName(targetRank)}</small>
        </span>
      </section>

      <section className="guide-rank-route">
        {steps.map((step) => (
          <article key={step.rank} className="guide-rank-step">
            <header>
              <span className="guide-rank-step__number">#{step.rank}</span>
              <span className="guide-rank-step__title">
                <strong>{rankName(step.rank)}</strong>
                <small>→ {rankName(step.nextRank)}</small>
              </span>
              <span className="guide-rank-step__location">L{step.location}</span>
            </header>

            <div className="guide-rank-step__facts">
              <span>
                <small>{ui.requirement}</small>
                <b>{step.requirement}</b>
              </span>
              <span>
                <small>{ui.growth}</small>
                <b>{step.growth}</b>
              </span>
            </div>

            <div className="guide-rank-step__boosts">
              <small>{ui.boosts}</small>
              {step.boosts.length ? (
                <span>
                  {step.boosts.map((boost) => (
                    <i key={`${boost.stat}-${boost.amount}`}>
                      {boostLabel(boost.stat, ui)} +{boost.amount}%
                    </i>
                  ))}
                </span>
              ) : (
                <em>{ui.noBoosts}</em>
              )}
            </div>
          </article>
        ))}
      </section>
    </div>
  )
}

function rankOptions(from: number, to: number) {
  return Array.from({ length: to - from + 1 }, (_, index) => from + index)
}

function boostLabel(stat: GuideRebirthBoostStat, ui: ReturnType<typeof guideUiText>) {
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

function GuideSubpageHeader({
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
        RB
      </span>
    </header>
  )
}