import { useEffect, type ReactNode } from "react"
import { createPortal } from "react-dom"
import { CurrencyMark } from "./MnemosIcon"
import "./wallet-purchase-sheet.css"

export type WalletPurchaseMessage = {
  tone: "error" | "info" | "warning"
  text: string
}

type Props = {
  eyebrow: string
  title: string
  copy: string
  receiveLabel: string
  receiveAmount: string
  receiveCurrency: "COIN" | "PRIME"
  payLabel: string
  payAmount: string
  payCurrency: "COIN" | "PRIME" | "STARS"
  note: ReactNode
  message?: WalletPurchaseMessage | null
  confirmLabel: string
  cancelLabel: string
  busyLabel?: string
  busy?: boolean
  onConfirm: () => void
  onCancel: () => void
}

export function WalletPurchaseSheet({
  eyebrow,
  title,
  copy,
  receiveLabel,
  receiveAmount,
  receiveCurrency,
  payLabel,
  payAmount,
  payCurrency,
  note,
  message,
  confirmLabel,
  cancelLabel,
  busyLabel,
  busy = false,
  onConfirm,
  onCancel,
}: Props) {
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape" && !busy) {
        onCancel()
      }
    }

    window.addEventListener("keydown", handleKeyDown)
    return () => window.removeEventListener("keydown", handleKeyDown)
  }, [busy, onCancel])

  return createPortal(
    <div
      className="wallet-purchase-backdrop"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !busy) {
          onCancel()
        }
      }}
    >
      <section
        className="wallet-purchase-sheet"
        role="dialog"
        aria-modal="true"
        aria-labelledby="wallet-purchase-title"
      >
        <div className="wallet-purchase-sheet__glow" aria-hidden="true" />
        <div className="wallet-purchase-sheet__grip" aria-hidden="true" />

        <header className="wallet-purchase-sheet__header">
          <span>{eyebrow}</span>
          <strong id="wallet-purchase-title">{title}</strong>
          <p>{copy}</p>
        </header>

        <div className="wallet-purchase-sheet__exchange" aria-label={title}>
          <article>
            <small>{receiveLabel}</small>
            <span>
              <CurrencyMark currency={receiveCurrency} size={18} />
              <strong>{receiveAmount}</strong>
              <em>{receiveCurrency}</em>
            </span>
          </article>

          <i aria-hidden="true">←</i>

          <article>
            <small>{payLabel}</small>
            <span>
              {payCurrency === "STARS" ? (
                <b className="wallet-purchase-sheet__star" aria-hidden="true">
                  ★
                </b>
              ) : (
                <CurrencyMark currency={payCurrency} size={18} />
              )}
              <strong>{payAmount}</strong>
              <em>{payCurrency === "STARS" ? "STARS" : payCurrency}</em>
            </span>
          </article>
        </div>

        <div className="wallet-purchase-sheet__note">{note}</div>

        {message ? (
          <div
            className={`wallet-purchase-sheet__message is-${message.tone}`}
            role={message.tone === "error" ? "alert" : "status"}
            aria-live="polite"
          >
            <span className="wallet-purchase-sheet__message-mark" aria-hidden="true" />
            <span>{message.text}</span>
          </div>
        ) : null}

        <footer className="wallet-purchase-sheet__actions">
          <button type="button" className="is-secondary" onClick={onCancel} disabled={busy}>
            {cancelLabel}
          </button>
          <button type="button" className="is-primary" onClick={onConfirm} disabled={busy}>
            {busy ? (busyLabel ?? confirmLabel) : confirmLabel}
          </button>
        </footer>
      </section>
    </div>,
    document.body,
  )
}
