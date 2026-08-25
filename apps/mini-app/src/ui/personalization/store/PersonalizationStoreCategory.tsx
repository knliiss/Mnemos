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
import { PersonalizationPager } from "../components/PersonalizationPager"
import {
  personalizationIllustrations,
  personalizationTargets,
  personalizationTypeImpact,
  personalizationTypeNote,
  personalizationTypeTitle,
  readPersonalizationType,
} from "../personalizationPresentation"

type PersonalizationStoreCategoryProps = {
  params?: Record<string, string>
  items: Personalization[]
  loading: boolean
  activeAccount: Account | null
  prime: number
  navigate: (route: Route) => void
  back: () => void
}

type NotificationPackSort = "DEFAULT" | "PRICE_ASC" | "PRICE_DESC" | "NAME" | "OWNED_FIRST"

const notificationPackSorts: NotificationPackSort[] = [
  "DEFAULT",
  "PRICE_ASC",
  "PRICE_DESC",
  "NAME",
  "OWNED_FIRST",
]
const pageSize = 1

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
  const [page, setPage] = useState(0)
  const [query, setQuery] = useState("")
  const [notificationSort, setNotificationSort] = useState<NotificationPackSort>("DEFAULT")

  const visibleItems = useMemo(() => {
    const normalizedQuery = query.trim().toLocaleLowerCase(i18n.resolvedLanguage)
    const filtered = items.filter(
      (item) =>
        item.status === "PUBLISHED" &&
        item.type === type &&
        (notificationPackCategory || currency === "ALL" || item.currency === currency) &&
        (!notificationPackCategory ||
          !normalizedQuery ||
          item.name.toLocaleLowerCase(i18n.resolvedLanguage).includes(normalizedQuery)),
    )

    if (!notificationPackCategory) {
      return filtered
    }

    return [...filtered].sort((left, right) => {
      if (notificationSort === "PRICE_ASC") {
        return left.price - right.price || left.name.localeCompare(right.name)
      }

      if (notificationSort === "PRICE_DESC") {
        return right.price - left.price || left.name.localeCompare(right.name)
      }

      if (notificationSort === "NAME") {
        return left.name.localeCompare(right.name, i18n.resolvedLanguage)
      }

      if (notificationSort === "OWNED_FIRST") {
        const leftRank = left.active ? 2 : left.owned ? 1 : 0
        const rightRank = right.active ? 2 : right.owned ? 1 : 0

        return (
          rightRank - leftRank || left.price - right.price || left.name.localeCompare(right.name)
        )
      }

      return 0
    })
  }, [
    currency,
    i18n.resolvedLanguage,
    items,
    notificationPackCategory,
    notificationSort,
    query,
    type,
  ])

  const pageCount = notificationPackCategory
    ? 1
    : Math.max(1, Math.ceil(visibleItems.length / pageSize))
  const safePage = Math.min(page, pageCount - 1)
  const pageItems = notificationPackCategory
    ? visibleItems
    : visibleItems.slice(safePage * pageSize, safePage * pageSize + pageSize)

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

      {notificationPackCategory ? (
        <section className="notification-pack-catalog-controls">
          <label className="notification-pack-catalog-search">
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

          <div
            className="notification-pack-catalog-sort"
            aria-label={t("personalization.category.sortTitle")}
          >
            {notificationPackSorts.map((sort) => (
              <button
                type="button"
                key={sort}
                className={notificationSort === sort ? "is-active" : ""}
                onClick={() => setNotificationSort(sort)}
              >
                {t(`personalization.category.sort.${sort}`)}
              </button>
            ))}
          </div>
        </section>
      ) : null}

      <div className="personalization-category-toolbar">
        <span>{t("personalization.category.positions", { count: visibleItems.length })}</span>

        {!notificationPackCategory ? (
          <Segmented
            value={currency}
            options={["ALL", ...CURRENCIES] as const}
            onChange={(value) => {
              setCurrency(value)
              setPage(0)
            }}
            label={(value) => (value === "ALL" ? t("personalization.category.all") : value)}
          />
        ) : null}
      </div>

      {loading ? (
        <PersonalizationLoadingState />
      ) : notificationPackCategory ? (
        <div className="notification-pack-catalog-grid">
          {pageItems.map((item) => (
            <motion.button
              type="button"
              key={item.id}
              className={`notification-pack-catalog-tile ${notificationPackTileClass(item)}`}
              whileTap={{ scale: 0.985 }}
              onClick={() =>
                navigate({
                  id: "personalization-detail",
                  params: { personalizationId: item.id },
                })
              }
            >
              <span className="notification-pack-catalog-tile__art" aria-hidden="true">
                <img src={personalizationIllustrations.NOTIFICATION_PACK} alt="" />
              </span>

              <span className="notification-pack-catalog-tile__footer">
                <strong>{item.name}</strong>
                {item.owned ? (
                  <span
                    className={`notification-pack-catalog-state${item.active ? " is-active" : ""}`}
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

          {!pageItems.length ? (
            <p className="inline-hint">{t("personalization.category.empty")}</p>
          ) : null}
        </div>
      ) : (
        <div className="personalization-category-list">
          {pageItems.map((item) => (
            <motion.article
              key={item.id}
              className="personalization-category-card personalization-category-card--featured"
              whileTap={{ scale: 0.992 }}
              role="button"
              tabIndex={0}
              onClick={() =>
                navigate({
                  id: "personalization-detail",
                  params: { personalizationId: item.id },
                })
              }
              onKeyDown={(event) => {
                if (event.key === "Enter" || event.key === " ") {
                  event.preventDefault()
                  navigate({
                    id: "personalization-detail",
                    params: { personalizationId: item.id },
                  })
                }
              }}
            >
              <span className="personalization-category-visual">
                <PersonalizationVisual item={item} />
              </span>

              <span className="personalization-category-copy">
                <small>{personalizationTypeTitle(t, item.type)}</small>
                <strong>{item.name}</strong>
                <em>{item.creator}</em>
              </span>

              <span className="personalization-category-price">
                {item.owned ? (
                  <span className="catalog-state">
                    <Glyph name="check" size={13} />
                    {item.active
                      ? t("personalization.category.active")
                      : t("personalization.category.owned")}
                  </span>
                ) : (
                  <CurrencyValue currency={item.currency} value={item.price} compact />
                )}
              </span>
            </motion.article>
          ))}

          {!pageItems.length ? (
            <p className="inline-hint">{t("personalization.category.empty")}</p>
          ) : null}
        </div>
      )}

      {!notificationPackCategory && pageCount > 1 ? (
        <PersonalizationPager page={safePage} pageCount={pageCount} onChange={setPage} />
      ) : null}
    </div>
  )
}

function notificationPackTileClass(item: Personalization): string {
  if (item.active) {
    return "is-active"
  }

  if (item.owned) {
    return "is-owned"
  }

  if (item.price <= 25) {
    return "price-entry"
  }

  if (item.price <= 30) {
    return "price-standard"
  }

  if (item.price <= 35) {
    return "price-elevated"
  }

  return "price-premium"
}
