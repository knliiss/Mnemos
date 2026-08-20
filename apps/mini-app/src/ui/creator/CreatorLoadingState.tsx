import { useTranslation } from "react-i18next"
import { FeatureLoading } from "../system/FeatureLoading"

export function CreatorLoadingState() {
  const { t } = useTranslation()

  return <FeatureLoading label={t("creator.loading")} />
}
