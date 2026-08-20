import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"
import { AccountConfirmPanel } from "./AccountConfirmPanel"

type ConfirmAction = "DEACTIVATE" | "LEAVE" | null

type Props = {
  owner: boolean
  confirmAction: ConfirmAction
  busy: boolean
  onRequestDeactivate: () => void
  onRequestLeave: () => void
  onCancel: () => void
  onDeactivate: () => void
  onLeave: () => void
}

export function AccountLifecycleSettings({
  owner,
  confirmAction,
  busy,
  onRequestDeactivate,
  onRequestLeave,
  onCancel,
  onDeactivate,
  onLeave,
}: Props) {
  const { t } = useTranslation()

  return (
    <section className="account-settings-panel account-lifecycle-panel">
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

      <div className="account-danger-card">
        <span>
          <Glyph name="close" size={18} />
        </span>
        <span>
          <strong>
            {owner ? t("accountSettings.deactivateTitle") : t("accountSettings.leaveTitle")}
          </strong>
          <small>
            {owner ? t("accountSettings.deactivateCopy") : t("accountSettings.leaveCopy")}
          </small>
        </span>
        <button type="button" disabled={busy} onClick={owner ? onRequestDeactivate : onRequestLeave}>
          {owner ? t("accountSettings.deactivate") : t("accountSettings.leave")}
        </button>
      </div>

      {confirmAction === "DEACTIVATE" ? (
        <AccountConfirmPanel
          tone="danger"
          title={t("accountSettings.deactivateConfirmTitle")}
          note={t("accountSettings.deactivateConfirmCopy")}
          confirmLabel={t("accountSettings.deactivate")}
          busy={busy}
          onCancel={onCancel}
          onConfirm={onDeactivate}
        />
      ) : null}

      {confirmAction === "LEAVE" ? (
        <AccountConfirmPanel
          tone="danger"
          title={t("accountSettings.leaveConfirmTitle")}
          note={t("accountSettings.leaveConfirmCopy")}
          confirmLabel={t("accountSettings.leave")}
          busy={busy}
          onCancel={onCancel}
          onConfirm={onLeave}
        />
      ) : null}
    </section>
  )
}
