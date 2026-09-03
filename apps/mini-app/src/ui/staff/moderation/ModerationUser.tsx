import { useState } from "react"
import { useTranslation } from "react-i18next"
import {
  deactivateAccount,
  reactivateAccount,
  type AccountLifecycleStatus,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { PageHeader, Section } from "../../shared/Primitives"
import { FeatureError } from "../../system/FeatureError"
import { FeatureLoading } from "../../system/FeatureLoading"
import { ModerationAccountControlPanel } from "./ModerationAccountControlPanel"
import "./moderation-directory.css"
import { ModerationSanctions } from "./ModerationSanctions"
import { ModerationWalletAdjustments } from "./ModerationWalletAdjustments"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"
import { useModerationUser } from "./useModerationUser"

type Props = {
  params?: Record<string, string>
  back: () => void
}

export function ModerationUser({ params, back }: Props) {
  const { t, i18n } = useTranslation()
  const mnemos = useMnemosData()
  const copy = resolveModerationDirectoryCopy(i18n.language)
  const userId = params?.userId ?? ""
  const [accountStatusBusyId, setAccountStatusBusyId] = useState<string | null>(null)
  const [managedAccountId, setManagedAccountId] = useState<string | null>(null)
  const canAdjustWallet = mnemos.user.systemRoles.some(
    (role) => role === "ADMIN" || role === "SUPER_ADMIN",
  )
  const canManageAccounts = canAdjustWallet
  const canDeleteAccounts = mnemos.user.systemRoles.includes("SUPER_ADMIN")
  const canAdjustPrime = canDeleteAccounts
  const canSanction = Boolean(userId && mnemos.user.id && userId !== mnemos.user.id)
  const controller = useModerationUser({
    userId,
    canAdjustWallet,
    canAdjustPrime,
  })

  const changeAccountStatus = async (accountId: string, status: AccountLifecycleStatus) => {
    if (!canManageAccounts || accountStatusBusyId || status === "DELETED") {
      return
    }

    setAccountStatusBusyId(accountId)

    try {
      if (status === "ACTIVE") {
        await deactivateAccount(accountId)
      } else {
        await reactivateAccount(accountId)
      }

      await controller.refreshUser()
    } catch (error) {
      handleError(error)
    } finally {
      setAccountStatusBusyId(null)
    }
  }

  if (controller.loading) {
    return (
      <div className="screen internal-user-screen">
        <PageHeader title={t("moderation.title")} back={back} />
        <FeatureLoading />
      </div>
    )
  }

  if (controller.loadError) {
    return <FeatureError back={back} onRetry={() => void controller.refreshUser()} />
  }

  if (!controller.identity) {
    return (
      <div className="screen internal-user-screen">
        <PageHeader title={t("moderation.title")} back={back} />
        <p className="inline-hint">{t("moderation.userNotFound")}</p>
      </div>
    )
  }

  const identity = controller.identity

  return (
    <div className="screen internal-user-screen">
      <PageHeader title={`@${identity.username}`} back={back} />

      <section className="internal-user-hero">
        <span className="internal-user-hero__avatar internal-user-hero__avatar--mnemos">
          <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" aria-hidden="true" />
        </span>
        <span>
          <small>{t("moderation.user")}</small>
          <strong>@{identity.username}</strong>
          <em>{identity.userId}</em>
        </span>
        <span
          className={controller.activeBan ? "internal-user-state is-banned" : "internal-user-state"}
        >
          {controller.activeBan ? t("moderation.banned") : t("moderation.ok")}
        </span>
      </section>

      <section className="internal-info-grid">
        <article>
          <small>{t("moderation.telegramId")}</small>
          <strong>{identity.telegramId}</strong>
        </article>
        <article>
          <small>{t("moderation.accounts")}</small>
          <strong>{identity.accounts.length}</strong>
        </article>
      </section>

      <Section title={t("moderation.cristalixAccounts")}>
        <div className="internal-account-list">
          {identity.accounts.map((account) => {
            const statusBusy = accountStatusBusyId === account.accountId
            const statusActionLabel = statusBusy
              ? copy.changingAccountStatus
              : resolveAccountStatusActionLabel(account.status, copy)
            const managed = managedAccountId === account.accountId

            return (
              <article key={account.accountId} className={managed ? "is-managed" : ""}>
                <span className={account.status === "ACTIVE" ? "is-active" : ""}>
                  <i />
                </span>
                <div>
                  <strong>{account.cristalixNickname}</strong>
                  <small>{account.cristalixUuid}</small>
                  <em>{account.accountId}</em>
                </div>
                <span className="internal-account-list__actions">
                  <b className={`is-${account.status.toLowerCase()}`}>
                    {resolveAccountStatusLabel(account.status, copy)}
                  </b>
                  {canManageAccounts && account.status !== "DELETED" ? (
                    <button
                      type="button"
                      className="moderation-control-button moderation-control-button--neutral"
                      disabled={Boolean(accountStatusBusyId)}
                      onClick={() => void changeAccountStatus(account.accountId, account.status)}
                    >
                      {statusActionLabel}
                    </button>
                  ) : null}
                  {canManageAccounts ? (
                    <button
                      type="button"
                      className="moderation-control-button moderation-control-button--primary"
                      disabled={Boolean(accountStatusBusyId)}
                      onClick={() => setManagedAccountId(managed ? null : account.accountId)}
                    >
                      {managed ? copy.closeAccount : copy.manageAccount}
                    </button>
                  ) : null}
                </span>

                {managed ? (
                  <ModerationAccountControlPanel
                    accountId={account.accountId}
                    canDelete={canDeleteAccounts}
                    copy={copy}
                    onChanged={controller.refreshUser}
                  />
                ) : null}
              </article>
            )
          })}
        </div>
      </Section>

      <ModerationSanctions
        controller={controller}
        language={i18n.language}
        canSanction={canSanction}
      />

      {canAdjustWallet ? (
        <ModerationWalletAdjustments
          controller={controller}
          identity={identity}
          language={i18n.language}
          canAdjustPrime={canAdjustPrime}
        />
      ) : null}
    </div>
  )
}

function resolveAccountStatusActionLabel(
  status: AccountLifecycleStatus,
  copy: ReturnType<typeof resolveModerationDirectoryCopy>,
): string {
  return status === "ACTIVE" ? copy.deactivateAccount : copy.activateAccount
}

function resolveAccountStatusLabel(
  status: AccountLifecycleStatus,
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
