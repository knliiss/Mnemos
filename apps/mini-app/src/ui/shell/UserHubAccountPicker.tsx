import { AnimatePresence, motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { CristalixHead } from "../components/profile/CristalixHead"
import type { Account, Route } from "../model"
import "./account-picker-illustrations.css"

type UserHubAccountPickerProps = {
  open: boolean
  activeAccount: Account
  accounts: Account[]
  switchingAccountId: string | null
  onSelectAccount: (account: Account) => void
  navigate: (route: Route) => void
}

export function UserHubAccountPicker({
  open,
  activeAccount,
  accounts,
  switchingAccountId,
  onSelectAccount,
  navigate,
}: UserHubAccountPickerProps) {
  const { t } = useTranslation()

  return (
    <AnimatePresence initial={false}>
      {open ? (
        <motion.div
          className="user-hub-account-picker"
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: "auto" }}
          exit={{ opacity: 0, height: 0 }}
        >
          {accounts.map((account) => {
            const active = account.id === activeAccount.id
            const inactive = account.active === false
            const switching = switchingAccountId === account.id

            return (
              <button
                type="button"
                key={account.id}
                className={active ? "is-active" : ""}
                aria-current={active ? "true" : undefined}
                aria-disabled={inactive}
                disabled={inactive || switchingAccountId !== null}
                onClick={() => onSelectAccount(account)}
              >
                <CristalixHead
                  className="user-hub-account-picker__head"
                  skinUrl={account.skinUrl}
                  size={30}
                />
                <strong>{account.nickname}</strong>
                <small>
                  {switching
                    ? t("appShell.userMenu.switching")
                    : inactive
                      ? t("appShell.userMenu.inactive")
                      : t(account.role === "OWNER" ? "account.owner" : "account.participant")}
                </small>
                {active ? (
                  <span className="user-hub-account-picker__active-mark" aria-hidden="true">
                    <i />
                    <i />
                    <i />
                    <i />
                  </span>
                ) : null}
              </button>
            )
          })}

          <footer>
            <button
              type="button"
              disabled={switchingAccountId !== null}
              onClick={() => navigate({ id: "account-create" })}
            >
              <span className="user-hub-account-picker__action-art" aria-hidden="true">
                <img src="/ui-illustrations/account-create.svg" alt="" />
              </span>
              <span>{t("appShell.userMenu.create")}</span>
            </button>

            <button
              type="button"
              disabled={switchingAccountId !== null}
              onClick={() => navigate({ id: "account-join" })}
            >
              <span className="user-hub-account-picker__action-art" aria-hidden="true">
                <img src="/ui-illustrations/account-join.svg" alt="" />
              </span>
              <span>{t("appShell.userMenu.joinCode")}</span>
            </button>
          </footer>
        </motion.div>
      ) : null}
    </AnimatePresence>
  )
}
