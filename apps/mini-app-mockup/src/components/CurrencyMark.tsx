import type { Currency } from "../types"

type CurrencyMarkProps = {
  currency: Currency
}

export function CurrencyMark({ currency }: CurrencyMarkProps) {
  if (currency === "coin") {
    return (
      <svg className="currency-mark" viewBox="0 0 24 24" role="img" aria-label="Coins">
        <circle cx="12" cy="12" r="8.1" />
        <circle cx="12" cy="12" r="3.15" />
      </svg>
    )
  }

  return (
    <svg
      className="currency-mark currency-mark--prime"
      viewBox="0 0 24 24"
      role="img"
      aria-label="Primes"
    >
      <path d="M12 3.2 20.2 12 12 20.8 3.8 12 12 3.2Z" />
      <path d="m12 3.2 2.45 8.8L12 20.8 9.55 12 12 3.2Z" />
    </svg>
  )
}
