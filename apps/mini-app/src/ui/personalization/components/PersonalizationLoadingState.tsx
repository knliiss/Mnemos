import { useTranslation } from "react-i18next"
import { FeatureLoading } from "../../system/FeatureLoading"

export function PersonalizationLoadingState() {
  const { t } = useTranslation()

  return <FeatureLoading label={t("personalization.loading")} />
}
