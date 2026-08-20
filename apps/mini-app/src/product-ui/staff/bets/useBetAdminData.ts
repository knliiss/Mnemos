import { useCallback, useEffect, useRef, useState } from "react"
import {
  listEventBetRounds,
  listItemRaces,
  type EventBetRoundResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { handleError } from "@/shared/errors/errorHandler"

export function useBetAdminData() {
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
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
      const [nextRounds, nextRaces] = await Promise.all([listEventBetRounds(), listItemRaces()])

      if (revision === requestRevision.current) {
        setRounds(nextRounds)
        setRaces(nextRaces)
      }
    } catch (error) {
      if (revision === requestRevision.current) {
        setRounds([])
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
    races,
    loading,
    error,
    refresh,
  }
}
