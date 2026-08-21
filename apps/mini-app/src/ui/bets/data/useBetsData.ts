import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react"
import {
  listEventBetRounds,
  listItemRaces,
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
  error: boolean
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
  const [error, setError] = useState(false)
  const [loadedAccountId, setLoadedAccountId] = useState<string | null>(null)
  const requestRevision = useRef(0)
  const accountIdRef = useRef(activeAccountId)

  useLayoutEffect(() => {
    accountIdRef.current = activeAccountId
  }, [activeAccountId])

  const refresh = useCallback(async () => {
    const requestedAccountId = activeAccountId
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)
    setError(false)

    try {
      const [roundResponses, raceResponses] = await Promise.all([
        listEventBetRounds(),
        listItemRaces(),
      ])

      if (revision !== requestRevision.current || accountIdRef.current !== requestedAccountId) {
        return
      }

      const currentPositions = raceResponses.flatMap((race) =>
        race.participants.filter((participant) => participant.accountId === requestedAccountId),
      )

      setRounds(roundResponses)
      setRaces(raceResponses)
      setPositions(currentPositions)
      setLoadedAccountId(requestedAccountId)
    } catch (error) {
      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setRounds([])
        setRaces([])
        setPositions([])
        setLoadedAccountId(requestedAccountId)
        setError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setLoading(false)
      }
    }
  }, [activeAccountId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
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

  const current = loadedAccountId === activeAccountId

  return {
    rounds: current ? rounds : [],
    races: current ? races : [],
    positions: current ? positions : [],
    loading: loading || !current,
    error: current && error,
    refresh,
  }
}
