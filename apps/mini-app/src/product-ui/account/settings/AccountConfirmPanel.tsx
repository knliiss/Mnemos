import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"

type Props = {
  tone: "warning" | "danger"
  title: string
  note: string
  confirmLabel: string
  busy: boolean
  onCancel: () => void
  onConfirm: () => void
}

export function AccountConfirmPanel({
  tone,
  title,
  note,
  confirmLabel,
  busy,
  onCancel,
  onConfirm,
}: Props) {
  const { t } = useTranslation()

  return (
    <div className={`account-confirm is-${tone}`}>
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
