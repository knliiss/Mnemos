import { useMemo, useState } from "react"
import { useApp } from "@/app/providers/AppProvider"
import {
  deactivateAccount,
  transferAccountOwnership,
  updateAccountBaseRole,
} from "@/shared/api/accountSettingsApi"
import { handleError } from "@/shared/errors/errorHandler"
import { CristalixHead } from "../components/CristalixHead"
import { useProductData } from "../data/ProductDataProvider"
import type { AccountRole, Member, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"

type ConfirmAction = "TRANSFER" | "DEACTIVATE" | "LEAVE" | null

const NO_BASE_ROLE = "__none__"

export function AccountSettingsProduction({ navigate, back }: {
  navigate: (route: Route) => void
  back: () => void
}) {
  const app = useApp()
  const product = useProductData()
  const activeAccount = product.activeAccount
  const contextAccount = app.context?.activeAccount ?? null
  const availableRoles = useMemo(
    () => [...product.roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank),
    [product.roles],
  )
  const transferableMembers = useMemo(
    () => product.members.filter((member) => !member.owner),
    [product.members],
  )
  const initialBaseRoleId = contextAccount?.baseRoleId ?? NO_BASE_ROLE
  const [baseRoleId, setBaseRoleId] = useState(initialBaseRoleId)
  const [savedRoleId, setSavedRoleId] = useState(initialBaseRoleId)
  const [transferTo, setTransferTo] = useState(transferableMembers[0]?.id ?? "")
  const [confirmAction, setConfirmAction] = useState<ConfirmAction>(null)
  const [busy, setBusy] = useState(false)

  if (!activeAccount) return null

  const baseRole = availableRoles.find((role) => role.id === baseRoleId) ?? null
  const transferTarget = transferableMembers.find((member) => member.id === transferTo) ?? null
  const roleChanged = baseRoleId !== savedRoleId

  const saveBaseRole = async () => {
    if (busy || !roleChanged) return

    setBusy(true)

    try {
      await updateAccountBaseRole(
        activeAccount.id,
        baseRoleId === NO_BASE_ROLE ? null : baseRoleId,
      )
      await app.refresh()
      setSavedRoleId(baseRoleId)
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const transfer = async () => {
    if (!activeAccount.owner || !transferTarget || busy) return

    setBusy(true)

    try {
      await transferAccountOwnership(activeAccount.id, transferTarget.id)
      await app.refresh()
      await product.refreshAccount()
      setConfirmAction(null)
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const deactivate = async () => {
    if (!activeAccount.owner || busy) return

    setBusy(true)

    try {
      await deactivateAccount(activeAccount.id)
      await app.refresh()
      navigate({ id: "account" })
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const leave = async () => {
    if (activeAccount.owner || busy) return

    setBusy(true)

    try {
      await app.leaveAccount()
      navigate({ id: "account" })
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="screen account-settings-v2">
      <PageHeader title="Налаштування аккаунта" back={back} />

      <section className="account-settings-hero-v2">
        <CristalixHead skinUrl={activeAccount.skinUrl} size={58} />
        <span className="account-settings-hero-v2__copy">
          <small>CRISTALIX ACCOUNT</small>
          <strong>{activeAccount.nickname}</strong>
          <em>{activeAccount.owner ? "Власник" : "Учасник"} · активний</em>
        </span>
        <span className="account-settings-status-v2 is-active">
          <i />
          ACTIVE
        </span>
        <div className="account-settings-hero-v2__stats">
          <span><small>Учасники</small><strong>{activeAccount.memberCount}/{activeAccount.maxMembers}</strong></span>
          <span><small>Баланс</small><CurrencyValue currency="COIN" value={activeAccount.coin} compact /></span>
        </div>
      </section>

      <nav className="account-settings-shortcuts account-settings-shortcuts--four">
        <button onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })}>
          <span className="is-lime"><Glyph name="spark" size={18} /></span>
          <strong>Персоналізація</strong>
          <small>Вигляд аккаунта</small>
          <b>›</b>
        </button>
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
            <em>Backend застосує цю роль до учасників, які приєднаються після зміни.</em>
          </span>
          {roleChanged ? <b className="is-unsaved">Не збережено</b> : <b className="is-saved"><Glyph name="check" size={12} />Збережено</b>}
        </header>

        <div className="account-default-role-grid">
          <button
            className={baseRoleId === NO_BASE_ROLE ? "is-selected" : ""}
            onClick={() => setBaseRoleId(NO_BASE_ROLE)}
          >
            <span className="account-base-role-empty"><Glyph name="close" size={16} /></span>
            <strong>Без ролі</strong>
            <small>0 дозволів</small>
          </button>
          {availableRoles.map((role) => (
            <RoleOption
              key={role.id}
              role={role}
              selected={role.id === baseRoleId}
              onClick={() => setBaseRoleId(role.id)}
            />
          ))}
        </div>

        <div className="account-role-summary">
          {baseRole ? (
            <RoleSigil color={baseRole.color} size={42} />
          ) : (
            <span className="account-base-role-empty account-base-role-empty--summary"><Glyph name="close" size={18} /></span>
          )}
          <span>
            <small>Нові учасники отримають</small>
            <strong>{baseRole?.name ?? "Без ролі"}</strong>
            <em>{countEnabledPermissions(baseRole)} дозволів</em>
          </span>
          <button disabled={!roleChanged || busy} onClick={() => void saveBaseRole()}>
            {busy ? "…" : "Зберегти"}
          </button>
        </div>
      </section>

      {activeAccount.owner ? (
        <section className="account-settings-panel-v2 account-owner-panel-v2">
          <header>
            <span>
              <small>ВЛАСНІСТЬ</small>
              <strong>Передача аккаунта</strong>
              <em>Новий власник отримає повний контроль. Backend перевірить допустимість передачі.</em>
            </span>
          </header>

          <div className="account-transfer-grid-v2">
            {transferableMembers.map((member) => (
              <MemberOption
                key={member.id}
                member={member}
                selected={member.id === transferTo}
                onClick={() => setTransferTo(member.id)}
              />
            ))}
          </div>

          {transferTarget ? (
            <div className="account-transfer-summary-v2">
              <span className="account-transfer-summary-v2__avatar" aria-hidden="true"><Glyph name="person" size={18} /></span>
              <span>
                <small>Новий власник</small>
                <strong>{transferTarget.name}</strong>
                <em>Твій доступ після передачі визначатимуть ролі аккаунта.</em>
              </span>
              <button disabled={busy} onClick={() => setConfirmAction("TRANSFER")}>Передати</button>
            </div>
          ) : (
            <p className="inline-hint inline-hint--left">Немає іншого учасника, якому можна передати аккаунт.</p>
          )}

          {confirmAction === "TRANSFER" ? (
            <ConfirmPanel
              tone="warning"
              title={`Передати аккаунт ${transferTarget?.name ?? ""}?`}
              note="Власність зміниться одразу після підтвердження backend."
              confirmLabel="Так, передати"
              busy={busy}
              onCancel={() => setConfirmAction(null)}
              onConfirm={() => void transfer()}
            />
          ) : null}
        </section>
      ) : null}

      <section className="account-settings-panel-v2 account-lifecycle-panel-v2">
        <header>
          <span>
            <small>СТАН АККАУНТА</small>
            <strong>Аккаунт активний</strong>
            <em>Учасники можуть користуватися функціями аккаунта.</em>
          </span>
          <span className="account-lifecycle-indicator is-active"><i />Працює</span>
        </header>

        {activeAccount.owner ? (
          <div className="account-danger-card-v2">
            <span><Glyph name="close" size={18} /></span>
            <span>
              <strong>Деактивувати аккаунт</strong>
              <small>Дані, ролі та історія залишаться. Аккаунт можна буде активувати пізніше з перемикача аккаунтів.</small>
            </span>
            <button disabled={busy} onClick={() => setConfirmAction("DEACTIVATE")}>Деактивувати</button>
          </div>
        ) : (
          <div className="account-danger-card-v2">
            <span><Glyph name="close" size={18} /></span>
            <span>
              <strong>Вийти з аккаунта</strong>
              <small>Повернутися можна буде лише через нове запрошення.</small>
            </span>
            <button disabled={busy} onClick={() => setConfirmAction("LEAVE")}>Вийти</button>
          </div>
        )}

        {confirmAction === "DEACTIVATE" ? (
          <ConfirmPanel
            tone="danger"
            title="Деактивувати аккаунт?"
            note="Це не видалення. Власник зможе активувати аккаунт пізніше."
            confirmLabel="Деактивувати"
            busy={busy}
            onCancel={() => setConfirmAction(null)}
            onConfirm={() => void deactivate()}
          />
        ) : null}

        {confirmAction === "LEAVE" ? (
          <ConfirmPanel
            tone="danger"
            title="Вийти з аккаунта?"
            note="Ти втратиш доступ до цього аккаунта."
            confirmLabel="Вийти"
            busy={busy}
            onCancel={() => setConfirmAction(null)}
            onConfirm={() => void leave()}
          />
        ) : null}
      </section>
    </div>
  )
}

function RoleOption({ role, selected, onClick }: {
  role: AccountRole
  selected: boolean
  onClick: () => void
}) {
  return (
    <button className={selected ? "is-selected" : ""} onClick={onClick}>
      <RoleSigil color={role.color} size={40} />
      <strong>{role.name}</strong>
      <small>{countEnabledPermissions(role)} дозволів</small>
    </button>
  )
}

function MemberOption({ member, selected, onClick }: {
  member: Member
  selected: boolean
  onClick: () => void
}) {
  return (
    <button className={selected ? "is-selected" : ""} onClick={onClick}>
      <span aria-hidden="true"><Glyph name="person" size={16} /></span>
      <strong>{member.name}</strong>
      <small>{formatJoinedAt(member.joinedAt)}</small>
      <b>{selected ? <Glyph name="check" size={13} /> : null}</b>
    </button>
  )
}

function ConfirmPanel({
  tone,
  title,
  note,
  confirmLabel,
  busy,
  onCancel,
  onConfirm,
}: {
  tone: "warning" | "danger"
  title: string
  note: string
  confirmLabel: string
  busy: boolean
  onCancel: () => void
  onConfirm: () => void
}) {
  return (
    <div className={`account-confirm-v2 is-${tone}`}>
      <span><Glyph name={tone === "danger" ? "close" : "swap"} size={17} /></span>
      <span><strong>{title}</strong><small>{note}</small></span>
      <div>
        <button disabled={busy} onClick={onCancel}>Скасувати</button>
        <button disabled={busy} onClick={onConfirm}>{busy ? "…" : confirmLabel}</button>
      </div>
    </div>
  )
}

function countEnabledPermissions(role: AccountRole | null) {
  if (!role) return 0
  return Object.values(role.permissions).filter(Boolean).length
}

function formatJoinedAt(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat("uk-UA", {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date)
}
