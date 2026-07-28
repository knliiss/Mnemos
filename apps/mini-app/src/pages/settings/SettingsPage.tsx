import { Bell, ChevronRight, CircleHelp, Eye, Globe2, Info, Link2, Moon, Shield, SlidersHorizontal } from "lucide-react"
import { useTranslation } from "react-i18next"
import { useApp } from "@/app/providers/AppProvider"
import type { Language, ThemeMode } from "@/shared/types/domain"

export function SettingsPage() {
  const { t } = useTranslation()
  const { user, theme, setTheme, setLanguage } = useApp()
  const rows = [[Bell, t("settings.notifications")], [Eye, t("settings.privacy")], [Shield, t("settings.security")], [Link2, t("settings.linked")], [SlidersHorizontal, t("settings.display")], [CircleHelp, t("settings.support")], [Info, t("settings.about")]] as const
  return <div><h1 className="page-title">{t("settings.title")}</h1><div className="settings-list"><label><Globe2 /><span>{t("settings.language")}</span><select value={user?.localizationLanguage ?? "RU"} onChange={event => void setLanguage(event.target.value as Language)}><option value="RU">Русский</option><option value="UK">Українська</option><option value="EN">English</option></select></label><label><Moon /><span>{t("settings.theme")}</span><select value={theme} onChange={event => setTheme(event.target.value as ThemeMode)}><option value="dark">{t("settings.dark")}</option><option value="light">{t("settings.light")}</option><option value="system">{t("settings.system")}</option></select></label>{rows.map(([Icon, label]) => <button key={label} onClick={() => console.info(label)}><Icon /><span>{label}</span><ChevronRight /></button>)}</div></div>
}
