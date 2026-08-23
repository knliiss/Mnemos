import { useTranslation } from "react-i18next"
import type { Route } from "../../model"
import { FeatureError } from "../../system/FeatureError"
import { BetAdminDashboard } from "./BetAdminDashboard"
import { BetAdminRaceManagement } from "./BetAdminRaceManagement"
import { BetAdminRoundDetail } from "./BetAdminRoundDetail"
import { BetAdminRoundList } from "./BetAdminRoundList"
import { activeBetRounds, normalizeBetAdminSection, settledBetRounds } from "./betAdminPresentation"
import { useBetAdminData } from "./useBetAdminData"
import "./bet-admin.css"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

export function BetAdmin({ params, navigate, back }: Props) {
  const { t } = useTranslation()
  const { rounds, races, loading, error, refresh } = useBetAdminData()
  const section = normalizeBetAdminSection(params?.section)

  if (error && !rounds.length && !races.length) {
    return <FeatureError back={back} onRetry={() => void refresh()} />
  }

  if (section === "round") {
    const round = rounds.find((item) => item.id === params?.roundId) ?? null

    return (
      <BetAdminRoundDetail
        key={params?.roundId ?? "missing"}
        round={round}
        loading={loading}
        back={back}
        onResolved={refresh}
      />
    )
  }

  if (section === "rounds") {
    return (
      <BetAdminRoundList
        title={t("betAdmin.currentRounds")}
        rounds={activeBetRounds(rounds)}
        loading={loading}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (section === "history") {
    return (
      <BetAdminRoundList
        title={t("betAdmin.historyTitle")}
        rounds={settledBetRounds(rounds)}
        loading={loading}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (section === "races") {
    return (
      <BetAdminRaceManagement races={races} loading={loading} back={back} onChanged={refresh} />
    )
  }

  return (
    <BetAdminDashboard
      rounds={rounds}
      races={races}
      loading={loading}
      navigate={navigate}
      back={back}
    />
  )
}
