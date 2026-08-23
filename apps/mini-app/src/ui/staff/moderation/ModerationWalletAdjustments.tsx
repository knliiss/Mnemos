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
  const walletTargets = canAdjustPrime ? (["USER", "ACCOUNT"] as const) : (["ACCOUNT"] as const)
  const numericAmount = Number(controller.amount)
  const debitExceedsBalance =
    controller.operation === "DEBIT" &&
    (!controller.balance || numericAmount > controller.balance.amount)
  const amountAllowed = controller.amountValid && !debitExceedsBalance

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
          onChange={controller.setWalletTarget}
          label={(value) =>
            value === "USER" ? t("moderation.userTarget") : t("moderation.accountTarget")
          }
        />
      </Field>

      {controller.walletTarget === "ACCOUNT" ? (
        <Field label={t("moderation.account")}>
          <ChoiceGrid
            value={controller.accountTargetId}
            options={identity.accounts.map((account) => account.accountId)}
            onChange={controller.setAccountTargetId}
            labels={Object.fromEntries(
              identity.accounts.map((account) => [account.accountId, account.cristalixNickname]),
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
          onChange={controller.setOperation}
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
            onChange={(event) => controller.setAmount(event.target.value)}
          />
        </div>
      </Field>

      <Field label={t("moderation.reason")}>
        <input
          value={controller.walletReason}
          maxLength={256}
          onChange={(event) => controller.setWalletReason(event.target.value)}
        />
      </Field>

      <button
        type="button"
        className="primary-action"
        disabled={
          !controller.targetId ||
          !amountAllowed ||
          !controller.walletReason.trim() ||
          controller.busy
        }
        onClick={() => void controller.submitAdjustment()}
      >
        {controller.busy ? t("moderation.sending") : t("moderation.apply")}
      </button>

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
