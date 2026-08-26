import { motion } from "framer-motion"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { PersonalizationVisual } from "../../components/personalization/PersonalizationPreview"
import { CURRENCIES } from "../../contracts"
import type { Account, Currency, Personalization, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { CurrencyValue, PageHeader, Segmented } from "../../shared/Primitives"
import { PersonalizationBalanceBar } from "../components/PersonalizationBalanceBar"
import { PersonalizationLoadingState } from "../components/PersonalizationLoadingState"
import {
  personalizationIllustrations,
  personalizationTargets,
  personalizationTypeImpact,
  personalizationTypeNote,
  personalizationTypeTitle,
  readPersonalizationType,
} from "../personalizationPresentation"
import { NotificationPackCatalogSortSelect } from "./NotificationPackCatalogSortSelect"

type PersonalizationStoreCategoryProps = {
  params?: Record<string, string>
  items: Personalization[]
  loading: boolean
  activeAccount: Account | null
  prime: number
  navigate: (route: Route) => void
  back: () => void
}

type CatalogPriceSort = "PRICE_ASC" | "PRICE_DESC"

const catalogPriceSorts: CatalogPriceSort[] = ["PRICE_ASC", "PRICE_DESC"]

export function PersonalizationStoreCategory({
  params,
  items,
  loading,
  activeAccount,
  prime,
  navigate,
  back,
}: PersonalizationStoreCategoryProps) {
  const { t, i18n } = useTranslation()
  const type = readPersonalizationType(params?.type) ?? "PROFILE_THEME"
  const notificationPackCategory = type === "NOTIFICATION_PACK"
  const [currency, setCurrency] = useState<"ALL" | Currency>("ALL")
  const [query, setQuery] = useState("")
  const [priceSort, setPriceSort] = useState<CatalogPriceSort>("PRICE_ASC")

  const visibleItems = useMemo(() => {
    const normalizedQuery = query.trim().toLocaleLowerCase(i18n.resolvedLanguage)
    const filtered = items.filter(
      (item) =>
        item.status === "PUBLISHED" &&
        item.type === type &&
        (notificationPackCategory || currency === "ALL" || item.currency === currency) &&
        (!normalizedQuery ||
          item.name.toLocaleLowerCase(i18n.resolvedLanguage).includes(normalizedQuery)),
    )

    if (!notificationPackCategory) {
      return filtered
    }

    return [...filtered].sort((left, right) => {
      const priceDifference =
        priceSort === "PRICE_ASC" ? left.price - right.price : right.price - left.price

      return priceDifference || left.name.localeCompare(right.name, i18n.resolvedLanguage)
    })
  }, [currency, i18n.resolvedLanguage, items, notificationPackCategory, priceSort, query, type])

  const priceSortOptions = catalogPriceSorts.map((sort) => ({
    value: sort,
    label: t(`personalization.category.sort.${sort}`),
  }))

  return (
    <div className="screen personalization-store-category">
      <PageHeader title={personalizationTypeTitle(t, type)} back={back} />

      <PersonalizationBalanceBar activeAccount={activeAccount} prime={prime} navigate={navigate} />

      <section className="personalization-category-intro">
        <span className="personalization-category-icon personalization-category-icon--illustrated">
          <img src={personalizationIllustrations[type]} alt="" aria-hidden="true" />
        </span>
        <span>
          <small>
            {personalizationTargets[type] === "ACCOUNT"
              ? t("personalization.target.accountEyebrow")
              : t("personalization.target.userEyebrow")}
          </small>
          <strong>{personalizationTypeNote(t, type)}</strong>
          <em>{personalizationTypeImpact(t, type)}</em>
        </span>
      </section>

      <section
        className={`personalization-catalog-controls${
          notificationPackCategory ? " has-price-sort" : ""
        }`}
      >
        <label className="personalization-catalog-search">
          <Glyph name="search" size={15} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={t("personalization.category.searchPlaceholder")}
          />
          {query ? (
            <button type="button" onClick={() => setQuery("")} aria-label={t("common.clear")}>
              <Glyph name="close" size={13} />
            </button>
          ) : null}
        </label>

        {notificationPackCategory ? (
          <NotificationPackCatalogSortSelect
            value={priceSort}
            options={priceSortOptions}
            label={t("personalization.category.sortTitle")}
            onChange={setPriceSort}
          />
        ) : null}
      </section>

      <div className="personalization-category-toolbar">
        <span>{t("personalization.category.positions", { count: visibleItems.length })}</span>

        {!notificationPackCategory ? (
          <Segmented
            value={currency}
            options={["ALL", ...CURRENCIES] as const}
            onChange={setCurrency}
            label={(value) => (value === "ALL" ? t("personalization.category.all") : value)}
          />
        ) : null}
      </div>

      {loading ? (
        <PersonalizationLoadingState />
      ) : (
        <div className="personalization-catalog-grid">
          {visibleItems.map((item) => (
            <motion.button
              type="button"
              key={item.id}
              className={`personalization-catalog-tile ${catalogTileClass(item)}`}
              whileTap={{ scale: 0.985 }}
              onClick={() =>
                navigate({
                  id: "personalization-detail",
                  params: { personalizationId: item.id },
                })
              }
            >
              <span className="personalization-catalog-tile__art" aria-hidden="true">
                {item.type === "NOTIFICATION_PACK" ? (
                  <img src={personalizationIllustrations.NOTIFICATION_PACK} alt="" />
                ) : (
                  <PersonalizationVisual item={item} />
                )}
              </span>

              <span className="personalization-catalog-tile__footer">
                <strong>{item.name}</strong>
                {item.owned ? (
                  <span
                    className={`personalization-catalog-state${item.active ? " is-active" : ""}`}
                  >
                    <Glyph name="check" size={11} />
                    {item.active
                      ? t("personalization.category.active")
                      : t("personalization.category.owned")}
                  </span>
                ) : (
                  <CurrencyValue currency={item.currency} value={item.price} compact />
                )}
              </span>
            </motion.button>
          ))}

          {!visibleItems.length ? (
            <p className="inline-hint">{t("personalization.category.empty")}</p>
          ) : null}
        </div>
      )}
    </div>
  )
}

function catalogTileClass(item: Personalization): string {
  if (item.active) {
    return "is-active"
  }

  if (item.owned) {
    return "is-owned"
  }

  if (item.currency === "COIN") {
    if (item.price <= 1_500) {
      return "price-entry"
    }

    if (item.price <= 4_000) {
      return "price-standard"
    }

    if (item.price <= 10_000) {
      return "price-elevated"
    }

    return "price-premium"
  }

  if (item.price <= 15) {
    return "price-entry"
  }

  if (item.price <= 30) {
    return "price-standard"
  }

  if (item.price <= 75) {
    return "price-elevated"
  }

  return "price-premium"
}
