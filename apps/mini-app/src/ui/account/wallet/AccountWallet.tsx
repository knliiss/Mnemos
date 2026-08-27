import { useTranslation } from "react-i18next"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { CurrencyMark } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import "./account-wallet.css"
import { CoinPurchasePanel } from "./CoinPurchasePanel"
import { useAccountWalletTransactions } from "./useAccountWalletTransactions"
import { WalletHistoryPanel } from "./WalletHistoryPanel"

export function AccountWallet({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const data = useMnemosData()
  const activeAccount = data.activeAccount
  const canViewHistory = data.hasPermission("VIEW_TRANSACTION_HISTORY")
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const { transactions, loading, loadError, totalEarned, totalSpent, retry } =
    useAccountWalletTransactions(activeAccount?.id, canViewHistory)

  if (!activeAccount) {
    return null
  }

  return (
    <div className="screen account-wallet-screen">
      <PageHeader title={t("accountWallet.title")} eyebrow={activeAccount.nickname} back={back} />

      <section className="coin-wallet-hero">
        <div className="coin-wallet-hero__copy">
          <span className="coin-wallet-hero__label">
            <CurrencyMark currency="COIN" size={20} />
            <strong>COIN</strong>
          </span>

          <strong className="coin-wallet-hero__value">
            {activeAccount.coin.toLocaleString(locale)}
          </strong>
          <small>{t("accountWallet.balance")}</small>
        </div>

        <img
          className="coin-wallet-hero__art"
          src="/ui-illustrations/coin-vault.png"
          alt=""
          aria-hidden="true"
        />

        <div className="coin-wallet-hero__stats">
          <span>
            <small>{t("accountWallet.earned")}</small>
            <strong>{canViewHistory ? `+${totalEarned.toLocaleString(locale)}` : "—"}</strong>
          </span>
          <span>
            <small>{t("accountWallet.spent")}</small>
            <strong>{canViewHistory ? `−${totalSpent.toLocaleString(locale)}` : "—"}</strong>
          </span>
        </div>
      </section>

      <CoinPurchasePanel
        accountId={activeAccount.id}
        accountNickname={activeAccount.nickname}
        coinBalance={activeAccount.coin}
        locale={locale}
        onPurchased={retry}
      />

      <WalletHistoryPanel
        canViewHistory={canViewHistory}
        transactions={transactions}
        loading={loading}
        loadError={loadError}
        locale={locale}
        onRetry={retry}
      />
    </div>
  )
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
