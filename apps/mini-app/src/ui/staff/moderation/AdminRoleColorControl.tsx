import { useTranslation } from "react-i18next"
import { AdminColorControl } from "../shared/AdminColorControl"

type Props = {
  value: string
  disabled?: boolean
  compact?: boolean
  onChange(value: string): void
}

export function AdminRoleColorControl({
  value,
  disabled = false,
  compact = false,
  onChange,
}: Props) {
  const { t } = useTranslation()

  return (
    <AdminColorControl
      value={value}
      label={t("roleEditor.color")}
      disabled={disabled}
      compact={compact}
      onChange={onChange}
    />
  )
}
