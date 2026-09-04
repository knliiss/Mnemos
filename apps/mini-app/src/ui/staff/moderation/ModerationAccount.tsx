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
import "./moderation-account-control-polish.css"
import "./moderation-detail.css"
import "./moderation-directory.css"
import { ModerationAccountControlPanel } from "./ModerationAccountControlPanel"
import { ModerationAccountPersonalizationPanel } from "./ModerationAccountPersonalizationPanel"
import { ModerationAccountRolesPanel } from "./ModerationAccountRolesPanel"
import { ModerationAccountTransactionPanel } from "./ModerationAccountTransactionPanel"
import { ModerationDetailTabs } from "./ModerationDetailTabs"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type AccountTab =
  | "overview"
  | "members"
  | "roles"
  | "transactions"
  | "personalizations"
  | "settings"

export function ModerationAccount({ params, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const copy = resolveModerationDirectoryCopy(i18n.language)
  const accountId = params?.accountId ?? ""
  const [details, setDetails] = useState<AdminAccountDetailsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [statusBusy, setStatusBusy] = useState(false)
  const [statusConfirmationVisible, setStatusConfirmationVisible] = useState(false)
  const [activeTab, setActiveTab] = useState<AccountTab>("overview")

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

      setStatusConfirmationVisible(false)
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
  const deactivating = details.status === "ACTIVE"
  const tabs = [
    { id: "overview", label: copy.overviewTab },
    { id: "members", label: copy.membersTab, badge: details.members.length },
    { id: "roles", label: copy.rolesTab },
    { id: "transactions", label: copy.transactionsTab },
    { id: "personalizations", label: copy.personalizationsTab },
    { id: "settings", label: copy.settingsTab },
  ]

  return (
    <div className="screen internal-user-screen moderation-detail-page">
      <PageHeader title={details.cristalixNickname} back={back} />

      <section className="moderation-detail-hero is-account">
        <span className="moderation-detail-hero__avatar" aria-hidden="true">
          C
        </span>
        <span className="moderation-detail-hero__identity">
          <small>{t("moderation.accounts")}</small>
          <strong>{details.cristalixNickname}</strong>
          <em>{details.accountId}</em>
        </span>
        <span className={`moderation-detail-state is-${details.status.toLowerCase()}`}>
          {resolveStatusLabel(details.status, copy)}
        </span>
      </section>

      <ModerationDetailTabs
        tabs={tabs}
        activeTab={activeTab}
        ariaLabel={copy.accountOverview}
        onChange={(tabId) => setActiveTab(tabId as AccountTab)}
      />

      <div className="moderation-detail-content">
        {activeTab === "overview" ? (
          <AccountOverview details={details} owner={owner} navigate={navigate} copy={copy} />
        ) : null}

        {activeTab === "members" ? (
          <ModerationAccountControlPanel
            mode="MEMBERS"
            accountId={details.accountId}
            canDelete
            copy={copy}
            onChanged={refresh}
            onOpenUser={(userId) => navigate({ id: "moderation-user", params: { userId } })}
          />
        ) : null}

        {activeTab === "roles" ? (
          <ModerationAccountRolesPanel details={details} onChanged={refresh} />
        ) : null}

        {activeTab === "transactions" ? (
          <ModerationAccountTransactionPanel accountId={details.accountId} />
        ) : null}

        {activeTab === "personalizations" ? (
          <ModerationAccountPersonalizationPanel
            accountId={details.accountId}
            deleted={details.status === "DELETED"}
          />
        ) : null}

        {activeTab === "settings" ? (
          <div className="moderation-detail-settings">
            <section className="moderation-detail-settings__status">
              <span>
                <strong>{copy.accountSettings}</strong>
                <small>{copy.accountSettingsHint}</small>
              </span>

              {details.status !== "DELETED" ? (
                <AccountStatusAction
                  copy={copy}
                  deactivating={deactivating}
                  statusBusy={statusBusy}
                  confirmationVisible={statusConfirmationVisible}
                  onRequestConfirmation={() => setStatusConfirmationVisible(true)}
                  onCancel={() => setStatusConfirmationVisible(false)}
                  onConfirm={() => void changeStatus()}
                />
              ) : (
                <div className="moderation-detail-empty">{copy.deletedAccount}</div>
              )}
            </section>

            <ModerationAccountControlPanel
              mode="SETTINGS"
              accountId={details.accountId}
              canDelete
              copy={copy}
              onChanged={refresh}
              onOpenUser={(userId) => navigate({ id: "moderation-user", params: { userId } })}
            />
          </div>
        ) : null}
      </div>
    </div>
  )
}

type AccountOverviewProps = {
  details: AdminAccountDetailsResponse
  owner: AdminAccountDetailsResponse["members"][number] | null
  navigate: (route: Route) => void
  copy: ReturnType<typeof resolveModerationDirectoryCopy>
}

function AccountOverview({ details, owner, navigate, copy }: AccountOverviewProps) {
  return (
    <div className="moderation-detail-overview">
      <div className="moderation-detail-stat-grid">
        <article className="moderation-detail-stat">
          <small>Cristalix UUID</small>
          <strong>{details.cristalixUuid}</strong>
        </article>
        <article className="moderation-detail-stat">
          <small>{copy.owner}</small>
          {owner ? (
            <button
              type="button"
              onClick={() => navigate({ id: "moderation-user", params: { userId: owner.userId } })}
            >
              @{owner.username}
            </button>
          ) : (
            <strong>—</strong>
          )}
        </article>
        <article className="moderation-detail-stat">
          <small>{copy.membersTab}</small>
          <strong>
            {details.members.length}/{details.maxMembers}
          </strong>
        </article>
        <article className="moderation-detail-stat">
          <small>{copy.settingsTab}</small>
          <strong>{resolveStatusLabel(details.status, copy)}</strong>
        </article>
      </div>

      <section className="moderation-detail-section">
        <header className="moderation-detail-section__head">
          <span>
            <small>{copy.accountOverview}</small>
            <strong>{details.cristalixNickname}</strong>
          </span>
          <em>{details.members.length}</em>
        </header>
        <div className="moderation-detail-empty">{copy.accountOverviewHint}</div>
      </section>
    </div>
  )
}

type AccountStatusActionProps = {
  copy: ReturnType<typeof resolveModerationDirectoryCopy>
  deactivating: boolean
  statusBusy: boolean
  confirmationVisible: boolean
  onRequestConfirmation(): void
  onCancel(): void
  onConfirm(): void
}

function AccountStatusAction({
  copy,
  deactivating,
  statusBusy,
  confirmationVisible,
  onRequestConfirmation,
  onCancel,
  onConfirm,
}: AccountStatusActionProps) {
  const { t } = useTranslation()

  if (!confirmationVisible) {
    return (
      <button
        type="button"
        className={
          deactivating
            ? "moderation-control-button moderation-control-button--danger"
            : "moderation-control-button moderation-control-button--primary"
        }
        disabled={statusBusy}
        onClick={onRequestConfirmation}
      >
        {deactivating ? copy.deactivateAccount : copy.activateAccount}
      </button>
    )
  }

  return (
    <div className={`moderation-account-status-confirmation ${deactivating ? "is-danger" : ""}`}>
      <span>
        <strong>
          {deactivating
            ? t("moderation.confirmDeactivateAccount", "Deactivate this account?")
            : t("moderation.confirmActivateAccount", "Reactivate this account?")}
        </strong>
        <small>
          {deactivating
            ? t(
                "moderation.deactivateAccountWarning",
                "Members will keep historical data, but this account cannot be used until reactivated.",
              )
            : t(
                "moderation.activateAccountWarning",
                "The account becomes usable immediately after reactivation.",
              )}
        </small>
      </span>
      <div>
        <button
          type="button"
          className={
            deactivating
              ? "moderation-control-button moderation-control-button--danger"
              : "moderation-control-button moderation-control-button--primary"
          }
          disabled={statusBusy}
          onClick={onConfirm}
        >
          {statusBusy ? copy.changingAccountStatus : t("common.confirm", "Confirm")}
        </button>
        <button
          type="button"
          className="moderation-control-button moderation-control-button--neutral"
          disabled={statusBusy}
          onClick={onCancel}
        >
          {copy.cancel}
        </button>
      </div>
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
