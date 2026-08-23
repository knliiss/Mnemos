import type {
  PersonalizationPriceRangeResponse,
  PersonalizationPricingResponse,
} from "@/shared/api/personalizationApi"
import type { Currency, PersonalizationType } from "../../model"
import type { CreatorEditorForm } from "./creatorEditorModel"

export function resolveCreatorPriceRange(
  pricing: PersonalizationPricingResponse,
  type: PersonalizationType,
  currency: Currency,
): PersonalizationPriceRangeResponse | null {
  const resolvedCurrency = type === "NOTIFICATION_PACK" ? "PRIME" : currency

  return (
    pricing.ranges.find(
      (range) =>
        range.origin === "CREATOR" && range.type === type && range.currency === resolvedCurrency,
    ) ?? null
  )
}

export function normalizeCreatorPrice(
  form: CreatorEditorForm,
  pricing: PersonalizationPricingResponse,
): CreatorEditorForm {
  const currency = form.type === "NOTIFICATION_PACK" ? "PRIME" : form.currency
  const range = resolveCreatorPriceRange(pricing, form.type, currency)

  if (!range) {
    return {
      ...form,
      currency,
    }
  }

  const amount = Number(form.price)
  const valid = Number.isInteger(amount) && amount >= range.min && amount <= range.max

  return {
    ...form,
    currency,
    price: valid ? form.price : String(range.min),
  }
}

export function isCreatorPriceValid(
  form: CreatorEditorForm,
  pricing: PersonalizationPricingResponse | null,
): boolean {
  if (!pricing) {
    return false
  }

  const range = resolveCreatorPriceRange(pricing, form.type, form.currency)

  if (!range) {
    return false
  }

  const amount = Number(form.price)

  return Number.isInteger(amount) && amount >= range.min && amount <= range.max
}
