import { useState } from "react"
import type { Account } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, Field, PageHeader, Section } from "../shared/Primitives"

type InviteStatus = "ACTIVE" | "CONSUMED" | "REVOKED" | "EXPIRED"
type Expiry = "NONE" | "1H" | "1D" | "7D"

type Invite = {
  id: string
  code: string
  createdByUserId: string
  createdAt: string
  expiresAt?: string
  consumedAt?: string
  revokedAt?: string
  status: InviteStatus
}

type CommonProps = {
  activeAccount: Account
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  setActiveAccount: (account: Account) => void
  back: () => void
}

const alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

export function AccountInvitesFinal({ back }: Pick<CommonProps, "back">) {
  const [expiry, setExpiry] = useState<Expiry>("1D")
  const [invites, setInvites] = useState<Invite[]>([
    {
      id: "0198f6ce-e934-7000-8000-000000000801",
      code: "G7QK-9H2M-X4TR-6WNP",
      createdByUserId: "0198f6ce-e934-7000-8000-000000000001",
      createdAt: "сьогодні · 18:00",
      expiresAt: "завтра · 18:00",
      status: "ACTIVE",
    },
    {
      id: "0198f6ce-e934-7000-8000-000000000802",
      code: "K2TV-8MPQ-4RZH-7NXG",
      createdByUserId: "0198f6ce-e934-7000-8000-000000000001",
      createdAt: "16 серп. · 20:30",
      consumedAt: "16 серп. · 20:43",
      status: "CONSUMED",
    },
  ])

  const expiryMinutes: Record<Expiry, number | null> = {
    NONE: null,
    "1H": 60,
    "1D": 1_440,
    "7D": 10_080,
  }

  const createInvite = () => {
    const minutes = expiryMinutes[expiry]
    const invite: Invite = {
      id: crypto.randomUUID(),
      code: generateInviteCode(),
      createdByUserId: "0198f6ce-e934-7000-8000-000000000001",
      createdAt: "зараз",
      expiresAt: minutes === null ? undefined : expiry === "1H" ? "через 1 год" : expiry === "1D" ? "через 24 год" : "через 7 днів",
      status: "ACTIVE",
    }

    setInvites([invite, ...invites])
  }

  const revoke = (inviteId: string) => {
    setInvites(invites.map((invite) => invite.id === inviteId
      ? { ...invite, status: "REVOKED", revokedAt: "зараз" }
      : invite))
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
        <small>Backend: expiresInMinutes · 1–10080 або null</small>
        <button className="primary-action" onClick={createInvite}><Glyph name="plus" size={16} />Створити інвайт</button>
      </section>

      <Section title="Інвайти">
        <div className="invite-final-list">
          {invites.map((invite) => (
            <article key={invite.id} className={`invite-final-item invite-final-item--${invite.status.toLowerCase()}`}>
              <span className="invite-final-item__code">
                <strong>{invite.code}</strong>
                <small>{invite.expiresAt ? `до ${invite.expiresAt}` : "без строку"}</small>
              </span>
              <span className="invite-final-item__status">{inviteStatusLabel(invite.status)}</span>
              {invite.status === "ACTIVE" ? (
                <span className="invite-final-item__actions">
                  <button aria-label="Копіювати" onClick={() => navigator.clipboard?.writeText(invite.code)}><Glyph name="external" size={14} /></button>
                  <button aria-label="Відкликати" className="is-danger" onClick={() => revoke(invite.id)}><Glyph name="trash" size={14} /></button>
                </span>
              ) : null}
            </article>
          ))}
        </div>
      </Section>
    </div>
  )
}

export function AccountJoinFinal({ activeAccount, accounts, setAccounts, setActiveAccount, back }: CommonProps) {
  const [rawCode, setRawCode] = useState("")
  const normalized = normalizeInviteCode(rawCode)
  const valid = normalized.length === 16 && [...normalized].every((symbol) => alphabet.includes(symbol))
  const formatted = formatInviteCode(normalized)

  const join = () => {
    if (!valid) return

    const id = crypto.randomUUID()
    const joined: Account = {
      id,
      cristalixUuid: id,
      nickname: "north",
      role: "Учасник",
      skinUrl: activeAccount.skinUrl,
      memberCount: 5,
      maxMembers: 8,
      owner: false,
      active: true,
      coin: 0,
    }

    setAccounts([...accounts, joined])
    setActiveAccount(joined)
    back()
  }

  return (
    <div className="screen account-join-final">
      <PageHeader title="Приєднатись" back={back} />
      <section className="join-code-panel">
        <span className="join-code-panel__icon"><Glyph name="invite" size={22} /></span>
        <div><strong>Код інвайта</strong><small>16 символів · групи по 4</small></div>
      </section>
      <div className="form-stack account-create">
        <Field label="Код" hint={rawCode && !valid ? "A–Z без I/O, цифри 2–9 · 16 символів" : "XXXX-XXXX-XXXX-XXXX"}>
          <input
            autoFocus
            autoCapitalize="characters"
            autoCorrect="off"
            spellCheck={false}
            value={formatted}
            maxLength={19}
            onChange={(event) => setRawCode(event.target.value)}
            placeholder="XXXX-XXXX-XXXX-XXXX"
          />
        </Field>
        <button className="primary-action" disabled={!valid} onClick={join}>Приєднатись</button>
      </div>
    </div>
  )
}

function generateInviteCode() {
  const symbols = Array.from({ length: 16 }, () => alphabet[Math.floor(Math.random() * alphabet.length)]).join("")
  return formatInviteCode(symbols)
}

function normalizeInviteCode(value: string) {
  return value
    .trim()
    .toUpperCase()
    .replace(/[\s-]/g, "")
    .slice(0, 16)
}

function formatInviteCode(value: string) {
  return value.match(/.{1,4}/g)?.join("-") ?? value
}

function inviteStatusLabel(status: InviteStatus) {
  if (status === "ACTIVE") return "Активний"
  if (status === "CONSUMED") return "Використано"
  if (status === "REVOKED") return "Відкликано"
  return "Прострочено"
}
