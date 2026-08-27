import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createStarPrimePurchase,
  getStarPrimePackages,
  getStarPrimePurchase,
  type StarPrimePackageResponse,
  type StarPrimePurchaseStatus,
} from "@/shared/api/paymentApi"
import { notifications } from "@/shared/notifications/notificationCenter"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { CurrencyMark } from "../../shared/MnemosIcon"
import { WalletPurchaseSheet } from "../../shared/WalletPurchaseSheet"

type Props = {
  onPurchased: () => Promise<void>
}

const FULFILLMENT_POLL_DELAY_MS = 900
const FULFILLMENT_POLL_ATTEMPTS = 22

export function PrimeTopUpPanel({ onPurchased }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [packages, setPackages] = useState<StarPrimePackageResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [selectedPackage, setSelectedPackage] = useState<StarPrimePackageResponse | null>(null)
  const [busy, setBusy] = useState(false)

  const loadPackages = useCallback(async () => {
    setLoading(true)
    setLoadError(false)

    try {
      setPackages(await getStarPrimePackages())
    } catch {
      setPackages([])
      setLoadError(true)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void loadPackages()
  }, [loadPackages])

  const confirmPurchase = async () => {
    if (!selectedPackage || busy) {
      return
    }

    const webApp = window.Telegram?.WebApp

    if (!webApp?.openInvoice) {
      notifications.error(t("userWallet.topup.telegramOnly"))
      return
    }

    setBusy(true)

    try {
      const purchase = await createStarPrimePurchase(selectedPackage.id)

      webApp.HapticFeedback?.impactOccurred("light")
      webApp.openInvoice(purchase.invoiceUrl, (status) => {
        void handleInvoiceResult(
          status,
          purchase.purchaseId,
          webApp,
          data.refreshAccount,
          onPurchased,
          t,
          () => {
            setSelectedPackage(null)
            setBusy(false)
          },
        )
      })
    } catch {
      setBusy(false)
      notifications.error(t("userWallet.topup.createFailed"))
    }
  }

  return (
    <>
      <section className="prime-topup-panel">
        <header>
          <span>
            <small>{t("userWallet.topup.eyebrow")}</small>
            <strong>{t("userWallet.topup.title")}</strong>
            <em>{t("userWallet.topup.copy")}</em>
          </span>
          <span className="prime-topup-status">{t("userWallet.topup.status")}</span>
        </header>

        {loading ? (
          <div className="prime-topup-state">{t("userWallet.topup.loading")}</div>
        ) : loadError ? (
          <button type="button" className="prime-topup-retry" onClick={() => void loadPackages()}>
            {t("userWallet.topup.retry")}
          </button>
        ) : (
          <div className="prime-topup-packages">
            {packages.map((item) => (
              <button
                key={item.id}
                type="button"
                className="prime-topup-package"
                onClick={() => setSelectedPackage(item)}
              >
                <span className="prime-topup-package__prime">
                  <CurrencyMark currency="PRIME" size={17} />
                  <strong>{item.primeAmount}</strong>
                  <small>PRIME</small>
                </span>
                <span className="prime-topup-package__price">
                  <b aria-hidden="true">★</b>
                  <strong>{item.starsAmount}</strong>
                </span>
              </button>
            ))}
          </div>
        )}
      </section>

      {selectedPackage ? (
        <WalletPurchaseSheet
          eyebrow={t("userWallet.topup.confirm.eyebrow")}
          title={t("userWallet.topup.confirm.title")}
          copy={t("userWallet.topup.confirm.copy")}
          receiveLabel={t("userWallet.topup.confirm.receive")}
          receiveAmount={selectedPackage.primeAmount.toLocaleString()}
          receiveCurrency="PRIME"
          payLabel={t("userWallet.topup.confirm.pay")}
          payAmount={selectedPackage.starsAmount.toLocaleString()}
          payCurrency="STARS"
          note={t("userWallet.topup.confirm.note")}
          confirmLabel={t("userWallet.topup.confirm.submit")}
          cancelLabel={t("userWallet.topup.confirm.cancel")}
          busyLabel={t("userWallet.topup.confirm.busy")}
          busy={busy}
          onConfirm={() => void confirmPurchase()}
          onCancel={() => setSelectedPackage(null)}
        />
      ) : null}
    </>
  )
}

async function handleInvoiceResult(
  status: TelegramInvoiceStatus,
  purchaseId: string,
  webApp: TelegramWebApp,
  refreshAccount: () => Promise<void>,
  reloadHistory: () => Promise<void>,
  t: (key: string) => string,
  finish: () => void,
) {
  if (status === "cancelled") {
    finish()
    notifications.info(t("userWallet.topup.cancelled"))
    return
  }

  if (status === "failed") {
    finish()
    webApp.HapticFeedback?.notificationOccurred("error")
    notifications.error(t("userWallet.topup.paymentFailed"))
    return
  }

  try {
    const finalStatus = await waitForFulfillment(purchaseId)

    await Promise.all([refreshAccount(), reloadHistory()])
    finish()

    if (finalStatus === "FULFILLED") {
      webApp.HapticFeedback?.notificationOccurred("success")
      notifications.success(t("userWallet.topup.success"))
      return
    }

    if (finalStatus === "FULFILLMENT_FAILED") {
      webApp.HapticFeedback?.notificationOccurred("warning")
      notifications.warning(t("userWallet.topup.fulfillmentDelayed"))
      return
    }

    notifications.info(t("userWallet.topup.processing"))
  } catch {
    finish()
    notifications.info(t("userWallet.topup.processing"))
  }
}

async function waitForFulfillment(purchaseId: string): Promise<StarPrimePurchaseStatus> {
  let lastStatus: StarPrimePurchaseStatus = "PAID"

  for (let attempt = 0; attempt < FULFILLMENT_POLL_ATTEMPTS; attempt += 1) {
    const purchase = await getStarPrimePurchase(purchaseId)

    lastStatus = purchase.status

    if (lastStatus === "FULFILLED" || lastStatus === "FULFILLMENT_FAILED") {
      return lastStatus
    }

    await delay(FULFILLMENT_POLL_DELAY_MS)
  }

  return lastStatus
}

function delay(milliseconds: number): Promise<void> {
  return new Promise((resolve) => window.setTimeout(resolve, milliseconds))
}
