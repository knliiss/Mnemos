import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  assignMemberRole,
  getMemberRoles,
  kickAccountMember,
  revokeMemberRole,
} from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { CristalixHead } from "../components/CristalixHead"
import { EventArt } from "../components/EventArt"
import { useProductData } from "../data/ProductDataProvider"
import { eventVisualTone } from "../eventVisualTone"
import type { AccountRole, Member, Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  initialMemberId?: string | null
}

type RewardResult = {
  coin: number
  prime: number
}

type RewardParticleKind = "spark" | "orb" | "shard" | "coin"

type RewardParticle = {
  kind: RewardParticleKind
  tone: "coin" | "prime" | "lime"
  x: number
  y: number
  rotate: number
  delay: number
  duration: number
}

const rewardParticles: RewardParticle[] = [
  { kind: "coin", tone: "coin", x: -78, y: -44, rotate: -42, delay: 0.02, duration: 1.68 },
  { kind: "spark", tone: "lime", x: -48, y: -72, rotate: 38, delay: 0.08, duration: 1.55 },
  { kind: "shard", tone: "prime", x: -14, y: -82, rotate: -62, delay: 0.04, duration: 1.85 },
  { kind: "orb", tone: "coin", x: 34, y: -76, rotate: 24, delay: 0.11, duration: 1.72 },
  { kind: "spark", tone: "prime", x: 70, y: -48, rotate: 68, delay: 0.06, duration: 1.64 },
  { kind: "coin", tone: "coin", x: 82, y: -12, rotate: 96, delay: 0.14, duration: 1.9 },
  { kind: "shard", tone: "lime", x: 52, y: 18, rotate: 116, delay: 0.1, duration: 1.62 },
  { kind: "orb", tone: "prime", x: -54, y: 16, rotate: -108, delay: 0.13, duration: 1.78 },
]

export function AccountHome({ navigate, back, initialMemberId }: Props) {
  const { t, i18n } = useTranslation()
  const product = useProductData()
  const activeAccount = product.activeAccount
  const [rewardResult, setRewardResult] = useState<RewardResult | null>(null)
  const [rewardBusy, setRewardBusy] = useState(false)
  const selectedMember = product.members.find((member) => member.id === initialMemberId) ?? null
  const locale = localeForLanguage(i18n.resolvedLanguage)

  const drops = useMemo(() => {
    if (!activeAccount) {
      return []
    }

    return product.feed
      .filter(
        (event) =>
          event.kind === "ITEM_DROP" &&
          event.detail.toLowerCase() === activeAccount.nickname.toLowerCase(),
      )
      .slice(0, 3)
  }, [activeAccount, product.feed])

  if (!activeAccount) {
    return null
  }

  if (initialMemberId && selectedMember) {
    return (
      <AccountMemberManagementProduction
        member={selectedMember}
        roles={product.roles}
        accountId={activeAccount.id}
        refresh={product.refreshAccount}
        back={back}
      />
    )
  }

  const reward = product.dailyReward
  const claimAvailable = Boolean(reward && !reward.claimedToday && !reward.recoverable)
  const restoreAvailable = Boolean(
    reward && !reward.claimedToday && reward.recoverable && reward.restoresRemaining > 0,
  )

  const claimDaily = async () => {
    if (!claimAvailable || rewardBusy) {
      return
    }

    setRewardBusy(true)

    try {
      const result = await product.claimReward()
      setRewardResult(result)
    } catch {
      // Product API already routes the backend error through the global handler.
    } finally {
      setRewardBusy(false)
    }
  }

  const restoreDaily = async () => {
    if (!restoreAvailable || rewardBusy) {
      return
    }

    setRewardBusy(true)

    try {
      await product.restoreReward()
    } catch {
      // Product API already routes the backend error through the global handler.
    } finally {
      setRewardBusy(false)
    }
  }

  const roleFor = (roleId: string | null) =>
    product.roles.find((role) => role.id === roleId) ?? null

  return (
    <div className="screen account-home-screen root-screen">
      <motion.button
        className="account-home-profile"
        whileTap={{ scale: 0.992 }}
        onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}
      >
        <span className="account-home-profile__head">
          <CristalixHead skinUrl={activeAccount.skinUrl} size={82} />
        </span>

        <span className="account-home-profile__copy">
          <small>{t("accountHome.activeAccount")}</small>
          <strong>{activeAccount.nickname}</strong>
          <span className="account-home-profile__pulse" aria-hidden="true">
            <i />
            <i />
            <i />
          </span>
        </span>

        <img
          className="account-home-profile__art"
          src="/ui-illustrations/mnemos-mascot-cat.svg"
          alt=""
          aria-hidden="true"
        />
      </motion.button>

      <section className={`account-home-reward ${reward?.claimedToday ? "is-claimed" : ""}`}>
        <span className="account-home-reward__flame" aria-hidden="true">
          <i />
          <img src="/ui-icons/flame.png" alt="" />
        </span>

        <span className="account-home-reward__copy">
          <small>{t("accountHome.dailyReward")}</small>
          <span className="account-home-reward__streak">
            <strong>{reward?.currentStreak || reward?.recoverableStreak || 0}</strong>
            <span>{t("accountHome.streakDays")}</span>
          </span>

          {reward && !rewardResult ? (
            <span className="account-home-reward__range">
              <span>
                <CurrencyMark currency="COIN" size={14} />
                {reward.nextCoinMin}–{reward.nextCoinMax}
              </span>
              <span>
                <CurrencyMark currency="PRIME" size={13} />
                {t("accountHome.primeChance", { chance: Math.round(reward.primeChance * 100) })}
              </span>
            </span>
          ) : null}
        </span>

        {claimAvailable ? (
          <motion.button
            whileTap={{ scale: 0.96 }}
            disabled={rewardBusy}
            onClick={() => void claimDaily()}
            aria-label={t("accountHome.claimAria")}
          >
            <Glyph name="daily" size={15} />
            {t("accountHome.claim")}
          </motion.button>
        ) : restoreAvailable ? (
          <motion.button
            className="is-restore"
            whileTap={{ scale: 0.96 }}
            disabled={rewardBusy}
            onClick={() => void restoreDaily()}
          >
            <Glyph name="swap" size={14} />
            {t("accountHome.restore")}
            <small>{reward?.restoresRemaining}×</small>
          </motion.button>
        ) : null}

        <AnimatePresence>
          {rewardResult ? (
            <>
              <span className="account-home-reward-particles" aria-hidden="true">
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
                className="account-home-reward-result"
                initial={{ opacity: 0, scale: 0.94, y: 7 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                transition={{ duration: 0.42, ease: [0.22, 1, 0.36, 1] }}
              >
                <span>
                  <small>{t("accountHome.received")}</small>
                  <strong>
                    <CurrencyMark currency="COIN" size={17} />+{rewardResult.coin}
                  </strong>
                </span>
                <span className={rewardResult.prime ? "is-prime" : "is-empty"}>
                  <small>PRIME</small>
                  <strong>
                    <CurrencyMark currency="PRIME" size={15} />
                    {rewardResult.prime
                      ? `+${rewardResult.prime}`
                      : t("accountHome.primeNotDropped")}
                  </strong>
                </span>
              </motion.div>
            </>
          ) : null}
        </AnimatePresence>
      </section>

      <section className="account-home-feature-grid">
        <motion.button
          className="account-home-wallet"
          whileTap={{ scale: 0.985 }}
          onClick={() => navigate({ id: "account-wallet" })}
        >
          <span className="account-home-feature-copy">
            <small>{t("accountHome.wallet")}</small>
            <strong>
              <CurrencyMark currency="COIN" size={19} />
              {activeAccount.coin.toLocaleString(locale)}
            </strong>
            <span>COIN</span>
          </span>
          <img src="/ui-illustrations/coin-vault.png" alt="" aria-hidden="true" />
        </motion.button>

        <motion.button
          className="account-home-personalization"
          whileTap={{ scale: 0.985 }}
          onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })}
        >
          <span className="account-home-feature-copy">
            <small>{t("accountHome.appearance")}</small>
            <strong>{t("accountHome.personalization")}</strong>
            <span>{t("accountHome.personalizationSummary")}</span>
          </span>
          <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
        </motion.button>
      </section>

      <nav className="account-home-actions" aria-label={t("accountHome.managementAria")}>
        <motion.button whileTap={{ scale: 0.97 }} onClick={() => navigate({ id: "roles" })}>
          <RoleSigil color="#A985A8" size={27} compact />
          <span>
            <strong>{t("accountHome.roles")}</strong>
            <small>{t("accountHome.accessRights")}</small>
          </span>
        </motion.button>
        <motion.button
          whileTap={{ scale: 0.97 }}
          onClick={() => navigate({ id: "account-invites" })}
        >
          <span className="account-home-actions__icon is-amber">
            <Glyph name="invite" size={17} />
          </span>
          <span>
            <strong>{t("accountHome.invites")}</strong>
            <small>{t("accountHome.invitations")}</small>
          </span>
        </motion.button>
        <motion.button
          whileTap={{ scale: 0.97 }}
          onClick={() => navigate({ id: "account-settings" })}
        >
          <span className="account-home-actions__icon">
            <Glyph name="settings" size={17} />
          </span>
          <span>
            <strong>{t("accountHome.settings")}</strong>
            <small>{t("accountHome.account")}</small>
          </span>
        </motion.button>
      </nav>

      <section className="account-home-panel account-home-members">
        <header className="account-home-panel__head account-home-members__head">
          <span>
            <small>{t("accountHome.team")}</small>
            <strong>{t("accountHome.members")}</strong>
            <em>
              {t("accountHome.membersCount", {
                count: product.members.length,
                max: activeAccount.maxMembers,
              })}
            </em>
          </span>
          <img src="/ui-illustrations/team-roster.svg" alt="" aria-hidden="true" />
        </header>

        <div className="account-home-member-list">
          {product.members.map((member) => {
            const highestRole = roleFor(member.highestRoleId)
            const extraRoleCount = Math.max(0, member.roleIds.length - 1)

            return (
              <motion.button
                className="account-home-member"
                key={member.id}
                whileTap={{ scale: 0.992 }}
                onClick={() => navigate({ id: "member", params: { memberId: member.id } })}
              >
                <span className="account-home-member__avatar" aria-hidden="true" />

                <span className="account-home-member__copy">
                  <span className="account-home-member__name">
                    <strong>{member.name}</strong>
                    {member.owner ? (
                      <img
                        className="account-home-owner-crown"
                        src="/ui-illustrations/account-owner-crown.svg"
                        alt={t("accountHome.owner")}
                      />
                    ) : null}
                  </span>

                  {highestRole ? (
                    <span className="account-home-member__role">
                      <RoleSigil color={highestRole.color} size={21} compact />
                      <span style={{ color: highestRole.color }}>{highestRole.name}</span>
                      {extraRoleCount > 0 ? <small>+{extraRoleCount}</small> : null}
                    </span>
                  ) : (
                    <small className="account-home-member__no-role">
                      {t("accountHome.noRole")}
                    </small>
                  )}
                </span>

                <time>{formatMemberDate(member.joinedAt, locale)}</time>
              </motion.button>
            )
          })}
        </div>
      </section>

      <section className="account-home-panel account-home-drops">
        <header className="account-home-panel__head account-home-drops__head">
          <span className="account-home-drops__title-icon">
            <img src="/ui-icons/drop-stack.svg" alt="" aria-hidden="true" />
          </span>
          <span>
            <small>{t("accountHome.activity")}</small>
            <strong>{t("accountHome.latestDrops")}</strong>
          </span>
          <img
            className="account-home-drops__activity-art"
            src="/ui-illustrations/account-activity.svg"
            alt=""
            aria-hidden="true"
          />
        </header>

        <div className="account-home-drop-list">
          {drops.length ? (
            drops.map((drop) => {
              const tone = eventVisualTone(drop)

              return (
                <motion.button
                  className={`account-home-drop event-surface--${tone}`}
                  key={drop.id}
                  whileTap={{ scale: 0.992 }}
                  onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}
                >
                  <EventArt event={drop} size={42} />
                  <span className="account-home-drop__copy">
                    <strong>{drop.title}</strong>
                    <small>
                      {drop.rarity
                        ? t(`events.rarities.${drop.rarity}`, { defaultValue: drop.rarity })
                        : drop.meta}
                    </small>
                  </span>
                  <time>{formatOccurredAt(drop.occurredAt, locale)}</time>
                </motion.button>
              )
            })
          ) : (
            <p className="inline-hint inline-hint--left">{t("accountHome.noDrops")}</p>
          )}
        </div>
      </section>
    </div>
  )
}

function AccountMemberManagementProduction({
  member,
  roles,
  accountId,
  refresh,
  back,
}: {
  member: Member
  roles: AccountRole[]
  accountId: string
  refresh: () => Promise<void>
  back: () => void
}) {
  const { t, i18n } = useTranslation()
  const [roleIds, setRoleIds] = useState<string[]>(member.roleIds)
  const [busyRoleId, setBusyRoleId] = useState<string | null>(null)
  const [kickBusy, setKickBusy] = useState(false)
  const highestRole = roles.find((role) => role.id === member.highestRoleId) ?? null
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useEffect(() => {
    void getMemberRoles(accountId, member.id)
      .then((memberRoles) => setRoleIds(memberRoles.map((role) => role.id)))
      .catch(handleError)
  }, [accountId, member.id])

  const toggleRole = async (role: AccountRole) => {
    if (member.owner || busyRoleId) {
      return
    }

    const assigned = roleIds.includes(role.id)
    setBusyRoleId(role.id)

    try {
      if (assigned) {
        await revokeMemberRole(accountId, member.id, role.id)
        setRoleIds((current) => current.filter((id) => id !== role.id))
      } else {
        await assignMemberRole(accountId, member.id, role.id)
        setRoleIds((current) => [...current, role.id])
      }

      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusyRoleId(null)
    }
  }

  const kick = async () => {
    if (member.owner || kickBusy) {
      return
    }

    setKickBusy(true)

    try {
      await kickAccountMember(accountId, member.id)
      await refresh()
      back()
    } catch (error) {
      handleError(error)
      setKickBusy(false)
    }
  }

  return (
    <div className="screen account-home-member-detail">
      <PageHeader title={t("accountMember.title")} back={back} />

      <section className="account-member-v4__hero">
        <span className="account-member-v4__avatar" aria-hidden="true" />
        <span className="account-member-v4__identity">
          <small>ACCOUNT MEMBER</small>
          <strong>{member.name}</strong>
          <em>{t("accountMember.since", { date: formatMemberDate(member.joinedAt, locale) })}</em>
        </span>
        {highestRole ? <RoleSigil color={highestRole.color} size={42} /> : null}
      </section>

      <section className="account-member-v4__roles">
        <header>
          <span>
            <small>{t("accountMember.access")}</small>
            <strong>{t("accountMember.roles")}</strong>
          </span>
          <em>{t("accountMember.activeRoles", { count: roleIds.length })}</em>
        </header>

        <div>
          {roles.map((role) => {
            const assigned = roleIds.includes(role.id)
            const locked = member.owner

            return (
              <motion.button
                key={role.id}
                className={assigned ? "is-active" : ""}
                disabled={locked || busyRoleId !== null}
                whileTap={locked ? undefined : { scale: 0.985 }}
                onClick={() => void toggleRole(role)}
              >
                <RoleSigil color={role.color} size={34} compact />
                <span>
                  <strong style={{ color: assigned ? role.color : undefined }}>{role.name}</strong>
                  <small>
                    {t("accountMember.permissionCount", { count: enabledPermissions(role) })}
                  </small>
                </span>
                <i aria-hidden="true">
                  <b />
                </i>
              </motion.button>
            )
          })}
        </div>
      </section>

      {!member.owner ? (
        <section className="account-member-v4__danger">
          <span>
            <Glyph name="close" size={18} />
          </span>
          <span>
            <strong>{t("accountMember.removeTitle")}</strong>
            <small>{t("accountMember.removeCopy")}</small>
          </span>
          <button disabled={kickBusy} onClick={() => void kick()}>
            {kickBusy ? "…" : t("accountMember.kick")}
          </button>
        </section>
      ) : null}
    </div>
  )
}

function enabledPermissions(role: AccountRole) {
  return Object.values(role.permissions).filter(Boolean).length
}

function formatMemberDate(value: string, locale: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value.replace(" 2026", "")
  }

  return new Intl.DateTimeFormat(locale, { day: "numeric", month: "short" }).format(date)
}

function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }
  if (language === "ru") {
    return "ru-RU"
  }
  return "uk-UA"
}
