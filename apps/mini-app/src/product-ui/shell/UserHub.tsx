import { AnimatePresence, motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { CristalixHead } from "../components/profile/CristalixHead"
import { useProductData } from "../data/ProductDataProvider"
import type { Account, LocalizationLanguage, Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { localizationOptions } from "./navigation"

type UserHubTriggerProps = {
  open: boolean
  onToggle: () => void
}

export function UserHubTrigger({ open, onToggle }: UserHubTriggerProps) {
  const { t } = useTranslation()

  return (
    <motion.button
      type="button"
      className={`global-user-button ${open ? "is-open" : ""}`}
      whileTap={{ scale: 0.95 }}
      onClick={onToggle}
      aria-label={t("productShell.userMenu.open")}
    >
      <UserAvatar className="global-user-button__avatar" size={34} />
      <i />
    </motion.button>
  )
}

type UserHubOverlayProps = {
  open: boolean
  accountPickerOpen: boolean
  activeAccount: Account
  accounts: Account[]
  localization: LocalizationLanguage
  username: string
  prime: number
  systemRoles: string[]
  onLocalizationChange: (localization: LocalizationLanguage) => void
  onClose: () => void
  onToggleAccounts: () => void
  onSelectAccount: (account: Account) => void
  navigate: (route: Route) => void
}

export function UserHubOverlay({
  open,
  accountPickerOpen,
  activeAccount,
  accounts,
  localization,
  username,
  prime,
  systemRoles,
  onLocalizationChange,
  onClose,
  onToggleAccounts,
  onSelectAccount,
  navigate,
}: UserHubOverlayProps) {
  const { t } = useTranslation()
  const creator = systemRoles.some((role) =>
    ["PERSONALIZATION_CREATOR", "ADMIN", "SUPER_ADMIN"].includes(role),
  )
  const moderation = systemRoles.some((role) =>
    ["MODERATOR", "ADMIN", "SUPER_ADMIN"].includes(role),
  )
  const administration = systemRoles.some((role) => ["ADMIN", "SUPER_ADMIN"].includes(role))
  const personalizationReviewer = systemRoles.includes("PERSONALIZATION_MODERATOR")
  const reviewerOnly = personalizationReviewer && !administration
  const localizationOption =
    localizationOptions.find((option) => option.value === localization) ?? localizationOptions[0]

  return (
    <AnimatePresence>
      {open ? (
        <>
          <motion.button
            type="button"
            className="user-hub-backdrop"
            aria-label={t("productShell.userMenu.close")}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />

          <motion.aside
            className="user-hub"
            initial={{ opacity: 0, y: -10, scale: 0.975 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -7, scale: 0.985 }}
          >
            <header className="user-hub-card">
              <UserAvatar
                className="user-hub-card__avatar"
                placeholderClassName="user-hub-card__avatar--placeholder"
                size={40}
              />
              <span className="user-hub-card__copy">
                <small>MNEMOS USER</small>
                <strong>@{username}</strong>
              </span>
              <button
                type="button"
                className="user-hub-prime"
                onClick={() => navigate({ id: "user-wallet" })}
              >
                <CurrencyMark currency="PRIME" size={16} />
                <strong>{prime.toLocaleString(localizationOption.numberLocale)}</strong>
                <b>›</b>
              </button>
            </header>

            <button
              type="button"
              className={`user-hub-account-switch ${accountPickerOpen ? "is-open" : ""}`}
              onClick={onToggleAccounts}
            >
              <CristalixHead
                className="user-hub-account-switch__mark user-hub-account-switch__mark--skin"
                skinUrl={activeAccount.skinUrl}
                size={34}
              />
              <span>
                <small>{t("productShell.userMenu.activeAccount")}</small>
                <strong>{activeAccount.nickname}</strong>
              </span>
              <span className="user-hub-account-switch__action">
                <Glyph name="swap" size={16} />
                {t("productShell.userMenu.change")}
              </span>
            </button>

            <AnimatePresence initial={false}>
              {accountPickerOpen ? (
                <motion.div
                  className="user-hub-account-picker"
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: "auto" }}
                  exit={{ opacity: 0, height: 0 }}
                >
                  {accounts.map((account) => (
                    <button
                      type="button"
                      key={account.id}
                      className={account.id === activeAccount.id ? "is-active" : ""}
                      aria-disabled={account.active === false && !account.owner}
                      onClick={() => onSelectAccount(account)}
                    >
                      <CristalixHead
                        className="user-hub-account-picker__head"
                        skinUrl={account.skinUrl}
                        size={30}
                      />
                      <strong>{account.nickname}</strong>
                      <small>
                        {account.active === false
                          ? account.owner
                            ? t("productShell.userMenu.reactivate")
                            : t("productShell.userMenu.inactive")
                          : account.role}
                      </small>
                      {account.id === activeAccount.id ? <Glyph name="check" size={14} /> : null}
                    </button>
                  ))}

                  <footer>
                    <button type="button" onClick={() => navigate({ id: "account-create" })}>
                      <Glyph name="plus" size={15} />
                      {t("productShell.userMenu.create")}
                    </button>
                    <button type="button" onClick={() => navigate({ id: "account-join" })}>
                      <Glyph name="invite" size={15} />
                      {t("productShell.userMenu.joinCode")}
                    </button>
                  </footer>
                </motion.div>
              ) : null}
            </AnimatePresence>

            <section
              className="user-hub-localization"
              aria-label={t("productShell.userMenu.languageAria")}
            >
              <span className="user-hub-localization__identity">
                <span className="user-hub-localization__mark" aria-hidden="true" />
                <span className="user-hub-localization__copy">
                  <small>{t("productShell.userMenu.language")}</small>
                  <strong>{localizationOption.label}</strong>
                </span>
              </span>

              <span
                className="user-hub-localization__choices"
                role="group"
                aria-label={t("productShell.userMenu.chooseLanguage")}
              >
                {localizationOptions.map((option) => (
                  <motion.button
                    key={option.value}
                    type="button"
                    className={option.value === localization ? "is-active" : ""}
                    aria-pressed={option.value === localization}
                    aria-label={option.label}
                    whileTap={{ scale: 0.93 }}
                    onClick={() => onLocalizationChange(option.value)}
                  >
                    {option.short}
                  </motion.button>
                ))}
              </span>
            </section>

            <div className="user-hub-workspace-label">{t("productShell.userMenu.workspaces")}</div>

            <nav className="user-hub-workspace-grid">
              {creator ? (
                <button
                  type="button"
                  className="is-creator"
                  onClick={() => navigate({ id: "creator-studio" })}
                >
                  <span>
                    <Glyph name="creator" size={20} />
                  </span>
                  <strong>Creator Studio</strong>
                  <small>{t("productShell.userMenu.creatorDescription")}</small>
                  <b>›</b>
                </button>
              ) : null}

              {moderation && !administration ? (
                <button
                  type="button"
                  className="is-moderation"
                  onClick={() => navigate({ id: "moderation" })}
                >
                  <span>
                    <Glyph name="moderation" size={20} />
                  </span>
                  <strong>{t("productShell.userMenu.moderation")}</strong>
                  <small>{t("productShell.userMenu.moderationDescription")}</small>
                  <b>›</b>
                </button>
              ) : null}

              {reviewerOnly ? (
                <button
                  type="button"
                  className="is-creator"
                  onClick={() => navigate({ id: "personalization-review" })}
                >
                  <span>
                    <Glyph name="creator" size={20} />
                  </span>
                  <strong>{t("productShell.userMenu.review")}</strong>
                  <small>{t("productShell.userMenu.reviewDescription")}</small>
                  <b>›</b>
                </button>
              ) : null}

              {administration ? (
                <button
                  type="button"
                  className="is-admin"
                  onClick={() => navigate({ id: "administration" })}
                >
                  <span>
                    <Glyph name="admin" size={20} />
                  </span>
                  <strong>{t("productShell.userMenu.administration")}</strong>
                  <small>{t("productShell.userMenu.administrationDescription")}</small>
                  <b>›</b>
                </button>
              ) : null}
            </nav>
          </motion.aside>
        </>
      ) : null}
    </AnimatePresence>
  )
}

type UserAvatarProps = {
  className?: string
  placeholderClassName?: string
  size: number
}

function UserAvatar({ className, placeholderClassName, size }: UserAvatarProps) {
  const product = useProductData()
  const skinUrl = product.activeAccount?.skinUrl

  if (skinUrl) {
    return (
      <CristalixHead
        className={`${className ?? ""} user-avatar-head`.trim()}
        skinUrl={skinUrl}
        size={size}
      />
    )
  }

  return (
    <span
      className={`user-avatar-placeholder ${className ?? ""} ${placeholderClassName ?? ""}`.trim()}
      style={{ width: size, height: size }}
      aria-hidden="true"
    >
      <Glyph name="profile-mark" size={Math.round(size * 0.48)} />
    </span>
  )
}
