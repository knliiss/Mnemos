import { useTranslation } from "react-i18next"
import { RootNavIllustration } from "../components/navigation/RootNavIllustration"
import type { RootRoute } from "../model"
import { rootItems, rootNavigationKeys } from "./navigation"

type Props = {
  current: RootRoute
  onChange: (route: RootRoute) => void
}

export function RootNavigation({ current, onChange }: Props) {
  const { t } = useTranslation()

  return (
    <nav
      className="root-navigation root-navigation--five root-navigation--illustrated"
      aria-label={t("productShell.navigation.aria")}
    >
      {rootItems.map((item) => (
        <button
          type="button"
          key={item}
          className={current === item ? "is-active" : ""}
          onClick={() => onChange(item)}
        >
          <span className="root-navigation__icon root-navigation__icon--illustrated">
            <RootNavIllustration route={item} />
          </span>
          <span>{t(rootNavigationKeys[item])}</span>
        </button>
      ))}
    </nav>
  )
}
