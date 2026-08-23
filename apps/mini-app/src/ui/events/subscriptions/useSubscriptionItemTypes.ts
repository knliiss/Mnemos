import { useEffect, useState } from "react"
import { searchItemCatalog } from "@/shared/api/eventApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { BetItemType, Subscription } from "../../model"
import { normalizeSubscriptionItemName } from "./subscriptionPresentation"

export function useSubscriptionItemTypes(subscriptions: Subscription[]) {
  const [itemTypesByName, setItemTypesByName] = useState<Record<string, BetItemType>>({})

  useEffect(() => {
    const unknownNames = Array.from(
      new Set(
        subscriptions
          .filter(
            (subscription): subscription is Extract<Subscription, { type: "ITEM_DROP" }> =>
              subscription.type === "ITEM_DROP" && Boolean(subscription.itemName),
          )
          .map((subscription) => subscription.itemName as string)
          .filter((name) => name.trim().length >= 2)
          .filter((name) => !itemTypesByName[normalizeSubscriptionItemName(name)]),
      ),
    )

    if (!unknownNames.length) {
      return
    }

    let cancelled = false

    void Promise.all(
      unknownNames.map(async (name) => {
        const matches = await searchItemCatalog(name, 8)
        const normalizedName = normalizeSubscriptionItemName(name)
        const exact = matches.find(
          (item) => normalizeSubscriptionItemName(item.name) === normalizedName,
        )

        return exact ? ([normalizedName, exact.type] as const) : null
      }),
    )
      .then((entries) => {
        if (cancelled) {
          return
        }

        const resolved = entries.filter(
          (entry): entry is readonly [string, BetItemType] => entry !== null,
        )

        if (!resolved.length) {
          return
        }

        setItemTypesByName((current) => ({
          ...current,
          ...Object.fromEntries(resolved),
        }))
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
        }
      })

    return () => {
      cancelled = true
    }
  }, [itemTypesByName, subscriptions])

  return itemTypesByName
}
