import { useTranslation } from "react-i18next"
import "./system.css"

type Props = {
  label?: string
}

export function FeatureLoading({ label }: Props) {
  const { t } = useTranslation()

  return (
    <div
      className="feature-loading"
      role="status"
      aria-live="polite"
      aria-atomic="true"
      aria-busy="true"
      aria-label={label ?? t("common.loading")}
    >
      <i />
      <i />
      <i />
    </div>
  )
}
