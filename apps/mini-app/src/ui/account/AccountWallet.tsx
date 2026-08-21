import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { getAccountTransactions } from "@/shared/api/walletApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useMnemosData } from "../data/MnemosDataProvider"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"
import "./account-wallet.css"

type HistorySort = "NEWEST" | "OLDEST" | "AMOUNT_DESC" | "AMOUNT_ASC"

const historySorts: HistorySort[] = ["NEWEST", "OLDEST", "AMOUNT_DESC", "AMOUNT_ASC"]

export function AccountWallet({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const data = useMnemosData()
  const activeAccount = data.activeAccount
  const activeAccountId = activeAccount?.id
  const canViewHistory = data.hasPermission("VIEW_TRANSACTION_HISTORY")
  const [transactions, setTransactions] = useState<WalletTransactionResponse[]>([])
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")
  const [sortOpen, setSortOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [loadedAccountId, setLoadedAccountId] = useState<string | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [reloadRevision, setReloadRevision] = useState(0)
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useEffect(() => {
    if (!activeAccountId) {
      return
    }

    let cancelled = false
    const timeout = window.setTimeout(() => {
      setLoading(true)
      setLoadError(false)

      if (!canViewHistory) {
        setTransactions([])
        setLoadedAccountId(activeAccountId)
        setLoading(false)
        return
      }

      void getAccountTransactions(activeAccountId, 0, 100)
        .then((response) => {
          if (!cancelled) {
            setTransactions(response.items)
            setLoadedAccountId(activeAccountId)
          }
        })
        .catch((error) => {
          if (!cancelled) {
            setTransactions([])
            setLoadError(true)
            setLoadedAccountId(activeAccountId)
            handleError(error)
          }
        })
        .finally(() => {
          if (!cancelled) {
            setLoading(false)
          }
        })
    }, 0)

    return () => {
      cancelled = true
      window.clearTimeout(timeout)
    }
  }, [activeAccountId, canViewHistory, reloadRevision])

  const visibleTransactions = useMemo(
    () => (loadedAccountId === activeAccountId ? transactions : []),
    [activeAccountId, loadedAccountId, transactions],
  )
  const visibleLoading = canViewHistory && (loading || loadedAccountId !== activeAccountId)

  const totalEarned = useMemo(
    () =>
      visibleTransactions
        .filter((transaction) => transaction.type === "CREDIT")
        .reduce((sum, transaction) => sum + transaction.amount, 0),
    [visibleTransactions],
  )

  const totalSpent = useMemo(
    () =>
      visibleTransactions
        .filter((transaction) => transaction.type === "DEBIT")
        .reduce((sum, transaction) => sum + transaction.amount, 0),
    [visibleTransactions],
  )

  const history = useMemo(
    () =>
      [...visibleTransactions].sort((left, right) => {
        if (historySort === "OLDEST") {
          return new Date(left.createdAt).getTime() - new Date(right.createdAt).getTime()
        }

        if (historySort === "AMOUNT_DESC") {
          return right.amount - left.amount
        }

        if (historySort === "AMOUNT_ASC") {
          return left.amount - right.amount
        }

        return new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
      }),
    [historySort, visibleTransactions],
  )

  if (!activeAccount) {
    return null
  }

  const selectSort = (sort: HistorySort) => {
    setHistorySort(sort)
    setSortOpen(false)
  }

  const sortLabel = (sort: HistorySort) => t(`accountWallet.sort.${sort}`)
  const formatTransactionDate = (value: string) => {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
      return value
    }

    return new Intl.DateTimeFormat(locale, {
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date)
  }
  const transactionSourceLabel = (source: string) => {
    const translated = t(`accountWallet.sources.${source}`, { defaultValue: "" })
    return translated || source.replaceAll("_", " ")
  }

  return (
    <div className="screen account-wallet-screen">
      <PageHeader title={t("accountWallet.title")} eyebrow={activeAccount.nickname} back={back} />

      <section className="coin-wallet-hero">
        <div className="coin-wallet-hero__copy">
          <span className="coin-wallet-hero__label">
            <CurrencyMark currency="COIN" size={20} />
            <strong>COIN</strong>
          </span>
          <strong className="coin-wallet-hero__value">
            {activeAccount.coin.toLocaleString(locale)}
          </strong>
          <small>{t("accountWallet.balance")}</small>
        </div>

        <img
          className="coin-wallet-hero__art"
          src="/ui-illustrations/coin-vault.png"
          alt=""
          aria-hidden="true"
        />

        <div className="coin-wallet-hero__stats">
          <span>
            <small>{t("accountWallet.earned")}</small>
            <strong>{canViewHistory ? `+${totalEarned.toLocaleString(locale)}` : "—"}</strong>
          </span>
          <span>
            <small>{t("accountWallet.spent")}</small>
            <strong>{canViewHistory ? `−${totalSpent.toLocaleString(locale)}` : "—"}</strong>
          </span>
        </div>
      </section>

      <section className="coin-history-panel coin-history-panel--detailed">
        <header className="wallet-history-head">
          <span>
            <small>{t("accountWallet.movement")}</small>
            <strong>{t("accountWallet.history")}</strong>
          </span>

          <div
            className={`wallet-history-sort wallet-history-sort--custom ${sortOpen ? "is-open" : ""}`}
          >
            <button
              type="button"
              className="wallet-history-sort__trigger"
              aria-expanded={sortOpen}
              disabled={!canViewHistory}
              onClick={() => setSortOpen((value) => !value)}
            >
              <Glyph name="swap" size={14} />
              <span>{sortLabel(historySort)}</span>
              <b>⌄</b>
            </button>

            <AnimatePresence>
              {sortOpen && canViewHistory ? (
                <motion.div
                  className="wallet-history-sort__menu"
                  initial={{ opacity: 0, y: -6, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -4, scale: 0.985 }}
                >
                  {historySorts.map((sort) => (
                    <button
                      key={sort}
                      type="button"
                      className={historySort === sort ? "is-active" : ""}
                      onClick={() => selectSort(sort)}
                    >
                      <span>{sortLabel(sort)}</span>
                      {historySort === sort ? <Glyph name="check" size={13} /> : null}
                    </button>
                  ))}
                </motion.div>
              ) : null}
            </AnimatePresence>
          </div>
        </header>

        <div className="coin-history-list">
          {canViewHistory
            ? history.map((transaction) => {
                const credit = transaction.type === "CREDIT"

                return (
                  <article key={transaction.id}>
                    <span
                      className={`coin-history-list__sign ${credit ? "is-credit" : "is-debit"}`}
                    >
                      {credit ? "+" : "−"}
                    </span>
                    <span className="coin-history-list__copy">
                      <strong>{transactionSourceLabel(transaction.source)}</strong>
                      <small>{formatTransactionDate(transaction.createdAt)}</small>
                    </span>
                    <span className={`coin-history-list__amount ${credit ? "is-credit" : ""}`}>
                      {credit ? "+" : "−"}
                      <CurrencyValue currency="COIN" value={transaction.amount} compact />
                    </span>
                  </article>
                )
              })
            : null}

          {!canViewHistory ? (
            <div className="coin-history-empty">
              <Glyph name="wallet" size={22} />
              <strong>{t("accountWallet.historyUnavailableTitle")}</strong>
              <small>{t("accountWallet.historyUnavailableCopy")}</small>
            </div>
          ) : null}

          {canViewHistory && !visibleLoading && loadError ? (
            <div className="coin-history-empty">
              <Glyph name="events" size={22} />
              <strong>{t("common.loadErrorTitle")}</strong>
              <small>{t("common.loadErrorCopy")}</small>
              <button type="button" onClick={() => setReloadRevision((value) => value + 1)}>
                {t("common.retry")}
              </button>
            </div>
          ) : null}

          {canViewHistory && !visibleLoading && !loadError && !history.length ? (
            <div className="coin-history-empty">
              <CurrencyMark currency="COIN" size={22} />
              <strong>{t("accountWallet.emptyTitle")}</strong>
              <small>{t("accountWallet.emptyCopy")}</small>
            </div>
          ) : null}

          {visibleLoading ? (
            <div className="coin-history-loading">
              <i />
              <i />
              <i />
            </div>
          ) : null}
        </div>
      </section>
    </div>
  )
}

function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }

  if (language === "ru") {
    return "ru-RU"
  }

  return "uk-UA"
}
