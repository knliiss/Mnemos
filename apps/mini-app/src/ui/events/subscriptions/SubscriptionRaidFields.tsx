import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"
import { Field } from "../../shared/Primitives"

type SubscriptionRaidFieldsProps = {
  raidInput: string
  raidLocations: number[]
  cleanRaidInput: string
  raidInputValid: boolean
  onRaidInputChange(value: string): void
  onAddLocation(): void
  onRemoveLocation(location: number): void
}

export function SubscriptionRaidFields({
  raidInput,
  raidLocations,
  cleanRaidInput,
  raidInputValid,
  onRaidInputChange,
  onAddLocation,
  onRemoveLocation,
}: SubscriptionRaidFieldsProps) {
  const { t } = useTranslation()

  return (
    <Field
      label={t("events.subscriptions.locations")}
      hint={
        raidInputValid
          ? t("events.subscriptions.allLocationsHint")
          : t("events.subscriptions.locationInvalid")
      }
    >
      <div className="inline-input">
        <input
          type="number"
          min="1"
          step="1"
          value={raidInput}
          onChange={(event) => onRaidInputChange(event.target.value)}
          placeholder="№"
        />
        <button
          type="button"
          aria-label={t("events.subscriptions.addLocation")}
          disabled={!cleanRaidInput || !raidInputValid}
          onClick={onAddLocation}
        >
          <Glyph name="plus" size={17} />
        </button>
      </div>

      <div className="token-list">
        {raidLocations.map((location) => (
          <button key={location} type="button" onClick={() => onRemoveLocation(location)}>
            {location}
            <Glyph name="close" size={12} />
          </button>
        ))}
      </div>
    </Field>
  )
}
