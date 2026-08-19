import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { EventArt } from "../components/EventArt"
import { eventVisualTone } from "../eventVisualTone"
import { feed, initialMembers, initialRoles } from "../mockData"
import type { Account, AccountRole, Member, Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
  onRewardClaimed: (coin: number, prime: number) => void
  initialMemberId?: string | null
}

type DailyRewardState = {
  currentStreak: number
  claimedToday: boolean
  recoverable: boolean
  recoverableStreak: number
  restoresRemaining: number
  nextCoinMin: number
  nextCoinMax: number
  primeChance: number
}

type RewardClaimResult = {
  coin: number
  prime: number
}

type RewardParticleKind = "spark" | "orb" | "shard" | "coin"

const initialReward: DailyRewardState = {
  currentStreak: 18,
  claimedToday: false,
  recoverable: false,
  recoverableStreak: 17,
  restoresRemaining: 2,
  nextCoinMin: 320,
  nextCoinMax: 520,
  primeChance: 0.04,
}

const rewardParticles: {
  x: number
  y: number
  delay: number
  duration: number
  rotate: number
  tone: "lime" | "gold" | "plum"
  kind: RewardParticleKind
}[] = [
  { x: -122, y: -52, delay: 0.02, duration: 1.78, rotate: -95, tone: "lime", kind: "spark" },
  { x: -88, y: -82, delay: 0.08, duration: 1.96, rotate: 72, tone: "gold", kind: "coin" },
  { x: -48, y: -64, delay: 0.16, duration: 1.68, rotate: -130, tone: "plum", kind: "shard" },
  { x: -12, y: -94, delay: 0.04, duration: 2.08, rotate: 48, tone: "lime", kind: "orb" },
  { x: 34, y: -88, delay: 0.12, duration: 1.88, rotate: 110, tone: "gold", kind: "spark" },
  { x: 76, y: -68, delay: 0.2, duration: 1.72, rotate: -82, tone: "plum", kind: "shard" },
  { x: 118, y: -46, delay: 0.06, duration: 1.92, rotate: 130, tone: "lime", kind: "orb" },
  { x: -106, y: 8, delay: 0.18, duration: 1.82, rotate: -60, tone: "gold", kind: "spark" },
  { x: 98, y: 12, delay: 0.22, duration: 1.74, rotate: 84, tone: "gold", kind: "coin" },
  { x: 58, y: 28, delay: 0.28, duration: 1.62, rotate: -120, tone: "plum", kind: "spark" },
] as const

export function AccountHomePolished({
  activeAccount,
  navigate,
  back,
  onRewardClaimed,
  initialMemberId = null,
}: Props) {
  const [members, setMembers] = useState<Member[]>(initialMembers)
  const [reward, setReward] = useState<DailyRewardState>(initialReward)
  const [rewardResult, setRewardResult] = useState<RewardClaimResult | null>(null)

  const roles = initialRoles
  const selectedMember = members.find((member) => member.id === initialMemberId) ?? null
  const restoreAvailable = reward.currentStreak === 0 && reward.recoverable && reward.restoresRemaining > 0
  const claimAvailable = !reward.claimedToday && !restoreAvailable

  const drops = useMemo(
    () => feed
      .filter((event) => event.kind === "ITEM_DROP" && event.detail.toLowerCase() === activeAccount.nickname.toLowerCase())
      .sort((left, right) => {
        const timeDifference = Date.parse(right.occurredAt) - Date.parse(left.occurredAt)

        if (timeDifference !== 0) return timeDifference

        return right.id.localeCompare(left.id)
      })
      .slice(0, 3),
    [activeAccount.nickname],
  )

  const roleFor = (roleId: string | null) => roles.find((role) => role.id === roleId) ?? null

  const toggleMemberRole = (member: Member, role: AccountRole) => {
    if (member.owner || role.locked) return

    const roleIds = member.roleIds.includes(role.id)
      ? member.roleIds.filter((id) => id !== role.id)
      : [...member.roleIds, role.id]

    const highestRoleId = roles
      .filter((candidate) => roleIds.includes(candidate.id))
      .sort((left, right) => left.hierarchyRank - right.hierarchyRank)[0]?.id ?? null

    setMembers((current) => current.map((item) => item.id === member.id ? { ...item, roleIds, highestRoleId } : item))
  }

  const kickMember = (member: Member) => {
    if (member.owner) return

    setMembers((current) => current.filter((item) => item.id !== member.id))
    back()
  }

  const claimDaily = () => {
    if (!claimAvailable) return

    const coin = Math.floor(
      reward.nextCoinMin + Math.random() * (reward.nextCoinMax - reward.nextCoinMin + 1),
    )
    const prime = Math.random() < reward.primeChance ? 1 : 0

    setRewardResult({ coin, prime })
    onRewardClaimed(coin, prime)
    setReward((current) => ({
      ...current,
      currentStreak: current.currentStreak <= 0 ? 1 : current.currentStreak + 1,
      claimedToday: true,
      recoverable: false,
      recoverableStreak: 0,
    }))
  }

  const restoreDaily = () => {
    if (!restoreAvailable) return

    setReward((current) => ({
      ...current,
      currentStreak: current.recoverableStreak,
      recoverable: false,
      recoverableStreak: 0,
      restoresRemaining: current.restoresRemaining - 1,
    }))
  }

  if (initialMemberId && selectedMember) {
    return (
      <AccountMemberManagement
        member={selectedMember}
        roles={roles}
        onToggleRole={toggleMemberRole}
        onKick={kickMember}
        back={back}
      />
    )
  }

  return (
    <div className="screen account-v4-screen root-screen">
      <motion.button
        className="account-v4-profile"
        whileTap={{ scale: 0.992 }}
        onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}
      >
        <span className="account-v4-profile__head">
          <CristalixHead skinUrl={activeAccount.skinUrl} size={82} />
        </span>

        <span className="account-v4-profile__copy">
          <small>АКТИВНИЙ АККАУНТ</small>
          <strong>{activeAccount.nickname}</strong>
          <span className="account-v4-profile__pulse" aria-hidden="true">
            <i />
            <i />
            <i />
          </span>
        </span>

        <img
          className="account-v4-profile__art"
          src="/ui-illustrations/account-pass.svg"
          alt=""
          aria-hidden="true"
        />
      </motion.button>

      <section className={`account-v4-reward ${reward.claimedToday ? "is-claimed" : ""}`}>
        <motion.span
          className="account-v4-reward__flame"
          animate={{
            y: [0, -0.45, 0],
            scale: [1, 1.008, 1],
          }}
          transition={{ duration: 4.6, repeat: Infinity, ease: "easeInOut" }}
          aria-hidden="true"
        >
          <i />
          <img src="/ui-icons/flame.png" alt="" />
        </motion.span>

        <span className="account-v4-reward__copy">
          <small>ЩОДЕННА НАГОРОДА</small>
          <span className="account-v4-reward__streak">
            <strong>{reward.currentStreak || reward.recoverableStreak}</strong>
            <span>днів поспіль</span>
          </span>

          {!rewardResult ? (
            <span className="account-v4-reward__range">
              <span><CurrencyMark currency="COIN" size={14} />{reward.nextCoinMin}–{reward.nextCoinMax}</span>
              <span><CurrencyMark currency="PRIME" size={13} />{Math.round(reward.primeChance * 100)}% шанс</span>
            </span>
          ) : null}
        </span>

        {claimAvailable ? (
          <motion.button whileTap={{ scale: 0.96 }} onClick={claimDaily}>
            <Glyph name="daily" size={15} />
            Забрати
          </motion.button>
        ) : restoreAvailable ? (
          <motion.button className="is-restore" whileTap={{ scale: 0.96 }} onClick={restoreDaily}>
            <Glyph name="swap" size={14} />
            Відновити
            <small>{reward.restoresRemaining}×</small>
          </motion.button>
        ) : null}

        <AnimatePresence>
          {rewardResult ? (
            <>
              <span className="account-v5-reward-particles" aria-hidden="true">
                {rewardParticles.map((particle, index) => (
                  <motion.span
                    className={`reward-particle reward-particle--${particle.kind} is-${particle.tone}`}
                    key={`${particle.kind}-${particle.tone}-${index}`}
                    initial={{ opacity: 0, x: 0, y: 0, scale: 0.35, rotate: 0 }}
                    animate={{
                      opacity: [0, 0.96, 0.72, 0],
                      x: [0, particle.x * 0.24, particle.x * 0.68, particle.x],
                      y: [0, particle.y * 0.2, particle.y * 0.62, particle.y],
                      scale: [0.35, 1, 0.82, 0.18],
                      rotate: [0, particle.rotate * 0.28, particle.rotate * 0.7, particle.rotate],
                    }}
                    transition={{
                      duration: particle.duration,
                      delay: particle.delay,
                      ease: [0.2, 0.72, 0.22, 1],
                      times: [0, 0.2, 0.7, 1],
                    }}
                  />
                ))}
              </span>

              <motion.div
                className="account-v4-reward-result"
                initial={{ opacity: 0, scale: 0.94, y: 7 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                transition={{ duration: 0.42, ease: [0.22, 1, 0.36, 1] }}
              >
                <span>
                  <small>ОТРИМАНО</small>
                  <strong><CurrencyMark currency="COIN" size={17} />+{rewardResult.coin}</strong>
                </span>
                <span className={rewardResult.prime ? "is-prime" : "is-empty"}>
                  <small>PRIME</small>
                  <strong>
                    <CurrencyMark currency="PRIME" size={15} />
                    {rewardResult.prime ? `+${rewardResult.prime}` : "не випали"}
                  </strong>
                </span>
              </motion.div>
            </>
          ) : null}
        </AnimatePresence>
      </section>

      <section className="account-v4-feature-grid">
        <motion.button
          className="account-v4-wallet"
          whileTap={{ scale: 0.985 }}
          onClick={() => navigate({ id: "account-wallet" })}
        >
          <span className="account-v4-feature-copy">
            <small>ГАМАНЕЦЬ</small>
            <strong><CurrencyMark currency="COIN" size={19} />{activeAccount.coin.toLocaleString("uk-UA")}</strong>
            <span>COIN</span>
          </span>
          <img src="/ui-illustrations/coin-vault.png" alt="" aria-hidden="true" />
        </motion.button>

        <motion.button
          className="account-v4-personalization"
          whileTap={{ scale: 0.985 }}
          onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })}
        >
          <span className="account-v4-feature-copy">
            <small>ОБРАЗ</small>
            <strong>Персоналізація</strong>
            <span>Нік · тема · рамка · ефект</span>
          </span>
          <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
        </motion.button>
      </section>

      <nav className="account-v4-actions" aria-label="Керування аккаунтом">
        <motion.button whileTap={{ scale: 0.97 }} onClick={() => navigate({ id: "roles" })}>
          <RoleSigil color="#A985A8" size={27} compact />
          <span><strong>Ролі</strong><small>Права доступу</small></span>
        </motion.button>
        <motion.button whileTap={{ scale: 0.97 }} onClick={() => navigate({ id: "account-invites" })}>
          <span className="account-v4-actions__icon is-amber"><Glyph name="invite" size={17} /></span>
          <span><strong>Інвайти</strong><small>Запрошення</small></span>
        </motion.button>
        <motion.button whileTap={{ scale: 0.97 }} onClick={() => navigate({ id: "account-settings" })}>
          <span className="account-v4-actions__icon"><Glyph name="settings" size={17} /></span>
          <span><strong>Налаштування</strong><small>Аккаунт</small></span>
        </motion.button>
      </nav>

      <section className="account-v4-panel account-v4-members">
        <header className="account-v4-panel__head account-v4-members__head">
          <span>
            <small>КОМАНДА</small>
            <strong>Учасники</strong>
            <em>{members.length} з {activeAccount.maxMembers}</em>
          </span>
          <img src="/ui-illustrations/team-roster.svg" alt="" aria-hidden="true" />
        </header>

        <div className="account-v4-member-list">
          {members.map((member) => {
            const highestRole = roleFor(member.highestRoleId)
            const extraRoleCount = Math.max(0, member.roleIds.length - 1)

            return (
              <motion.button
                className="account-v4-member"
                key={member.id}
                whileTap={{ scale: 0.992 }}
                onClick={() => navigate({ id: "member", params: { memberId: member.id } })}
              >
                <span className="account-v4-member__avatar">{member.name.slice(0, 1).toUpperCase()}</span>

                <span className="account-v4-member__copy">
                  <span className="account-v4-member__name">
                    <strong>{member.name}</strong>
                    {member.owner ? <span className="account-v4-owner"><Glyph name="crown" size={11} /></span> : null}
                  </span>

                  {highestRole ? (
                    <span className="account-v4-member__role">
                      <RoleSigil color={highestRole.color} size={21} compact />
                      <span style={{ color: highestRole.color }}>{highestRole.name}</span>
                      {extraRoleCount > 0 ? <small>+{extraRoleCount}</small> : null}
                    </span>
                  ) : <small className="account-v4-member__no-role">Без ролі</small>}
                </span>

                <time>{member.joinedAt.replace(" 2026", "")}</time>
              </motion.button>
            )
          })}
        </div>
      </section>

      <section className="account-v4-panel account-v4-drops">
        <header className="account-v4-panel__head account-v4-drops__head">
          <span className="account-v4-drops__title-icon">
            <img src="/ui-icons/drop-stack.svg" alt="" aria-hidden="true" />
          </span>
          <span>
            <small>АКТИВНІСТЬ</small>
            <strong>Останні дропи</strong>
          </span>
        </header>

        <div className="account-v4-drop-list">
          {drops.length ? drops.map((drop) => {
            const tone = eventVisualTone(drop)

            return (
              <motion.button
                className={`account-v4-drop event-surface--${tone}`}
                key={drop.id}
                whileTap={{ scale: 0.992 }}
                onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}
              >
                <EventArt event={drop} size={42} />
                <span className="account-v4-drop__copy">
                  <strong>{drop.title}</strong>
                  <small>{localizeRarity(drop.rarity) ?? drop.meta}</small>
                </span>
                <time>{formatOccurredAt(drop.occurredAt)}</time>
              </motion.button>
            )
          }) : <p className="inline-hint inline-hint--left">Дропів ще немає</p>}
        </div>
      </section>
    </div>
  )
}

function AccountMemberManagement({
  member,
  roles,
  onToggleRole,
  onKick,
  back,
}: {
  member: Member
  roles: AccountRole[]
  onToggleRole: (member: Member, role: AccountRole) => void
  onKick: (member: Member) => void
  back: () => void
}) {
  const highestRole = roles.find((role) => role.id === member.highestRoleId) ?? null

  return (
    <div className="screen account-member-v4">
      <PageHeader title="Учасник" back={back} />

      <section className="account-member-v4__hero">
        <span className="account-member-v4__avatar">{member.name.slice(0, 1).toUpperCase()}</span>
        <span className="account-member-v4__identity">
          <small>ACCOUNT MEMBER</small>
          <strong>{member.name}</strong>
          <em>У команді з {member.joinedAt}</em>
        </span>
        {highestRole ? <RoleSigil color={highestRole.color} size={42} /> : null}
      </section>

      <section className="account-member-v4__roles">
        <header>
          <span>
            <small>ДОСТУП</small>
            <strong>Ролі учасника</strong>
          </span>
          <em>{member.roleIds.length} активні</em>
        </header>

        <div>
          {roles.map((role) => {
            const assigned = member.roleIds.includes(role.id)
            const locked = member.owner || role.locked

            return (
              <motion.button
                key={role.id}
                className={assigned ? "is-active" : ""}
                disabled={locked}
                whileTap={locked ? undefined : { scale: 0.985 }}
                onClick={() => onToggleRole(member, role)}
              >
                <RoleSigil color={role.color} size={34} compact />
                <span>
                  <strong style={{ color: assigned ? role.color : undefined }}>{role.name}</strong>
                  <small>{enabledPermissions(role)} дозволів</small>
                </span>
                <i aria-hidden="true"><b /></i>
              </motion.button>
            )
          })}
        </div>
      </section>

      {!member.owner ? (
        <section className="account-member-v4__danger">
          <span><Glyph name="close" size={18} /></span>
          <span>
            <strong>Видалити з аккаунта</strong>
            <small>Учасник втратить доступ до цього аккаунта.</small>
          </span>
          <button onClick={() => onKick(member)}>Кікнути</button>
        </section>
      ) : null}
    </div>
  )
}

function enabledPermissions(role: AccountRole) {
  return Object.values(role.permissions).filter(Boolean).length
}

function localizeRarity(value?: string) {
  if (value === "MYTHICAL") return "Міфічний"
  if (value === "SECRET") return "Секретний"
  return value
}
