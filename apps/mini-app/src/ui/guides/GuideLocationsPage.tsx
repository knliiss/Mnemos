import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { GUIDE_LOCATIONS, type GuideLocation } from "./guideCatalog"
import { GuideLocationDetail } from "./GuideLocationDetail"
import { GUIDE_DUMP_LOCATION_BY_ID } from "./guideLocationDumpData"
import { guideLocationUiText } from "./guideLocationUiText"
import {
  localizationKeyMatchesQuery,
  locationMatchesQuery,
  type SaoCatalogs,
} from "./guideLocalization"
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
    () =>
      GUIDE_LOCATIONS.filter((location) =>
        locationMatchesDumpQuery(location, query, catalogs),
      ),
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
    <div className="screen guides-screen guide-locations-screen guide-locations-screen--rebuild">
      <GuideLocationsHeader title={locationUi.locationArchive} back={back} backLabel={ui.back} />

      <section className="guide-locations-intro">
        <strong>{locationUi.locationArchive}</strong>
        <p>{locationUi.locationArchiveCopy}</p>
      </section>

      <label className="guides-search guides-search--location-rebuild">
        <span className="guides-search__mark" aria-hidden="true" />
        <input
          type="search"
          value={query}
          onInput={(event) => setQuery(event.currentTarget.value)}
          placeholder={locationUi.searchPlaceholder}
          aria-label={locationUi.searchPlaceholder}
          autoCapitalize="off"
          autoComplete="off"
          autoCorrect="off"
          spellCheck={false}
          enterKeyHint="search"
        />
        {query ? (
          <button type="button" onClick={() => setQuery("")} aria-label={ui.clearSearch}>
            ×
          </button>
        ) : null}
      </label>

      {locations.length ? (
        <section className="guide-location-index" aria-label={locationUi.locationArchive}>
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
        <section className="guide-location-no-results">
          <span aria-hidden="true">
            <i />
            <b />
          </span>
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
    <button type="button" className="guide-location-index-card" onClick={onOpen}>
      <span className="guide-location-index-card__banner">
        <img src={location.artworkUrl} alt="" loading="lazy" aria-hidden="true" />
        <span>
          {location.id > 0 ? `#${location.id}` : locationUi.specialZone}
        </span>
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
            <b>{detailsAvailable ? dump?.cases.length ?? 0 : "—"}</b>
          </i>
          <i>
            <small>{locationUi.mobs}</small>
            <b>{detailsAvailable ? dump?.mobs.length ?? 0 : "—"}</b>
          </i>
        </span>
      </span>

      <span className="guide-location-index-card__chevron" aria-hidden="true" />
    </button>
  )
}

function GuideLocationsHeader({
  title,
  back,
  backLabel,
}: {
  title: string
  back: () => void
  backLabel: string
}) {
  return (
    <header className="guides-page-header guides-page-header--plain">
      <button type="button" onClick={back} aria-label={backLabel}>
        <span aria-hidden="true">‹</span>
      </button>
      <strong>{title}</strong>
      <span aria-hidden="true" />
    </header>
  )
}

function locationMatchesDumpQuery(
  location: GuideLocation,
  query: string,
  catalogs: SaoCatalogs,
) {
  if (locationMatchesQuery(location, query, catalogs)) {
    return true
  }

  const dump = GUIDE_DUMP_LOCATION_BY_ID.get(location.id)

  if (!dump || !query.trim()) {
    return !query.trim()
  }

  const keys = [
    ...dump.cases.flatMap((caseData) => [
      caseData.key,
      ...caseData.drops.map((drop) => drop.key),
    ]),
    ...dump.mobs.flatMap((mob) => [mob.key, ...mob.drops.map((drop) => drop.key)]),
  ]

  return keys.some((key) => localizationKeyMatchesQuery(key, query, catalogs))
}
