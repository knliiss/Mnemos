import { motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  getBoosterLeaderboard,
  getCoinLeaderboard,
  getDropLeaderboard,
  type DropLeaderboardDirection,
  type DropLeaderboardSort,
} from "@/shared/api/leaderboardApi"
import { listWipes, type Wipe } from "@/shared/api/wipeApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"
import {
  orderDropLeaderboardEntries,
  presentBoosterEntries,
  presentCoinEntries,
  presentDropEntries,
} from "./leaderboardPresentation"
import { LeaderboardPagination } from "./LeaderboardPagination"
import { LeaderboardPeriodSelector } from "./LeaderboardPeriodSelector"
import { LeaderboardPodium } from "./LeaderboardPodium"
import { DropRankingControls } from "./LeaderboardRankingControls"
import { LeaderboardRows } from "./LeaderboardRows"
import "./leaderboards.css"
import "./leaderboard-ranking.css"
import "./leaderboard-polish.css"

type LeaderboardTab = "DROPS" | "COINS" | "BOOSTERS"

const tabs: LeaderboardTab[] = ["DROPS", "COINS", "BOOSTERS"]
const tabIllustrations: Record<LeaderboardTab, string> = {
  DROPS: "/ui-illustrations/leaderboard-tab-drops.svg",
  COINS: "/ui-illustrations/leaderboard-tab-coins.svg",
  BOOSTERS: "/ui-illustrations/leaderboard-tab-boosters.svg",
}
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
  const [tab, setTab] = useState<LeaderboardTab>("DROPS")
  const [wipes, setWipes] = useState<Wipe[]>([])
  const [wipeId, setWipeId] = useState<string | null>(null)
  const [wipeRevision, setWipeRevision] = useState(0)
  const [requestRevision, setRequestRevision] = useState(0)
  const [dropSort, setDropSort] = useState<DropLeaderboardSort>("TOTAL")
  const [dropDirection, setDropDirection] = useState<DropLeaderboardDirection>("DESC")
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
          : tab === "BOOSTERS"
            ? getBoosterLeaderboard({
                wipeId,
                limit: leaderboardLimit,
              }).then((result) =>
                presentBoosterEntries(result, locale, t("leaderboards.metrics.boosters")),
              )
            : getDropLeaderboard({
                wipeId,
                sort: dropSort,
                direction: dropDirection,
                limit: leaderboardLimit,
              }).then((result) =>
                presentDropEntries(
                  orderDropLeaderboardEntries(result, dropSort, dropDirection),
                  dropSort,
                  locale,
                  {
                    total: t("leaderboards.metrics.drops"),
                    mythical: t("leaderboards.metrics.mythical"),
                    secret: t("leaderboards.metrics.secret"),
                  },
                ),
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
  }, [dropDirection, dropSort, locale, requestRevision, t, tab, wipeId])

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

  function changePage(nextPage: number) {
    setPageMotionDirection(nextPage > effectivePage ? 1 : -1)
    setPage(nextPage)
  }

  function openProfile(entry: LeaderboardDisplayEntry) {
    navigate({
      id: "public-profile",
      params: entry.accountId ? { accountId: entry.accountId } : { nickname: entry.nickname },
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
      <header className="leaderboard-header">
        <button
          type="button"
          className="leaderboard-header__back"
          onClick={back}
          aria-label={t("common.back")}
        >
          <Glyph name="back" size={18} />
        </button>

        <div className="leaderboard-header__copy">
          <span>{t("leaderboards.eyebrow")}</span>
          <h1>{t("leaderboards.rankBy")}</h1>
          <p>{t("leaderboards.heroDescription")}</p>
        </div>

        <img
          className="leaderboard-header__art"
          src="/ui-illustrations/leaderboard-hero.svg"
          alt=""
          aria-hidden="true"
        />
      </header>

      <div className="leaderboard-navigation">
        <nav className="leaderboard-tabs" aria-label={t("leaderboards.rankBy")}>
          {tabs.map((value) => {
            const active = value === tab

            return (
              <button
                key={value}
                type="button"
                className={active ? "is-active" : ""}
                aria-pressed={active}
                onClick={() => changeTab(value)}
              >
                <span className="leaderboard-tabs__art" aria-hidden="true">
                  <img src={tabIllustrations[value]} alt="" />
                </span>
                <span className="leaderboard-tabs__label">
                  {t(`leaderboards.tabs.${value.toLowerCase()}`)}
                </span>
              </button>
            )
          })}
        </nav>

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
            <Glyph name="wallet" size={17} />
            <span>{t("leaderboards.currentBalanceDescription")}</span>
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
      </div>

      <section
        className={`leaderboard-results ${loading && entries.length > 0 ? "is-refreshing" : ""}`}
        aria-busy={loading}
      >
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
                <LeaderboardPodium entries={podiumEntries} onSelect={openProfile} />
              ) : null}
              <LeaderboardRows entries={rowEntries} onSelect={openProfile} />
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
