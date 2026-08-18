import { useMemo, useState } from "react"
import type { CSSProperties } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { initialMembers, initialRoles } from "../mockData"
import type { Account, AccountRole, Member, Route } from "../model"
import { CurrencyValue, PageHeader } from "../shared/Primitives"
import { Glyph } from "../shared/MnemosIcon"

type Props = {
  activeAccount: Account
  setActiveAccount: (account: Account) => void
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  navigate: (route: Route) => void
  back: () => void
}

type ConfirmAction = "TRANSFER" | "DEACTIVATE" | "LEAVE" | null

export function AccountSettingsFinal({
  activeAccount,
  setActiveAccount,
  accounts,
  setAccounts,
  navigate,
  back,
}: Props) {
  const availableRoles = useMemo(() => initialRoles.filter((role) => !role.locked), [])
  const transferableMembers = useMemo(() => initialMembers.filter((member) => !member.owner), [])
  const [baseRoleId, setBaseRoleId] = useState(availableRoles[availableRoles.length - 1]?.id ?? "")
  const [transferTo, setTransferTo] = useState(transferableMembers[0]?.id ?? "")
  const [confirmAction, setConfirmAction] = useState<ConfirmAction>(null)
  const [savedRoleId, setSavedRoleId] = useState(baseRoleId)
  const active = activeAccount.active !== false
  const baseRole = availableRoles.find((role) => role.id === baseRoleId)
  const transferTarget = transferableMembers.find((member) => member.id === transferTo)
  const roleChanged = baseRoleId !== savedRoleId

  const patchAccount = (patch: Partial<Account>) => {
    const updated = { ...activeAccount, ...patch }

    setAccounts(accounts.map((account) => account.id === activeAccount.id ? updated : account))
    setActiveAccount(updated)
  }

  const transfer = () => {
    if (!activeAccount.owner || !transferTarget) return

    patchAccount({ owner: false, role: "Учасник" })
    setConfirmAction(null)
  }

  const deactivate = () => {
    if (!activeAccount.owner || !active) return

    const deactivated = { ...activeAccount, active: false }
    const updatedAccounts = accounts.map((account) => account.id === activeAccount.id ? deactivated : account)
    const next = updatedAccounts.find((account) => account.id !== activeAccount.id && account.active !== false)

    setAccounts(updatedAccounts)
    if (next) setActiveAccount(next)
    navigate({ id: "account" })
  }

  const reactivate = () => {
    if (!activeAccount.owner || active) return

    patchAccount({ active: true })
    setConfirmAction(null)
  }

  const leave = () => {
    if (activeAccount.owner) return

    const remaining = accounts.filter((account) => account.id !== activeAccount.id)
    const next = remaining.find((account) => account.active !== false)

    setAccounts(remaining)
    if (next) setActiveAccount(next)
    navigate({ id: "account" })
  }

  return (
    <div className="screen account-settings-v2">
      <PageHeader title="Налаштування аккаунта" back={back} />

      <section className="account-settings-hero-v2">
        <CristalixHead skinUrl={activeAccount.skinUrl} size={58} />
        <span className="account-settings-hero-v2__copy">
          <small>CRISTALIX ACCOUNT</small>
          <strong>{activeAccount.nickname}</strong>
          <em>{activeAccount.role} · {active ? "активний" : "деактивований"}</em>
        </span>
        <span className={`account-settings-status-v2 ${active ? "is-active" : "is-inactive"}`}>
          <i />
          {active ? "ACTIVE" : "INACTIVE"}
        </span>
        <div className="account-settings-hero-v2__stats">
          <span><small>Учасники</small><strong>{activeAccount.memberCount}/{activeAccount.maxMembers}</strong></span>
          <span><small>Баланс</small><CurrencyValue currency="COIN" value={activeAccount.coin} compact /></span>
        </div>
      </section>

      <nav className="account-settings-shortcuts">
        <button onClick={() => navigate({ id: "roles" })}>
          <span className="is-plum"><Glyph name="roles" size={18} /></span>
          <strong>Ролі</strong>
          <small>Права та ієрархія</small>
          <b>›</b>
        </button>
        <button onClick={() => navigate({ id: "account-invites" })}>
          <span className="is-amber"><Glyph name="invite" size={18} /></span>
          <strong>Інвайти</strong>
          <small>Запрошення</small>
          <b>›</b>
        </button>
        <button onClick={() => navigate({ id: "account-wallet" })}>
          <span className="is-lime"><Glyph name="wallet" size={18} /></span>
          <strong>Гаманець</strong>
          <small>Історія COIN</small>
          <b>›</b>
        </button>
      </nav>

      <section className="account-settings-panel-v2">
        <header>
          <span>
            <small>ДОСТУП</small>
            <strong>Роль нових учасників</strong>
            <em>Визначає базові права для тих, хто приєднається до аккаунта.</em>
          </span>
          {roleChanged ? <b className="is-unsaved">Не збережено</b> : <b className="is-saved"><Glyph name="check" size={12} />Збережено</b>}
        </header>

        <div className="account-default-role-grid">
          {availableRoles.map((role) => (
            <RoleOption key={role.id} role={role} selected={role.id === baseRoleId} onClick={() => setBaseRoleId(role.id)} />
          ))}
        </div>

        <div className="account-role-summary">
          <span style={{ "--role-color": baseRole?.color ?? "#8A877F" } as CSSProperties}><i /></span>
          <span>
            <small>Нові учасники отримають</small>
            <strong>{baseRole?.name ?? "Роль не вибрана"}</strong>
            <em>{countEnabledPermissions(baseRole)} дозволів із {Object.keys(baseRole?.permissions ?? {}).length}</em>
          </span>
          <button disabled={!roleChanged} onClick={() => setSavedRoleId(baseRoleId)}>Зберегти</button>
        </div>
      </section>

      {activeAccount.owner ? (
        <section className="account-settings-panel-v2 account-owner-panel-v2">
          <header>
            <span>
              <small>ВЛАСНІСТЬ</small>
              <strong>Передача аккаунта</strong>
              <em>Новий власник отримає повний контроль. Ця дія змінює твою роль.</em>
            </span>
          </header>

          <div className="account-transfer-grid-v2">
            {transferableMembers.map((member) => (
              <MemberOption key={member.id} member={member} selected={member.id === transferTo} onClick={() => setTransferTo(member.id)} />
            ))}
          </div>

          <div className="account-transfer-summary-v2">
            <span className="account-transfer-summary-v2__avatar">{transferTarget?.name.slice(0, 1).toUpperCase() ?? "?"}</span>
            <span><small>Новий власник</small><strong>{transferTarget?.name ?? "Не вибрано"}</strong><em>Твоя роль після передачі: Учасник</em></span>
            <button disabled={!transferTarget} onClick={() => setConfirmAction("TRANSFER")}>Передати</button>
          </div>

          {confirmAction === "TRANSFER" ? (
            <ConfirmPanel
              tone="warning"
              title={`Передати аккаунт ${transferTarget?.name ?? ""}?`}
              note="Власність зміниться одразу. Повернути її зможе тільки новий власник."
              confirmLabel="Так, передати"
              onCancel={() => setConfirmAction(null)}
              onConfirm={transfer}
            />
          ) : null}
        </section>
      ) : null}

      <section className="account-settings-panel-v2 account-lifecycle-panel-v2">
        <header>
          <span>
            <small>СТАН АККАУНТА</small>
            <strong>{active ? "Аккаунт активний" : "Аккаунт деактивований"}</strong>
            <em>{active ? "Учасники можуть користуватися функціями аккаунта." : "Аккаунт збережений, але не може бути активним контекстом."}</em>
          </span>
          <span className={`account-lifecycle-indicator ${active ? "is-active" : "is-inactive"}`}><i />{active ? "Працює" : "Вимкнено"}</span>
        </header>

        {activeAccount.owner ? (
          active ? (
            <div className="account-danger-card-v2">
              <span><Glyph name="close" size={18} /></span>
              <span><strong>Деактивувати аккаунт</strong><small>Дані, ролі та історія залишаться. Аккаунт перестане бути доступним як активний.</small></span>
              <button onClick={() => setConfirmAction("DEACTIVATE")}>Деактивувати</button>
            </div>
          ) : (
            <div className="account-reactivate-card-v2">
              <span><Glyph name="check" size={18} /></span>
              <span><strong>Повернути аккаунт у роботу</strong><small>Після активації його знову можна буде вибрати як активний.</small></span>
              <button onClick={reactivate}>Активувати</button>
            </div>
          )
        ) : (
          <div className="account-danger-card-v2">
            <span><Glyph name="close" size={18} /></span>
            <span><strong>Вийти з аккаунта</strong><small>Ти втратиш доступ до ролей, історії та функцій цього аккаунта.</small></span>
            <button onClick={() => setConfirmAction("LEAVE")}>Вийти</button>
          </div>
        )}

        {confirmAction === "DEACTIVATE" ? (
          <ConfirmPanel
            tone="danger"
            title="Деактивувати аккаунт?"
            note="Це не видалення. Власник зможе активувати аккаунт пізніше."
            confirmLabel="Деактивувати"
            onCancel={() => setConfirmAction(null)}
            onConfirm={deactivate}
          />
        ) : null}

        {confirmAction === "LEAVE" ? (
          <ConfirmPanel
            tone="danger"
            title="Вийти з аккаунта?"
            note="Повернутися можна буде лише через нове запрошення."
            confirmLabel="Вийти"
            onCancel={() => setConfirmAction(null)}
            onConfirm={leave}
          />
        ) : null}
      </section>
    </div>
  )
}

function RoleOption({ role, selected, onClick }: { role: AccountRole; selected: boolean; onClick: () => void }) {
  return (
    <button className={selected ? "is-selected" : ""} onClick={onClick}>
      <span style={{ "--role-color": role.color } as CSSProperties}><i /></span>
      <strong>{role.name}</strong>
      <small>{countEnabledPermissions(role)} дозволів</small>
      <b>{selected ? <Glyph name="check" size={13} /> : null}</b>
    </button>
  )
}

function MemberOption({ member, selected, onClick }: { member: Member; selected: boolean; onClick: () => void }) {
  return (
    <button className={selected ? "is-selected" : ""} onClick={onClick}>
      <span>{member.name.slice(0, 1).toUpperCase()}</span>
      <strong>{member.name}</strong>
      <small>{member.joinedAt}</small>
      <b>{selected ? <Glyph name="check" size={13} /> : null}</b>
    </button>
  )
}

function ConfirmPanel({ tone, title, note, confirmLabel, onCancel, onConfirm }: {
  tone: "warning" | "danger"
  title: string
  note: string
  confirmLabel: string
  onCancel: () => void
  onConfirm: () => void
}) {
  return (
    <div className={`account-confirm-v2 is-${tone}`}>
      <span><Glyph name={tone === "danger" ? "close" : "swap"} size={17} /></span>
      <span><strong>{title}</strong><small>{note}</small></span>
      <div><button onClick={onCancel}>Скасувати</button><button onClick={onConfirm}>{confirmLabel}</button></div>
    </div>
  )
}

function countEnabledPermissions(role?: AccountRole) {
  if (!role) return 0

  return Object.values(role.permissions).filter(Boolean).length
}
