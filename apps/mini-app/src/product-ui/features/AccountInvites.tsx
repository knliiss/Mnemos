import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createAccountInvite,
  getAccountInvites,
  revokeAccountInvite,
  type AccountInvite,
  type AccountInviteStatus,
} from "@/shared/api/accountInviteApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useProductData } from "../data/ProductDataProvider"
import { Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, Field, PageHeader, Section } from "../shared/Primitives"

type Expiry = "NONE" | "1H" | "1D" | "7D"

const expiryMinutes: Record<Expiry, number | null> = {
  NONE: null,
  "1H": 60,
  "1D": 1_440,
  "7D": 10_080,
}

export function AccountInvites({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const product = useProductData()
  const accountId = product.activeAccount?.id ?? null
  const [expiry, setExpiry] = useState<Expiry>("1D")
  const [invites, setInvites] = useState<AccountInvite[]>([])
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [revokingId, setRevokingId] = useState<string | null>(null)
  const locale = localeForLanguage(i18n.resolvedLanguage)

  const refresh = useCallback(async () => {
    if (!accountId) {
      setInvites([])
      setLoading(false)
      return
    }

    setLoading(true)

    try {
      setInvites(await getAccountInvites(accountId))
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [accountId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refresh])

  const create = async () => {
    if (!accountId || creating) return

    setCreating(true)

    try {
      await createAccountInvite(accountId, expiryMinutes[expiry])
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setCreating(false)
    }
  }

  const revoke = async (inviteId: string) => {
    if (!accountId || revokingId) return

    setRevokingId(inviteId)

    try {
      await revokeAccountInvite(accountId, inviteId)
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setRevokingId(null)
    }
  }

  const expiryLabels: Record<Expiry, string> = {
    NONE: t("accountInvites.expiryOptions.none"),
    "1H": t("accountInvites.expiryOptions.oneHour"),
    "1D": t("accountInvites.expiryOptions.oneDay"),
    "7D": t("accountInvites.expiryOptions.sevenDays"),
  }

  const formatDate = (value: string) => {
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value

    return new Intl.DateTimeFormat(locale, {
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date)
  }

  const inviteExpiryLabel = (invite: AccountInvite) => {
    if (!invite.expiresAt) return t("accountInvites.noExpiry")

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
    <div className="screen invites-screen invites-screen">
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
          className="primary-action"
          disabled={creating || !accountId}
          onClick={() => void create()}
        >
          <Glyph name="plus" size={16} />
          {creating ? t("accountInvites.creating") : t("accountInvites.create")}
        </button>
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
                    aria-label={t("accountInvites.copy")}
                    onClick={() => void navigator.clipboard?.writeText(invite.code)}
                  >
                    <Glyph name="external" size={14} />
                  </button>
                  <button
                    aria-label={t("accountInvites.revoke")}
                    className="is-danger"
                    disabled={revokingId !== null}
                    onClick={() => void revoke(invite.id)}
                  >
                    {revokingId === invite.id ? <span>…</span> : <Glyph name="trash" size={14} />}
                  </button>
                </span>
              ) : null}
            </article>
          ))}

          {!loading && !invites.length ? (
            <div className="events-r2-empty">
              <Glyph name="invite" size={24} />
              <strong>{t("accountInvites.emptyTitle")}</strong>
              <small>{t("accountInvites.emptyCopy")}</small>
            </div>
          ) : null}

          {loading && !invites.length ? (
            <div className="events-r2-loading" aria-label={t("accountInvites.loading")}>
              <i />
              <i />
              <i />
            </div>
          ) : null}
        </div>
      </Section>
    </div>
  )
}

function localeForLanguage(language?: string) {
  if (language === "en") return "en-US"
  if (language === "ru") return "ru-RU"
  return "uk-UA"
}
