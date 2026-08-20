import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react"
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
import { FeatureEmpty } from "../system/FeatureEmpty"
import { FeatureLoading } from "../system/FeatureLoading"

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
  const [loadError, setLoadError] = useState(false)
  const [loadedAccountId, setLoadedAccountId] = useState<string | null>(null)
  const [creating, setCreating] = useState(false)
  const [revokingId, setRevokingId] = useState<string | null>(null)
  const requestRevision = useRef(0)
  const accountIdRef = useRef(accountId)
  const createLocked = useRef(false)
  const revokeLocked = useRef(false)
  const mountedRef = useRef(true)
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useLayoutEffect(() => {
    accountIdRef.current = accountId
  }, [accountId])

  const refresh = useCallback(async () => {
    const requestedAccountId = accountId
    const revision = requestRevision.current + 1

    requestRevision.current = revision

    if (!accountId) {
      if (accountIdRef.current === requestedAccountId) {
        setInvites([])
        setLoadedAccountId(null)
        setLoading(false)
      }
      return
    }

    setLoading(true)
    setLoadError(false)

    try {
      const nextInvites = await getAccountInvites(accountId)

      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setInvites(nextInvites)
        setLoadedAccountId(requestedAccountId)
      }
    } catch (error) {
      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setInvites([])
        setLoadedAccountId(requestedAccountId)
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setLoading(false)
      }
    }
  }, [accountId])

  useEffect(() => {
    mountedRef.current = true
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      mountedRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const create = async () => {
    if (!accountId || createLocked.current) {
      return
    }

    const requestedAccountId = accountId
    createLocked.current = true
    setCreating(true)

    try {
      await createAccountInvite(accountId, expiryMinutes[expiry])
      if (accountIdRef.current === requestedAccountId) {
        await refresh()
      }
    } catch (error) {
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        handleError(error)
      }
    } finally {
      createLocked.current = false
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        setCreating(false)
      }
    }
  }

  const revoke = async (inviteId: string) => {
    if (!accountId || revokeLocked.current) {
      return
    }

    const requestedAccountId = accountId
    revokeLocked.current = true
    setRevokingId(inviteId)

    try {
      await revokeAccountInvite(accountId, inviteId)
      if (accountIdRef.current === requestedAccountId) {
        await refresh()
      }
    } catch (error) {
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        handleError(error)
      }
    } finally {
      revokeLocked.current = false
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        setRevokingId(null)
      }
    }
  }

  const copyInviteCode = async (code: string) => {
    try {
      await navigator.clipboard.writeText(code)
    } catch (error) {
      handleError(error)
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
  const current = loadedAccountId === accountId
  const visibleInvites = current ? invites : []
  const visibleLoading = loading || !current

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
          disabled={creating || !accountId}
          onClick={() => void create()}
        >
          <Glyph name="plus" size={16} />
          {creating ? t("accountInvites.creating") : t("accountInvites.create")}
        </button>
      </section>

      <Section title={t("accountInvites.title")}>
        <div className="invite-list">
          {visibleInvites.map((invite) => (
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
                    onClick={() => void revoke(invite.id)}
                  >
                    {revokingId === invite.id ? <span>…</span> : <Glyph name="trash" size={14} />}
                  </button>
                </span>
              ) : null}
            </article>
          ))}

          {!visibleLoading && loadError ? (
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

          {!visibleLoading && !loadError && !visibleInvites.length ? (
            <FeatureEmpty
              icon="invite"
              title={t("accountInvites.emptyTitle")}
              copy={t("accountInvites.emptyCopy")}
            />
          ) : null}

          {visibleLoading && !visibleInvites.length ? (
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
