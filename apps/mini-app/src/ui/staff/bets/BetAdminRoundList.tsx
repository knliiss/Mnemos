import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { EventBetRoundResponse } from "@/shared/api/betApi"
import type { Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import { BetAdminRoundRow } from "./BetAdminRoundRow"
import { betAdminEventLabel, betAdminRoundStatusLabel } from "./betAdminPresentation"

type Props = {
  title: string
  rounds: EventBetRoundResponse[]
  loading: boolean
  navigate: (route: Route) => void
  back: () => void
}

export function BetAdminRoundList({ title, rounds, loading, navigate, back }: Props) {
  const { t } = useTranslation()
  const [query, setQuery] = useState("")
  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    if (!normalized) {
      return rounds
    }

    return rounds.filter((round) => {
      const eventLabel = betAdminEventLabel(t, round.eventType).toLowerCase()
      const statusLabel = betAdminRoundStatusLabel(t, round.status).toLowerCase()

      return eventLabel.includes(normalized) || statusLabel.includes(normalized)
    })
  }, [query, rounds, t])

  return (
    <div className="screen bet-admin-screen">
      <PageHeader title={title} back={back} />

      <label className="staff-search">
        <Glyph name="search" size={17} />
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={t("betAdmin.searchPlaceholder")}
        />
      </label>

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
