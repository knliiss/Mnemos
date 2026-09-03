import { useTranslation } from "react-i18next"
import type { Route } from "../../model"
import { PageHeader, Section } from "../../shared/Primitives"
import { FeatureError } from "../../system/FeatureError"
import { FeatureLoading } from "../../system/FeatureLoading"
import "./moderation-directory.css"
import { ModerationSanctions } from "./ModerationSanctions"
import { ModerationWalletAdjustments } from "./ModerationWalletAdjustments"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"
import { useModerationUser } from "./useModerationUser"
import { useMnemosData } from "../../data/MnemosDataProvider"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

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
          {identity.accounts.map((account) => (
            <button
              type="button"
              key={account.accountId}
              className="moderation-user-account-link"
              onClick={() =>
                navigate({ id: "moderation-account", params: { accountId: account.accountId } })
              }
            >
              <span className={account.status === "ACTIVE" ? "is-active" : ""}>
                <i />
              </span>
              <div>
                <strong>{account.cristalixNickname}</strong>
                <small>{account.cristalixUuid}</small>
                <em>{account.accountId}</em>
              </div>
              <b className={`is-${account.status.toLowerCase()}`}>
                {resolveAccountStatusLabel(account.status, copy)}
              </b>
              <span aria-hidden="true">›</span>
            </button>
          ))}
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
