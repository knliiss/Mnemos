import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { EventArt } from "../components/events/EventArt"
import { CristalixHead } from "../components/profile/CristalixHead"
import { useProductData } from "../data/ProductDataProvider"
import { eventVisualTone } from "../eventVisualTone"
import type { Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { RoleSigil } from "../shared/RoleSigil"
import { formatOccurredAt } from "../timePresentation"
import { AccountDailyReward } from "./AccountDailyReward"
import { AccountMemberManagement } from "./AccountMemberManagement"
import { formatMemberDate, localeForLanguage } from "./accountPresentation"
import { useLatestAccountDrops } from "./data/useLatestAccountDrops"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  initialMemberId?: string | null
}

export function AccountHome({ navigate, back, initialMemberId }: Props) {
  const { t, i18n } = useTranslation()
  const product = useProductData()
  const activeAccount = product.activeAccount
  const selectedMember = product.members.find((member) => member.id === initialMemberId) ?? null
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const { drops } = useLatestAccountDrops(activeAccount?.nickname ?? null, !initialMemberId)

  if (!activeAccount) {
    return null
  }

  if (initialMemberId && selectedMember) {
    return (
      <AccountMemberManagement
        member={selectedMember}
        roles={product.roles}
        accountId={activeAccount.id}
        refresh={product.refreshAccount}
        back={back}
      />
    )
  }

  const roleFor = (roleId: string | null) =>
    product.roles.find((role) => role.id === roleId) ?? null

  return (
    <div className="screen account-home-screen root-screen">
      <motion.button type="button"
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

      <AccountDailyReward />

      <section className="account-home-feature-grid">
        <motion.button type="button"
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

        <motion.button type="button"
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
        <motion.button type="button" whileTap={{ scale: 0.97 }} onClick={() => navigate({ id: "roles" })}>
          <RoleSigil color="#A985A8" size={27} compact />
          <span>
            <strong>{t("accountHome.roles")}</strong>
            <small>{t("accountHome.accessRights")}</small>
          </span>
        </motion.button>

        <motion.button type="button"
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

        <motion.button type="button"
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
              <motion.button type="button"
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
                    <small className="account-home-member__no-role">{t("accountHome.noRole")}</small>
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
                <motion.button type="button"
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
