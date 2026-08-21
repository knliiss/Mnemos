import { useEffect, useRef, useState } from "react"
import type { ItemCatalogResponse } from "@/shared/api/backendContracts"
import { searchItemCatalog } from "@/shared/api/eventApi"
import {
  createBoosterSubscription,
  createDungeonSubscription,
  createGlobalSubscription,
  createItemDropSubscription,
  createRaidSubscription,
} from "@/shared/api/subscriptionApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type {
  BoosterType,
  DungeonType,
  GlobalEventType,
  SubscriptionType,
} from "../../model"

export function useSubscriptionCreateDraft(onSaved: () => void) {
  const data = useMnemosData()
  const [busy, setBusy] = useState(false)
  const [type, setType] = useState<SubscriptionType>("ITEM_DROP")
  const [droppedFor, setDroppedFor] = useState("")
  const [itemName, setItemName] = useState("")
  const [boosterType, setBoosterType] = useState<BoosterType>("LUCK")
  const [dungeonType, setDungeonType] = useState<DungeonType>("EASY")
  const [globalType, setGlobalType] = useState<GlobalEventType>("ECLIPSE")
  const [raidLocations, setRaidLocations] = useState<number[]>([])
  const [raidInput, setRaidInput] = useState("")
  const [itemSearch, setItemSearch] = useState<{
    query: string
    items: ItemCatalogResponse[]
  }>({ query: "", items: [] })
  const mutationLocked = useRef(false)
  const mountedRef = useRef(true)

  const cleanDroppedFor = droppedFor.trim()
  const cleanItemName = itemName.trim()
  const cleanRaidInput = raidInput.trim()
  const raidInputValue = Number(cleanRaidInput)

  const droppedForValid =
    !cleanDroppedFor ||
    (cleanDroppedFor.length >= 4 &&
      cleanDroppedFor.length <= 20 &&
      /^[\p{L}0-9_]+$/u.test(cleanDroppedFor))
  const itemNameValid = !cleanItemName || cleanItemName.length <= 24
  const itemDropValid =
    droppedForValid && itemNameValid && Boolean(cleanDroppedFor || cleanItemName)
  const raidInputValid = !cleanRaidInput || (Number.isInteger(raidInputValue) && raidInputValue > 0)
  const createValid = (type !== "ITEM_DROP" || itemDropValid) && (type !== "RAID" || raidInputValid)
  const itemSuggestions = itemSearch.query === cleanItemName ? itemSearch.items : []

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    if (cleanItemName.length < 2) {
      return
    }

    let cancelled = false
    const timeout = window.setTimeout(() => {
      void searchItemCatalog(cleanItemName, 8)
        .then((items) => {
          if (!cancelled) {
            setItemSearch({ query: cleanItemName, items })
          }
        })
        .catch((error) => {
          if (!cancelled) {
            handleError(error)
          }
        })
    }, 220)

    return () => {
      cancelled = true
      window.clearTimeout(timeout)
    }
  }, [cleanItemName])

  const selectItem = (item: ItemCatalogResponse) => {
    setItemName(item.name)
    setItemSearch({ query: item.name, items: [] })
  }

  const addRaidLocation = () => {
    if (!raidInputValid || !cleanRaidInput) {
      return
    }

    if (!raidLocations.includes(raidInputValue)) {
      setRaidLocations((current) => [...current, raidInputValue])
    }

    setRaidInput("")
  }

  const removeRaidLocation = (location: number) => {
    setRaidLocations((current) => current.filter((value) => value !== location))
  }

  const create = async () => {
    if (mutationLocked.current || !createValid) {
      return
    }

    mutationLocked.current = true
    setBusy(true)

    try {
      const submittedRaidLocations =
        cleanRaidInput && raidInputValid
          ? Array.from(new Set([...raidLocations, raidInputValue]))
          : raidLocations

      const saved = await data.mutateSubscriptions(async () => {
        if (type === "ITEM_DROP") {
          await createItemDropSubscription(cleanDroppedFor || undefined, cleanItemName || undefined)
        } else if (type === "BOOSTER") {
          await createBoosterSubscription(boosterType)
        } else if (type === "DUNGEON") {
          await createDungeonSubscription(dungeonType)
        } else if (type === "GLOBAL") {
          await createGlobalSubscription(globalType)
        } else {
          await createRaidSubscription(submittedRaidLocations)
        }
      })

      if (saved && mountedRef.current) {
        onSaved()
      }
    } finally {
      mutationLocked.current = false

      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  return {
    busy,
    type,
    setType,
    droppedFor,
    setDroppedFor,
    cleanDroppedFor,
    droppedForValid,
    itemName,
    setItemName,
    itemSuggestions,
    selectItem,
    boosterType,
    setBoosterType,
    dungeonType,
    setDungeonType,
    globalType,
    setGlobalType,
    raidInput,
    setRaidInput,
    cleanRaidInput,
    raidInputValid,
    raidLocations,
    addRaidLocation,
    removeRaidLocation,
    createValid,
    create,
  }
}
