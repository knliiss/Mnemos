import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { listAdminBadges } from "@/shared/api/badgeAdminApi"
import { listEventBetRounds, listItemRaces } from "@/shared/api/betApi"
import { listLeaderboardExclusions } from "@/shared/api/leaderboardExclusionApi"
import { listPersonalizationReviewQueue } from "@/shared/api/personalizationModerationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { createSpotlightPreview, spotlightPreviews } from "@/shared/realtime/spotlightPreviewBus"
import type { Route } from "../../model"
import { PageHeader } from "../../shared/Primitives"
import { LeaderboardExclusionAdmin } from "./leaderboards/LeaderboardExclusionAdmin"
import "./administration-control-polish.css"

type AdminStats = {
  review: number
  activeRounds: number
  activeRaces: number
  badges: number
  leaderboardExclusions: number
}

type AdministrationWorkspaceProps = {
  illustration: string
  title: string
  description: string
  tone:
    | "users"
    | "accounts"
    | "review"
    | "bets"
    | "badges"
    | "collectors"
    | "wipes"
    | "spotlight"
    | "leaderboards"
  indicator?: string
  onClick(): void
}

function AdministrationWorkspace({
  illustration,
  title,
  description,
  tone,
  indicator = "›",
  onClick,
}: AdministrationWorkspaceProps) {
  return (
    <button type="button" className={`administration-workspace is-${tone}`} onClick={onClick}>
      <span className="administration-workspace__art" aria-hidden="true">
        <img src={illustration} alt="" />
      </span>
      <span className="administration-workspace__copy">
        <strong>{title}</strong>
        <small>{description}</small>
      </span>
      <b aria-hidden="true">{indicator}</b>
    </button>
  )
}

export function AdministrationHome({
  navigate,
  back,
}: {
  navigate: (route: Route) => void
  back: () => void
}) {
  const { t } = useTranslation()
  const [stats, setStats] = useState<AdminStats | null>(null)
  const [leaderboardExclusionsOpen, setLeaderboardExclusionsOpen] = useState(false)

  useEffect(() => {
    let cancelled = false
    const frame = window.requestAnimationFrame(() => {
      void Promise.all([
        listPersonalizationReviewQueue(),
        listEventBetRounds(),
        listItemRaces(),
        listAdminBadges(),
        listLeaderboardExclusions(),
      ])
        .then(([review, rounds, races, badges, exclusions]) => {
          if (!cancelled) {
            setStats({
              review: review.length,
              activeRounds: rounds.filter((round) => round.status !== "SETTLED").length,
              activeRaces: races.filter(
                (race) => !["SETTLED", "REFUNDED", "CANCELLED"].includes(race.status),
              ).length,
              badges: badges.length,
              leaderboardExclusions: exclusions.length,
            })
          }
        })
        .catch((error) => {
          if (!cancelled) {
            handleError(error)
          }
        })
    })

    return () => {
      cancelled = true
      window.cancelAnimationFrame(frame)
    }
  }, [])

  if (leaderboardExclusionsOpen) {
    return <LeaderboardExclusionAdmin back={() => setLeaderboardExclusionsOpen(false)} />
  }

  const launchSpotlightPreview = () => {
    const preview = createSpotlightPreview({
      itemName: t("administration.spotlightPreviewItem"),
      droppedFor: t("administration.spotlightPreviewPlayer"),
    })

    spotlightPreviews.publish(preview)
  }

  return (
    <div className="screen administration-home-screen">
      <PageHeader title={t("administration.title")} back={back} />

      <section className="workspace-intro workspace-intro--admin workspace-intro--illustrated">
        <span className="workspace-intro__art" aria-hidden="true">
          <img src="/ui-illustrations/workspace-admin.svg" alt="" />
        </span>
        <div>
          <strong>{t("administration.introTitle")}</strong>
          <small>{t("administration.introDescription")}</small>
        </div>
      </section>

      <div className="administration-workspace-grid administration-workspace-grid--identity">
        <AdministrationWorkspace
          illustration="/ui-illustrations/mnemos-mascot-search.svg"
          title={t("administration.users")}
          description={t("administration.usersDescription")}
          tone="users"
          onClick={() => navigate({ id: "moderation-users" })}
        />

        <AdministrationWorkspace
          illustration="/ui-illustrations/mnemos-mascot-cat.svg"
          title={t("moderation.accounts")}
          description={t(
            "administration.accountsDescription",
            "Account lifecycle, ownership, members, roles, wallet history and owned personalization.",
          )}
          tone="accounts"
          onClick={() => navigate({ id: "moderation-accounts" })}
        />
      </div>

      <div className="administration-workspace-grid">
        <AdministrationWorkspace
          illustration="/ui-illustrations/admin-review.svg"
          title={t("administration.personalizations")}
          description={
            stats
              ? t("administration.reviewCount", { count: stats.review })
              : t("administration.reviewQueue")
          }
          tone="review"
          onClick={() => navigate({ id: "personalization-review" })}
        />

        <AdministrationWorkspace
          illustration="/ui-illustrations/admin-bets.svg"
          title={t("administration.bets")}
          description={
            stats
              ? t("administration.betsStats", {
                  rounds: stats.activeRounds,
                  races: stats.activeRaces,
                })
              : t("administration.betsFallback")
          }
          tone="bets"
          onClick={() => navigate({ id: "bet-admin" })}
        />

        <AdministrationWorkspace
          illustration="/ui-illustrations/admin-badges.svg"
          title={t("administration.badges")}
          description={
            stats
              ? t("administration.badgesCount", { count: stats.badges })
              : t("administration.badgesFallback")
          }
          tone="badges"
          onClick={() => navigate({ id: "badge-admin" })}
        />

        <AdministrationWorkspace
          illustration="/ui-illustrations/admin-leaderboards.svg"
          title={t("leaderboardExclusions.workspaceTitle")}
          description={
            stats
              ? t("leaderboardExclusions.workspaceCount", {
                  count: stats.leaderboardExclusions,
                })
              : t("leaderboardExclusions.workspaceDescription")
          }
          tone="leaderboards"
          onClick={() => setLeaderboardExclusionsOpen(true)}
        />

        <AdministrationWorkspace
          illustration="/ui-illustrations/mnemos-mascot-event.svg"
          title={t("administration.spotlightPreviewTitle")}
          description={t("administration.spotlightPreviewDescription")}
          tone="spotlight"
          indicator="▶"
          onClick={launchSpotlightPreview}
        />

        <AdministrationWorkspace
          illustration="/ui-illustrations/wipe-workspace.svg"
          title={t("wipeAdmin.workspaceTitle")}
          description={t("wipeAdmin.workspaceDescription")}
          tone="wipes"
          onClick={() => navigate({ id: "wipe-admin" })}
        />

        <AdministrationWorkspace
          illustration="/ui-illustrations/mnemos-mascot-race.svg"
          title={t("collectorAdmin.workspaceTitle")}
          description={t("collectorAdmin.workspaceDescription")}
          tone="collectors"
          onClick={() => navigate({ id: "collector-admin" })}
        />
      </div>
    </div>
  )
}
