import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { PersonalizationVisual } from "../../components/personalization/PersonalizationPreview"
import type { Personalization, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { PersonalizationLoadingState } from "../components/PersonalizationLoadingState"
import { PersonalizationTypeGlyph } from "../components/PersonalizationTypeGlyph"
import {
  accountPersonalizationTypes,
  personalizationTargets,
  personalizationTypeImpact,
  personalizationTypeNote,
  personalizationTypeTitle,
  readPersonalizationType,
} from "../personalizationPresentation"

type OwnedPersonalizationsProps = {
  params?: Record<string, string>
  items: Personalization[]
  loading: boolean
  busyId: string | null
  onToggleActive: (item: Personalization) => Promise<void>
  navigate: (route: Route) => void
  back: () => void
}

export function OwnedPersonalizations({
  params,
  items,
  loading,
  busyId,
  onToggleActive,
  navigate,
  back,
}: OwnedPersonalizationsProps) {
  const { t } = useTranslation()
  const target = params?.target === "ACCOUNT" || params?.target === "USER" ? params.target : null
  const type = readPersonalizationType(params?.type)

  if (target === "ACCOUNT" && type && personalizationTargets[type] === "ACCOUNT") {
    const typeItems = items.filter(
      (item) => item.owned && item.target === "ACCOUNT" && item.type === type,
    )

    return (
      <div className="screen account-personalization-category">
        <PageHeader title={personalizationTypeTitle(t, type)} back={back} />

        <section className="personalization-category-intro">
          <span className="personalization-category-icon">
            <PersonalizationTypeGlyph type={type} />
          </span>
          <span>
            <small>{t("personalization.owned.accountEyebrow")}</small>
            <strong>{personalizationTypeNote(t, type)}</strong>
            <em>{personalizationTypeImpact(t, type)}</em>
          </span>
        </section>

        {loading ? (
          <PersonalizationLoadingState />
        ) : (
          <section className="account-personalization-category-list">
            {typeItems.map((item) => (
              <motion.button
                type="button"
                key={item.id}
                className={item.active ? "is-selected" : ""}
                whileTap={{ scale: 0.992 }}
                disabled={busyId === item.id}
                onClick={() => void onToggleActive(item)}
              >
                <PersonalizationVisual item={item} />
                <span className="account-personalization-category-list__copy">
                  <small>
                    {item.active
                      ? t("personalization.owned.activeEyebrow")
                      : t("personalization.owned.purchasedEyebrow")}
                  </small>
                  <strong>{item.name}</strong>
                  <em>{item.creator}</em>
                </span>
                <span className="catalog-state">
                  {busyId === item.id
                    ? "…"
                    : item.active
                      ? t("personalization.owned.deselect")
                      : t("personalization.owned.select")}
                </span>
              </motion.button>
            ))}

            {!typeItems.length ? (
              <p className="inline-hint">{t("personalization.owned.typeEmpty")}</p>
            ) : null}
          </section>
        )}
      </div>
    )
  }

  if (target === "ACCOUNT") {
    const ownedItems = items.filter((item) => item.owned && item.target === "ACCOUNT")

    return (
      <div className="screen account-personalization-overview">
        <PageHeader title={t("personalization.owned.accountTitle")} back={back} />

        <section className="account-personalization-overview-hero">
          <span>
            <small>{t("personalization.owned.accountImageEyebrow")}</small>
            <strong>{t("personalization.owned.accountImageTitle")}</strong>
            <em>
              {t("personalization.owned.accountImageStats", {
                active: ownedItems.filter((item) => item.active).length,
                owned: ownedItems.length,
              })}
            </em>
          </span>
          <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
        </section>

        {loading ? (
          <PersonalizationLoadingState />
        ) : (
          <section className="account-personalization-overview-categories">
            {accountPersonalizationTypes.map((itemType) => {
              const typeItems = ownedItems.filter((item) => item.type === itemType)
              const selected = typeItems.find((item) => item.active) ?? null

              return (
                <motion.button
                  type="button"
                  key={itemType}
                  className={selected ? "is-selected" : ""}
                  whileTap={{ scale: 0.99 }}
                  onClick={() =>
                    navigate({
                      id: "personalization-owned",
                      params: {
                        target: "ACCOUNT",
                        type: itemType,
                      },
                    })
                  }
                >
                  <span className="account-personalization-overview-category__icon">
                    <PersonalizationTypeGlyph type={itemType} />
                  </span>

                  <span className="account-personalization-overview-category__copy">
                    <small>{personalizationTypeTitle(t, itemType)}</small>
                    <strong>{selected?.name ?? t("personalization.owned.notSelected")}</strong>
                    <em>{personalizationTypeNote(t, itemType)}</em>
                    <span>
                      {t("personalization.owned.purchasedCount", { count: typeItems.length })}
                    </span>
                  </span>

                  <span className="account-personalization-overview-category__preview">
                    {selected ? (
                      <PersonalizationVisual item={selected} small />
                    ) : (
                      <Glyph name="spark" size={18} />
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

  const visibleItems = items.filter((item) => item.owned && (!target || item.target === target))

  return (
    <div className="screen personalization-owned-screen">
      <PageHeader
        title={
          target === "USER"
            ? t("personalization.owned.userTitle")
            : t("personalization.owned.myTitle")
        }
        back={back}
      />

      {loading ? (
        <PersonalizationLoadingState />
      ) : (
        <section className="personalization-owned-list">
          {visibleItems.map((item) => (
            <motion.button
              type="button"
              key={item.id}
              whileTap={{ scale: 0.992 }}
              onClick={() =>
                navigate({
                  id: "personalization-detail",
                  params: { personalizationId: item.id },
                })
              }
            >
              <PersonalizationVisual item={item} small />
              <span>
                <small>{personalizationTypeTitle(t, item.type)}</small>
                <strong>{item.name}</strong>
                <em>
                  {item.active
                    ? t("personalization.owned.active")
                    : t("personalization.owned.purchased")}
                </em>
              </span>
            </motion.button>
          ))}

          {!visibleItems.length ? (
            <p className="inline-hint">{t("personalization.owned.empty")}</p>
          ) : null}
        </section>
      )}
    </div>
  )
}
