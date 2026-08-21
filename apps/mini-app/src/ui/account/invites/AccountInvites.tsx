import { useTranslation } from "react-i18next"
import type { AccountInvite, AccountInviteStatus } from "@/shared/api/accountInviteApi"
import { Glyph } from "../../shared/MnemosIcon"
import { ChoiceGrid, Field, PageHeader, Section } from "../../shared/Primitives"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"
import "./account-invites.css"
import { type InviteExpiry, useAccountInvites } from "./useAccountInvites"

export function AccountInvites({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const {
    accountId,
    canManageInvites,
    expiry,
    setExpiry,
    invites,
    loading,
    loadError,
    creating,
    revokingId,
    refresh,
    createInvite,
    revokeInvite,
    copyInviteCode,
  } = useAccountInvites()
  const locale = localeForLanguage(i18n.resolvedLanguage)

  const expiryLabels: Record<InviteExpiry, string> = {
    NONE: t("accountInvites.expiryOptions.none"),
    "1H": t("accountInvites.expiryOptions.oneHour"),
    "1D": t("accountInvites.expiryOptions.oneDay"),
    "7D": t("accountInvites.expiryOptions.sevenDays"),
  }

  const formatDate = (value: string) => {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
      return value
    }

    return new Intl.DateTimeFormat(locale, {
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date)
  }

  const inviteExpiryLabel = (invite: AccountInvite) => {
    if (!invite.expiresAt) {
      return t("accountInvites.noExpiry")
    }

    if (invite.status === "CONSUMED" && invite.consumedAt) {
      return t("accountInvites.consumedAt", { date: formatDate(invite.consumedAt) })
    }

    if (invite.status === "REVOKED" && invite.revokedAt) {
      return t("accountInvites.revokedAt", { date: formatDate(invite.revokedAt) })
    }

    return t("accountInvites.expiresAt", { date: formatDate(invite.expiresAt) })
  }

  const inviteStatusLabel = (status: AccountInviteStatus) => t(`accountInvites.status.${status}`)

  return (
    <div className="screen invites-screen">
      <PageHeader title={t("accountInvites.title")} back={back} />

      <section className="invite-create-panel">
        <Field label={t("accountInvites.expiry")}>
          <ChoiceGrid
            value={expiry}
            options={["NONE", "1H", "1D", "7D"] as const}
            onChange={setExpiry}
            labels={expiryLabels}
          />
        </Field>

        <button
          type="button"
          className="primary-action"
          disabled={creating || !accountId || !canManageInvites}
          onClick={() => void createInvite()}
        >
          <Glyph name="plus" size={16} />
          {creating ? t("accountInvites.creating") : t("accountInvites.create")}
        </button>

        {!canManageInvites ? (
          <small className="inline-hint inline-hint--left">
            {t("accountInvites.unavailableCopy")}
          </small>
        ) : null}
      </section>

      <Section title={t("accountInvites.title")}>
        <div className="invite-list">
          {invites.map((invite) => (
            <article
              key={invite.id}
              className={`invite-item invite-item--${invite.status.toLowerCase()}`}
            >
              <span className="invite-item__code">
                <strong>{invite.code}</strong>
                <small>{inviteExpiryLabel(invite)}</small>
              </span>

              <span className="invite-item__status">{inviteStatusLabel(invite.status)}</span>

              {invite.status === "ACTIVE" ? (
                <span className="invite-item__actions">
                  <button
                    type="button"
                    aria-label={t("accountInvites.copy")}
                    onClick={() => void copyInviteCode(invite.code)}
                  >
                    <Glyph name="external" size={14} />
                  </button>

                  <button
                    type="button"
                    aria-label={t("accountInvites.revoke")}
                    className="is-danger"
                    disabled={revokingId !== null}
                    onClick={() => void revokeInvite(invite.id)}
                  >
                    {revokingId === invite.id ? <span>…</span> : <Glyph name="trash" size={14} />}
                  </button>
                </span>
              ) : null}
            </article>
          ))}

          {!loading && !canManageInvites ? (
            <FeatureEmpty
              icon="invite"
              title={t("accountInvites.unavailableTitle")}
              copy={t("accountInvites.unavailableCopy")}
            />
          ) : null}

          {canManageInvites && !loading && loadError ? (
            <FeatureEmpty
              icon="invite"
              title={t("common.loadErrorTitle")}
              copy={t("common.loadErrorCopy")}
            >
              <button type="button" onClick={() => void refresh()}>
                {t("common.retry")}
              </button>
            </FeatureEmpty>
          ) : null}

          {canManageInvites && !loading && !loadError && !invites.length ? (
            <FeatureEmpty
              icon="invite"
              title={t("accountInvites.emptyTitle")}
              copy={t("accountInvites.emptyCopy")}
            />
          ) : null}

          {loading && !invites.length ? (
            <FeatureLoading label={t("accountInvites.loading")} />
          ) : null}
        </div>
      </Section>
    </div>
  )
}

function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }

  if (language === "ru") {
    return "ru-RU"
  }

  return "uk-UA"
}
