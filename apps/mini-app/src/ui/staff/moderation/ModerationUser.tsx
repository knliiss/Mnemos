import { useState } from "react"
import { useTranslation } from "react-i18next"
import type { Route } from "../../model"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { PageHeader } from "../../shared/Primitives"
import { FeatureError } from "../../system/FeatureError"
import { FeatureLoading } from "../../system/FeatureLoading"
import "./moderation-detail.css"
import "./moderation-directory.css"
import { ModerationDetailTabs } from "./ModerationDetailTabs"
import { ModerationSanctions } from "./ModerationSanctions"
import { ModerationWalletAdjustments } from "./ModerationWalletAdjustments"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"
import { useModerationUser } from "./useModerationUser"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type UserTab = "overview" | "sanctions" | "wallet"

export function ModerationUser({ params, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const mnemos = useMnemosData()
  const copy = resolveModerationDirectoryCopy(i18n.language)
  const userId = params?.userId ?? ""
  const canAdjustWallet = mnemos.user.systemRoles.some(
    (role) => role === "ADMIN" || role === "SUPER_ADMIN",
  )
  const canAdjustPrime = mnemos.user.systemRoles.includes("SUPER_ADMIN")
  const canSanction = Boolean(userId && mnemos.user.id && userId !== mnemos.user.id)
  const controller = useModerationUser({ userId, canAdjustWallet, canAdjustPrime })
  const [activeTab, setActiveTab] = useState<UserTab>("overview")

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
  const activeSanctions = controller.sanctions.filter((sanction) => sanction.status === "ACTIVE").length
  const tabs = [
    { id: "overview", label: copy.overviewTab },
    { id: "sanctions", label: copy.sanctionsTab, badge: activeSanctions },
    ...(canAdjustWallet ? [{ id: "wallet", label: copy.walletTab }] : []),
  ]

  return (
    <div className="screen internal-user-screen moderation-detail-page">
      <PageHeader title={`@${identity.username}`} back={back} />

      <section className="moderation-detail-hero">
        <span className="moderation-detail-hero__avatar" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
        </span>
        <span className="moderation-detail-hero__identity">
          <small>{t("moderation.user")}</small>
          <strong>@{identity.username}</strong>
          <em>{identity.userId}</em>
        </span>
        <span className={`moderation-detail-state ${controller.activeBan ? "is-danger" : ""}`}>
          {controller.activeBan ? t("moderation.banned") : t("moderation.ok")}
        </span>
      </section>

      <ModerationDetailTabs
        tabs={tabs}
        activeTab={activeTab}
        ariaLabel={copy.userOverview}
        onChange={(tabId) => setActiveTab(tabId as UserTab)}
      />

      <div className="moderation-detail-content">
        {activeTab === "overview" ? (
          <UserOverview
            identity={identity}
            activeSanctions={activeSanctions}
            navigate={navigate}
            copy={copy}
            telegramLabel={t("moderation.telegramId")}
            accountsLabel={t("moderation.cristalixAccounts")}
            sanctionsLabel={copy.sanctionsTab}
            emptyAccountsLabel={t("moderation.noAccounts", "No linked Cristalix accounts")}
          />
        ) : null}

        {activeTab === "sanctions" ? (
          <ModerationSanctions
            controller={controller}
            language={i18n.language}
            canSanction={canSanction}
          />
        ) : null}

        {activeTab === "wallet" && canAdjustWallet ? (
          <ModerationWalletAdjustments
            controller={controller}
            identity={identity}
            language={i18n.language}
            canAdjustPrime={canAdjustPrime}
          />
        ) : null}
      </div>
    </div>
  )
}

type UserOverviewProps = {
  identity: NonNullable<ReturnType<typeof useModerationUser>["identity"]>
  activeSanctions: number
  navigate: (route: Route) => void
  copy: ReturnType<typeof resolveModerationDirectoryCopy>
  telegramLabel: string
  accountsLabel: string
  sanctionsLabel: string
  emptyAccountsLabel: string
}

function UserOverview({
  identity,
  activeSanctions,
  navigate,
  copy,
  telegramLabel,
  accountsLabel,
  sanctionsLabel,
  emptyAccountsLabel,
}: UserOverviewProps) {
  return (
    <div className="moderation-detail-overview">
      <div className="moderation-detail-stat-grid">
        <article className="moderation-detail-stat">
          <small>{telegramLabel}</small>
          <strong>{identity.telegramId}</strong>
        </article>
        <article className="moderation-detail-stat">
          <small>{accountsLabel}</small>
          <strong>{identity.accounts.length}</strong>
        </article>
        <article className="moderation-detail-stat">
          <small>{sanctionsLabel}</small>
          <strong>{activeSanctions}</strong>
        </article>
        <article className="moderation-detail-stat">
          <small>User ID</small>
          <strong>{identity.userId}</strong>
        </article>
      </div>

      <section className="moderation-detail-section">
        <header className="moderation-detail-section__head">
          <span>
            <small>{copy.userOverview}</small>
            <strong>{accountsLabel}</strong>
          </span>
          <em>{identity.accounts.length}</em>
        </header>

        {identity.accounts.length > 0 ? (
          <div className="moderation-detail-account-list">
            {identity.accounts.map((account) => (
              <button
                type="button"
                key={account.accountId}
                className="moderation-detail-account-link"
                onClick={() =>
                  navigate({ id: "moderation-account", params: { accountId: account.accountId } })
                }
              >
                <span
                  className={`moderation-detail-account-link__dot ${
                    account.status === "ACTIVE" ? "is-active" : ""
                  }`}
                  aria-hidden="true"
                />
                <span>
                  <strong>{account.cristalixNickname}</strong>
                  <small>{account.cristalixUuid}</small>
                  <em>{account.accountId}</em>
                </span>
                <em
                  className={`moderation-detail-account-link__status is-${account.status.toLowerCase()}`}
                >
                  {resolveAccountStatusLabel(account.status, copy)}
                </em>
                <span aria-hidden="true">›</span>
              </button>
            ))}
          </div>
        ) : (
          <div className="moderation-detail-empty">{emptyAccountsLabel}</div>
        )}
      </section>
    </div>
  )
}

function resolveAccountStatusLabel(
  status: "ACTIVE" | "DEACTIVATED" | "DELETED",
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
