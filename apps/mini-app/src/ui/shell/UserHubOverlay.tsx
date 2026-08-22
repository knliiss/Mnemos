import { AnimatePresence, motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { CristalixHead } from "../components/profile/CristalixHead"
import type { Account, LocalizationLanguage, Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { localizationOptions } from "./navigation"
import { UserHubAccountPicker } from "./UserHubAccountPicker"
import { UserHubLocalization } from "./UserHubLocalization"
import { UserHubWorkspaces } from "./UserHubWorkspaces"

type UserHubOverlayProps = {
  open: boolean
  accountPickerOpen: boolean
  activeAccount: Account
  accounts: Account[]
  switchingAccountId: string | null
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
  switchingAccountId,
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
  const localizationOption =
    localizationOptions.find((option) => option.value === localization) ?? localizationOptions[0]

  return (
    <AnimatePresence>
      {open ? (
        <>
          <motion.button
            type="button"
            className="user-hub-backdrop"
            aria-label={t("appShell.userMenu.close")}
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
              <span className="user-hub-card__avatar user-hub-card__avatar--mnemos">
                <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" aria-hidden="true" />
              </span>
              <span className="user-hub-card__copy">
                <small>{t("appShell.userMenu.userEyebrow")}</small>
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
                <small>{t("appShell.userMenu.activeAccount")}</small>
                <strong>{activeAccount.nickname}</strong>
              </span>
              <span className="user-hub-account-switch__action">
                <Glyph name="swap" size={16} />
                {t("appShell.userMenu.change")}
              </span>
            </button>

            <UserHubAccountPicker
              open={accountPickerOpen}
              activeAccount={activeAccount}
              accounts={accounts}
              switchingAccountId={switchingAccountId}
              onSelectAccount={onSelectAccount}
              navigate={navigate}
            />

            <UserHubLocalization localization={localization} onChange={onLocalizationChange} />

            <UserHubWorkspaces systemRoles={systemRoles} navigate={navigate} />
          </motion.aside>
        </>
      ) : null}
    </AnimatePresence>
  )
}
