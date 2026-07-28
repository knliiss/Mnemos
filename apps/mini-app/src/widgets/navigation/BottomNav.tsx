import {Clock3, Home, Search, Settings, UserRound} from "lucide-react"
import {NavLink} from "react-router-dom"
import {useTranslation} from "react-i18next"

export function BottomNav() {
    const {t} = useTranslation()
    const items = [
        ["/settings", Settings, t("nav.settings")],
        ["/history", Clock3, t("nav.history")],
        ["/", Home, t("nav.home")],
        ["/search", Search, t("nav.search")],
        ["/account", UserRound, t("nav.account")]
    ] as const
    return <nav className="bottom-nav">{items.map(([to, Icon, label]) => <NavLink key={to} to={to} end={to === "/"}
                                                                                  aria-label={label}><Icon
        size={19}/><span>{label}</span></NavLink>)}</nav>
}
