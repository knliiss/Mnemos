import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createCollectorActivation,
  disableCollectorCredential,
  enableCollectorCredential,
  getCollectorPresence,
  listCollectorCredentials,
  revokeCollectorCredential,
  type CollectorActivationResponse,
  type CollectorCredentialResponse,
  type CollectorPresenceResponse,
} from "@/shared/api/collectorAdminApi"
import { handleError } from "@/shared/errors/errorHandler"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import {
  filterAndSortCollectors,
  collectorCredentialState,
  type CollectorFilter,
  type CollectorSort,
} from "./collectorAdminPresentation"
import { CollectorCredentialList } from "./CollectorCredentialList"

type CredentialMutation = (credentialId: string) => Promise<CollectorCredentialResponse>

const filters: CollectorFilter[] = ["ALL", "ENABLED", "DISABLED", "REVOKED"]
const sorts: CollectorSort[] = ["STATUS", "RECENT", "NAME"]

export function CollectorAdmin({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const [credentials, setCredentials] = useState<CollectorCredentialResponse[]>([])
  const [presence, setPresence] = useState<CollectorPresenceResponse | null>(null)
  const [activation, setActivation] = useState<CollectorActivationResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const [refreshing, setRefreshing] = useState(false)
  const [creatingActivation, setCreatingActivation] = useState(false)
  const [copied, setCopied] = useState(false)
  const [busyId, setBusyId] = useState<string | null>(null)
  const [revokeCandidate, setRevokeCandidate] = useState<string | null>(null)
  const [filter, setFilter] = useState<CollectorFilter>("ALL")
  const [sort, setSort] = useState<CollectorSort>("STATUS")
  const [query, setQuery] = useState("")
  const requestRevision = useRef(0)

  const dateFormatter = useMemo(
    () =>
      new Intl.DateTimeFormat(i18n.language, {
        dateStyle: "medium",
        timeStyle: "short",
      }),
    [i18n.language],
  )

  const refresh = useCallback(async (background = false) => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision

    if (background) {
      setRefreshing(true)
    } else {
      setLoading(true)
    }

    setError(false)

    try {
      const [nextCredentials, nextPresence] = await Promise.all([
        listCollectorCredentials(),
        getCollectorPresence(),
      ])

      if (revision !== requestRevision.current) {
        return
      }

      setCredentials(nextCredentials)
      setPresence(nextPresence)
    } catch (refreshError) {
      if (revision === requestRevision.current) {
        setError(true)
        handleError(refreshError)
      }
    } finally {
      if (revision === requestRevision.current) {
        setLoading(false)
        setRefreshing(false)
      }
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const counts = useMemo(() => {
    const enabled = credentials.filter(
      (credential) => collectorCredentialState(credential) === "ENABLED",
    ).length
    const disabled = credentials.filter(
      (credential) => collectorCredentialState(credential) === "DISABLED",
    ).length
    const revoked = credentials.filter(
      (credential) => collectorCredentialState(credential) === "REVOKED",
    ).length

    return {
      total: credentials.length,
      enabled,
      disabled,
      revoked,
    }
  }, [credentials])

  const visibleCredentials = useMemo(
    () => filterAndSortCollectors(credentials, filter, sort, query),
    [credentials, filter, query, sort],
  )

  const createActivation = async () => {
    if (creatingActivation) {
      return
    }

    setCreatingActivation(true)
    setCopied(false)

    try {
      setActivation(await createCollectorActivation())
    } catch (activationError) {
      handleError(activationError)
    } finally {
      setCreatingActivation(false)
    }
  }

  const copyActivationToken = async () => {
    if (!activation) {
      return
    }

    try {
      await navigator.clipboard.writeText(activation.token)
      setCopied(true)
    } catch (copyError) {
      handleError(copyError)
    }
  }

  const mutateCredential = async (credentialId: string, mutation: CredentialMutation) => {
    if (busyId) {
      return
    }

    setBusyId(credentialId)

    try {
      const updated = await mutation(credentialId)

      setCredentials((current) =>
        current.map((credential) => (credential.id === updated.id ? updated : credential)),
      )

      try {
        setPresence(await getCollectorPresence())
      } catch (presenceError) {
        handleError(presenceError)
      }
    } catch (mutationError) {
      handleError(mutationError)
    } finally {
      setBusyId(null)
      setRevokeCandidate(null)
    }
  }

  return (
    <div className="screen collector-admin-screen">
      <PageHeader title={t("collectorAdmin.title")} back={back} />

      <section className="collector-admin-hero">
        <div className="collector-admin-hero__copy">
          <span>{t("collectorAdmin.eyebrow")}</span>
          <strong>{t("collectorAdmin.heroTitle")}</strong>
          <p>{t("collectorAdmin.heroDescription")}</p>

          <button
            type="button"
            disabled={creatingActivation}
            onClick={() => void createActivation()}
          >
            <Glyph name="plus" size={16} />
            {creatingActivation
              ? t("collectorAdmin.creatingActivation")
              : t("collectorAdmin.createActivation")}
          </button>
        </div>

        <span className="collector-admin-hero__art" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
        </span>
      </section>

      {activation ? (
        <section className="collector-activation-card">
          <header>
            <span>
              <small>{t("collectorAdmin.activationEyebrow")}</small>
              <strong>{t("collectorAdmin.activationTitle")}</strong>
            </span>
            <time dateTime={activation.expiresAt}>
              {t("collectorAdmin.expires", {
                time: dateFormatter.format(new Date(activation.expiresAt)),
              })}
            </time>
          </header>

          <button
            type="button"
            className="collector-activation-token"
            onClick={() => void copyActivationToken()}
          >
            <code>{activation.token}</code>
            <span>
              <Glyph name={copied ? "check" : "external"} size={15} />
              {copied ? t("collectorAdmin.copied") : t("collectorAdmin.copy")}
            </span>
          </button>

          <p>{t("collectorAdmin.activationHint")}</p>
        </section>
      ) : null}

      <section className="collector-admin-stats" aria-label={t("collectorAdmin.statsAria")}>
        <article>
          <small>{t("collectorAdmin.total")}</small>
          <strong>{counts.total}</strong>
        </article>
        <article className="is-positive">
          <small>{t("collectorAdmin.enabled")}</small>
          <strong>{counts.enabled}</strong>
        </article>
        <article>
          <small>{t("collectorAdmin.disabled")}</small>
          <strong>{counts.disabled}</strong>
        </article>
        <article className="is-live">
          <small>{t("collectorAdmin.activeObservers")}</small>
          <strong>{presence?.activeCollectors ?? "—"}</strong>
        </article>
      </section>

      <section className="collector-admin-directory">
        <header>
          <span>
            <h2>{t("collectorAdmin.directoryTitle")}</h2>
            <small>{t("collectorAdmin.directoryDescription")}</small>
          </span>
          <button
            type="button"
            className="collector-refresh"
            disabled={refreshing || loading}
            onClick={() => void refresh(true)}
          >
            <Glyph name="swap" size={15} />
            {refreshing ? t("collectorAdmin.refreshing") : t("collectorAdmin.refresh")}
          </button>
        </header>

        <div className="collector-admin-controls">
          <label className="collector-admin-search">
            <Glyph name="search" size={17} />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder={t("collectorAdmin.searchPlaceholder")}
            />
          </label>

          <label className="collector-admin-sort">
            <span>{t("collectorAdmin.sortLabel")}</span>
            <select value={sort} onChange={(event) => setSort(event.target.value as CollectorSort)}>
              {sorts.map((option) => (
                <option key={option} value={option}>
                  {t(`collectorAdmin.sort.${option.toLowerCase()}`)}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="collector-admin-filters" aria-label={t("collectorAdmin.filterAria")}>
          {filters.map((option) => (
            <button
              key={option}
              type="button"
              className={filter === option ? "is-active" : undefined}
              onClick={() => setFilter(option)}
            >
              {t(`collectorAdmin.filter.${option.toLowerCase()}`)}
              {option === "ALL" ? <b>{counts.total}</b> : null}
              {option === "ENABLED" ? <b>{counts.enabled}</b> : null}
              {option === "DISABLED" ? <b>{counts.disabled}</b> : null}
              {option === "REVOKED" ? <b>{counts.revoked}</b> : null}
            </button>
          ))}
        </div>

        {loading ? <FeatureLoading label={t("collectorAdmin.loading")} /> : null}

        {!loading && error ? (
          <div className="collector-admin-empty">
            <img src="/ui-illustrations/mnemos-mascot-404.svg" alt="" aria-hidden="true" />
            <strong>{t("collectorAdmin.errorTitle")}</strong>
            <small>{t("collectorAdmin.errorCopy")}</small>
            <button type="button" onClick={() => void refresh()}>
              {t("collectorAdmin.retry")}
            </button>
          </div>
        ) : null}

        {!loading && !error && visibleCredentials.length ? (
          <CollectorCredentialList
            credentials={visibleCredentials}
            busyId={busyId}
            revokeCandidate={revokeCandidate}
            dateFormatter={dateFormatter}
            onEnable={(credentialId) =>
              void mutateCredential(credentialId, enableCollectorCredential)
            }
            onDisable={(credentialId) =>
              void mutateCredential(credentialId, disableCollectorCredential)
            }
            onRequestRevoke={setRevokeCandidate}
            onCancelRevoke={() => setRevokeCandidate(null)}
            onConfirmRevoke={(credentialId) =>
              void mutateCredential(credentialId, revokeCollectorCredential)
            }
          />
        ) : null}

        {!loading && !error && !visibleCredentials.length ? (
          <div className="collector-admin-empty is-compact">
            <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
            <strong>{t("collectorAdmin.emptyTitle")}</strong>
            <small>{t("collectorAdmin.emptyCopy")}</small>
          </div>
        ) : null}
      </section>
    </div>
  )
}
