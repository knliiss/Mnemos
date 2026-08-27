import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { ApiError } from "@/shared/api/httpClient"
import {
  getCoinPurchaseCatalog,
  purchaseCoin,
  type CoinPurchaseCatalogResponse,
  type CoinPurchaseOptionResponse,
} from "@/shared/api/walletApi"
import { notifications } from "@/shared/notifications/notificationCenter"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { CurrencyMark } from "../../shared/MnemosIcon"
import { WalletPurchaseSheet } from "../../shared/WalletPurchaseSheet"
import "./coin-purchase.css"

type Props = {
  accountId: string
  accountNickname: string
  onPurchased: () => Promise<void>
}

export function CoinPurchasePanel({ accountId, accountNickname, onPurchased }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [catalog, setCatalog] = useState<CoinPurchaseCatalogResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [selectedOption, setSelectedOption] = useState<CoinPurchaseOptionResponse | null>(null)
  const [busy, setBusy] = useState(false)

  const loadCatalog = useCallback(async () => {
    setLoading(true)
    setLoadError(false)

    try {
      setCatalog(await getCoinPurchaseCatalog(accountId))
    } catch {
      setCatalog(null)
      setLoadError(true)
    } finally {
      setLoading(false)
    }
  }, [accountId])

  useEffect(() => {
    void loadCatalog()
  }, [loadCatalog])

  const confirmPurchase = async () => {
    if (!selectedOption || busy) {
      return
    }

    setBusy(true)

    try {
      const result =
        await purchaseCoin(accountId, {
          requestId: crypto.randomUUID(),
          primeAmount: selectedOption.primeAmount,
        })

      await Promise.all([data.refreshAccount(), onPurchased()])
      setSelectedOption(null)
      notifications.success(
        t("accountWallet.purchase.success", {
          coinAmount: result.coinAmount.toLocaleString(),
        }),
      )
      window.Telegram?.WebApp.HapticFeedback?.notificationOccurred("success")
    } catch (error) {
      if (isApiErrorCode(error, "INSUFFICIENT_BALANCE")) {
        notifications.error(t("accountWallet.purchase.insufficientPrime"))
      } else {
        notifications.error(t("accountWallet.purchase.failed"))
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <section className="coin-purchase-panel">
        <header className="coin-purchase-panel__header">
          <span>
            <small>{t("accountWallet.purchase.eyebrow")}</small>
            <strong>{t("accountWallet.purchase.title")}</strong>
            <em>{t("accountWallet.purchase.copy")}</em>
          </span>

          {catalog ? (
            <span className="coin-purchase-panel__rate">
              1 PRIME = {catalog.coinPerPrime.toLocaleString()} COIN
            </span>
          ) : null}
        </header>

        {loading ? (
          <div className="coin-purchase-panel__state">{t("accountWallet.purchase.loading")}</div>
        ) : loadError || !catalog ? (
          <button type="button" className="coin-purchase-panel__retry" onClick={() => void loadCatalog()}>
            {t("accountWallet.purchase.retry")}
          </button>
        ) : (
          <div className="coin-purchase-options">
            {catalog.options.map((option) => (
              <button
                key={option.primeAmount}
                type="button"
                className="coin-purchase-option"
                onClick={() => setSelectedOption(option)}
              >
                <span>
                  <CurrencyMark currency="COIN" size={17} />
                  <strong>{option.coinAmount.toLocaleString()}</strong>
                  <small>COIN</small>
                </span>
                <b>←</b>
                <span>
                  <CurrencyMark currency="PRIME" size={15} />
                  <strong>{option.primeAmount.toLocaleString()}</strong>
                </span>
              </button>
            ))}
          </div>
        )}
      </section>

      {selectedOption ? (
        <WalletPurchaseSheet
          eyebrow={t("accountWallet.purchase.confirm.eyebrow")}
          title={t("accountWallet.purchase.confirm.title")}
          copy={t("accountWallet.purchase.confirm.copy", { account: accountNickname })}
          receiveLabel={t("accountWallet.purchase.confirm.receive")}
          receiveAmount={selectedOption.coinAmount.toLocaleString()}
          receiveCurrency="COIN"
          payLabel={t("accountWallet.purchase.confirm.pay")}
          payAmount={selectedOption.primeAmount.toLocaleString()}
          payCurrency="PRIME"
          note={t("accountWallet.purchase.confirm.note", {
            balance: data.user.prime.toLocaleString(),
          })}
          confirmLabel={t("accountWallet.purchase.confirm.submit")}
          cancelLabel={t("accountWallet.purchase.confirm.cancel")}
          busyLabel={t("accountWallet.purchase.confirm.busy")}
          busy={busy}
          onConfirm={() => void confirmPurchase()}
          onCancel={() => setSelectedOption(null)}
        />
      ) : null}
    </>
  )
}

function isApiErrorCode(error: unknown, code: string): boolean {
  return error instanceof ApiError && typeof error.body !== "string" && error.body.code === code
}
