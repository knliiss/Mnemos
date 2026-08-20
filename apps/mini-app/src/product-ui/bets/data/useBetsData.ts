import { useCallback, useEffect, useState } from "react"
import {
  listEventBetRounds,
  listItemRaces,
  listMyItemRacePositions,
  type EventBetRoundResponse,
  type ItemRaceParticipantResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"

type BetsData = {
  rounds: EventBetRoundResponse[]
  races: ItemRaceResponse[]
  positions: ItemRaceParticipantResponse[]
  loading: boolean
  refresh(): Promise<void>
}

export function useBetsData(refreshAccount: () => Promise<void>): BetsData {
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
  const [races, setRaces] = useState<ItemRaceResponse[]>([])
  const [positions, setPositions] = useState<ItemRaceParticipantResponse[]>([])
  const [loading, setLoading] = useState(true)

  const refresh = useCallback(async () => {
    try {
      const [roundResponses, raceResponses, positionResponses] = await Promise.all([
        listEventBetRounds(),
        listItemRaces(),
        listMyItemRacePositions(),
      ])

      setRounds(roundResponses)
      setRaces(raceResponses)
      setPositions(positionResponses)
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

  useEffect(() => {
    return realtimeEvents.subscribe((event) => {
      if (event.type !== "bet.operation.completed") {
        return
      }

      void refresh()
      void refreshAccount()
    })
  }, [refresh, refreshAccount])

  return {
    rounds,
    races,
    positions,
    loading,
    refresh,
  }
}
