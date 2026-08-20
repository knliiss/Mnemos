import { useCallback, useEffect, useState } from "react"
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

  const refresh = useCallback(async () => {
    try {
      const [nextRounds, nextRaces] = await Promise.all([listEventBetRounds(), listItemRaces()])

      setRounds(nextRounds)
      setRaces(nextRaces)
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refresh])

  return {
    rounds,
    races,
    loading,
    refresh,
  }
}
