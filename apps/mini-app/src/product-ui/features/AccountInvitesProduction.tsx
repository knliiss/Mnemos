import { useCallback, useEffect, useState } from "react"
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

export function AccountInvitesProduction({ back }: { back: () => void }) {
  const product = useProductData()
  const accountId = product.activeAccount?.id ?? null
  const [expiry, setExpiry] = useState<Expiry>("1D")
  const [invites, setInvites] = useState<AccountInvite[]>([])
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [revokingId, setRevokingId] = useState<string | null>(null)

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
    void refresh()
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

  return (
    <div className="screen invites-screen-redesign invite-final-screen">
      <PageHeader title="Інвайти" back={back} />

      <section className="invite-create-final">
        <Field label="Строк дії">
          <ChoiceGrid
            value={expiry}
            options={["NONE", "1H", "1D", "7D"] as const}
            onChange={setExpiry}
            labels={{ NONE: "Без строку", "1H": "1 година", "1D": "24 години", "7D": "7 днів" }}
          />
        </Field>
        <button className="primary-action" disabled={creating || !accountId} onClick={() => void create()}>
          <Glyph name="plus" size={16} />
          {creating ? "Створюємо…" : "Створити інвайт"}
        </button>
      </section>

      <Section title="Інвайти">
        <div className="invite-final-list">
          {invites.map((invite) => (
            <article key={invite.id} className={`invite-final-item invite-final-item--${invite.status.toLowerCase()}`}>
              <span className="invite-final-item__code">
                <strong>{invite.code}</strong>
                <small>{expiryLabel(invite)}</small>
              </span>
              <span className="invite-final-item__status">{inviteStatusLabel(invite.status)}</span>
              {invite.status === "ACTIVE" ? (
                <span className="invite-final-item__actions">
                  <button
                    aria-label="Копіювати"
                    onClick={() => void navigator.clipboard?.writeText(invite.code)}
                  >
                    <Glyph name="external" size={14} />
                  </button>
                  <button
                    aria-label="Відкликати"
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
              <strong>Інвайтів ще немає</strong>
              <small>Створи код і передай його учаснику.</small>
            </div>
          ) : null}

          {loading && !invites.length ? (
            <div className="events-r2-loading" aria-label="Завантаження інвайтів"><i /><i /><i /></div>
          ) : null}
        </div>
      </Section>
    </div>
  )
}

function expiryLabel(invite: AccountInvite) {
  if (!invite.expiresAt) return "без строку"

  if (invite.status === "CONSUMED" && invite.consumedAt) {
    return `використано ${formatDate(invite.consumedAt)}`
  }

  if (invite.status === "REVOKED" && invite.revokedAt) {
    return `відкликано ${formatDate(invite.revokedAt)}`
  }

  return `до ${formatDate(invite.expiresAt)}`
}

function formatDate(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat("uk-UA", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date)
}

function inviteStatusLabel(status: AccountInviteStatus) {
  if (status === "ACTIVE") return "Активний"
  if (status === "CONSUMED") return "Використано"
  if (status === "REVOKED") return "Відкликано"
  return "Прострочено"
}
