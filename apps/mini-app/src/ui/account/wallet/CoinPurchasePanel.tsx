import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { ApiError } from "@/shared/api/httpClient"
import {
  getCoinPurchaseCatalog,
  purchaseCoin,
  type CoinPurchaseCatalogResponse,
} from "@/shared/api/walletApi"
import { notifications } from "@/shared/notifications/notificationCenter"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { CurrencyMark } from "../../shared/MnemosIcon"
import { CurrencyValue } from "../../shared/Primitives"
import {
  WalletPurchaseSheet,
  type WalletPurchaseMessage,
} from "../../shared/WalletPurchaseSheet"
import "./coin-purchase.css"

type Props = {
  accountId: string
  accountNickname: string
  coinBalance: number
  locale: string
  onPurchased: () => void | Promise<void>
}

export function CoinPurchasePanel({
  accountId,
  accountNickname,
  coinBalance,
  locale,
  onPurchased,
}: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [catalog, setCatalog] = useState<CoinPurchaseCatalogResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [selectedPrimeAmount, setSelectedPrimeAmount] = useState<number | null>(null)
  const [confirmationOpen, setConfirmationOpen] = useState(false)
  const [sheetMessage, setSheetMessage] = useState<WalletPurchaseMessage | null>(null)
  const [busy, setBusy] = useState(false)

  const selectedOption = useMemo(
    () =>
      catalog?.options.find((option) => option.primeAmount === selectedPrimeAmount) ??
      catalog?.options[0] ??
      null,
    [catalog, selectedPrimeAmount],
  )

  const loadCatalog = useCallback(async () => {
    setLoading(true)
    setLoadError(false)

    try {
      const loadedCatalog = await getCoinPurchaseCatalog(accountId)

      setCatalog(loadedCatalog)
      setSelectedPrimeAmount((currentPrimeAmount) => {
        if (loadedCatalog.options.some((option) => option.primeAmount === currentPrimeAmount)) {
          return currentPrimeAmount
        }

        return (loadedCatalog.options[1] ?? loadedCatalog.options[0])?.primeAmount ?? null
      })
    } catch {
      setCatalog(null)
      setSelectedPrimeAmount(null)
      setLoadError(true)
    } finally {
      setLoading(false)
    }
  }, [accountId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void loadCatalog()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [loadCatalog])

  const canBuy = selectedOption !== null && data.user.prime >= selectedOption.primeAmount
  const coinAfterPurchase = selectedOption ? coinBalance + selectedOption.coinAmount : coinBalance
  const primeAfterPurchase = selectedOption
    ? Math.max(0, data.user.prime - selectedOption.primeAmount)
    : data.user.prime

  const openConfirmation = () => {
    if (!selectedOption || !canBuy) {
      return
    }

    setSheetMessage(null)
    setConfirmationOpen(true)
  }

  const closeConfirmation = () => {
    if (busy) {
      return
    }

    setConfirmationOpen(false)
    setSheetMessage(null)
  }

  const confirmPurchase = async () => {
    if (!selectedOption || busy) {
      return
    }

    setBusy(true)
    setSheetMessage(null)

    try {
      const result = await purchaseCoin(accountId, {
        requestId: crypto.randomUUID(),
        primeAmount: selectedOption.primeAmount,
      })

      await Promise.all([data.refreshAccount(), onPurchased()])
      setBusy(false)
      setConfirmationOpen(false)
      setSheetMessage(null)

      notifications.success(
        t("accountWallet.purchase.success", {
          coinAmount: result.coinAmount.toLocaleString(locale),
        }),
      )
      window.Telegram?.WebApp.HapticFeedback?.notificationOccurred("success")
    } catch (error) {
      setBusy(false)

      if (isApiErrorCode(error, "INSUFFICIENT_BALANCE")) {
        setSheetMessage({
          tone: "error",
          text: t("accountWallet.purchase.insufficientPrime"),
        })
        return
      }

      setSheetMessage({
        tone: "error",
        text: t("accountWallet.purchase.failed"),
      })
    }
  }

  return (
    <>
      <section className="coin-buy-panel">
        <header>
          <span>
            <small>{t("accountWallet.purchase.eyebrow")}</small>
            <strong>{t("accountWallet.purchase.title")}</strong>
            <em>{t("accountWallet.purchase.copy")}</em>
          </span>

          <span className="coin-buy-panel__prime">
            <small>{t("accountWallet.purchase.available")}</small>
            <CurrencyValue currency="PRIME" value={data.user.prime} compact />
          </span>
        </header>

        {loading ? (
          <div className="coin-buy-panel__state">{t("accountWallet.purchase.loading")}</div>
        ) : loadError || !catalog ? (
          <button type="button" className="coin-buy-panel__retry" onClick={() => void loadCatalog()}>
            {t("accountWallet.purchase.retry")}
          </button>
        ) : (
          <>
            <div className="coin-pack-grid">
              {catalog.options.map((option) => {
                const selected = option.primeAmount === selectedOption?.primeAmount

                return (
                  <button
                    key={option.primeAmount}
                    type="button"
                    className={selected ? "is-selected" : ""}
                    onClick={() => {
                      setSelectedPrimeAmount(option.primeAmount)
                      setSheetMessage(null)
                    }}
                  >
                    <span className="coin-pack-grid__coin">
                      <CurrencyMark currency="COIN" size={21} />
                    </span>
                    <span className="coin-pack-grid__copy">
                      <small>
                        1 PRIME = {catalog.coinPerPrime.toLocaleString(locale)} COIN
                      </small>
                      <strong>{option.coinAmount.toLocaleString(locale)} COIN</strong>
                      <em>{option.primeAmount.toLocaleString(locale)} PRIME</em>
                    </span>
                  </button>
                )
              })}
            </div>

            {selectedOption ? (
              <div className="coin-buy-summary">
                <div>
                  <small>{t("accountWallet.purchase.afterCoin")}</small>
                  <strong>
                    {coinBalance.toLocaleString(locale)} → {coinAfterPurchase.toLocaleString(locale)}
                  </strong>
                </div>
                <div>
                  <small>{t("accountWallet.purchase.afterPrime")}</small>
                  <strong>
                    {data.user.prime.toLocaleString(locale)} → {primeAfterPurchase.toLocaleString(locale)}
                  </strong>
                </div>
              </div>
            ) : null}

            <button
              type="button"
              className="coin-buy-action"
              disabled={!canBuy}
              onClick={openConfirmation}
            >
              {canBuy && selectedOption ? (
                <>
                  {t("accountWallet.purchase.buyFor")}
                  <CurrencyValue currency="PRIME" value={selectedOption.primeAmount} compact />
                </>
              ) : (
                t("accountWallet.purchase.notEnoughPrime")
              )}
            </button>
          </>
        )}
      </section>

      {confirmationOpen && selectedOption ? (
        <WalletPurchaseSheet
          eyebrow={t("accountWallet.purchase.confirm.eyebrow")}
          title={t("accountWallet.purchase.confirm.title")}
          copy={t("accountWallet.purchase.confirm.copy", { account: accountNickname })}
          receiveLabel={t("accountWallet.purchase.confirm.receive")}
          receiveAmount={selectedOption.coinAmount.toLocaleString(locale)}
          receiveCurrency="COIN"
          payLabel={t("accountWallet.purchase.confirm.pay")}
          payAmount={selectedOption.primeAmount.toLocaleString(locale)}
          payCurrency="PRIME"
          note={t("accountWallet.purchase.confirm.note", {
            balance: data.user.prime.toLocaleString(locale),
          })}
          message={sheetMessage}
          confirmLabel={t("accountWallet.purchase.confirm.submit")}
          cancelLabel={t("accountWallet.purchase.confirm.cancel")}
          busyLabel={t("accountWallet.purchase.confirm.busy")}
          busy={busy}
          onConfirm={() => void confirmPurchase()}
          onCancel={closeConfirmation}
        />
      ) : null}
    </>
  )
}

function isApiErrorCode(error: unknown, code: string): boolean {
  return error instanceof ApiError && typeof error.body !== "string" && error.body.code === code
}
