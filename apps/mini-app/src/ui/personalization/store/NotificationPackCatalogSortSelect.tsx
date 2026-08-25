import { useEffect } from "react"
import { CurrencyMark } from "../../shared/MnemosIcon"

type SortOption<TValue extends string> = {
  value: TValue
  label: string
}

type NotificationPackCatalogSortSelectProps<TValue extends string> = {
  value: TValue
  options: SortOption<TValue>[]
  label: string
  onChange: (value: TValue) => void
}

export function NotificationPackCatalogSortSelect<TValue extends string>({
  value,
  options,
  onChange,
}: NotificationPackCatalogSortSelectProps<TValue>) {
  const ascending = options.find((option) => option.value === "PRICE_ASC")
  const descending = options.find((option) => option.value === "PRICE_DESC")
  const priceAscending = value !== descending?.value

  useEffect(() => {
    if (value === ascending?.value || value === descending?.value || !ascending) {
      return
    }

    onChange(ascending.value)
  }, [ascending, descending, onChange, value])

  const next = priceAscending ? descending : ascending

  return (
    <button
      type="button"
      className="notification-pack-catalog-price-sort"
      aria-label={next?.label}
      title={next?.label}
      disabled={!next}
      onClick={() => {
        if (next) {
          onChange(next.value)
        }
      }}
    >
      <CurrencyMark currency="PRIME" size={15} />
      <svg
        className="notification-pack-catalog-price-sort__direction"
        viewBox="0 0 16 16"
        aria-hidden="true"
      >
        {priceAscending ? <path d="M8 13V3m-3 3 3-3 3 3" /> : <path d="M8 3v10m-3-3 3 3 3-3" />}
      </svg>
    </button>
  )
}
