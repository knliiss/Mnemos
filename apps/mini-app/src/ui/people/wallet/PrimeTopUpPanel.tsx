import { useCallback, useEffect, useMemo, useState } from "react"
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
import { Glyph } from "../../shared/MnemosIcon"
import { CurrencyValue } from "../../shared/Primitives"
import {
  WalletPurchaseSheet,
  type WalletPurchaseMessage,
} from "../../shared/WalletPurchaseSheet"
import "./prime-topup.css"

type Props = {
  onPurchased: () => Promise<void>
}

const FULFILLMENT_POLL_DELAY_MS = 900
const FULFILLMENT_POLL_ATTEMPTS = 22

export function PrimeTopUpPanel({ onPurchased }: Props) {
  const { t, i18n } = useTranslation()
  const data = useMnemosData()
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const [packages, setPackages] = useState<StarPrimePackageResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [selectedPackageId, setSelectedPackageId] = useState<string | null>(null)
  const [confirmationOpen, setConfirmationOpen] = useState(false)
  const [sheetMessage, setSheetMessage] = useState<WalletPurchaseMessage | null>(null)
  const [busy, setBusy] = useState(false)

  const selectedPackage = useMemo(
    () => packages.find((item) => item.id === selectedPackageId) ?? packages[0] ?? null,
    [packages, selectedPackageId],
  )

  const loadPackages = useCallback(async () => {
    setLoading(true)
    setLoadError(false)

    try {
      const loadedPackages = await getStarPrimePackages()

      setPackages(loadedPackages)
      setSelectedPackageId((currentPackageId) => {
        if (loadedPackages.some((item) => item.id === currentPackageId)) {
          return currentPackageId
        }

        return (loadedPackages[1] ?? loadedPackages[0])?.id ?? null
      })
    } catch {
      setPackages([])
      setSelectedPackageId(null)
      setLoadError(true)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void loadPackages()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [loadPackages])

  const openConfirmation = () => {
    if (!selectedPackage) {
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
    if (!selectedPackage || busy) {
      return
    }

    const webApp = window.Telegram?.WebApp

    if (!webApp?.openInvoice) {
      setSheetMessage({
        tone: "error",
        text: t("userWallet.topup.telegramOnly"),
      })
      return
    }

    setBusy(true)
    setSheetMessage(null)

    try {
      const purchase = await createStarPrimePurchase(selectedPackage.id)

      webApp.HapticFeedback?.impactOccurred("light")
      webApp.openInvoice(purchase.invoiceUrl, (status) => {
        void handleInvoiceResult(status, purchase.purchaseId, webApp)
      })
    } catch {
      setBusy(false)
      setSheetMessage({
        tone: "error",
        text: t("userWallet.topup.createFailed"),
      })
    }
  }

  const handleInvoiceResult = async (
    status: TelegramInvoiceStatus,
    purchaseId: string,
    webApp: TelegramWebApp,
  ) => {
    if (status === "cancelled") {
      setBusy(false)
      setSheetMessage({
        tone: "info",
        text: t("userWallet.topup.cancelled"),
      })
      return
    }

    if (status === "failed") {
      setBusy(false)
      setSheetMessage({
        tone: "error",
        text: t("userWallet.topup.paymentFailed"),
      })
      webApp.HapticFeedback?.notificationOccurred("error")
      return
    }

    setSheetMessage({
      tone: "info",
      text: t("userWallet.topup.processing"),
    })

    try {
      const finalStatus = await waitForFulfillment(purchaseId)

      await Promise.all([data.refreshAccount(), onPurchased()])
      setBusy(false)
      setConfirmationOpen(false)
      setSheetMessage(null)

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
      setBusy(false)
      setConfirmationOpen(false)
      setSheetMessage(null)
      notifications.info(t("userWallet.topup.processing"))
    }
  }

  const balanceAfterPurchase = selectedPackage
    ? data.user.prime + selectedPackage.primeAmount
    : data.user.prime

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
          <>
            <div className="prime-pack-grid">
              {packages.map((item) => {
                const selected = item.id === selectedPackage?.id

                return (
                  <button
                    key={item.id}
                    type="button"
                    className={selected ? "is-selected" : ""}
                    onClick={() => {
                      setSelectedPackageId(item.id)
                      setSheetMessage(null)
                    }}
                  >
                    <img
                      src="/ui-illustrations/prime-package.png"
                      alt=""
                      aria-hidden="true"
                    />
                    <span>
                      <small>{item.starsAmount.toLocaleString(locale)} STARS</small>
                      <strong>{item.primeAmount.toLocaleString(locale)} PRIME</strong>
                      <em>Telegram Stars</em>
                    </span>
                    <b>{selected ? <Glyph name="check" size={13} /> : null}</b>
                  </button>
                )
              })}
            </div>

            {selectedPackage ? (
              <div className="prime-topup-summary">
                <span>
                  <small>{t("userWallet.topup.afterPurchase")}</small>
                  <strong>
                    {data.user.prime.toLocaleString(locale)} →{" "}
                    {balanceAfterPurchase.toLocaleString(locale)} PRIME
                  </strong>
                  <em>
                    {t("userWallet.topup.price", {
                      stars: selectedPackage.starsAmount.toLocaleString(locale),
                    })}
                  </em>
                </span>
                <span className="prime-topup-summary__amount">
                  <CurrencyValue currency="PRIME" value={selectedPackage.primeAmount} compact />
                </span>
              </div>
            ) : null}

            <button
              type="button"
              className="prime-topup-action"
              disabled={!selectedPackage}
              onClick={openConfirmation}
            >
              {selectedPackage
                ? t("userWallet.topup.buyForStars", {
                    stars: selectedPackage.starsAmount.toLocaleString(locale),
                  })
                : t("userWallet.topup.loading")}
            </button>
            <p className="prime-topup-note">{t("userWallet.topup.note")}</p>
          </>
        )}
      </section>

      {confirmationOpen && selectedPackage ? (
        <WalletPurchaseSheet
          eyebrow={t("userWallet.topup.confirm.eyebrow")}
          title={t("userWallet.topup.confirm.title")}
          copy={t("userWallet.topup.confirm.copy")}
          receiveLabel={t("userWallet.topup.confirm.receive")}
          receiveAmount={selectedPackage.primeAmount.toLocaleString(locale)}
          receiveCurrency="PRIME"
          payLabel={t("userWallet.topup.confirm.pay")}
          payAmount={selectedPackage.starsAmount.toLocaleString(locale)}
          payCurrency="STARS"
          note={t("userWallet.topup.confirm.note")}
          message={sheetMessage}
          confirmLabel={t("userWallet.topup.confirm.submit")}
          cancelLabel={t("userWallet.topup.confirm.cancel")}
          busyLabel={t("userWallet.topup.confirm.busy")}
          busy={busy}
          onConfirm={() => void confirmPurchase()}
          onCancel={closeConfirmation}
        />
      ) : null}
    </>
  )
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

function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }

  if (language === "ru") {
    return "ru-RU"
  }

  return "uk-UA"
}
