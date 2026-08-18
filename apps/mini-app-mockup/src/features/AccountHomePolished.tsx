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
  const [rolesOpen, setRolesOpen] = useState(false)

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
    setRolesOpen(false)
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

  const openMemberProfile = (member: Member) => {
    const accountId = member.owner ? activeAccount.id : `profile-${member.name}`

    navigate({ id: "public-profile", params: { accountId } })
  }

  return (
    <div className="screen account-dashboard-screen root-screen">
      <section className="account-dashboard-hero">
        <div className="account-dashboard-hero__ambient" aria-hidden="true" />

        <button
          className="account-dashboard-identity"
          onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}
        >
          <span className="account-dashboard-head">
            <CristalixHead skinUrl={activeAccount.skinUrl} size={76} />
          </span>

          <span className="account-dashboard-identity__copy">
            <small>АКТИВНИЙ АККАУНТ</small>
            <span className="account-dashboard-identity__name">
              <strong>{activeAccount.nickname}</strong>
              <Glyph name="external" size={13} />
            </span>
            <span
              className="account-dashboard-role"
              style={{ "--account-role-color": activeRole.color } as CSSProperties}
            >
              <RoleSigil roleId={activeRole.id} color={activeRole.color} size={24} compact />
              <span>{activeAccount.role}</span>
            </span>
          </span>
        </button>

        <div className="account-dashboard-hero__facts">
          <span>
            <small>Учасники</small>
            <strong>{members.length}/{activeAccount.maxMembers}</strong>
          </span>
          <span>
            <small>Баланс</small>
            <strong>{activeAccount.coin.toLocaleString("uk-UA")} <em>COIN</em></strong>
          </span>
        </div>
      </section>

      <section className="account-dashboard-highlights">
        <article className={`account-dashboard-daily ${reward.claimedToday ? "is-claimed" : ""} ${restoreAvailable ? "is-restore" : ""}`}>
          <img className="account-dashboard-daily__art" src="/ui-icons/flame.png" alt="" aria-hidden="true" />

          <span className="account-dashboard-daily__copy">
            <small>ЩОДЕННА НАГОРОДА</small>
            <span className="account-dashboard-daily__streak">
              <strong>{reward.currentStreak || reward.recoverableStreak}</strong>
              <span>днів поспіль</span>
            </span>
            <span className="account-dashboard-daily__reward">
              <span><CurrencyMark currency="COIN" size={14} />{reward.nextCoinMin}–{reward.nextCoinMax}</span>
              <span><CurrencyMark currency="PRIME" size={13} />{Math.round(reward.primeChance * 100)}%</span>
            </span>
          </span>

          {claimAvailable ? (
            <motion.button whileTap={{ scale: 0.97 }} onClick={claimDaily}>
              <Glyph name="daily" size={15} />
              Забрати
            </motion.button>
          ) : restoreAvailable ? (
            <motion.button className="is-restore" whileTap={{ scale: 0.97 }} onClick={restoreDaily}>
              <Glyph name="swap" size={14} />
              Відновити
              <small>{reward.restoresRemaining}×</small>
            </motion.button>
          ) : (
            <span className="account-dashboard-daily__done"><Glyph name="check" size={13} />Отримано</span>
          )}
        </article>

        <button className="account-dashboard-wallet" onClick={() => navigate({ id: "account-wallet" })}>
          <span className="account-dashboard-wallet__copy">
            <small>ГАМАНЕЦЬ АККАУНТА</small>
            <strong>{activeAccount.coin.toLocaleString("uk-UA")}</strong>
            <span><CurrencyMark currency="COIN" size={15} />COIN <b>·</b> історія та обмін</span>
          </span>
          <img src="/ui-illustrations/coin-vault.png" alt="" aria-hidden="true" />
          <Glyph name="external" size={14} />
        </button>
      </section>

      <nav className="account-dashboard-tools" aria-label="Інструменти аккаунта">
        <button onClick={() => navigate({ id: "roles" })}>
          <span className="is-role"><RoleSigil roleId="role-moderator" color="#A985A8" size={29} compact /></span>
          <strong>Ролі</strong>
          <small>Права</small>
        </button>
        <button onClick={() => navigate({ id: "account-invites" })}>
          <span className="is-amber"><Glyph name="invite" size={18} /></span>
          <strong>Інвайти</strong>
          <small>Запрошення</small>
        </button>
        <button onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })}>
          <span className="is-lime"><Glyph name="spark" size={18} /></span>
          <strong>Вигляд</strong>
          <small>Персоналізація</small>
        </button>
        <button onClick={() => navigate({ id: "account-settings" })}>
          <span className="is-neutral"><Glyph name="settings" size={18} /></span>
          <strong>Налаштування</strong>
          <small>Аккаунт</small>
        </button>
      </nav>

      <section className="account-dashboard-panel account-dashboard-members">
        <header className="account-dashboard-panel__head">
          <span>
            <small>КОМАНДА АККАУНТА</small>
            <strong>Учасники</strong>
          </span>
          <span className="account-dashboard-panel__count">{members.length} з {activeAccount.maxMembers}</span>
        </header>

        <div className="account-dashboard-member-list">
          {members.map((member) => {
            const highestRole = roleFor(member.highestRoleId)
            const selected = member.id === selectedMemberId
            const extraRoleCount = Math.max(0, member.roleIds.length - 1)

            return (
              <div className="account-dashboard-member-wrap" key={member.id}>
                <motion.button
                  className={`account-dashboard-member ${selected ? "is-selected" : ""}`}
                  layout
                  whileTap={{ scale: 0.993 }}
                  onClick={() => {
                    setSelectedMemberId(selected ? null : member.id)
                    setRolesOpen(false)
                  }}
                >
                  <span className="account-dashboard-member__avatar">{member.name.slice(0, 1).toUpperCase()}</span>

                  <span className="account-dashboard-member__copy">
                    <span className="account-dashboard-member__name">
                      <strong>{member.name}</strong>
                      {member.owner ? <span className="account-dashboard-owner"><Glyph name="crown" size={11} /></span> : null}
                    </span>

                    {highestRole ? (
                      <span className="account-dashboard-member__role">
                        <RoleSigil roleId={highestRole.id} color={highestRole.color} size={22} compact />
                        <span style={{ color: highestRole.color }}>{highestRole.name}</span>
                        {extraRoleCount > 0 ? <small>+{extraRoleCount}</small> : null}
                      </span>
                    ) : <small className="account-dashboard-member__no-role">Без ролі</small>}
                  </span>

                  <span className="account-dashboard-member__meta">
                    <small>{member.joinedAt.replace(" 2026", "")}</small>
                    <b>›</b>
                  </span>
                </motion.button>

                <AnimatePresence initial={false}>
                  {selected && selectedMember ? (
                    <motion.div
                      className="account-dashboard-member-menu"
                      initial={{ opacity: 0, y: -6, height: 0 }}
                      animate={{ opacity: 1, y: 0, height: "auto" }}
                      exit={{ opacity: 0, y: -4, height: 0 }}
                      transition={{ duration: 0.18, ease: [0.22, 1, 0.36, 1] }}
                    >
                      <button onClick={() => openMemberProfile(selectedMember)}>
                        <Glyph name="person" size={16} />
                        <span>Профіль</span>
                        <b>›</b>
                      </button>

                      <button onClick={() => setRolesOpen((value) => !value)}>
                        <Glyph name="roles" size={16} />
                        <span>Ролі</span>
                        <b>{rolesOpen ? "⌃" : "›"}</b>
                      </button>

                      <AnimatePresence initial={false}>
                        {rolesOpen ? (
                          <motion.div
                            className="account-dashboard-member-menu__roles"
                            initial={{ opacity: 0, height: 0 }}
                            animate={{ opacity: 1, height: "auto" }}
                            exit={{ opacity: 0, height: 0 }}
                          >
                            {roles.map((role) => {
                              const assigned = selectedMember.roleIds.includes(role.id)
                              const lockedForMember = selectedMember.owner || role.locked

                              return (
                                <button
                                  key={role.id}
                                  className={assigned ? "is-active" : ""}
                                  disabled={lockedForMember}
                                  onClick={() => toggleMemberRole(selectedMember, role)}
                                >
                                  <RoleSigil roleId={role.id} color={role.color} size={25} compact />
                                  <span>
                                    <strong>{role.name}</strong>
                                    <small>{enabledPermissions(role)} дозволів</small>
                                  </span>
                                  {assigned ? <Glyph name="check" size={13} /> : null}
                                </button>
                              )
                            })}
                          </motion.div>
                        ) : null}
                      </AnimatePresence>

                      {!selectedMember.owner ? (
                        <button className="is-danger" onClick={() => kickMember(selectedMember)}>
                          <Glyph name="close" size={16} />
                          <span>Кікнути</span>
                        </button>
                      ) : null}
                    </motion.div>
                  ) : null}
                </AnimatePresence>
              </div>
            )
          })}
        </div>
      </section>

      <section className="account-dashboard-panel account-dashboard-drops">
        <header className="account-dashboard-panel__head">
          <span>
            <small>АКТИВНІСТЬ</small>
            <strong>Останні дропи</strong>
          </span>
          <span className="account-dashboard-panel__count">для {activeAccount.nickname}</span>
        </header>

        <div className="account-dashboard-drop-list">
          {drops.length ? drops.map((drop) => {
            const tone = eventVisualTone(drop)

            return (
              <motion.button
                className={`account-dashboard-drop event-surface--${tone}`}
                key={drop.id}
                whileTap={{ scale: 0.993 }}
                onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}
              >
                <EventArt event={drop} size={42} />
                <span className="account-dashboard-drop__copy">
                  <strong>{drop.title}</strong>
                  <small>{localizeRarity(drop.rarity) ?? drop.meta}</small>
                </span>
                <time>{formatOccurredAt(drop.occurredAt)}</time>
                <b>›</b>
              </motion.button>
            )
          }) : <p className="inline-hint inline-hint--left">Дропів ще немає</p>}
        </div>
      </section>
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
