import { useTranslation } from "react-i18next"
import type { Route } from "../../model"
import "./realms.css"

type Props = {
  navigate: (route: Route) => void
}

export function SaoRealmEntryCard({ navigate }: Props) {
  const { t } = useTranslation()

  return (
    <button
      type="button"
      className="sao-realm-entry-card"
      onClick={() => navigate({ id: "sao-realms" })}
    >
      <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" aria-hidden="true" />
      <span>
        <small>{t("people.realms.eyebrow")}</small>
        <strong>{t("people.realms.entryTitle")}</strong>
        <em>{t("people.realms.entryCopy")}</em>
      </span>
      <b>›</b>
    </button>
  )
}
