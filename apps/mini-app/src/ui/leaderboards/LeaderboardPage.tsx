import { motion } from "framer-motion"
import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  getCoinLeaderboard,
  getDropLeaderboard,
  getPredictorLeaderboard,
  type DropLeaderboardDirection,
  type DropLeaderboardSort,
  type PredictorLeaderboardSort,
} from "@/shared/api/leaderboardApi"
import { searchPublicProfiles } from "@/shared/api/profileSocialApi"
import { listWipes, type Wipe } from "@/shared/api/wipeApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { Route } from "../model"
import { PageHeader, Segmented } from "../shared/Primitives"
import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"
import {
  presentCoinEntries,
  presentDropEntries,
  presentPredictorEntries,
} from "./leaderboardPresentation"
import { LeaderboardPagination } from "./LeaderboardPagination"
import { LeaderboardPeriodSelector } from "./LeaderboardPeriodSelector"
import { LeaderboardPodium } from "./LeaderboardPodium"
import { DropRankingControls, PredictorRankingControls } from "./LeaderboardRankingControls"
import { LeaderboardRows } from "./LeaderboardRows"
import "./leaderboards.css"
import "./leaderboard-ranking.css"

type LeaderboardTab = "DROPS" | "COINS" | "PREDICTORS"

const tabs: LeaderboardTab[] = ["DROPS", "COINS", "PREDICTORS"]
const pageSize = 10
const leaderboardLimit = 100

export function LeaderboardPage({
  navigate,
  back,
}: {
  navigate: (route: Route) => void
  back: () => void
}) {
  const { t, i18n } = useTranslation()
  const profileAccountIds = useRef(new Map<string, string>())
  const [tab, setTab] = useState<LeaderboardTab>("DROPS")
  const [wipes, setWipes] = useState<Wipe[]>([])
  const [wipeId, setWipeId] = useState<string | null>(null)
  const [wipeRevision, setWipeRevision] = useState(0)
  const [requestRevision, setRequestRevision] = useState(0)
  const [dropSort, setDropSort] = useState<DropLeaderboardSort>("TOTAL")
  const [dropDirection, setDropDirection] = useState<DropLeaderboardDirection>("DESC")
  const [predictorSort, setPredictorSort] = useState<PredictorLeaderboardSort>("PROFIT")
  const [entries, setEntries] = useState<LeaderboardDisplayEntry[]>([])
  const [page, setPage] = useState(1)
  const [pageMotionDirection, setPageMotionDirection] = useState(1)
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
          ? getCoinLeaderboard(leaderboardLimit).then((result) =>
              presentCoinEntries(result, locale, t("leaderboards.metrics.coins")),
            )
          : tab === "PREDICTORS"
            ? getPredictorLeaderboard({
                wipeId,
                sort: predictorSort,
                minimumSample: 10,
                limit: leaderboardLimit,
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
                direction: dropDirection,
                limit: leaderboardLimit,
              }).then((result) =>
                presentDropEntries(result, dropSort, locale, {
                  total: t("leaderboards.metrics.drops"),
                  mythical: t("leaderboards.metrics.mythical"),
                  secret: t("leaderboards.metrics.secret"),
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
  }, [dropDirection, dropSort, locale, predictorSort, requestRevision, t, tab, wipeId])

  const selectedWipe = useMemo(
    () => wipes.find((wipe) => wipe.id === wipeId) ?? null,
    [wipeId, wipes],
  )
  const totalPages = Math.max(1, Math.ceil(entries.length / pageSize))
  const effectivePage = Math.min(page, totalPages)
  const pageEntries = useMemo(
    () => entries.slice((effectivePage - 1) * pageSize, effectivePage * pageSize),
    [effectivePage, entries],
  )
  const podiumEntries = effectivePage === 1 ? pageEntries.slice(0, 3) : []
  const rowEntries = effectivePage === 1 ? pageEntries.slice(3) : pageEntries
  const resultsTitle =
    tab === "COINS"
      ? t("leaderboards.coinRankingTitle")
      : tab === "PREDICTORS"
        ? t("leaderboards.predictorRankingTitle")
        : t("leaderboards.dropRankingTitle")

  function changeTab(value: LeaderboardTab) {
    setTab(value)
    setEntries([])
    setPage(1)
  }

  function changeWipe(value: string) {
    setWipeId(value)
    setPage(1)
  }

  function changeDropSort(value: DropLeaderboardSort) {
    setDropSort(value)
    setPage(1)
  }

  function changeDropDirection(value: DropLeaderboardDirection) {
    setDropDirection(value)
    setPage(1)
  }

  function changePredictorSort(value: PredictorLeaderboardSort) {
    setPredictorSort(value)
    setPage(1)
  }

  function changePage(nextPage: number) {
    setPageMotionDirection(nextPage > effectivePage ? 1 : -1)
    setPage(nextPage)
  }

  async function openProfile(entry: LeaderboardDisplayEntry) {
    const cacheKey = entry.nickname.toLocaleLowerCase()
    let accountId = entry.accountId ?? profileAccountIds.current.get(cacheKey)

    if (!accountId) {
      try {
        const profiles = await searchPublicProfiles(entry.nickname, 0, 20)
        const exactProfile = profiles.content.find(
          (profile) => profile.cristalixNickname.toLocaleLowerCase() === cacheKey,
        )

        if (!exactProfile) {
          return
        }

        accountId = exactProfile.accountId
        profileAccountIds.current.set(cacheKey, accountId)
      } catch (error) {
        handleError(error)
        return
      }
    }

    navigate({
      id: "public-profile",
      params: { accountId },
    })
  }

  function retry() {
    if (wipes.length === 0) {
      setWipeRevision((value) => value + 1)
    }

    setRequestRevision((value) => value + 1)
  }

  const showInitialLoading = loading && entries.length === 0
  const showBlockingError = loadError && entries.length === 0

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

      <div className="leaderboard-navigation">
        <div className="leaderboard-tabs">
          <Segmented
            value={tab}
            options={tabs}
            onChange={changeTab}
            label={(value) => t(`leaderboards.tabs.${value.toLowerCase()}`)}
          />
        </div>

        {tab !== "COINS" ? (
          <LeaderboardPeriodSelector
            wipes={wipes}
            wipeId={wipeId}
            selectedWipe={selectedWipe}
            locale={locale}
            onChange={changeWipe}
          />
        ) : (
          <span className="leaderboard-current-balance">
            {t("leaderboards.currentBalanceDescription")}
          </span>
        )}

        {tab === "DROPS" ? (
          <DropRankingControls
            sort={dropSort}
            direction={dropDirection}
            onSortChange={changeDropSort}
            onDirectionChange={changeDropDirection}
          />
        ) : null}

        {tab === "PREDICTORS" ? (
          <PredictorRankingControls sort={predictorSort} onSortChange={changePredictorSort} />
        ) : null}
      </div>

      <section
        className={`leaderboard-results ${loading && entries.length > 0 ? "is-refreshing" : ""}`}
      >
        <header className="leaderboard-results__header">
          <div>
            <span>{t("leaderboards.rankBy")}</span>
            <strong>{resultsTitle}</strong>
          </div>

          {!showInitialLoading && !showBlockingError && entries.length > 0 ? (
            <small>
              {t("leaderboards.pageOf", {
                page: effectivePage,
                total: totalPages,
              })}
            </small>
          ) : null}
        </header>

        {showInitialLoading ? (
          <div className="leaderboard-state">
            <strong>{t("common.loading")}</strong>
            <span className="leaderboard-state__skeleton" />
            <span className="leaderboard-state__skeleton" />
            <span className="leaderboard-state__skeleton" />
          </div>
        ) : showBlockingError ? (
          <div className="leaderboard-state">
            <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" aria-hidden="true" />
            <strong>{t("common.loadErrorTitle")}</strong>
            <small>{t("common.loadErrorCopy")}</small>
            <button type="button" onClick={retry}>
              {t("common.retry")}
            </button>
          </div>
        ) : entries.length === 0 ? (
          <div className="leaderboard-state">
            <img src="/ui-illustrations/leaderboard-hero.svg" alt="" aria-hidden="true" />
            <strong>{t("leaderboards.emptyTitle")}</strong>
            <small>{t("leaderboards.emptyDescription")}</small>
          </div>
        ) : (
          <>
            <motion.div
              key={`${tab}:${effectivePage}`}
              className="leaderboard-results__page"
              initial={{ opacity: 0.55, x: pageMotionDirection * 10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.18, ease: "easeOut" }}
            >
              {effectivePage === 1 ? (
                <LeaderboardPodium
                  entries={podiumEntries}
                  onSelect={(entry) => void openProfile(entry)}
                />
              ) : null}
              <LeaderboardRows entries={rowEntries} onSelect={(entry) => void openProfile(entry)} />
            </motion.div>

            <LeaderboardPagination
              page={effectivePage}
              totalPages={totalPages}
              onChange={changePage}
            />
          </>
        )}
      </section>
    </div>
  )
}
