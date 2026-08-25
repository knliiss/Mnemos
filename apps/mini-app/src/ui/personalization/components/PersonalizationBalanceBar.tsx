import { useTranslation } from "react-i18next"
import type { Account, Route } from "@/ui/model"
import { CurrencyMark } from "@/ui/shared/MnemosIcon"
import { localeForLanguage } from "../personalizationPresentation"
import "./personalization-balance-bar.css"

type PersonalizationBalanceBarProps = {
  activeAccount: Account | null
  prime: number
  navigate: (route: Route) => void
  compact?: boolean
}

export function PersonalizationBalanceBar({
  activeAccount,
  prime,
  navigate,
  compact = false,
}: PersonalizationBalanceBarProps) {
  const { i18n } = useTranslation()
  const locale = localeForLanguage(i18n.resolvedLanguage)

  return (
    <div className={`personalization-balance-bar ${compact ? "is-compact" : ""}`.trim()}>
      <button
        type="button"
        className="personalization-balance personalisation-balance--coin"
        disabled={!activeAccount}
        onClick={() => navigate({ id: "account-wallet" })}
      >
        <CurrencyMark currency="COIN" size={compact ? 15 : 17} />
        <span>
          <small>{activeAccount?.nickname ?? "COIN"}</small>
          <strong>{activeAccount ? activeAccount.coin.toLocaleString(locale) : "—"}</strong>
        </span>
      </button>

      <button
        type="button"
        className="personalization-balance personalization-balance--prime"
        onClick={() => navigate({ id: "user-wallet" })}
      >
        <CurrencyMark currency="PRIME" size={compact ? 15 : 17} />
        <span>
          <small>PRIME</small>
          <strong>{prime.toLocaleString(locale)}</strong>
        </span>
      </button>
    </div>
  )
}
