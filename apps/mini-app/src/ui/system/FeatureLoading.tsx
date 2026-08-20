import { useTranslation } from "react-i18next"
import "./system.css"

type Props = {
  label?: string
}

export function FeatureLoading({ label }: Props) {
  const { t } = useTranslation()

  return (
    <div className="feature-loading" aria-label={label ?? t("common.loading")}>
      <i />
      <i />
      <i />
    </div>
  )
}
