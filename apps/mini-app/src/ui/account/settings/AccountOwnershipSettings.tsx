import { useTranslation } from "react-i18next"
import type { Member } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { formatJoinedAt, localeForLanguage } from "../accountSettingsPresentation"
import "./account-ownership.css"
import { AccountConfirmPanel } from "./AccountConfirmPanel"

type Props = {
  members: Member[]
  selectedMemberId: string
  confirmationMemberId: string | null
  confirmOpen: boolean
  busy: boolean
  onMemberChange: (memberId: string) => void
  onRequestTransfer: () => void
  onCancelTransfer: () => void
  onConfirmTransfer: () => void
}

export function AccountOwnershipSettings({
  members,
  selectedMemberId,
  confirmationMemberId,
  confirmOpen,
  busy,
  onMemberChange,
  onRequestTransfer,
  onCancelTransfer,
  onConfirmTransfer,
}: Props) {
  const { t, i18n } = useTranslation()
  const selectedMember = members.find((member) => member.id === selectedMemberId) ?? null
  const confirmationMember =
    members.find((member) => member.id === confirmationMemberId) ?? selectedMember
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const selectionLocked = confirmOpen || busy

  return (
    <section className="account-settings-panel account-owner-panel">
      <header>
        <span>
          <small>{t("accountSettings.ownershipEyebrow")}</small>
          <strong>{t("accountSettings.transferTitle")}</strong>
          <em>{t("accountSettings.transferCopy")}</em>
        </span>
      </header>

      <div className="account-transfer-grid">
        {members.map((member) => (
          <button
            type="button"
            key={member.id}
            className={member.id === selectedMemberId ? "is-selected" : ""}
            disabled={selectionLocked}
            onClick={() => onMemberChange(member.id)}
          >
            <span className="account-transfer-user-mark" aria-hidden="true">
              <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
            </span>
            <strong>{member.name}</strong>
            <small>{formatJoinedAt(member.joinedAt, locale)}</small>
            <b>{member.id === selectedMemberId ? <Glyph name="check" size={13} /> : null}</b>
          </button>
        ))}
      </div>

      {selectedMember ? (
        <div className="account-transfer-summary">
          <span className="account-transfer-summary__avatar" aria-hidden="true">
            <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
          </span>
          <span>
            <small>{t("accountSettings.newOwner")}</small>
            <strong>{selectedMember.name}</strong>
            <em>{t("accountSettings.afterTransferCopy")}</em>
          </span>
          <button type="button" disabled={selectionLocked} onClick={onRequestTransfer}>
            {t("accountSettings.transfer")}
          </button>
        </div>
      ) : (
        <p className="inline-hint inline-hint--left">{t("accountSettings.noTransferTarget")}</p>
      )}

      {confirmOpen ? (
        <AccountConfirmPanel
          tone="warning"
          title={t("accountSettings.transferConfirmTitle", {
            name: confirmationMember?.name ?? "",
          })}
          note={t("accountSettings.transferConfirmCopy")}
          confirmLabel={t("accountSettings.transferConfirm")}
          busy={busy}
          onCancel={onCancelTransfer}
          onConfirm={onConfirmTransfer}
        />
      ) : null}
    </section>
  )
}
