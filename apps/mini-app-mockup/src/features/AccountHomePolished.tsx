import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import type { CSSProperties } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { EventArt } from "../components/EventArt"
import { eventVisualTone } from "../eventVisualTone"
import { feed, initialMembers, initialRoles } from "../mockData"
import type { Account, AccountRole, Member, Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { RoleSigil } from "../shared/RoleSigil"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
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

export function AccountHomePolished({ activeAccount, navigate, initialMemberId = null }: Props) {
  const [members, setMembers] = useState<Member[]>(initialMembers)
  const [reward, setReward] = useState<DailyRewardState>(initialReward)
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(initialMemberId)

  const roles = initialRoles
  const selectedMember = members.find((member) => member.id === selectedMemberId) ?? null
  const restoreAvailable = reward.currentStreak === 0 && reward.recoverable && reward.restoresRemaining > 0
  const claimAvailable = !reward.claimedToday && !restoreAvailable
  const activeRole = roles.find((role) => role.name === activeAccount.role) ?? roles[0]

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
    <div className="screen account-next-screen root-screen">
      <section className="account-next-hero">
        <div className="account-next-hero__glow" aria-hidden="true" />

        <button
          className="account-next-identity"
          onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}
        >
          <span className="account-next-head-stage">
            <CristalixHead skinUrl={activeAccount.skinUrl} size={88} />
          </span>

          <span className="account-next-identity__copy">
            <small>Активний аккаунт</small>
            <span className="account-next-identity__name">
              <strong>{activeAccount.nickname}</strong>
              <Glyph name="external" size={13} />
            </span>
            <span
              className="account-next-role-pill"
              style={{ "--account-role-color": activeRole.color } as CSSProperties}
            >
              <RoleSigil color={activeRole.color} size={18} compact />
              {activeAccount.role}
            </span>
          </span>
        </button>

        <button
          className="account-next-settings-link"
          onClick={() => navigate({ id: "account-settings" })}
          aria-label="Налаштування аккаунта"
        >
          <Glyph name="settings" size={18} />
        </button>

        <div className={`account-next-daily ${reward.claimedToday ? "is-claimed" : ""} ${restoreAvailable ? "is-restore" : ""}`}>
          <span className="account-next-daily__flame"><FlameMark /></span>

          <span className="account-next-daily__streak">
            <strong>{reward.currentStreak || reward.recoverableStreak}</strong>
            <small>днів</small>
          </span>

          <span className="account-next-daily__reward">
            <span><CurrencyMark currency="COIN" size={14} />{reward.nextCoinMin}–{reward.nextCoinMax}</span>
            <span><CurrencyMark currency="PRIME" size={13} />{Math.round(reward.primeChance * 100)}%</span>
          </span>

          {claimAvailable ? (
            <motion.button whileTap={{ scale: 0.97 }} onClick={claimDaily}>
              <Glyph name="daily" size={16} />
              <span>Забрати</span>
            </motion.button>
          ) : restoreAvailable ? (
            <motion.button className="is-restore" whileTap={{ scale: 0.97 }} onClick={restoreDaily}>
              <Glyph name="swap" size={15} />
              <span>Відновити</span>
              <small>{reward.restoresRemaining}×</small>
            </motion.button>
          ) : (
            <span className="account-next-daily__done"><Glyph name="check" size={14} />Отримано</span>
          )}
        </div>
      </section>

      <button className="account-next-wallet" onClick={() => navigate({ id: "account-wallet" })}>
        <span className="account-next-wallet__mark"><CurrencyMark currency="COIN" size={24} /></span>
        <span className="account-next-wallet__copy">
          <small>Баланс аккаунта</small>
          <strong>{activeAccount.coin.toLocaleString("uk-UA")} <em>COIN</em></strong>
        </span>
        <span className="account-next-wallet__action">
          <Glyph name="clock" size={15} />
          <span>Історія</span>
          <b>›</b>
        </span>
      </button>

      <section className="account-next-panel account-next-members">
        <header className="account-next-panel__head">
          <span className="account-next-panel__title">
            <span className="account-next-panel__icon"><Glyph name="members" size={18} /></span>
            <span>
              <strong>Учасники</strong>
              <small>{members.length} з {activeAccount.maxMembers}</small>
            </span>
          </span>

          <div className="account-next-panel__actions">
            <button aria-label="Ролі" onClick={() => navigate({ id: "roles" })}><Glyph name="roles" size={17} /></button>
            <button aria-label="Інвайти" onClick={() => navigate({ id: "account-invites" })}><Glyph name="invite" size={17} /></button>
          </div>
        </header>

        <div className="account-next-member-list">
          {members.map((member) => {
            const highestRole = roleFor(member.highestRoleId)
            const selected = member.id === selectedMemberId
            const extraRoleCount = Math.max(0, member.roleIds.length - 1)

            return (
              <div className="account-next-member-wrap" key={member.id}>
                <motion.button
                  className={`account-next-member ${selected ? "is-selected" : ""} ${member.owner ? "is-owner" : ""}`}
                  layout
                  whileTap={member.owner ? undefined : { scale: 0.992 }}
                  onClick={() => {
                    if (member.owner) {
                      navigate({ id: "public-profile", params: { accountId: activeAccount.id } })
                      return
                    }

                    setSelectedMemberId(selected ? null : member.id)
                  }}
                >
                  <span className="account-next-member__avatar">{member.name.slice(0, 1).toUpperCase()}</span>

                  <span className="account-next-member__copy">
                    <span className="account-next-member__name">
                      <strong>{member.name}</strong>
                      {member.owner ? <span className="account-next-owner-mark"><Glyph name="crown" size={12} /></span> : null}
                    </span>

                    {highestRole ? (
                      <span
                        className="account-next-member__role"
                        style={{ "--member-role": highestRole.color } as CSSProperties}
                      >
                        <RoleSigil color={highestRole.color} size={19} compact />
                        <span>{highestRole.name}</span>
                        {extraRoleCount > 0 ? <small>+{extraRoleCount}</small> : null}
                      </span>
                    ) : <small className="account-next-member__no-role">Без ролі</small>}
                  </span>

                  <span className="account-next-member__meta">
                    <small>{member.joinedAt.replace(" 2026", "")}</small>
                    <b>›</b>
                  </span>
                </motion.button>

                <AnimatePresence initial={false}>
                  {selected && selectedMember ? (
                    <motion.div
                      className="account-next-member-editor"
                      initial={{ opacity: 0, y: -7, height: 0 }}
                      animate={{ opacity: 1, y: 0, height: "auto" }}
                      exit={{ opacity: 0, y: -5, height: 0 }}
                      transition={{ duration: 0.18, ease: [0.22, 1, 0.36, 1] }}
                    >
                      <header>
                        <span>
                          <small>КЕРУВАННЯ УЧАСНИКОМ</small>
                          <strong>{selectedMember.name}</strong>
                        </span>
                        <em>з {selectedMember.joinedAt}</em>
                      </header>

                      <div className="account-next-role-grid">
                        {roles.filter((role) => !role.locked).map((role) => {
                          const active = selectedMember.roleIds.includes(role.id)

                          return (
                            <button
                              key={role.id}
                              className={active ? "is-active" : ""}
                              style={{ "--member-role": role.color } as CSSProperties}
                              onClick={() => toggleMemberRole(selectedMember, role)}
                            >
                              <RoleSigil color={role.color} size={30} compact />
                              <strong>{role.name}</strong>
                              <small>{active ? "Призначена" : `${enabledPermissions(role)} дозволів`}</small>
                            </button>
                          )
                        })}
                      </div>

                      <footer>
                        <button
                          className="account-next-member-editor__profile"
                          onClick={() => navigate({ id: "public-profile", params: { accountId: `profile-${selectedMember.name}` } })}
                        >
                          <Glyph name="person" size={15} />
                          Профіль
                        </button>
                        <button className="account-next-member-editor__danger" onClick={() => kickMember(selectedMember)}>
                          <Glyph name="close" size={15} />
                          Кікнути
                        </button>
                      </footer>
                    </motion.div>
                  ) : null}
                </AnimatePresence>
              </div>
            )
          })}
        </div>
      </section>

      <section className="account-next-panel account-next-drops">
        <header className="account-next-panel__head">
          <span className="account-next-panel__title">
            <span className="account-next-panel__icon is-drop"><Glyph name="item-drop" size={18} /></span>
            <span>
              <strong>Останні дропи</strong>
              <small>Для {activeAccount.nickname}</small>
            </span>
          </span>
        </header>

        <div className="account-next-drop-list">
          {drops.length ? drops.map((drop) => {
            const tone = eventVisualTone(drop)

            return (
              <button
                className={`event-row-surface event-surface--${tone}`}
                key={drop.id}
                onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}
              >
                <EventArt event={drop} size={38} />
                <span><strong>{drop.title}</strong><small>{localizeRarity(drop.rarity) ?? drop.meta}</small></span>
                <time>{formatOccurredAt(drop.occurredAt)}</time>
                <b>›</b>
              </button>
            )
          }) : <p className="inline-hint inline-hint--left">Дропів ще немає</p>}
        </div>
      </section>
    </div>
  )
}

function FlameMark() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M13.9 2.8c.5 3-1.1 4.3-2.5 5.7-1.2 1.2-2.4 2.4-2.4 4.3 0 1.5 1 2.6 2.4 2.6 1.8 0 2.8-1.5 2.5-3.4 2.2 1.2 3.6 3.2 3.6 5.4 0 3-2.4 5-5.6 5-3.8 0-6.4-2.7-6.4-6.3 0-4.6 3.2-7.1 5.5-9.3 1.3-1.2 2.2-2.3 2.9-4Z" />
    </svg>
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
