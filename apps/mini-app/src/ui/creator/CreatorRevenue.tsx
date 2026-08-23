import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  getCreatorRevenueOperations,
  getCreatorRevenueSummary,
  type CreatorRevenueOperationResponse,
  type CreatorRevenueSummaryResponse,
} from "@/shared/api/creatorPersonalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { CurrencyMark } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"
import { FeatureEmpty } from "../system/FeatureEmpty"
import { CreatorLoadingState } from "./CreatorLoadingState"
import { creatorLocale, formatCreatorOperationDate } from "./creatorPresentation"

type Props = {
  back: () => void
}

export function CreatorRevenue({ back }: Props) {
  const { t, i18n } = useTranslation()
  const [summary, setSummary] = useState<CreatorRevenueSummaryResponse | null>(null)
  const [operations, setOperations] = useState<CreatorRevenueOperationResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [reloadRevision, setReloadRevision] = useState(0)
  const locale = creatorLocale(i18n.resolvedLanguage)

  useEffect(() => {
    let cancelled = false
    const frame = window.requestAnimationFrame(() => {
      setLoading(true)
      setLoadError(false)

      void Promise.all([getCreatorRevenueSummary(), getCreatorRevenueOperations()])
        .then(([nextSummary, nextOperations]) => {
          if (!cancelled) {
            setSummary(nextSummary)
            setOperations(nextOperations)
          }
        })
        .catch((error) => {
          if (!cancelled) {
            setSummary(null)
            setOperations([])
            setLoadError(true)
            handleError(error)
          }
        })
        .finally(() => {
          if (!cancelled) {
            setLoading(false)
          }
        })
    })

    return () => {
      cancelled = true
      window.cancelAnimationFrame(frame)
    }
  }, [reloadRevision])

  return (
    <div className="screen creator-screen creator-revenue">
      <PageHeader title={t("creator.revenuePage.title")} back={back} />

      <section className="creator-revenue-hero">
        <span className="creator-revenue-hero__mark">
          <CurrencyMark currency="PRIME" size={22} />
        </span>

        <span className="creator-revenue-hero__copy">
          <small>{t("creator.revenuePage.confirmed")}</small>
          <CurrencyValue currency="PRIME" value={summary?.confirmedPrime ?? 0} />
          <em>
            {t("creator.revenuePage.successfulOperations", {
              count: summary?.confirmedOperations ?? 0,
            })}
          </em>
        </span>

        <div className="creator-sparkline" aria-hidden="true">
          <i />
          <i />
          <i />
          <i />
          <i />
          <i />
          <i />
        </div>
      </section>

      <section className="creator-revenue-stats">
        <article>
          <small>{t("creator.revenuePage.pending")}</small>
          <strong>{summary?.pendingPrime ?? 0}</strong>
          <em>PRIME</em>
        </article>

        <article>
          <small>{t("creator.revenuePage.confirmedOperations")}</small>
          <strong>{summary?.confirmedOperations ?? 0}</strong>
          <em>{t("creator.revenuePage.operations")}</em>
        </article>

        <article>
          <small>{t("creator.revenuePage.errors")}</small>
          <strong>{summary?.failedOperations ?? 0}</strong>
          <em>{summary?.failedPrime ?? 0} PRIME</em>
        </article>
      </section>

      <header className="creator-section-title">
        <span>
          <small>{t("creator.revenuePage.operationsEyebrow")}</small>
          <strong>{t("creator.revenuePage.recentActivity")}</strong>
        </span>
      </header>

      {loading ? (
        <CreatorLoadingState />
      ) : loadError ? (
        <FeatureEmpty
          icon="wallet"
          title={t("common.loadErrorTitle")}
          copy={t("common.loadErrorCopy")}
        >
          <button type="button" onClick={() => setReloadRevision((value) => value + 1)}>
            {t("common.retry")}
          </button>
        </FeatureEmpty>
      ) : (
        <section className="creator-revenue-log">
          {operations.map((operation) => (
            <article key={operation.id}>
              <i className={`is-${operation.status.toLowerCase()}`} />
              <span>
                <strong>{t("creator.revenuePage.sale")}</strong>
                <small>{formatCreatorOperationDate(operation.createdAt, locale)}</small>
              </span>
              <span>
                <CurrencyValue currency="PRIME" value={operation.amountPrime} compact />
                <small>{operation.status}</small>
              </span>
            </article>
          ))}

          {!operations.length ? (
            <p className="inline-hint">{t("creator.revenuePage.empty")}</p>
          ) : null}
        </section>
      )}
    </div>
  )
}
