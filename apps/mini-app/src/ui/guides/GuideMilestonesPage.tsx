import { useState } from "react"
import { PageHeader } from "../shared/Primitives"
import {
  GUIDE_CASE_MILESTONES,
  GUIDE_DUNGEON_MILESTONES,
  type GuideCaseRewardType,
  type GuideDungeonRewardType,
  type GuideDungeonType,
} from "./guideMilestones"
import { guideUiText } from "./guideUiText"

type GuideUi = ReturnType<typeof guideUiText>

type Props = {
  kind: "dungeons" | "cases"
  ui: GuideUi
  text: (key: string) => string
  back: () => void
}

type DungeonPresentation = {
  type: GuideDungeonType
  tone: string
}

const DUNGEONS: DungeonPresentation[] = [
  { type: "EASY", tone: "easy" },
  { type: "NORMAL", tone: "normal" },
  { type: "HARD", tone: "hard" },
  { type: "INSANE", tone: "insane" },
]

export function GuideMilestonesPage({ kind, ui, text, back }: Props) {
  if (kind === "cases") {
    return <GuideCasesPage ui={ui} back={back} />
  }

  return <GuideDungeonsPage ui={ui} text={text} back={back} />
}

function GuideDungeonsPage({
  ui,
  text,
  back,
}: {
  ui: GuideUi
  text: (key: string) => string
  back: () => void
}) {
  const [activeType, setActiveType] = useState<GuideDungeonType>("EASY")
  const activeDungeon = DUNGEONS.find((dungeon) => dungeon.type === activeType) ?? DUNGEONS[0]
  const milestones = GUIDE_DUNGEON_MILESTONES[activeDungeon.type]
  const dungeonName = text(`sao.dungeon.${activeDungeon.type}.name`)
  const dungeonDescription = text(`sao.dungeon.${activeDungeon.type}.description`)

  return (
    <div className="screen guides-screen guide-milestones-screen guide-dungeons-screen guide-dungeons-screen--redesign">
      <PageHeader title={ui.dungeons} eyebrow={ui.dungeonMilestones} back={back} />

      <section className="guide-section-intro guide-section-intro--dungeons">
        <span>
          <small>{ui.dungeonDifficultyRange}</small>
          <strong>{ui.dungeons}</strong>
          <p>{ui.dungeonsCopy}</p>
        </span>
        <img
          className="guide-section-intro__art"
          src="/ui-illustrations/guides-dungeons.svg"
          alt=""
          aria-hidden="true"
        />
      </section>

      <section
        className="guide-dungeon-selector guide-dungeon-selector--redesign"
        aria-label={ui.dungeons}
      >
        {DUNGEONS.map((dungeon) => {
          const selected = dungeon.type === activeType

          return (
            <button
              key={dungeon.type}
              type="button"
              className={`guide-dungeon-selector__item guide-dungeon-selector__item--${dungeon.tone} ${selected ? "is-selected" : ""}`}
              aria-pressed={selected}
              onClick={() => setActiveType(dungeon.type)}
            >
              <span className="guide-dungeon-selector__copy">
                <strong>{text(`sao.dungeon.${dungeon.type}.name`)}</strong>
              </span>
            </button>
          )
        })}
      </section>

      <section
        className={`guide-dungeon-detail guide-dungeon-detail--${activeDungeon.tone} guide-dungeon-detail--redesign`}
      >
        <header className="guide-dungeon-detail__header">
          <span>
            <strong>{dungeonName}</strong>
            <small>{dungeonDescription}</small>
          </span>
        </header>

        <div className="guide-milestone-list guide-milestone-list--redesign">
          {milestones.map((milestone) => (
            <article
              key={milestone.stage}
              className="guide-milestone-row guide-milestone-row--redesign"
            >
              <span className="guide-milestone-row__stage">
                <small>{ui.stage}</small>
                <b>{milestone.stage}</b>
              </span>
              <span className="guide-milestone-row__reward">
                <small>{ui.reward}</small>
                <strong>{rewardLabel(milestone.reward, ui)}</strong>
              </span>
              <span className="guide-milestone-row__value">{milestone.value}</span>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}

function GuideCasesPage({ ui, back }: { ui: GuideUi; back: () => void }) {
  return (
    <div className="screen guides-screen guide-milestones-screen guide-cases-screen guide-cases-screen--redesign">
      <PageHeader title={ui.cases} eyebrow={ui.caseOpened} back={back} />

      <section className="guide-section-intro guide-section-intro--cases">
        <span>
          <small>{ui.caseOpened}</small>
          <strong>{ui.cases}</strong>
          <p>{ui.casesCopy}</p>
        </span>
        <b>{GUIDE_CASE_MILESTONES.length}</b>
      </section>

      <section className="guide-case-timeline guide-case-timeline--redesign">
        {GUIDE_CASE_MILESTONES.map((milestone) => (
          <article
            key={milestone.opened}
            className="guide-case-milestone guide-case-milestone--redesign"
          >
            <span className="guide-case-milestone__count">
              <b>{compactNumber(milestone.opened)}</b>
              <small>{ui.caseOpened}</small>
            </span>
            <span className="guide-case-milestone__reward">
              <small>{ui.caseReward}</small>
              <strong>{caseRewardLabel(milestone.reward, ui)}</strong>
            </span>
            <span className="guide-case-milestone__value">{milestone.value}</span>
          </article>
        ))}
      </section>
    </div>
  )
}

function rewardLabel(reward: GuideDungeonRewardType, ui: GuideUi) {
  const labels: Record<GuideDungeonRewardType, string> = {
    AURA_KEY: ui.rewardAuraKey,
    SPECIAL_SHARD: ui.rewardSpecialShard,
    ATTACK_SPEED: ui.rewardAttackSpeed,
    POWER: ui.rewardPower,
    DOUBLE_CASE: ui.rewardDoubleCase,
    DAMAGE: ui.rewardDamage,
    PET_SLOT: ui.rewardPetSlot,
    LUCK: ui.rewardLuck,
    JEWELRY_SLOT: ui.rewardJewelrySlot,
  }

  return labels[reward]
}

function caseRewardLabel(reward: GuideCaseRewardType, ui: GuideUi) {
  const labels: Record<GuideCaseRewardType, string> = {
    LUCK: ui.rewardLuck,
    PET_SLOT: ui.rewardPetSlot,
    AUTO_PET_CASE: ui.rewardAutoPetCase,
  }

  return labels[reward]
}

function compactNumber(value: number) {
  if (value >= 1_000_000) {
    return `${value / 1_000_000}M`
  }

  if (value >= 1_000) {
    const thousands = value / 1_000
    return `${Number.isInteger(thousands) ? thousands : thousands.toFixed(2).replace(/0+$/, "")}K`
  }

  return String(value)
}
