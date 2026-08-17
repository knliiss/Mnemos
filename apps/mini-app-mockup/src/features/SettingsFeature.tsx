import { useState } from "react"
import type { LocalizationLanguage } from "../model"
import { Field, PageHeader, Segmented } from "../shared/Primitives"

type PresentationMode = "FULL" | "COMPACT"

export function SettingsFeature({ back }: { back: () => void }) {
  const [username, setUsername] = useState("knalis")
  const [language, setLanguage] = useState<LocalizationLanguage>("UK")
  const [spotlightMode, setSpotlightMode] = useState<PresentationMode>("FULL")

  return <div className="screen"><PageHeader title="Налаштування" back={back} /><div className="form-stack settings-form"><Field label="Username"><input value={username} maxLength={32} onChange={event => setUsername(event.target.value)} /></Field><Field label="Мова"><Segmented value={language} options={["UK", "EN", "RU"] as const} onChange={setLanguage} label={value => ({ UK: "Українська", EN: "English", RU: "Русский" }[value])} /></Field><Field label="Spotlight" hint="Як показувати paid drop alerts"><Segmented value={spotlightMode} options={["FULL", "COMPACT"] as const} onChange={setSpotlightMode} label={value => value === "FULL" ? "Повний" : "Компактний"} /></Field><button className="primary-action">Зберегти</button></div></div>
}
