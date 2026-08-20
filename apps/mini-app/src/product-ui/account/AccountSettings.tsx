import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { useApp } from "@/app/providers/AppProvider"
import {
  deactivateAccount,
  transferAccountOwnership,
  updateAccountBaseRole,
} from "@/shared/api/accountSettingsApi"
import { handleError } from "@/shared/errors/errorHandler"
import { CristalixHead } from "../components/profile/CristalixHead"
import { useProductData } from "../data/ProductDataProvider"
import type { Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"
import { NO_BASE_ROLE } from "./accountSettingsPresentation"
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

  useEffect(() => {
    if (transferableMembers.some((member) => member.id === transferTo)) {
      return
    }

    setTransferTo(transferableMembers[0]?.id ?? "")
  }, [transferTo, transferableMembers])

  if (!activeAccount) {
    return null
  }

  const transferTarget = transferableMembers.find((member) => member.id === transferTo) ?? null
  const roleChanged = baseRoleId !== savedRoleId

  const saveBaseRole = async () => {
    if (busy || !roleChanged) {
      return
    }

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
    if (!activeAccount.owner || !transferTarget || busy) {
      return
    }

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
    if (!activeAccount.owner || busy) {
      return
    }

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
    if (activeAccount.owner || busy) {
      return
    }

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
    <div className="screen account-settings-screen">
      <PageHeader title={t("accountSettings.title")} back={back} />

      <section className="account-settings-hero">
        <CristalixHead skinUrl={activeAccount.skinUrl} size={58} />
        <span className="account-settings-hero-v2__copy">
          <small>CRISTALIX ACCOUNT</small>
          <strong>{activeAccount.nickname}</strong>
          <em>
            {activeAccount.owner ? t("accountSettings.owner") : t("accountSettings.member")} ·{" "}
            {t("accountSettings.active")}
          </em>
        </span>
        <span className="account-settings-status is-active">
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

      <AccountBaseRoleSettings
        availableRoles={availableRoles}
        baseRoleId={baseRoleId}
        savedRoleId={savedRoleId}
        busy={busy}
        onBaseRoleChange={setBaseRoleId}
        onSave={() => void saveBaseRole()}
      />

      {activeAccount.owner ? (
        <AccountOwnershipSettings
          members={transferableMembers}
          selectedMemberId={transferTo}
          confirmOpen={confirmAction === "TRANSFER"}
          busy={busy}
          onMemberChange={setTransferTo}
          onRequestTransfer={() => setConfirmAction("TRANSFER")}
          onCancelTransfer={() => setConfirmAction(null)}
          onConfirmTransfer={() => void transfer()}
        />
      ) : null}

      <AccountLifecycleSettings
        owner={activeAccount.owner}
        confirmAction={
          confirmAction === "DEACTIVATE" || confirmAction === "LEAVE" ? confirmAction : null
        }
        busy={busy}
        onRequestDeactivate={() => setConfirmAction("DEACTIVATE")}
        onRequestLeave={() => setConfirmAction("LEAVE")}
        onCancel={() => setConfirmAction(null)}
        onDeactivate={() => void deactivate()}
        onLeave={() => void leave()}
      />
    </div>
  )
}
