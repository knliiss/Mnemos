import { useCallback, useEffect, useLayoutEffect, useRef, useState } from "react"
import {
  getEventBetRound,
  getItemRace,
  listEventBetRounds,
  listItemRaces,
  listMyEventBets,
  listMyItemRaceHistory,
  type EventBetResponse,
  type EventBetRoundResponse,
  type ItemRaceParticipantResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"

export type EventBetHistoryEntry = {
  round: EventBetRoundResponse
  bet: EventBetResponse
}

export type ItemRaceHistoryEntry = {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse
}

type BetsData = {
  rounds: EventBetRoundResponse[]
  races: ItemRaceResponse[]
  positions: ItemRaceParticipantResponse[]
  eventHistory: EventBetHistoryEntry[]
  raceHistory: ItemRaceHistoryEntry[]
  loading: boolean
  error: boolean
  refresh(): Promise<void>
}

const currentRaceStatuses = new Set<ItemRaceResponse["status"]>([
  "REGISTRATION",
  "ACTIVE",
  "SETTLING",
])

async function hydrateEntities<T>(
  current: T[],
  historyIds: string[],
  idOf: (value: T) => string,
  load: (id: string) => Promise<T>,
): Promise<Map<string, T>> {
  const byId = new Map(current.map((value) => [idOf(value), value]))
  const missingIds = [...new Set(historyIds)].filter((id) => !byId.has(id))

  if (!missingIds.length) {
    return byId
  }

  const results = await Promise.allSettled(missingIds.map((id) => load(id)))

  results.forEach((result, index) => {
    if (result.status === "fulfilled") {
      byId.set(missingIds[index], result.value)
    }
  })

  return byId
}

function eventHistoryEntries(
  bets: EventBetResponse[],
  roundsById: Map<string, EventBetRoundResponse>,
): EventBetHistoryEntry[] {
  return bets.flatMap((bet) => {
    const round = roundsById.get(bet.roundId)

    return round ? [{ round, bet }] : []
  })
}

function raceHistoryEntries(
  positions: ItemRaceParticipantResponse[],
  racesById: Map<string, ItemRaceResponse>,
): ItemRaceHistoryEntry[] {
  return positions.flatMap((position) => {
    const race = racesById.get(position.raceId)

    return race ? [{ race, position }] : []
  })
}

function currentRaces(
  races: ItemRaceResponse[],
  positions: ItemRaceParticipantResponse[],
  racesById: Map<string, ItemRaceResponse>,
): ItemRaceResponse[] {
  const byId = new Map(races.map((race) => [race.id, race]))

  for (const position of positions) {
    const race = racesById.get(position.raceId)

    if (race && currentRaceStatuses.has(race.status)) {
      byId.set(race.id, race)
    }
  }

  return [...byId.values()]
}

function currentRacePositions(
  races: ItemRaceResponse[],
  myPositions: ItemRaceParticipantResponse[],
  accountId: string,
): ItemRaceParticipantResponse[] {
  const myPositionByRace = new Map(myPositions.map((position) => [position.raceId, position]))

  return races.flatMap((race) => {
    const embeddedPosition =
      race.participants.find((participant) => participant.accountId === accountId) ?? null
    const position = embeddedPosition ?? myPositionByRace.get(race.id) ?? null

    return position ? [position] : []
  })
}

export function useBetsData(
  activeAccountId: string,
  refreshAccount: () => Promise<void>,
): BetsData {
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
  const [races, setRaces] = useState<ItemRaceResponse[]>([])
  const [positions, setPositions] = useState<ItemRaceParticipantResponse[]>([])
  const [eventHistory, setEventHistory] = useState<EventBetHistoryEntry[]>([])
  const [raceHistory, setRaceHistory] = useState<ItemRaceHistoryEntry[]>([])
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
    setError(false)
    setLoading(true)

    try {
      const [roundResponses, raceResponses, eventBetResponses, racePositionResponses] =
        await Promise.all([
          listEventBetRounds(),
          listItemRaces(),
          listMyEventBets(),
          listMyItemRaceHistory(),
        ])

      if (revision !== requestRevision.current || accountIdRef.current !== requestedAccountId) {
        return
      }

      const [roundsById, racesById] = await Promise.all([
        hydrateEntities(
          roundResponses,
          eventBetResponses.map((bet) => bet.roundId),
          (round) => round.id,
          getEventBetRound,
        ),
        hydrateEntities(
          raceResponses,
          racePositionResponses.map((position) => position.raceId),
          (race) => race.id,
          getItemRace,
        ),
      ])

      if (revision !== requestRevision.current || accountIdRef.current !== requestedAccountId) {
        return
      }

      const betByRoundId = new Map(eventBetResponses.map((bet) => [bet.roundId, bet]))
      const currentRounds = roundResponses.map((round) =>
        round.myBet
          ? round
          : {
              ...round,
              myBet: betByRoundId.get(round.id) ?? null,
            },
      )
      const visibleRaces = currentRaces(raceResponses, racePositionResponses, racesById)
      const currentPositions = currentRacePositions(
        visibleRaces,
        racePositionResponses,
        requestedAccountId,
      )

      setRounds(currentRounds)
      setRaces(visibleRaces)
      setPositions(currentPositions)
      setEventHistory(eventHistoryEntries(eventBetResponses, roundsById))
      setRaceHistory(raceHistoryEntries(racePositionResponses, racesById))
      setLoadedAccountId(requestedAccountId)
    } catch (error) {
      if (revision === requestRevision.current && accountIdRef.current === requestedAccountId) {
        setRounds([])
        setRaces([])
        setPositions([])
        setEventHistory([])
        setRaceHistory([])
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
    eventHistory: current ? eventHistory : [],
    raceHistory: current ? raceHistory : [],
    loading: loading || !current,
    error: current && error,
    refresh,
  }
}
