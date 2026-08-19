import { useTranslation } from "react-i18next"

export function FeatureLoading() {
  const { t } = useTranslation()

  return (
    <div className="events-r2-loading" aria-label={t("common.loading")}>
      <i />
      <i />
      <i />
    </div>
  )
}
