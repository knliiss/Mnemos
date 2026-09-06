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
  back: () => void
}

export function GuideMilestonesPage({ kind, ui, back }: Props) {
  if (kind === "cases") {
    return <GuideCasesPage ui={ui} back={back} />
  }

  return <GuideDungeonsPage ui={ui} back={back} />
}

function GuideDungeonsPage({ ui, back }: { ui: GuideUi; back: () => void }) {
  const dungeons: Array<{
    type: GuideDungeonType
    label: string
    copy: string
    tone: string
  }> = [
    { type: "EASY", label: ui.dungeonEasy, copy: ui.dungeonEasyCopy, tone: "lp" },
    { type: "NORMAL", label: ui.dungeonNormal, copy: ui.dungeonNormalCopy, tone: "sp" },
    { type: "HARD", label: ui.dungeonHard, copy: ui.dungeonHardCopy, tone: "slp" },
    { type: "INSANE", label: ui.dungeonInsane, copy: ui.dungeonInsaneCopy, tone: "ep" },
  ]

  return (
    <div className="screen guides-screen guide-milestones-screen">
      <GuideSubpageHeader title={ui.dungeons} back={back} backLabel={ui.back} sigil="DG" />

      <section className="guide-context-hero guide-context-hero--explorer">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          <i>DG</i>
        </div>
        <span>
          <small>LP · SP · SLP · EP</small>
          <strong>{ui.dungeons}</strong>
          <p>{ui.dungeonsCopy}</p>
        </span>
      </section>

      <section className="guide-dungeon-grid">
        {dungeons.map((dungeon) => (
          <article key={dungeon.type} className={`guide-dungeon-card guide-dungeon-card--${dungeon.tone}`}>
            <header>
              <span>
                <b>{dungeon.label}</b>
                <small>{dungeon.copy}</small>
              </span>
              <i>{GUIDE_DUNGEON_MILESTONES[dungeon.type].length}</i>
            </header>

            <div className="guide-milestone-list">
              {GUIDE_DUNGEON_MILESTONES[dungeon.type].map((milestone) => (
                <span key={milestone.stage} className="guide-milestone-row">
                  <i>{milestone.stage}</i>
                  <span>
                    <small>{ui.stage}</small>
                    <strong>{rewardLabel(milestone.reward, ui)}</strong>
                  </span>
                  <b>{milestone.value}</b>
                </span>
              ))}
            </div>
          </article>
        ))}
      </section>
    </div>
  )
}

function GuideCasesPage({ ui, back }: { ui: GuideUi; back: () => void }) {
  return (
    <div className="screen guides-screen guide-milestones-screen">
      <GuideSubpageHeader title={ui.cases} back={back} backLabel={ui.back} sigil="CS" />

      <section className="guide-context-hero guide-context-hero--cases">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          <i>CS</i>
        </div>
        <span>
          <small>{ui.caseOpened}</small>
          <strong>{ui.cases}</strong>
          <p>{ui.casesCopy}</p>
        </span>
      </section>

      <section className="guide-case-timeline">
        {GUIDE_CASE_MILESTONES.map((milestone) => (
          <article key={milestone.opened} className="guide-case-milestone">
            <span className="guide-case-milestone__count">
              <b>{compactNumber(milestone.opened)}</b>
              <small>{ui.caseOpened}</small>
            </span>
            <span className="guide-case-milestone__reward">
              <small>{ui.caseReward}</small>
              <strong>{caseRewardLabel(milestone.reward, ui)}</strong>
            </span>
            <i>{milestone.value}</i>
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

function GuideSubpageHeader({
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
