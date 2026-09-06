import { useMemo, useState } from "react"
import { GUIDE_REBIRTH_STEPS, type GuideRebirthBoostStat } from "./guideRebirthData"
import { GUIDE_RANK_NAMES_RU, guideRankName } from "./guideRanks"
import { guideUiText } from "./guideUiText"

type Props = {
  ui: ReturnType<typeof guideUiText>
  back: () => void
}

export function GuideRebirthPage({ ui, back }: Props) {
  const [currentRank, setCurrentRank] = useState(1)
  const [targetRank, setTargetRank] = useState(10)
  const steps = useMemo(
    () =>
      GUIDE_REBIRTH_STEPS.filter(
        (step) => step.rank >= currentRank && step.nextRank <= targetRank,
      ),
    [currentRank, targetRank],
  )
  const validRange = targetRank > currentRank
  const locationCount = new Set(steps.map((step) => step.location)).size

  return (
    <div className="screen guides-screen guide-rebirth-screen">
      <GuideSubpageHeader title={ui.rebirth} back={back} backLabel={ui.back} />

      <section className="guide-context-hero guide-context-hero--trainer">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          <i>RB</i>
        </div>
        <span>
          <small>{ui.calculator}</small>
          <strong>{ui.rebirth}</strong>
          <p>{ui.calculatorCopy}</p>
        </span>
      </section>

      <section className="guide-rank-calculator">
        <label>
          <small>{ui.currentRank}</small>
          <select value={currentRank} onChange={(event) => setCurrentRank(Number(event.target.value))}>
            {rankOptions(1, 67).map((rank) => (
              <option key={rank} value={rank}>
                #{rank} · {guideRankName(rank)}
              </option>
            ))}
          </select>
        </label>

        <span className="guide-rank-calculator__arrow" aria-hidden="true">
          →
        </span>

        <label>
          <small>{ui.targetRank}</small>
          <select value={targetRank} onChange={(event) => setTargetRank(Number(event.target.value))}>
            {rankOptions(2, 68).map((rank) => (
              <option key={rank} value={rank}>
                #{rank} · {guideRankName(rank)}
              </option>
            ))}
          </select>
        </label>
      </section>

      {validRange ? (
        <>
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
              <small>{guideRankName(targetRank)}</small>
            </span>
          </section>

          <section className="guide-rank-route">
            {steps.map((step) => (
              <article key={step.rank} className="guide-rank-step">
                <header>
                  <span className="guide-rank-step__number">#{step.rank}</span>
                  <span className="guide-rank-step__title">
                    <strong>{guideRankName(step.rank)}</strong>
                    <small>→ {guideRankName(step.nextRank)}</small>
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
        </>
      ) : (
        <section className="guides-empty guide-rank-empty">
          <img src="/ui-illustrations/account-guides.svg" alt="" aria-hidden="true" />
          <strong>{ui.chooseForwardRank}</strong>
        </section>
      )}
    </div>
  )
}

function rankOptions(from: number, to: number) {
  return Object.keys(GUIDE_RANK_NAMES_RU)
    .map(Number)
    .filter((rank) => rank >= from && rank <= to)
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
