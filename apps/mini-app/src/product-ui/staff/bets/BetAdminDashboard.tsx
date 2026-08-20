import { useTranslation } from "react-i18next"
import type {
  EventBetRoundResponse,
  ItemRaceResponse,
} from "@/shared/api/betApi"
import type { Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureLoading } from "../../system/FeatureLoading"
import { BetAdminRoundRow } from "./BetAdminRoundRow"
import {
  activeBetRounds,
  activeItemRaces,
  settledBetRounds,
} from "./betAdminPresentation"

type Props = {
  rounds: EventBetRoundResponse[]
  races: ItemRaceResponse[]
  loading: boolean
  navigate: (route: Route) => void
  back: () => void
}

export function BetAdminDashboard({ rounds, races, loading, navigate, back }: Props) {
  const { t } = useTranslation()
  const active = activeBetRounds(rounds)
  const history = settledBetRounds(rounds)
  const activeRaces = activeItemRaces(races)
  const participants = new Set(
    races.flatMap((race) => race.participants.map((participant) => participant.accountId)),
  ).size

  return (
    <div className="screen bet-admin-screen bet-admin-dashboard">
      <PageHeader title={t("betAdmin.title")} back={back} />

      <section className="bet-admin-hero">
        <span className="bet-admin-hero__icon">
          <Glyph name="bet" size={22} />
        </span>
        <span>
          <small>{t("betAdmin.workspaceEyebrow")}</small>
          <strong>{t("betAdmin.workspaceTitle")}</strong>
          <em>{t("betAdmin.workspaceDescription")}</em>
        </span>
      </section>

      {loading ? (
        <FeatureLoading />
      ) : (
        <>
          <section className="bet-admin-kpis">
            <article>
              <small>{t("betAdmin.active")}</small>
              <strong>{active.length}</strong>
              <em>{t("betAdmin.rounds")}</em>
            </article>
            <article>
              <small>{t("betAdmin.history")}</small>
              <strong>{history.length}</strong>
              <em>{t("betAdmin.completed")}</em>
            </article>
            <article>
              <small>{t("betAdmin.races")}</small>
              <strong>{activeRaces.length}</strong>
              <em>{t("betAdmin.activeLabel")}</em>
            </article>
            <article>
              <small>{t("betAdmin.players")}</small>
              <strong>{participants}</strong>
              <em>{t("betAdmin.inRace")}</em>
            </article>
          </section>

          <nav className="bet-admin-nav-grid">
            <button type="button"
              onClick={() => navigate({ id: "bet-admin", params: { section: "rounds" } })}
            >
              <span className="is-lime">
                <Glyph name="events" size={20} />
              </span>
              <strong>{t("betAdmin.currentRounds")}</strong>
              <small>{t("betAdmin.currentRoundsDescription")}</small>
              <b>{active.length} ›</b>
            </button>

            <button type="button"
              onClick={() => navigate({ id: "bet-admin", params: { section: "history" } })}
            >
              <span className="is-plum">
                <Glyph name="clock" size={20} />
              </span>
              <strong>{t("betAdmin.historyTitle")}</strong>
              <small>{t("betAdmin.historyDescription")}</small>
              <b>{history.length} ›</b>
            </button>

            <button type="button"
              onClick={() => navigate({ id: "bet-admin", params: { section: "races" } })}
            >
              <span className="is-blue">
                <Glyph name="item-drop" size={20} />
              </span>
              <strong>{t("betAdmin.itemRace")}</strong>
              <small>{t("betAdmin.itemRaceDescription")}</small>
              <b>{activeRaces.length} ›</b>
            </button>
          </nav>

          <section className="bet-admin-recent">
            <header>
              <span>
                <small>{t("betAdmin.needsAttention")}</small>
                <strong>{t("betAdmin.rounds")}</strong>
              </span>
            </header>

            {active.slice(0, 4).map((round) => (
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

            {!active.length ? (
              <p className="inline-hint inline-hint--left">{t("betAdmin.noActiveRounds")}</p>
            ) : null}
          </section>
        </>
      )}
    </div>
  )
}
