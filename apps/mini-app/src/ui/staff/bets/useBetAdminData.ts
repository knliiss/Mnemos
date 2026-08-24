import { useCallback, useEffect, useRef, useState } from "react"
import type { EventBetRoundResponse, ItemRaceResponse } from "@/shared/api/betApi"
import {
  listAdminEventBetRoundMetadata,
  listAdminEventBetRounds,
  listAdminItemRaces,
  type AdminEventBetRoundMetadataResponse,
} from "@/shared/api/betAdminApi"
import { handleError } from "@/shared/errors/errorHandler"

export function useBetAdminData() {
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
  const [roundMetadata, setRoundMetadata] = useState<AdminEventBetRoundMetadataResponse[]>([])
  const [races, setRaces] = useState<ItemRaceResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(false)
  const requestRevision = useRef(0)

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)
    setError(false)

    try {
      const [nextRounds, nextRoundMetadata, nextRaces] = await Promise.all([
        listAdminEventBetRounds(),
        listAdminEventBetRoundMetadata(),
        listAdminItemRaces(),
      ])

      if (revision === requestRevision.current) {
        setRounds(nextRounds)
        setRoundMetadata(nextRoundMetadata)
        setRaces(nextRaces)
      }
    } catch (error) {
      if (revision === requestRevision.current) {
        setRounds([])
        setRoundMetadata([])
        setRaces([])
        setError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  return {
    rounds,
    roundMetadata,
    races,
    loading,
    error,
    refresh,
  }
}
