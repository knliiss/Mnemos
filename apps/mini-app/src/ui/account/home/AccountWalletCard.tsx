import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { Account, Route } from "../../model"
import { CurrencyMark } from "../../shared/MnemosIcon"

type AccountWalletCardProps = {
  account: Account
  locale: string
  navigate: (route: Route) => void
}

export function AccountWalletCard({ account, locale, navigate }: AccountWalletCardProps) {
  const { t } = useTranslation()

  return (
    <motion.button
      type="button"
      className="account-home-wallet account-home-wallet--wide"
      whileTap={{ scale: 0.99 }}
      onClick={() => navigate({ id: "account-wallet" })}
    >
      <span className="account-home-wallet__copy">
        <small>{t("accountHome.wallet")}</small>

        <strong className="account-home-balance">
          <CurrencyMark currency="COIN" size={19} />
          {account.coin.toLocaleString(locale)}
        </strong>

        <span>{t("accountHome.walletSummary")}</span>
      </span>

      <span className="account-home-wallet__scene" aria-hidden="true">
        <motion.span
          className="account-home-wallet__play-coin"
          animate={{ y: [0, -8, -2, 0], rotate: [0, 16, -8, 0] }}
          transition={{ duration: 2.8, repeat: Infinity, repeatDelay: 1.7, ease: "easeInOut" }}
        >
          <i />
        </motion.span>

        <motion.img
          className="account-home-wallet__mascot"
          src="/ui-illustrations/mnemos-mascot-cat.svg"
          alt=""
          animate={{ y: [0, -1.5, 0], rotate: [-2, -1, -2] }}
          transition={{ duration: 2.4, repeat: Infinity, ease: "easeInOut" }}
        />

        <img className="account-home-wallet__coins" src="/ui-illustrations/coin-vault.png" alt="" />

        <i className="account-home-wallet__spark account-home-wallet__spark--one" />
        <i className="account-home-wallet__spark account-home-wallet__spark--two" />
        <i className="account-home-wallet__spark account-home-wallet__spark--three" />
      </span>
    </motion.button>
  )
}
