import { useState } from "react"
import { useTranslation } from "react-i18next"
import { GuideHub, type GuideHubSection } from "./GuideHub"
import { GuideLocationsPage } from "./GuideLocationsPage"
import { GuideMechanicsPage } from "./GuideMechanicsPage"
import { GuideMilestonesPage } from "./GuideMilestonesPage"
import { useSaoGuideLocalization } from "./guideLocalization"
import { guideUiText } from "./guideUiText"
import "./guide-location-entities.css"
import "./guide-location-npcs.css"
import "./guides.css"
import "./guides-sections.css"
import "./guides-accessibility.css"
import "./guides-redesign.css"
import "./guides-location-rebuild.css"
import "./guides-hub-rebuild.css"
import "./guides-visual-redesign.css"
import "./guides-visual-audit.css"
import "./guides-final-polish.css"
import "./guides-source-fix.css"

type Props = {
  back: () => void
}

type GuideView = "hub" | GuideHubSection

export function GuidesPage({ back }: Props) {
  const { i18n } = useTranslation()
  const ui = guideUiText(i18n.resolvedLanguage)
  const localization = useSaoGuideLocalization(i18n.resolvedLanguage)
  const [view, setView] = useState<GuideView>("hub")
  const [initialLocationId, setInitialLocationId] = useState<number | null>(null)

  if (view === "locations") {
    return (
      <GuideLocationsPage
        ui={ui}
        catalogs={localization.catalogs}
        text={localization.text}
        initialLocationId={initialLocationId}
        back={() => {
          setInitialLocationId(null)
          setView("hub")
        }}
      />
    )
  }

  if (view === "mechanics") {
    return (
      <GuideMechanicsPage
        ui={ui}
        catalogs={localization.catalogs}
        text={localization.text}
        back={() => setView("hub")}
      />
    )
  }

  if (view === "dungeons") {
    return (
      <GuideMilestonesPage
        kind="dungeons"
        ui={ui}
        text={localization.text}
        back={() => setView("hub")}
      />
    )
  }

  return (
    <GuideHub
      ui={ui}
      catalogs={localization.catalogs}
      text={localization.text}
      back={back}
      openSection={setView}
      openLocation={(locationId) => {
        setInitialLocationId(locationId)
        setView("locations")
      }}
    />
  )
}
