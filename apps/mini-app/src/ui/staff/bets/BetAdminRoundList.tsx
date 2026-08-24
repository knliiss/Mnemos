import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { EventBetRoundResponse } from "@/shared/api/betApi"
import type { AdminEventBetRoundMetadataResponse } from "@/shared/api/betAdminApi"
import type { Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import { BetAdminRoundRow } from "./BetAdminRoundRow"
import { betAdminEventLabel, betAdminRoundStatusLabel } from "./betAdminPresentation"

type Props = {
  title: string
  rounds: EventBetRoundResponse[]
  roundMetadata: AdminEventBetRoundMetadataResponse[]
  loading: boolean
  navigate: (route: Route) => void
  back: () => void
}

export function BetAdminRoundList({
  title,
  rounds,
  roundMetadata,
  loading,
  navigate,
  back,
}: Props) {
  const { t } = useTranslation()
  const [query, setQuery] = useState("")
  const [hideEmptyRounds, setHideEmptyRounds] = useState(false)
  const metadataByRound = useMemo(
    () => new Map(roundMetadata.map((metadata) => [metadata.roundId, metadata])),
    [roundMetadata],
  )
  const emptyRoundCount = useMemo(
    () =>
      rounds.filter((round) => {
        const metadata = metadataByRound.get(round.id)

        return metadata ? metadata.betCount === 0 : false
      }).length,
    [metadataByRound, rounds],
  )
  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return rounds.filter((round) => {
      const metadata = metadataByRound.get(round.id)

      if (hideEmptyRounds && metadata?.betCount === 0) {
        return false
      }

      if (!normalized) {
        return true
      }

      const eventLabel = betAdminEventLabel(t, round.eventType).toLowerCase()
      const statusLabel = betAdminRoundStatusLabel(t, round.status).toLowerCase()
      const bettorMatch = metadata?.bettors.some((bettor) => {
        const nickname = bettor.accountNickname?.toLowerCase() ?? ""
        const accountId = bettor.accountId.toLowerCase()

        return nickname.includes(normalized) || accountId.includes(normalized)
      })

      return (
        eventLabel.includes(normalized) || statusLabel.includes(normalized) || bettorMatch === true
      )
    })
  }, [hideEmptyRounds, metadataByRound, query, rounds, t])

  return (
    <div className="screen bet-admin-screen">
      <PageHeader title={title} back={back} />

      <div className="bet-admin-round-tools">
        <label className="staff-search">
          <Glyph name="search" size={17} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={t("betAdmin.searchPlaceholder")}
          />
        </label>

        <button
          type="button"
          className={`bet-admin-empty-toggle ${hideEmptyRounds ? "is-active" : ""}`}
          aria-pressed={hideEmptyRounds}
          onClick={() => setHideEmptyRounds((current) => !current)}
        >
          <span className="bet-admin-empty-toggle__indicator" aria-hidden="true" />
          <span>{t("betAdmin.hideEmptyRounds")}</span>
          <b>{emptyRoundCount}</b>
        </button>
      </div>

      {loading ? (
        <FeatureLoading />
      ) : (
        <section className="bet-admin-round-stack">
          {visible.map((round) => (
            <BetAdminRoundRow
              key={round.id}
              round={round}
              onClick={() =>
                navigate({
                  id: "bet-admin",
                  params: {
                    section: "round",
                    roundId: round.id,
                  },
                })
              }
            />
          ))}

          {!visible.length ? <p className="inline-hint">{t("betAdmin.noRounds")}</p> : null}
        </section>
      )}
    </div>
  )
}
