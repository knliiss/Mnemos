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
import { LeaderboardPodium } from "./LeaderboardPodium"
import { LeaderboardRows } from "./LeaderboardRows"
import "./leaderboards.css"

type LeaderboardTab = "DROPS" | "COINS" | "PREDICTORS"

const tabs: LeaderboardTab[] = ["DROPS", "COINS", "PREDICTORS"]
const dropSorts: DropLeaderboardSort[] = ["TOTAL", "MYTHICAL", "SECRET", "RECENT"]
const predictorSorts: PredictorLeaderboardSort[] = ["PROFIT", "WIN_RATE", "WINS", "TURNOVER"]
const itemTypes: LeaderboardItemType[] = ["SWORD", "AURA", "PET", "BOOK", "JEWELRY"]
const rarities: LeaderboardItemRarity[] = ["MYTHICAL", "SECRET"]

export function LeaderboardPage({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const [tab, setTab] = useState<LeaderboardTab>("DROPS")
  const [wipes, setWipes] = useState<Wipe[]>([])
  const [wipeId, setWipeId] = useState<string | null>(null)
  const [dropSort, setDropSort] = useState<DropLeaderboardSort>("TOTAL")
  const [predictorSort, setPredictorSort] = useState<PredictorLeaderboardSort>("PROFIT")
  const [minimumSample, setMinimumSample] = useState(10)
  const [itemType, setItemType] = useState<LeaderboardItemType | "">("")
  const [rarity, setRarity] = useState<LeaderboardItemRarity | "">("")
  const [itemName, setItemName] = useState("")
  const [nickname, setNickname] = useState("")
  const [searchRevision, setSearchRevision] = useState(0)
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
        setWipeId(
          (current) => current ?? result.find((wipe) => wipe.active)?.id ?? result[0]?.id ?? null,
        )
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
          setLoadError(true)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

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
    searchRevision,
    t,
    tab,
    wipeId,
  ])

  const selectedWipe = useMemo(
    () => wipes.find((wipe) => wipe.id === wipeId) ?? null,
    [wipeId, wipes],
  )

  return (
    <div className="screen leaderboard-screen">
      <PageHeader title={t("leaderboards.title")} eyebrow={t("leaderboards.eyebrow")} back={back} />

      <section className="leaderboard-hero">
        <img src="/ui-illustrations/leaderboard-hero.svg" alt="" aria-hidden="true" />
        <div>
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

      <section className="leaderboard-toolbar">
        {tab !== "COINS" ? (
          <label>
            <span>{t("leaderboards.wipe")}</span>
            <select value={wipeId ?? ""} onChange={(event) => setWipeId(event.target.value)}>
              {wipes.map((wipe) => (
                <option key={wipe.id} value={wipe.id}>
                  #{wipe.number} {wipe.name ?? t("leaderboards.wipeFallback")}{" "}
                  {wipe.active ? "•" : ""}
                </option>
              ))}
            </select>
          </label>
        ) : (
          <div className="leaderboard-toolbar__note">
            <span>{t("leaderboards.currentBalance")}</span>
            <small>{t("leaderboards.currentBalanceDescription")}</small>
          </div>
        )}

        {selectedWipe && tab !== "COINS" ? (
          <small className="leaderboard-toolbar__boundary">
            {new Intl.DateTimeFormat(locale, { dateStyle: "medium" }).format(
              new Date(selectedWipe.startedAt),
            )}
          </small>
        ) : null}
      </section>

      {tab === "DROPS" ? (
        <form
          className="leaderboard-filters"
          onSubmit={(event) => {
            event.preventDefault()
            setSearchRevision((value) => value + 1)
          }}
        >
          <label>
            <span>{t("leaderboards.sort")}</span>
            <select
              value={dropSort}
              onChange={(event) => setDropSort(event.target.value as DropLeaderboardSort)}
            >
              {dropSorts.map((value) => (
                <option key={value} value={value}>
                  {t(`leaderboards.dropSort.${value.toLowerCase()}`)}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>{t("leaderboards.itemType")}</span>
            <select
              value={itemType}
              onChange={(event) => setItemType(event.target.value as LeaderboardItemType | "")}
            >
              <option value="">{t("common.all")}</option>
              {itemTypes.map((value) => (
                <option key={value} value={value}>
                  {t(`leaderboards.itemTypes.${value.toLowerCase()}`)}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>{t("leaderboards.rarity")}</span>
            <select
              value={rarity}
              onChange={(event) => setRarity(event.target.value as LeaderboardItemRarity | "")}
            >
              <option value="">{t("common.all")}</option>
              {rarities.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </label>

          <label className="leaderboard-filters__search">
            <span>{t("leaderboards.nickname")}</span>
            <input
              value={nickname}
              maxLength={20}
              placeholder={t("leaderboards.nicknamePlaceholder")}
              onChange={(event) => setNickname(event.target.value)}
            />
          </label>

          <label className="leaderboard-filters__search">
            <span>{t("leaderboards.item")}</span>
            <input
              value={itemName}
              maxLength={24}
              placeholder={t("leaderboards.itemPlaceholder")}
              onChange={(event) => setItemName(event.target.value)}
            />
          </label>

          <button type="submit" className="leaderboard-filters__apply">
            {t("leaderboards.apply")}
          </button>
        </form>
      ) : null}

      {tab === "PREDICTORS" ? (
        <div className="leaderboard-filters leaderboard-filters--predictor">
          <label>
            <span>{t("leaderboards.sort")}</span>
            <select
              value={predictorSort}
              onChange={(event) => setPredictorSort(event.target.value as PredictorLeaderboardSort)}
            >
              {predictorSorts.map((value) => (
                <option key={value} value={value}>
                  {t(`leaderboards.predictorSort.${value.toLowerCase()}`)}
                </option>
              ))}
            </select>
          </label>

          {predictorSort === "WIN_RATE" ? (
            <label>
              <span>{t("leaderboards.minimumSample")}</span>
              <input
                type="number"
                min={1}
                max={1000}
                value={minimumSample}
                onChange={(event) => setMinimumSample(Number(event.target.value) || 1)}
              />
            </label>
          ) : null}
        </div>
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
          <button type="button" onClick={() => setSearchRevision((value) => value + 1)}>
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
