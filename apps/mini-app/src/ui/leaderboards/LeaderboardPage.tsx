import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  getCoinLeaderboard,
  getDropLeaderboard,
  getPredictorLeaderboard,
  type DropLeaderboardSort,
  type LeaderboardItemRarity,
  type LeaderboardItemType,
  type PredictorLeaderboardSort,
} from "@/shared/api/leaderboardApi"
import { listWipes, type Wipe } from "@/shared/api/wipeApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PageHeader, Segmented } from "../shared/Primitives"
import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"
import {
  presentCoinEntries,
  presentDropEntries,
  presentPredictorEntries,
} from "./leaderboardPresentation"
import { LeaderboardPeriodSelector } from "./LeaderboardPeriodSelector"
import { LeaderboardPodium } from "./LeaderboardPodium"
import {
  DropRankingControls,
  PredictorRankingControls,
} from "./LeaderboardRankingControls"
import { LeaderboardRows } from "./LeaderboardRows"
import "./leaderboards.css"

type LeaderboardTab = "DROPS" | "COINS" | "PREDICTORS"

const tabs: LeaderboardTab[] = ["DROPS", "COINS", "PREDICTORS"]

export function LeaderboardPage({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const [tab, setTab] = useState<LeaderboardTab>("DROPS")
  const [wipes, setWipes] = useState<Wipe[]>([])
  const [wipeId, setWipeId] = useState<string | null>(null)
  const [wipeRevision, setWipeRevision] = useState(0)
  const [requestRevision, setRequestRevision] = useState(0)

  const [dropSort, setDropSort] = useState<DropLeaderboardSort>("TOTAL")
  const [itemType, setItemType] = useState<LeaderboardItemType | "">("")
  const [rarity, setRarity] = useState<LeaderboardItemRarity | "">("")
  const [nicknameDraft, setNicknameDraft] = useState("")
  const [itemNameDraft, setItemNameDraft] = useState("")
  const [nickname, setNickname] = useState("")
  const [itemName, setItemName] = useState("")

  const [predictorSort, setPredictorSort] = useState<PredictorLeaderboardSort>("PROFIT")
  const [minimumSample, setMinimumSample] = useState(10)

  const [entries, setEntries] = useState<LeaderboardDisplayEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)

  const locale =
    i18n.resolvedLanguage === "en" ? "en-US" : i18n.resolvedLanguage === "ru" ? "ru-RU" : "uk-UA"

  useEffect(() => {
    let cancelled = false

    void listWipes()
      .then((result) => {
        if (cancelled) {
          return
        }

        setWipes(result)
        setLoadError(false)
        setWipeId((current) => {
          if (current && result.some((wipe) => wipe.id === current)) {
            return current
          }

          return result.find((wipe) => wipe.active)?.id ?? result[0]?.id ?? null
        })

        if (result.length === 0) {
          setEntries([])
          setLoading(false)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
          setLoadError(true)
          setLoading(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [wipeRevision])

  useEffect(() => {
    if (tab !== "COINS" && !wipeId) {
      return
    }

    let cancelled = false
    const frame = window.requestAnimationFrame(() => {
      setLoading(true)
      setLoadError(false)

      const request =
        tab === "COINS"
          ? getCoinLeaderboard(50).then((result) =>
              presentCoinEntries(result, locale, t("leaderboards.metrics.coins")),
            )
          : tab === "PREDICTORS"
            ? getPredictorLeaderboard({
                wipeId,
                sort: predictorSort,
                minimumSample,
                limit: 50,
              }).then((result) =>
                presentPredictorEntries(result, predictorSort, locale, {
                  bets: t("leaderboards.metrics.bets"),
                  wins: t("leaderboards.metrics.wins"),
                  profit: t("leaderboards.metrics.coins"),
                  turnover: t("leaderboards.metrics.coins"),
                }),
              )
            : getDropLeaderboard({
                wipeId,
                sort: dropSort,
                itemType: itemType || null,
                rarity: rarity || null,
                itemName,
                nickname,
                limit: 50,
              }).then((result) =>
                presentDropEntries(result, dropSort, locale, {
                  total: t("leaderboards.metrics.drops"),
                  mythical: t("leaderboards.metrics.mythical"),
                  secret: t("leaderboards.metrics.secret"),
                  recent: t("leaderboards.metrics.recent"),
                }),
              )

      void request
        .then((result) => {
          if (!cancelled) {
            setEntries(result)
          }
        })
        .catch((error) => {
          if (!cancelled) {
            handleError(error)
            setEntries([])
            setLoadError(true)
          }
        })
        .finally(() => {
          if (!cancelled) {
            setLoading(false)
          }
        })
    })

    return () => {
      cancelled = true
      window.cancelAnimationFrame(frame)
    }
  }, [
    dropSort,
    itemName,
    itemType,
    locale,
    minimumSample,
    nickname,
    predictorSort,
    rarity,
    requestRevision,
    t,
    tab,
    wipeId,
  ])

  const selectedWipe = useMemo(
    () => wipes.find((wipe) => wipe.id === wipeId) ?? null,
    [wipeId, wipes],
  )

  function applyDropSearch() {
    setNickname(nicknameDraft.trim())
    setItemName(itemNameDraft.trim())
    setRequestRevision((value) => value + 1)
  }

  function resetDropFilters() {
    setItemType("")
    setRarity("")
    setNicknameDraft("")
    setItemNameDraft("")
    setNickname("")
    setItemName("")
    setRequestRevision((value) => value + 1)
  }

  function retry() {
    if (wipes.length === 0) {
      setWipeRevision((value) => value + 1)
    }

    setRequestRevision((value) => value + 1)
  }

  return (
    <div className="screen leaderboard-screen">
      <PageHeader title={t("leaderboards.title")} eyebrow={t("leaderboards.eyebrow")} back={back} />

      <section className="leaderboard-hero">
        <img src="/ui-illustrations/leaderboard-hero.svg" alt="" aria-hidden="true" />
        <div>
          <span>{t("leaderboards.heroEyebrow")}</span>
          <strong>{t("leaderboards.heroTitle")}</strong>
          <small>{t("leaderboards.heroDescription")}</small>
        </div>
      </section>

      <Segmented
        value={tab}
        options={tabs}
        onChange={setTab}
        label={(value) => t(`leaderboards.tabs.${value.toLowerCase()}`)}
      />

      {tab !== "COINS" ? (
        <LeaderboardPeriodSelector
          wipes={wipes}
          wipeId={wipeId}
          selectedWipe={selectedWipe}
          locale={locale}
          onChange={setWipeId}
        />
      ) : (
        <section className="leaderboard-balance-note">
          <div>
            <span>{t("leaderboards.currentBalance")}</span>
            <strong>{t("leaderboards.coinRankingTitle")}</strong>
          </div>
          <small>{t("leaderboards.currentBalanceDescription")}</small>
        </section>
      )}

      {tab === "DROPS" ? (
        <DropRankingControls
          sort={dropSort}
          itemType={itemType}
          rarity={rarity}
          nickname={nicknameDraft}
          itemName={itemNameDraft}
          onSortChange={setDropSort}
          onItemTypeChange={setItemType}
          onRarityChange={setRarity}
          onNicknameChange={setNicknameDraft}
          onItemNameChange={setItemNameDraft}
          onApply={applyDropSearch}
          onReset={resetDropFilters}
        />
      ) : null}

      {tab === "PREDICTORS" ? (
        <PredictorRankingControls
          sort={predictorSort}
          minimumSample={minimumSample}
          onSortChange={setPredictorSort}
          onMinimumSampleChange={setMinimumSample}
        />
      ) : null}

      {loading ? (
        <section className="leaderboard-state">
          <strong>{t("common.loading")}</strong>
          <span className="leaderboard-state__skeleton" />
          <span className="leaderboard-state__skeleton" />
          <span className="leaderboard-state__skeleton" />
        </section>
      ) : loadError ? (
        <section className="leaderboard-state">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" aria-hidden="true" />
          <strong>{t("common.loadErrorTitle")}</strong>
          <small>{t("common.loadErrorCopy")}</small>
          <button type="button" onClick={retry}>
            {t("common.retry")}
          </button>
        </section>
      ) : entries.length === 0 ? (
        <section className="leaderboard-state">
          <img src="/ui-illustrations/leaderboard-hero.svg" alt="" aria-hidden="true" />
          <strong>{t("leaderboards.emptyTitle")}</strong>
          <small>{t("leaderboards.emptyDescription")}</small>
        </section>
      ) : (
        <>
          <LeaderboardPodium entries={entries} />
          <LeaderboardRows entries={entries} />
        </>
      )}
    </div>
  )
}
