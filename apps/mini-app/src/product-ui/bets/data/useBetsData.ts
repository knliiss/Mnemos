import { useCallback, useEffect, useRef, useState } from "react"
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

export function useBetsData(
  activeAccountId: string,
  refreshAccount: () => Promise<void>,
): BetsData {
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
  const [races, setRaces] = useState<ItemRaceResponse[]>([])
  const [positions, setPositions] = useState<ItemRaceParticipantResponse[]>([])
  const [loading, setLoading] = useState(true)
  const requestRevision = useRef(0)
  const accountIdRef = useRef(activeAccountId)

  accountIdRef.current = activeAccountId

  const refresh = useCallback(async () => {
    const requestedAccountId = accountIdRef.current
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)

    try {
      const [roundResponses, raceResponses, positionResponses] = await Promise.all([
        listEventBetRounds(),
        listItemRaces(),
        listMyItemRacePositions(),
      ])

      if (
        revision !== requestRevision.current ||
        accountIdRef.current !== requestedAccountId
      ) {
        return
      }

      setRounds(roundResponses)
      setRaces(raceResponses)
      setPositions(positionResponses)
    } catch (error) {
      if (revision === requestRevision.current) {
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

    return () => window.cancelAnimationFrame(frame)
  }, [activeAccountId, refresh])

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
