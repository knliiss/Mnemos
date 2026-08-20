import { useTranslation } from "react-i18next"

export function PersonalizationLoadingState() {
  const { t } = useTranslation()

  return (
    <div className="personalization-loading" aria-label={t("personalization.loading")}>
      <i />
      <i />
      <i />
    </div>
  )
}
