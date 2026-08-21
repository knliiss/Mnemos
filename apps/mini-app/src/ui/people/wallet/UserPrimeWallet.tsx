import { useTranslation } from "react-i18next"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Route } from "../../model"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import "./people-wallet.css"
import { PrimeWalletHistory } from "./PrimeWalletHistory"
import { useUserPrimeTransactions } from "./useUserPrimeTransactions"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function UserPrimeWallet({ navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const data = useMnemosData()
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const { transactions, loading, loadError, earned, spent, reload } =
    useUserPrimeTransactions(data.user.id)

  return (
    <div className="screen prime-wallet-screen">
      <PageHeader title="PRIME" eyebrow={t("userWallet.userEyebrow")} back={back} />

      <section className="prime-wallet-hero">
        <div className="prime-wallet-hero__copy">
          <span className="prime-wallet-hero__label">
            <CurrencyMark currency="PRIME" size={19} />
            <strong>PRIME</strong>
          </span>
          <strong className="prime-wallet-hero__value">
            {data.user.prime.toLocaleString(locale)}
          </strong>
          <small>{t("userWallet.description")}</small>
        </div>

        <img
          className="prime-wallet-hero__art"
          src="/ui-illustrations/prime-cache.png"
          alt=""
          aria-hidden="true"
        />

        <div className="prime-wallet-hero__stats">
          <span>
            <small>{t("userWallet.earned")}</small>
            <strong>+{earned.toLocaleString(locale)}</strong>
          </span>
          <span>
            <small>{t("userWallet.spent")}</small>
            <strong>−{spent.toLocaleString(locale)}</strong>
          </span>
        </div>
      </section>

      <nav className="prime-wallet-shortcuts" aria-label={t("userWallet.shortcutsAria")}>
        <button type="button" onClick={() => navigate({ id: "personalization-store" })}>
          <span className="is-plum">
            <Glyph name="spark" size={19} />
          </span>
          <strong>{t("userWallet.store")}</strong>
          <small>{t("userWallet.personalizations")}</small>
          <b>›</b>
        </button>

        <button type="button" onClick={() => navigate({ id: "account-wallet" })}>
          <span className="is-lime">
            <Glyph name="wallet" size={19} />
          </span>
          <strong>COIN</strong>
          <small>{t("userWallet.accountWallet")}</small>
          <b>›</b>
        </button>
      </nav>

      <section className="prime-topup-panel">
        <header>
          <span>
            <small>{t("userWallet.topup.eyebrow")}</small>
            <strong>{t("userWallet.topup.title")}</strong>
            <em>{t("userWallet.topup.copy")}</em>
          </span>
          <span className="prime-topup-status">{t("userWallet.topup.soon")}</span>
        </header>

        <button type="button" className="prime-topup-disabled" disabled>
          <Glyph name="clock" size={16} />
          {t("userWallet.topup.unavailable")}
        </button>
      </section>

      <PrimeWalletHistory
        transactions={transactions}
        loading={loading}
        loadError={loadError}
        locale={locale}
        onRetry={reload}
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
