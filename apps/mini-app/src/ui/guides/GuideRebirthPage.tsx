import { useMemo, useState } from "react"
import { PageHeader } from "../shared/Primitives"
import { GuideRankPicker } from "./GuideRankPicker"
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
    <div className="screen guides-screen guide-rebirth-screen guide-rebirth-screen--redesign">
      <PageHeader title={ui.rebirth} eyebrow={ui.calculator} back={back} />

      <section className="guide-section-intro guide-section-intro--rebirth">
        <span>
          <small>{ui.calculator}</small>
          <strong>{ui.rebirth}</strong>
          <p>{ui.calculatorCopy}</p>
        </span>
        <img
          className="guide-section-intro__art"
          src="/ui-illustrations/guides-trainer.svg"
          alt=""
          aria-hidden="true"
        />
      </section>

      <section
        className="guide-rank-calculator guide-rank-calculator--redesign"
        aria-label={ui.calculator}
      >
        <GuideRankPicker
          label={ui.currentRank}
          value={currentRank}
          min={1}
          max={67}
          rankName={rankName}
          searchPlaceholder={ui.currentRank}
          closeLabel={ui.back}
          onChange={changeCurrentRank}
        />

        <span className="guide-rank-calculator__connector" aria-hidden="true" />

        <GuideRankPicker
          label={ui.targetRank}
          value={targetRank}
          min={currentRank + 1}
          max={68}
          rankName={rankName}
          searchPlaceholder={ui.targetRank}
          closeLabel={ui.back}
          onChange={setTargetRank}
        />
      </section>

      <section className="guide-rank-summary guide-rank-summary--redesign">
        <span>
          <b>{steps.length}</b>
          <small>{ui.transitions}</small>
        </span>
        <span>
          <b>{locationCount}</b>
          <small>{ui.locationsUsed}</small>
        </span>
        <span>
          <b>RB {targetRank}</b>
          <small>{rankName(targetRank)}</small>
        </span>
      </section>

      <section className="guide-rank-route guide-rank-route--redesign">
        {steps.map((step) => (
          <article key={step.rank} className="guide-rank-step guide-rank-step--redesign">
            <header>
              <span className="guide-rank-step__number">RB {step.rank}</span>
              <span className="guide-rank-step__title">
                <strong>{rankName(step.rank)}</strong>
                <small>→ {rankName(step.nextRank)}</small>
              </span>
              <span className="guide-rank-step__location">
                {ui.location} #{step.location}
              </span>
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

function boostLabel(boost: GuideRebirthBoostStat, ui: ReturnType<typeof guideUiText>) {
  const labels: Record<GuideRebirthBoostStat, string> = {
    POWER: ui.boostPower,
    DAMAGE: ui.boostDamage,
    LUCK: ui.boostLuck,
    MONEY: ui.boostMoney,
    ATTACK_SPEED: ui.boostAttackSpeed,
    RELIC_EXP: ui.boostRelicExp,
  }

  return labels[boost]
}
