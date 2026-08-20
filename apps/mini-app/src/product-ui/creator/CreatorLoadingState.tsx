import { useTranslation } from "react-i18next"

export function CreatorLoadingState() {
  const { t } = useTranslation()

  return (
    <div className="creator-loading" aria-label={t("creator.loading")}>
      <i />
      <i />
      <i />
    </div>
  )
}
