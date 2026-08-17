import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { feed, initialMembers, initialRoles } from "../mockData"
import type { Account, AccountRole, Member, Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
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

export function AccountHomePolished({ activeAccount, navigate }: Props) {
  const [members, setMembers] = useState<Member[]>(initialMembers)
  const [reward, setReward] = useState<DailyRewardState>(initialReward)
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null)

  const roles = initialRoles
  const selectedMember = members.find((member) => member.id === selectedMemberId) ?? null
  const restoreAvailable = reward.currentStreak === 0 && reward.recoverable && reward.restoresRemaining > 0
  const claimAvailable = !reward.claimedToday && !restoreAvailable

  const drops = useMemo(
    () => feed
      .filter((event) => event.kind === "ITEM_DROP" && event.detail.toLowerCase() === activeAccount.nickname.toLowerCase())
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
      .sort((left, right) => right.hierarchyRank - left.hierarchyRank)[0]?.id ?? null

    setMembers((current) => current.map((item) => item.id === member.id ? { ...item, roleIds, highestRoleId } : item))
  }

  const kickMember = (member: Member) => {
    if (member.owner) return

    setMembers((current) => current.filter((item) => item.id !== member.id))
    setSelectedMemberId(null)
  }

  const claimDaily = () => {
    if (!claimAvailable) return

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

  return (
    <div className="screen account-polished-screen root-screen">
      <section className="account-polished-hero">
        <button
          className="account-polished-identity"
          onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}
        >
          <CristalixHead skinUrl={activeAccount.skinUrl} size={56} />
          <span className="account-polished-identity__copy">
            <small>Активний аккаунт</small>
            <strong>{activeAccount.nickname}</strong>
            <em>{activeAccount.role}</em>
          </span>
          <Glyph name="external" size={14} />
        </button>

        <div className={`daily-compact ${reward.claimedToday ? "is-claimed" : ""} ${restoreAvailable ? "is-restore" : ""}`}>
          <span className="daily-compact__streak">
            <i><FlameMark /></i>
            <span><strong>{reward.currentStreak || reward.recoverableStreak}</strong><small>днів</small></span>
          </span>

          <span className="daily-compact__reward">
            <span><CurrencyMark currency="COIN" size={12} />{reward.nextCoinMin}–{reward.nextCoinMax}</span>
            <span><CurrencyMark currency="PRIME" size={11} />{Math.round(reward.primeChance * 100)}%</span>
          </span>

          {claimAvailable ? (
            <motion.button whileTap={{ scale: 0.97 }} onClick={claimDaily}>
              <Glyph name="daily" size={15} />
              <span>Забрати</span>
            </motion.button>
          ) : restoreAvailable ? (
            <motion.button className="is-restore" whileTap={{ scale: 0.97 }} onClick={restoreDaily}>
              <Glyph name="swap" size={15} />
              <span>Відновити {reward.recoverableStreak}</span>
              <small>{reward.restoresRemaining}×</small>
            </motion.button>
          ) : (
            <span className="daily-compact__done"><Glyph name="check" size={14} />Отримано</span>
          )}
        </div>
      </section>

      <button className="account-polished-wallet" onClick={() => navigate({ id: "account-wallet" })}>
        <span className="account-polished-wallet__currency"><CurrencyMark currency="COIN" size={22} /></span>
        <span><small>COIN</small><strong>{activeAccount.coin.toLocaleString("uk-UA")}</strong></span>
        <span className="account-polished-wallet__action"><Glyph name="clock" size={14} />Історія<b>›</b></span>
      </button>

      <section className="account-polished-actions" aria-label="Швидкі дії">
        <Action icon="roles" label="Ролі" tone="plum" onClick={() => navigate({ id: "roles" })} />
        <Action icon="invite" label="Інвайти" tone="amber" onClick={() => navigate({ id: "account-invites" })} />
        <Action icon="spark" label="Персоналізації" tone="lime" onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })} />
        <Action icon="settings" label="Налаштування" tone="neutral" onClick={() => navigate({ id: "account-settings" })} />
      </section>

      <section className="account-polished-panel account-polished-members">
        <header>
          <span><Glyph name="members" size={18} /><strong>Учасники</strong><small>{members.length}/{activeAccount.maxMembers}</small></span>
          <button aria-label="Інвайти" onClick={() => navigate({ id: "account-invites" })}><Glyph name="invite" size={17} /></button>
        </header>

        <div className="account-polished-member-list">
          {members.map((member) => {
            const highestRole = roleFor(member.highestRoleId)
            const selected = member.id === selectedMemberId

            return (
              <div className="account-polished-member-wrap" key={member.id}>
                <motion.button
                  className={`account-polished-member ${selected ? "is-selected" : ""} ${member.owner ? "is-owner" : ""}`}
                  layout
                  whileTap={member.owner ? undefined : { scale: 0.992 }}
                  onClick={() => {
                    if (member.owner) return
                    setSelectedMemberId(selected ? null : member.id)
                  }}
                >
                  <span className="account-polished-member__avatar">{member.name.slice(0, 1).toUpperCase()}</span>
                  <span className="account-polished-member__copy">
                    <strong>{member.name}</strong>
                    {highestRole ? (
                      <span className="account-polished-role-chips">
                        <i style={{ "--member-role": highestRole.color } as React.CSSProperties}>{highestRole.name}</i>
                      </span>
                    ) : <small className="account-polished-member__no-role">Без ролі</small>}
                  </span>
                  {!member.owner ? <span className="account-polished-member__chevron">›</span> : null}
                </motion.button>

                <AnimatePresence initial={false}>
                  {selected && selectedMember ? (
                    <motion.div
                      className="account-member-popover"
                      initial={{ opacity: 0, y: -5, scale: 0.985 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, y: -4, scale: 0.99 }}
                      transition={{ duration: 0.16, ease: [0.22, 1, 0.36, 1] }}
                    >
                      <div className="account-member-popover__header">
                        <span>{selectedMember.name}</span>
                        <small>Ролі</small>
                      </div>

                      <div className="account-member-popover__roles">
                        {roles.filter((role) => !role.locked).map((role) => {
                          const active = selectedMember.roleIds.includes(role.id)

                          return (
                            <button
                              key={role.id}
                              className={active ? "is-active" : ""}
                              onClick={() => toggleMemberRole(selectedMember, role)}
                            >
                              <i style={{ background: role.color }} />
                              <span>{role.name}</span>
                              {active ? <Glyph name="check" size={13} /> : <span className="role-empty" />}
                            </button>
                          )
                        })}
                      </div>

                      <button className="account-member-popover__danger" onClick={() => kickMember(selectedMember)}>
                        <Glyph name="close" size={16} />
                        <span>Кікнути</span>
                      </button>
                    </motion.div>
                  ) : null}
                </AnimatePresence>
              </div>
            )
          })}
        </div>
      </section>

      <section className="account-polished-panel account-polished-drops">
        <header>
          <span><Glyph name="item-drop" size={18} /><strong>Останні дропи</strong></span>
          <button onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}>Усі <b>›</b></button>
        </header>

        <div className="account-polished-drop-list">
          {drops.length ? drops.map((drop) => (
            <button key={drop.id} onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}>
              <span className={`drop-mark drop-mark--${(drop.rarity ?? "default").toLowerCase()}`}><Glyph name="item-drop" size={16} /></span>
              <span><strong>{drop.title}</strong><small>{drop.rarity ?? drop.meta}</small></span>
              <time>{drop.relative}</time>
              <b>›</b>
            </button>
          )) : <p className="inline-hint inline-hint--left">Дропів ще немає</p>}
        </div>
      </section>
    </div>
  )
}

function Action({ icon, label, tone, onClick }: {
  icon: "roles" | "invite" | "spark" | "settings"
  label: string
  tone: "lime" | "amber" | "plum" | "neutral"
  onClick: () => void
}) {
  return (
    <motion.button className={`account-polished-action is-${tone}`} whileTap={{ scale: 0.975 }} onClick={onClick}>
      <span><Glyph name={icon} size={19} /></span>
      <strong>{label}</strong>
      <b>›</b>
    </motion.button>
  )
}

function FlameMark() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M13.9 2.8c.5 3-1.1 4.3-2.5 5.7-1.2 1.2-2.4 2.4-2.4 4.3 0 1.5 1 2.6 2.4 2.6 1.8 0 2.8-1.5 2.5-3.4 2.2 1.2 3.6 3.2 3.6 5.4 0 3-2.4 5-5.6 5-3.8 0-6.4-2.7-6.4-6.3 0-4.6 3.2-7.1 5.5-9.3 1.3-1.2 2.2-2.3 2.9-4Z" />
    </svg>
  )
}
