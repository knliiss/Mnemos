import { AnimatePresence, motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { CristalixHead } from "../components/profile/CristalixHead"
import type { Account, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"

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
            const inactive = account.active === false
            const switching = switchingAccountId === account.id

            return (
              <button
                type="button"
                key={account.id}
                className={account.id === activeAccount.id ? "is-active" : ""}
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
                {account.id === activeAccount.id ? <Glyph name="check" size={14} /> : null}
              </button>
            )
          })}

          <footer>
            <button
              type="button"
              disabled={switchingAccountId !== null}
              onClick={() => navigate({ id: "account-create" })}
            >
              <Glyph name="plus" size={15} />
              {t("appShell.userMenu.create")}
            </button>

            <button
              type="button"
              disabled={switchingAccountId !== null}
              onClick={() => navigate({ id: "account-join" })}
            >
              <Glyph name="invite" size={15} />
              {t("appShell.userMenu.joinCode")}
            </button>
          </footer>
        </motion.div>
      ) : null}
    </AnimatePresence>
  )
}
