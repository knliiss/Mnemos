import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
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

export function AccountSettings({
  navigate,
  back,
}: {
  navigate: (route: Route) => void
  back: () => void
}) {
  const { t } = useTranslation()
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
      await updateAccountBaseRole(activeAccount.id, baseRoleId === NO_BASE_ROLE ? null : baseRoleId)
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
      <PageHeader title={t("accountSettings.title")} back={back} />

      <section className="account-settings-hero-v2">
        <CristalixHead skinUrl={activeAccount.skinUrl} size={58} />
        <span className="account-settings-hero-v2__copy">
          <small>CRISTALIX ACCOUNT</small>
          <strong>{activeAccount.nickname}</strong>
          <em>
            {activeAccount.owner ? t("accountSettings.owner") : t("accountSettings.member")} ·{" "}
            {t("accountSettings.active")}
          </em>
        </span>
        <span className="account-settings-status-v2 is-active">
          <i />
          ACTIVE
        </span>
        <div className="account-settings-hero-v2__stats">
          <span>
            <small>{t("accountSettings.members")}</small>
            <strong>
              {activeAccount.memberCount}/{activeAccount.maxMembers}
            </strong>
          </span>
          <span>
            <small>{t("accountSettings.balance")}</small>
            <CurrencyValue currency="COIN" value={activeAccount.coin} compact />
          </span>
        </div>
      </section>

      <nav className="account-settings-shortcuts account-settings-shortcuts--four">
        <button
          onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })}
        >
          <span className="is-lime">
            <Glyph name="spark" size={18} />
          </span>
          <strong>{t("accountSettings.personalization")}</strong>
          <small>{t("accountSettings.personalizationCopy")}</small>
          <b>›</b>
        </button>
        <button onClick={() => navigate({ id: "roles" })}>
          <span className="is-plum">
            <Glyph name="roles" size={18} />
          </span>
          <strong>{t("accountSettings.roles")}</strong>
          <small>{t("accountSettings.rolesCopy")}</small>
          <b>›</b>
        </button>
        <button onClick={() => navigate({ id: "account-invites" })}>
          <span className="is-amber">
            <Glyph name="invite" size={18} />
          </span>
          <strong>{t("accountSettings.invites")}</strong>
          <small>{t("accountSettings.invitesCopy")}</small>
          <b>›</b>
        </button>
        <button onClick={() => navigate({ id: "account-wallet" })}>
          <span className="is-lime">
            <Glyph name="wallet" size={18} />
          </span>
          <strong>{t("accountSettings.wallet")}</strong>
          <small>{t("accountSettings.walletCopy")}</small>
          <b>›</b>
        </button>
      </nav>

      <section className="account-settings-panel-v2">
        <header>
          <span>
            <small>{t("accountSettings.accessEyebrow")}</small>
            <strong>{t("accountSettings.defaultRoleTitle")}</strong>
            <em>{t("accountSettings.defaultRoleCopy")}</em>
          </span>
          {roleChanged ? (
            <b className="is-unsaved">{t("accountSettings.unsaved")}</b>
          ) : (
            <b className="is-saved">
              <Glyph name="check" size={12} />
              {t("accountSettings.saved")}
            </b>
          )}
        </header>

        <div className="account-default-role-grid">
          <button
            className={baseRoleId === NO_BASE_ROLE ? "is-selected" : ""}
            onClick={() => setBaseRoleId(NO_BASE_ROLE)}
          >
            <span className="account-base-role-empty">
              <Glyph name="close" size={16} />
            </span>
            <strong>{t("accountSettings.noRole")}</strong>
            <small>{t("accountSettings.permissionsCount", { count: 0 })}</small>
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
            <span className="account-base-role-empty account-base-role-empty--summary">
              <Glyph name="close" size={18} />
            </span>
          )}
          <span>
            <small>{t("accountSettings.newMembersReceive")}</small>
            <strong>{baseRole?.name ?? t("accountSettings.noRole")}</strong>
            <em>
              {t("accountSettings.permissionsCount", { count: countEnabledPermissions(baseRole) })}
            </em>
          </span>
          <button disabled={!roleChanged || busy} onClick={() => void saveBaseRole()}>
            {busy ? "…" : t("accountSettings.save")}
          </button>
        </div>
      </section>

      {activeAccount.owner ? (
        <section className="account-settings-panel-v2 account-owner-panel-v2">
          <header>
            <span>
              <small>{t("accountSettings.ownershipEyebrow")}</small>
              <strong>{t("accountSettings.transferTitle")}</strong>
              <em>{t("accountSettings.transferCopy")}</em>
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
              <span className="account-transfer-summary-v2__avatar" aria-hidden="true">
                <Glyph name="person" size={18} />
              </span>
              <span>
                <small>{t("accountSettings.newOwner")}</small>
                <strong>{transferTarget.name}</strong>
                <em>{t("accountSettings.afterTransferCopy")}</em>
              </span>
              <button disabled={busy} onClick={() => setConfirmAction("TRANSFER")}>
                {t("accountSettings.transfer")}
              </button>
            </div>
          ) : (
            <p className="inline-hint inline-hint--left">{t("accountSettings.noTransferTarget")}</p>
          )}

          {confirmAction === "TRANSFER" ? (
            <ConfirmPanel
              tone="warning"
              title={t("accountSettings.transferConfirmTitle", {
                name: transferTarget?.name ?? "",
              })}
              note={t("accountSettings.transferConfirmCopy")}
              confirmLabel={t("accountSettings.transferConfirm")}
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
            <small>{t("accountSettings.lifecycleEyebrow")}</small>
            <strong>{t("accountSettings.accountActive")}</strong>
            <em>{t("accountSettings.accountActiveCopy")}</em>
          </span>
          <span className="account-lifecycle-indicator is-active">
            <i />
            {t("accountSettings.working")}
          </span>
        </header>

        {activeAccount.owner ? (
          <div className="account-danger-card-v2">
            <span>
              <Glyph name="close" size={18} />
            </span>
            <span>
              <strong>{t("accountSettings.deactivateTitle")}</strong>
              <small>{t("accountSettings.deactivateCopy")}</small>
            </span>
            <button disabled={busy} onClick={() => setConfirmAction("DEACTIVATE")}>
              {t("accountSettings.deactivate")}
            </button>
          </div>
        ) : (
          <div className="account-danger-card-v2">
            <span>
              <Glyph name="close" size={18} />
            </span>
            <span>
              <strong>{t("accountSettings.leaveTitle")}</strong>
              <small>{t("accountSettings.leaveCopy")}</small>
            </span>
            <button disabled={busy} onClick={() => setConfirmAction("LEAVE")}>
              {t("accountSettings.leave")}
            </button>
          </div>
        )}

        {confirmAction === "DEACTIVATE" ? (
          <ConfirmPanel
            tone="danger"
            title={t("accountSettings.deactivateConfirmTitle")}
            note={t("accountSettings.deactivateConfirmCopy")}
            confirmLabel={t("accountSettings.deactivate")}
            busy={busy}
            onCancel={() => setConfirmAction(null)}
            onConfirm={() => void deactivate()}
          />
        ) : null}

        {confirmAction === "LEAVE" ? (
          <ConfirmPanel
            tone="danger"
            title={t("accountSettings.leaveConfirmTitle")}
            note={t("accountSettings.leaveConfirmCopy")}
            confirmLabel={t("accountSettings.leave")}
            busy={busy}
            onCancel={() => setConfirmAction(null)}
            onConfirm={() => void leave()}
          />
        ) : null}
      </section>
    </div>
  )
}

function RoleOption({
  role,
  selected,
  onClick,
}: {
  role: AccountRole
  selected: boolean
  onClick: () => void
}) {
  const { t } = useTranslation()

  return (
    <button className={selected ? "is-selected" : ""} onClick={onClick}>
      <RoleSigil color={role.color} size={40} />
      <strong>{role.name}</strong>
      <small>
        {t("accountSettings.permissionsCount", { count: countEnabledPermissions(role) })}
      </small>
    </button>
  )
}

function MemberOption({
  member,
  selected,
  onClick,
}: {
  member: Member
  selected: boolean
  onClick: () => void
}) {
  const { i18n } = useTranslation()

  return (
    <button className={selected ? "is-selected" : ""} onClick={onClick}>
      <span aria-hidden="true">
        <Glyph name="person" size={16} />
      </span>
      <strong>{member.name}</strong>
      <small>{formatJoinedAt(member.joinedAt, localeForLanguage(i18n.resolvedLanguage))}</small>
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
  const { t } = useTranslation()

  return (
    <div className={`account-confirm-v2 is-${tone}`}>
      <span>
        <Glyph name={tone === "danger" ? "close" : "swap"} size={17} />
      </span>
      <span>
        <strong>{title}</strong>
        <small>{note}</small>
      </span>
      <div>
        <button disabled={busy} onClick={onCancel}>
          {t("accountSettings.cancel")}
        </button>
        <button disabled={busy} onClick={onConfirm}>
          {busy ? "…" : confirmLabel}
        </button>
      </div>
    </div>
  )
}

function countEnabledPermissions(role: AccountRole | null) {
  if (!role) return 0
  return Object.values(role.permissions).filter(Boolean).length
}

function formatJoinedAt(value: string, locale: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat(locale, {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(date)
}

function localeForLanguage(language?: string) {
  if (language === "en") return "en-US"
  if (language === "ru") return "ru-RU"
  return "uk-UA"
}
