import { useEffect, useLayoutEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { useApp } from "@/app/providers/AppProvider"
import {
  deactivateAccount,
  transferAccountOwnership,
  updateAccountBaseRole,
} from "@/shared/api/accountSettingsApi"
import { handleError } from "@/shared/errors/errorHandler"
import { CristalixHead } from "../components/profile/CristalixHead"
import { useMnemosData } from "../data/MnemosDataProvider"
import type { Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"
import "./account-settings.css"
import { NO_BASE_ROLE } from "./accountSettingsPresentation"
import {
  resolveActorMember,
  resolveBaseRoleAccess,
  roleAccessReasonTranslationKey,
} from "./roleAccessPolicy"
import { AccountBaseRoleSettings } from "./settings/AccountBaseRoleSettings"
import { AccountLifecycleSettings } from "./settings/AccountLifecycleSettings"
import { AccountOwnershipSettings } from "./settings/AccountOwnershipSettings"

type ConfirmAction = "TRANSFER" | "DEACTIVATE" | "LEAVE" | null

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function AccountSettings({ navigate, back }: Props) {
  const { t } = useTranslation()
  const app = useApp()
  const data = useMnemosData()
  const activeAccount = data.activeAccount
  const contextAccount = app.context?.activeAccount ?? null
  const canManageBaseRole = data.hasPermission("MANAGE_BASE_ROLE")
  const actorMember = activeAccount ? resolveActorMember(activeAccount, data.members) : null
  const availableRoles = useMemo(
    () => [...data.roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank),
    [data.roles],
  )
  const transferableMembers = useMemo(
    () => data.members.filter((member) => !member.owner),
    [data.members],
  )
  const initialBaseRoleId = contextAccount?.baseRoleId ?? NO_BASE_ROLE
  const [baseRoleId, setBaseRoleId] = useState(initialBaseRoleId)
  const [savedRoleId, setSavedRoleId] = useState(initialBaseRoleId)
  const [transferTo, setTransferTo] = useState(transferableMembers[0]?.id ?? "")
  const [transferConfirmationMemberId, setTransferConfirmationMemberId] = useState<string | null>(
    null,
  )
  const [confirmAction, setConfirmAction] = useState<ConfirmAction>(null)
  const [busy, setBusy] = useState(false)
  const actionScopeRef = useRef<string | null>(null)
  const accountIdRef = useRef(activeAccount?.id ?? null)
  const mountedRef = useRef(true)

  useLayoutEffect(() => {
    accountIdRef.current = activeAccount?.id ?? null
  }, [activeAccount?.id])

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  if (!activeAccount) {
    return null
  }

  const roleAccessContext = {
    account: activeAccount,
    actorMember,
    roles: availableRoles,
  }
  const getBaseRoleAccessMessage = (role: (typeof availableRoles)[number]) => {
    const access = resolveBaseRoleAccess(
      roleAccessContext,
      canManageBaseRole,
      role,
      data.hasPermission,
    )

    return access === "ALLOWED" ? null : t(roleAccessReasonTranslationKey(access))
  }
  const selectedBaseRole = availableRoles.find((role) => role.id === baseRoleId) ?? null
  const selectedBaseRoleAccess = selectedBaseRole
    ? resolveBaseRoleAccess(
        roleAccessContext,
        canManageBaseRole,
        selectedBaseRole,
        data.hasPermission,
      )
    : "ALLOWED"
  const selectedTransferId = transferableMembers.some((member) => member.id === transferTo)
    ? transferTo
    : (transferableMembers[0]?.id ?? "")
  const transferTarget =
    transferableMembers.find((member) => member.id === selectedTransferId) ?? null
  const confirmedTransferTarget =
    transferableMembers.find((member) => member.id === transferConfirmationMemberId) ?? null
  const roleChanged = baseRoleId !== savedRoleId

  const saveBaseRole = async () => {
    const requestedAccountId = activeAccount.id

    if (
      !canManageBaseRole ||
      selectedBaseRoleAccess !== "ALLOWED" ||
      actionScopeRef.current ||
      !roleChanged
    ) {
      return
    }

    actionScopeRef.current = requestedAccountId
    setBusy(true)

    try {
      await updateAccountBaseRole(activeAccount.id, baseRoleId === NO_BASE_ROLE ? null : baseRoleId)
      await app.refresh()

      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        setSavedRoleId(baseRoleId)
      }
    } catch (error) {
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        handleError(error)
      }
    } finally {
      if (actionScopeRef.current === requestedAccountId) {
        actionScopeRef.current = null

        if (mountedRef.current && accountIdRef.current === requestedAccountId) {
          setBusy(false)
        }
      }
    }
  }

  const requestTransfer = () => {
    if (!activeAccount.owner || !transferTarget || actionScopeRef.current) {
      return
    }

    setTransferConfirmationMemberId(transferTarget.id)
    setConfirmAction("TRANSFER")
  }

  const cancelTransfer = () => {
    if (busy) {
      return
    }

    setTransferConfirmationMemberId(null)
    setConfirmAction(null)
  }

  const transfer = async () => {
    const requestedAccountId = activeAccount.id

    if (!activeAccount.owner || !confirmedTransferTarget || actionScopeRef.current) {
      return
    }

    actionScopeRef.current = requestedAccountId
    setBusy(true)

    try {
      await transferAccountOwnership(activeAccount.id, confirmedTransferTarget.id)
      await app.refresh()
      await data.refreshAccount()

      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        setTransferConfirmationMemberId(null)
        setConfirmAction(null)
      }
    } catch (error) {
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        handleError(error)
      }
    } finally {
      if (actionScopeRef.current === requestedAccountId) {
        actionScopeRef.current = null

        if (mountedRef.current && accountIdRef.current === requestedAccountId) {
          setBusy(false)
        }
      }
    }
  }

  const deactivate = async () => {
    const requestedAccountId = activeAccount.id

    if (!activeAccount.owner || actionScopeRef.current) {
      return
    }

    actionScopeRef.current = requestedAccountId
    setBusy(true)

    try {
      await deactivateAccount(activeAccount.id)
      await app.refresh()

      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        navigate({ id: "account" })
      }
    } catch (error) {
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        handleError(error)
      }
    } finally {
      if (actionScopeRef.current === requestedAccountId) {
        actionScopeRef.current = null

        if (mountedRef.current && accountIdRef.current === requestedAccountId) {
          setBusy(false)
        }
      }
    }
  }

  const leave = async () => {
    const requestedAccountId = activeAccount.id

    if (activeAccount.owner || actionScopeRef.current) {
      return
    }

    actionScopeRef.current = requestedAccountId
    setBusy(true)

    try {
      await app.leaveAccount()

      if (mountedRef.current) {
        navigate({ id: "account" })
      }
    } catch (error) {
      if (mountedRef.current && accountIdRef.current === requestedAccountId) {
        handleError(error)
      }
    } finally {
      if (actionScopeRef.current === requestedAccountId) {
        actionScopeRef.current = null

        if (mountedRef.current && accountIdRef.current === requestedAccountId) {
          setBusy(false)
        }
      }
    }
  }

  return (
    <div className="screen account-settings-screen">
      <PageHeader title={t("accountSettings.title")} back={back} />

      <section className="account-settings-hero">
        <CristalixHead skinUrl={activeAccount.skinUrl} size={58} />
        <span className="account-settings-hero__copy">
          <small>{t("accountSettings.eyebrow")}</small>
          <strong>{activeAccount.nickname}</strong>
          <em>
            {activeAccount.owner ? t("accountSettings.owner") : t("accountSettings.member")} ·{" "}
            {t("accountSettings.active")}
          </em>
        </span>
        <span className="account-settings-status is-active">
          <i />
          {t("accountSettings.active")}
        </span>
        <div className="account-settings-hero__stats">
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
          type="button"
          onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })}
        >
          <span className="is-lime">
            <Glyph name="spark" size={18} />
          </span>
          <strong>{t("accountSettings.personalization")}</strong>
          <small>{t("accountSettings.personalizationCopy")}</small>
          <b>›</b>
        </button>

        <button type="button" onClick={() => navigate({ id: "roles" })}>
          <span className="is-plum">
            <Glyph name="roles" size={18} />
          </span>
          <strong>{t("accountSettings.roles")}</strong>
          <small>{t("accountSettings.rolesCopy")}</small>
          <b>›</b>
        </button>

        <button type="button" onClick={() => navigate({ id: "account-invites" })}>
          <span className="is-amber">
            <Glyph name="invite" size={18} />
          </span>
          <strong>{t("accountSettings.invites")}</strong>
          <small>{t("accountSettings.invitesCopy")}</small>
          <b>›</b>
        </button>

        <button type="button" onClick={() => navigate({ id: "account-wallet" })}>
          <span className="is-lime">
            <Glyph name="wallet" size={18} />
          </span>
          <strong>{t("accountSettings.wallet")}</strong>
          <small>{t("accountSettings.walletCopy")}</small>
          <b>›</b>
        </button>
      </nav>

      <AccountBaseRoleSettings
        availableRoles={availableRoles}
        baseRoleId={baseRoleId}
        savedRoleId={savedRoleId}
        busy={busy}
        readable
        editable={canManageBaseRole}
        getRoleAccessMessage={getBaseRoleAccessMessage}
        onBaseRoleChange={setBaseRoleId}
        onSave={() => void saveBaseRole()}
      />

      {activeAccount.owner ? (
        <AccountOwnershipSettings
          members={transferableMembers}
          selectedMemberId={selectedTransferId}
          confirmationMemberId={transferConfirmationMemberId}
          confirmOpen={confirmAction === "TRANSFER"}
          busy={busy}
          onMemberChange={setTransferTo}
          onRequestTransfer={requestTransfer}
          onCancelTransfer={cancelTransfer}
          onConfirmTransfer={() => void transfer()}
        />
      ) : null}

      <AccountLifecycleSettings
        owner={activeAccount.owner}
        confirmAction={
          confirmAction === "DEACTIVATE" || confirmAction === "LEAVE" ? confirmAction : null
        }
        busy={busy}
        onRequestDeactivate={() => {
          setTransferConfirmationMemberId(null)
          setConfirmAction("DEACTIVATE")
        }}
        onRequestLeave={() => {
          setTransferConfirmationMemberId(null)
          setConfirmAction("LEAVE")
        }}
        onCancel={() => setConfirmAction(null)}
        onDeactivate={() => void deactivate()}
        onLeave={() => void leave()}
      />
    </div>
  )
}
