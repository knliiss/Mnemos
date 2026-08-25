import { motion } from "framer-motion"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { PersonalizationVisual } from "../../components/personalization/PersonalizationPreview"
import { CURRENCIES } from "../../contracts"
import type { Account, Currency, Personalization, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { CurrencyValue, PageHeader, Segmented } from "../../shared/Primitives"
import { NotificationPackSpotlightPreview } from "../components/NotificationPackSpotlightPreview"
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
  const { t } = useTranslation()
  const type = readPersonalizationType(params?.type) ?? "PROFILE_THEME"
  const [currency, setCurrency] = useState<"ALL" | Currency>("ALL")
  const [page, setPage] = useState(0)

  const visibleItems = useMemo(
    () =>
      items.filter(
        (item) =>
          item.status === "PUBLISHED" &&
          item.type === type &&
          (currency === "ALL" || item.currency === currency),
      ),
    [currency, items, type],
  )

  const pageCount = Math.max(1, Math.ceil(visibleItems.length / pageSize))
  const safePage = Math.min(page, pageCount - 1)
  const pageItems = visibleItems.slice(safePage * pageSize, safePage * pageSize + pageSize)

  return (
    <div className="screen personalization-store-category">
      <PageHeader title={personalizationTypeTitle(t, type)} back={back} />

      <PersonalizationBalanceBar
        activeAccount={activeAccount}
        prime={prime}
        navigate={navigate}
      />

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

      <div className="personalization-category-toolbar">
        <span>{t("personalization.category.positions", { count: visibleItems.length })}</span>
        <Segmented
          value={currency}
          options={["ALL", ...CURRENCIES] as const}
          onChange={(value) => {
            setCurrency(value)
            setPage(0)
          }}
          label={(value) => (value === "ALL" ? t("personalization.category.all") : value)}
        />
      </div>

      {loading ? (
        <PersonalizationLoadingState />
      ) : (
        <div className="personalization-category-list">
          {pageItems.map((item) => (
            <motion.button
              type="button"
              key={item.id}
              className="personalization-category-card personalization-category-card--featured"
              whileTap={{ scale: 0.992 }}
              onClick={() =>
                navigate({
                  id: "personalization-detail",
                  params: { personalizationId: item.id },
                })
              }
            >
              <span className="personalization-category-visual">
                {item.type === "NOTIFICATION_PACK" ? (
                  <NotificationPackSpotlightPreview item={item} size="CARD" />
                ) : (
                  <PersonalizationVisual item={item} />
                )}
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
            </motion.button>
          ))}

          {!pageItems.length ? (
            <p className="inline-hint">{t("personalization.category.empty")}</p>
          ) : null}
        </div>
      )}

      {pageCount > 1 ? (
        <PersonalizationPager page={safePage} pageCount={pageCount} onChange={setPage} />
      ) : null}
    </div>
  )
}
