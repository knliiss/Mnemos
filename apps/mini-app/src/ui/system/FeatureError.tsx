import { useTranslation } from "react-i18next"
import { ErrorMascot } from "../components/errors/ErrorMascot"
import { PageHeader } from "../shared/Primitives"
import "./system.css"
import "./feature-error-polish.css"

type Props = {
  back?: () => void
  onRetry: () => void
}

export function FeatureError({ back, onRetry }: Props) {
  const { t } = useTranslation()

  return (
    <div className="screen">
      {back ? <PageHeader title={t("common.unavailable")} back={back} /> : null}
      <section className="feature-empty feature-empty--error" role="status">
        <ErrorMascot variant="internal" />
        <strong>{t("common.loadErrorTitle")}</strong>
        <small>{t("common.loadErrorCopy")}</small>
        <button type="button" onClick={onRetry}>
          {t("common.retry")}
        </button>
      </section>
    </div>
  )
}
