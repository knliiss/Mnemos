import { useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { IdentitySearchResponse } from "@/shared/api/moderationApi"
import { TRANSACTION_TYPES } from "../../contracts"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { ChoiceGrid, Field, Segmented } from "../../shared/Primitives"
import { formatModerationDate } from "./moderationPresentation"
import type { ModerationUserController } from "./useModerationUser"

type Props = {
  controller: ModerationUserController
  identity: IdentitySearchResponse
  language: string
  canAdjustPrime: boolean
}

export function ModerationWalletAdjustments({
  controller,
  identity,
  language,
  canAdjustPrime,
}: Props) {
  const { t } = useTranslation()
  const [confirmationVisible, setConfirmationVisible] = useState(false)
  const confirmButtonRef = useRef<HTMLButtonElement>(null)
  const walletTargets = canAdjustPrime ? (["USER", "ACCOUNT"] as const) : (["ACCOUNT"] as const)
  const writableAccounts = identity.accounts.filter((account) => account.status !== "DELETED")
  const numericAmount = Number(controller.amount)
  const debitExceedsBalance =
    controller.operation === "DEBIT" &&
    (!controller.balance || numericAmount > controller.balance.amount)
  const amountAllowed = controller.amountValid && !debitExceedsBalance
  const adjustmentAllowed =
    Boolean(controller.targetId) &&
    amountAllowed &&
    Boolean(controller.walletReason.trim()) &&
    !controller.busy

  useEffect(() => {
    if (!confirmationVisible) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      confirmButtonRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [confirmationVisible])

  const dismissConfirmation = () => {
    if (!controller.busy) {
      setConfirmationVisible(false)
    }
  }

  const submitConfirmedAdjustment = async () => {
    if (!adjustmentAllowed) {
      return
    }

    await controller.submitAdjustment()
    setConfirmationVisible(false)
  }

  const changeWalletTarget = (value: (typeof walletTargets)[number]) => {
    setConfirmationVisible(false)
    controller.setWalletTarget(value)
  }

  return (
    <section className="admin-wallet-panel">
      <div className="admin-wallet-panel__head">
        <span className="admin-wallet-panel__mark">
          <Glyph name="wallet" size={20} />
        </span>
        <span>
          <strong>{t("moderation.walletAdjustment")}</strong>
          <small>{t("moderation.walletRoles")}</small>
        </span>
      </div>

      <Field label={t("moderation.target")}>
        <Segmented
          value={controller.walletTarget}
          options={walletTargets}
          onChange={changeWalletTarget}
          label={(value) =>
            value === "USER" ? t("moderation.userTarget") : t("moderation.accountTarget")
          }
        />
      </Field>

      {controller.walletTarget === "ACCOUNT" ? (
        <Field label={t("moderation.account")}>
          <ChoiceGrid
            value={controller.accountTargetId}
            options={writableAccounts.map((account) => account.accountId)}
            onChange={(value) => {
              setConfirmationVisible(false)
              controller.setAccountTargetId(value)
            }}
            labels={Object.fromEntries(
              writableAccounts.map((account) => [account.accountId, account.cristalixNickname]),
            )}
          />
        </Field>
      ) : null}

      <div className="admin-wallet-scope">
        <CurrencyMark currency={controller.currency} size={20} />
        <span>
          <small>{t("bets.balance")}</small>
          <strong>
            {controller.balance
              ? `${controller.balance.amount.toLocaleString()} ${controller.balance.currency}`
              : "—"}
          </strong>
          <small>{controller.targetId}</small>
        </span>
      </div>

      <Field label={t("moderation.operation")}>
        <Segmented
          value={controller.operation}
          options={TRANSACTION_TYPES}
          onChange={(value) => {
            setConfirmationVisible(false)
            controller.setOperation(value)
          }}
          label={(value) => (value === "CREDIT" ? t("moderation.credit") : t("moderation.debit"))}
        />
      </Field>

      <Field
        label={t("moderation.amount")}
        hint={
          debitExceedsBalance
            ? t("bets.sheet.insufficientBalance")
            : !controller.amountValid && controller.amount.length
              ? t("moderation.positiveInteger")
              : undefined
        }
      >
        <div className="amount-input">
          <span>
            <CurrencyMark currency={controller.currency} size={17} />
          </span>
          <input
            type="number"
            min={1}
            max={controller.operation === "DEBIT" ? controller.balance?.amount : undefined}
            step={1}
            value={controller.amount}
            disabled={confirmationVisible || controller.busy}
            onChange={(event) => {
              setConfirmationVisible(false)
              controller.setAmount(event.target.value)
            }}
          />
        </div>
      </Field>

      <Field label={t("moderation.reason")}>
        <input
          value={controller.walletReason}
          maxLength={256}
          disabled={confirmationVisible || controller.busy}
          onChange={(event) => {
            setConfirmationVisible(false)
            controller.setWalletReason(event.target.value)
          }}
        />
      </Field>

      {confirmationVisible ? (
        <div
          className={`moderation-wallet-confirmation is-${controller.operation.toLowerCase()}`}
          role="alertdialog"
          aria-label={t("moderation.walletAdjustment")}
          onKeyDown={(event) => {
            if (event.key === "Escape") {
              event.preventDefault()
              dismissConfirmation()
            }
          }}
        >
          <span className="moderation-wallet-confirmation__mark" aria-hidden="true">
            <CurrencyMark currency={controller.currency} size={19} />
          </span>
          <span className="moderation-wallet-confirmation__copy">
            <small>
              {controller.operation === "CREDIT" ? t("moderation.credit") : t("moderation.debit")}
            </small>
            <strong>
              {controller.operation === "CREDIT" ? "+" : "−"}
              {numericAmount.toLocaleString()} {controller.currency}
            </strong>
            <em>{controller.targetId}</em>
            <p>{controller.walletReason.trim()}</p>
          </span>
          <div className="moderation-wallet-confirmation__actions">
            <button
              ref={confirmButtonRef}
              type="button"
              className={
                controller.operation === "DEBIT"
                  ? "moderation-control-button moderation-control-button--danger"
                  : "moderation-control-button moderation-control-button--positive"
              }
              disabled={!adjustmentAllowed}
              onClick={() => void submitConfirmedAdjustment()}
            >
              {controller.busy ? t("moderation.sending") : t("common.confirm")}
            </button>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--neutral"
              disabled={controller.busy}
              onClick={dismissConfirmation}
            >
              {t("common.cancel")}
            </button>
          </div>
        </div>
      ) : (
        <button
          type="button"
          className="primary-action"
          disabled={!adjustmentAllowed}
          onClick={() => setConfirmationVisible(true)}
        >
          {controller.busy ? t("moderation.sending") : t("moderation.apply")}
        </button>
      )}

      <div className="wallet-adjustment-history">
        <h3>{t("moderation.operations")}</h3>
        {controller.adjustments.map((item) => (
          <article key={item.operationId}>
            <i className={`is-${item.status.toLowerCase()}`} />
            <span>
              <strong>
                {item.type === "CREDIT" ? "+" : "−"}
                {item.amount} {item.currency}
              </strong>
              <small>{item.reason}</small>
            </span>
            <span>
              <strong>{item.status}</strong>
              <small>{formatModerationDate(item.createdAt, language)}</small>
            </span>
          </article>
        ))}

        {!controller.adjustments.length ? (
          <p className="inline-hint inline-hint--left">{t("moderation.noOperations")}</p>
        ) : null}
      </div>
    </section>
  )
}
