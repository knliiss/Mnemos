import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  deactivateAccount,
  getAdminAccount,
  reactivateAccount,
  type AdminAccountDetailsResponse,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { Route } from "../../model"
import { PageHeader } from "../../shared/Primitives"
import { FeatureError } from "../../system/FeatureError"
import { FeatureLoading } from "../../system/FeatureLoading"
import { ModerationAccountControlPanel } from "./ModerationAccountControlPanel"
import { ModerationAccountRolesPanel } from "./ModerationAccountRolesPanel"
import { ModerationAccountTransactionPanel } from "./ModerationAccountTransactionPanel"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

export function ModerationAccount({ params, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const copy = resolveModerationDirectoryCopy(i18n.language)
  const accountId = params?.accountId ?? ""
  const [details, setDetails] = useState<AdminAccountDetailsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [statusBusy, setStatusBusy] = useState(false)

  const refresh = useCallback(async () => {
    if (!accountId) {
      setDetails(null)
      setLoadError(true)
      setLoading(false)
      return
    }

    setLoading(true)
    setLoadError(false)

    try {
      setDetails(await getAdminAccount(accountId))
    } catch (error) {
      setLoadError(true)
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [accountId])

  useEffect(() => {
    let active = true

    if (!accountId) {
      queueMicrotask(() => {
        if (active) {
          setDetails(null)
          setLoadError(true)
          setLoading(false)
        }
      })

      return () => {
        active = false
      }
    }

    void getAdminAccount(accountId)
      .then((accountDetails) => {
        if (active) {
          setDetails(accountDetails)
          setLoadError(false)
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setLoadError(true)
          handleError(error)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [accountId])

  const changeStatus = async () => {
    if (!details || details.status === "DELETED" || statusBusy) {
      return
    }

    setStatusBusy(true)

    try {
      if (details.status === "ACTIVE") {
        await deactivateAccount(details.accountId)
      } else {
        await reactivateAccount(details.accountId)
      }

      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setStatusBusy(false)
    }
  }

  if (loading && !details) {
    return (
      <div className="screen internal-user-screen">
        <PageHeader title={t("moderation.accounts")} back={back} />
        <FeatureLoading />
      </div>
    )
  }

  if (loadError || !details) {
    return <FeatureError back={back} onRetry={() => void refresh()} />
  }

  const owner = details.members.find((member) => member.owner) ?? null

  return (
    <div className="screen internal-user-screen">
      <PageHeader title={details.cristalixNickname} back={back} />

      <section className="internal-user-hero moderation-account-hero">
        <span className="internal-user-hero__avatar moderation-directory-account-mark">C</span>
        <span>
          <small>{t("moderation.accounts")}</small>
          <strong>{details.cristalixNickname}</strong>
          <em>{details.accountId}</em>
        </span>
        <span
          className={
            details.status === "ACTIVE" ? "internal-user-state" : "internal-user-state is-banned"
          }
        >
          {resolveStatusLabel(details.status, copy)}
        </span>
      </section>

      <section className="internal-info-grid">
        <article>
          <small>Cristalix UUID</small>
          <strong>{details.cristalixUuid}</strong>
        </article>
        <article>
          <small>{copy.owner}</small>
          {owner ? (
            <button
              type="button"
              className="moderation-account-owner-link"
              onClick={() => navigate({ id: "moderation-user", params: { userId: owner.userId } })}
            >
              @{owner.username}
            </button>
          ) : (
            <strong>—</strong>
          )}
        </article>
      </section>

      {details.status !== "DELETED" ? (
        <div className="moderation-account-status-actions">
          <button
            type="button"
            className="moderation-control-button moderation-control-button--neutral"
            disabled={statusBusy}
            onClick={() => void changeStatus()}
          >
            {statusBusy
              ? copy.changingAccountStatus
              : details.status === "ACTIVE"
                ? copy.deactivateAccount
                : copy.activateAccount}
          </button>
        </div>
      ) : null}

      <ModerationAccountControlPanel
        accountId={details.accountId}
        canDelete
        copy={copy}
        onChanged={refresh}
        onOpenUser={(userId) => navigate({ id: "moderation-user", params: { userId } })}
      />

      <ModerationAccountRolesPanel details={details} onChanged={refresh} />
      <ModerationAccountTransactionPanel accountId={details.accountId} />
    </div>
  )
}

function resolveStatusLabel(
  status: AdminAccountDetailsResponse["status"],
  copy: ReturnType<typeof resolveModerationDirectoryCopy>,
): string {
  if (status === "ACTIVE") {
    return copy.activeAccount
  }

  if (status === "DEACTIVATED") {
    return copy.deactivatedAccount
  }

  return copy.deletedAccount
}
