import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { PersonalizationVisual } from "../../components/personalization/PersonalizationPreview"
import { PERSONALIZATION_TYPES } from "../../contracts"
import type { Account, Personalization, Route } from "../../model"
import { NotificationPackSpotlightPreview } from "../components/NotificationPackSpotlightPreview"
import { PersonalizationBalanceBar } from "../components/PersonalizationBalanceBar"
import { PersonalizationLoadingState } from "../components/PersonalizationLoadingState"
import {
  localeForLanguage,
  personalizationIllustrations,
  personalizationTargets,
  personalizationTypeNote,
  personalizationTypeTitle,
} from "../personalizationPresentation"

type PersonalizationStoreProps = {
  items: Personalization[]
  loading: boolean
  activeAccount: Account | null
  prime: number
  navigate: (route: Route) => void
}

export function PersonalizationStore({
  items,
  loading,
  activeAccount,
  prime,
  navigate,
}: PersonalizationStoreProps) {
  const { t, i18n } = useTranslation()
  const published = items.filter((item) => item.status === "PUBLISHED")
  const locale = localeForLanguage(i18n.resolvedLanguage)

  return (
    <div className="screen personalization-store-screen root-screen">
      <header className="personalization-store-header">
        <span>
          <small>{t("personalization.store.eyebrow")}</small>
          <h1>{t("personalization.store.title")}</h1>
          <p>{t("personalization.store.description")}</p>
        </span>

        <PersonalizationBalanceBar
          activeAccount={activeAccount}
          prime={prime}
          navigate={navigate}
          compact
        />
      </header>

      <section className="personalization-store-hero">
        <span>
          <small>{t("personalization.store.catalogEyebrow")}</small>
          <strong>{t("personalization.store.heroTitle")}</strong>
          <em>{t("personalization.store.heroCopy")}</em>
        </span>

        <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
      </section>

      {loading ? (
        <PersonalizationLoadingState />
      ) : (
        <section className="personalization-store-categories">
          {PERSONALIZATION_TYPES.map((type) => {
            const typeItems = published.filter((item) => item.type === type)
            const ownedCount = items.filter((item) => item.type === type && item.owned).length
            const sample = typeItems[0] ?? items.find((item) => item.type === type && item.owned)

            return (
              <motion.button
                type="button"
                key={type}
                className={`personalization-store-category-card personalization-store-category-card--${type.toLowerCase()}`}
                whileTap={{ scale: 0.988 }}
                onClick={() =>
                  navigate({
                    id: "personalization-store-category",
                    params: { type },
                  })
                }
              >
                <span className="personalization-store-category-card__icon">
                  <img src={personalizationIllustrations[type]} alt="" aria-hidden="true" />
                </span>

                <span className="personalization-store-category-card__copy">
                  <small>{t(`personalization.target.${personalizationTargets[type]}`)}</small>
                  <strong>{personalizationTypeTitle(t, type)}</strong>
                  <em>{personalizationTypeNote(t, type)}</em>
                  <span>
                    {t("personalization.store.catalogCount", { count: typeItems.length })}
                    {ownedCount
                      ? ` · ${t("personalization.store.ownedCount", { count: ownedCount })}`
                      : ""}
                  </span>
                </span>

                <span className="personalization-store-category-card__preview">
                  {sample ? (
                    sample.type === "NOTIFICATION_PACK" ? (
                      <NotificationPackSpotlightPreview item={sample} size="THUMBNAIL" />
                    ) : (
                      <PersonalizationVisual item={sample} small />
                    )
                  ) : (
                    <img
                      className="personalization-store-category-card__fallback-art"
                      src={personalizationIllustrations[type]}
                      alt=""
                      aria-hidden="true"
                    />
                  )}
                </span>
              </motion.button>
            )
          })}
        </section>
      )}
    </div>
  )
}
