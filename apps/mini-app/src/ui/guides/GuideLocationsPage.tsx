import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { PageHeader } from "../shared/Primitives"
import { GUIDE_LOCATIONS, type GuideLocation } from "./guideCatalog"
import { GuideLocationDetail } from "./GuideLocationDetail"
import { GUIDE_DUMP_LOCATION_BY_ID } from "./guideLocationDumpData"
import { guideLocationUiText } from "./guideLocationUiText"
import {
  localizationKeyMatchesQuery,
  locationMatchesQuery,
  type SaoCatalogs,
} from "./guideLocalization"
import { GuideSearch } from "./GuideSearch"
import { guideUiText } from "./guideUiText"

type GuideUi = ReturnType<typeof guideUiText>

type Props = {
  ui: GuideUi
  catalogs: SaoCatalogs
  text: (key: string) => string
  initialLocationId?: number | null
  back: () => void
}

export function GuideLocationsPage({ ui, catalogs, text, initialLocationId, back }: Props) {
  const { i18n } = useTranslation()
  const locationUi = guideLocationUiText(i18n.resolvedLanguage)
  const [query, setQuery] = useState("")
  const [selectedLocationId, setSelectedLocationId] = useState<number | null>(
    initialLocationId ?? null,
  )
  const selectedLocation =
    GUIDE_LOCATIONS.find((location) => location.id === selectedLocationId) ?? null
  const locations = useMemo(
    () => GUIDE_LOCATIONS.filter((location) => locationMatchesDumpQuery(location, query, catalogs)),
    [catalogs, query],
  )

  if (selectedLocation) {
    return (
      <GuideLocationDetail
        location={selectedLocation}
        catalogs={catalogs}
        text={text}
        ui={ui}
        back={() => setSelectedLocationId(null)}
      />
    )
  }

  return (
    <div className="screen guides-screen guide-locations-screen guide-locations-screen--redesign">
      <PageHeader title={locationUi.locationArchive} eyebrow={ui.locations} back={back} />

      <section className="guide-context-hero guide-context-hero--locations">
        <div className="guide-context-hero__mascot" aria-hidden="true">
          <img src="/ui-illustrations/guides-cartographer.svg" alt="" />
        </div>
        <span>
          <strong>{locationUi.locationArchive}</strong>
          <p>{locationUi.locationArchiveCopy}</p>
        </span>
        <b>{GUIDE_LOCATIONS.length}</b>
      </section>

      <GuideSearch
        value={query}
        placeholder={locationUi.searchPlaceholder}
        clearLabel={ui.clearSearch}
        onChange={setQuery}
      />

      {locations.length ? (
        <section className="guide-location-index guide-location-index--redesign" aria-label={locationUi.locationArchive}>
          {locations.map((location) => (
            <GuideLocationIndexCard
              key={location.id}
              location={location}
              text={text}
              locationUi={locationUi}
              onOpen={() => setSelectedLocationId(location.id)}
            />
          ))}
        </section>
      ) : (
        <section className="guides-empty guides-empty--locations">
          <img src="/ui-illustrations/guides-cartographer.svg" alt="" aria-hidden="true" />
          <strong>{ui.noResults}</strong>
          <small>{ui.noResultsCopy}</small>
        </section>
      )}
    </div>
  )
}

function GuideLocationIndexCard({
  location,
  text,
  locationUi,
  onOpen,
}: {
  location: GuideLocation
  text: (key: string) => string
  locationUi: ReturnType<typeof guideLocationUiText>
  onOpen: () => void
}) {
  const dump = GUIDE_DUMP_LOCATION_BY_ID.get(location.id)
  const detailsAvailable = dump?.detailsAvailable ?? false

  return (
    <button type="button" className="guide-location-index-card guide-location-index-card--redesign" onClick={onOpen}>
      <span className="guide-location-index-card__banner">
        <img src={location.artworkUrl} alt="" loading="lazy" aria-hidden="true" />
        <span>{location.id > 0 ? `#${location.id}` : locationUi.specialZone}</span>
      </span>

      <span className="guide-location-index-card__body">
        <span className="guide-location-index-card__copy">
          <strong>{text(location.nameKey)}</strong>
          <small>{text(location.descriptionKey)}</small>
        </span>

        <span className="guide-location-index-card__facts">
          <i>
            <small>{locationUi.purchasePrice}</small>
            <b>{dump?.purchasePrice || "—"}</b>
          </i>
          <i>
            <small>{locationUi.cases}</small>
            <b>{detailsAvailable ? (dump?.cases.length ?? 0) : "—"}</b>
          </i>
          <i>
            <small>{locationUi.mobs}</small>
            <b>{detailsAvailable ? (dump?.mobs.length ?? 0) : "—"}</b>
          </i>
        </span>
      </span>

      <span className="guide-location-index-card__chevron" aria-hidden="true" />
    </button>
  )
}

function locationMatchesDumpQuery(location: GuideLocation, query: string, catalogs: SaoCatalogs) {
  if (locationMatchesQuery(location, query, catalogs)) {
    return true
  }

  const dump = GUIDE_DUMP_LOCATION_BY_ID.get(location.id)

  if (!dump || !query.trim()) {
    return !query.trim()
  }

  const keys = [
    ...dump.cases.flatMap((caseData) => [caseData.key, ...caseData.drops.map((drop) => drop.key)]),
    ...dump.mobs.flatMap((mob) => [mob.key, ...mob.drops.map((drop) => drop.key)]),
  ]

  return keys.some((key) => localizationKeyMatchesQuery(key, query, catalogs))
}
