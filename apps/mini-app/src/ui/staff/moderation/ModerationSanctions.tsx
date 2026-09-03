import { useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { SANCTION_TYPES } from "../../contracts"
import type { SanctionType } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { ChoiceGrid, Field, Section } from "../../shared/Primitives"
import { formatModerationDate } from "./moderationPresentation"
import type { ModerationUserController } from "./useModerationUser"

type Props = {
  controller: ModerationUserController
  language: string
  canSanction: boolean
}

export function ModerationSanctions({ controller, language, canSanction }: Props) {
  const { t } = useTranslation()
  const [confirmationVisible, setConfirmationVisible] = useState(false)
  const [revokeConfirmationId, setRevokeConfirmationId] = useState<string | null>(null)
  const confirmButtonRef = useRef<HTMLButtonElement>(null)
  const revokeConfirmButtonRef = useRef<HTMLButtonElement>(null)
  const activeBan = controller.activeBan
  const revokeCandidate =
    controller.sanctions.find((sanction) => sanction.id === revokeConfirmationId) ?? null
  const sanctionLabels: Record<SanctionType, string> = {
    BAN: t("moderation.sanctionBan"),
    NOTIFICATION_SUSPENSION: t("moderation.sanctionNotification"),
  }

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

  useEffect(() => {
    if (!revokeCandidate) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      revokeConfirmButtonRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [revokeCandidate])

  const closeSanctionForm = () => {
    setConfirmationVisible(false)
    controller.setSanctionFormOpen(false)
  }

  const dismissConfirmation = () => {
    if (!controller.busy) {
      setConfirmationVisible(false)
    }
  }

  const dismissRevokeConfirmation = () => {
    if (!controller.busy) {
      setRevokeConfirmationId(null)
    }
  }

  const reviewRevoke = (sanctionId: string) => {
    if (controller.busy) {
      return
    }

    setConfirmationVisible(false)
    setRevokeConfirmationId(sanctionId)
  }

  const submitConfirmedSanction = async () => {
    if (controller.busy) {
      return
    }

    const succeeded = await controller.submitSanction()

    if (succeeded) {
      setConfirmationVisible(false)
    }
  }

  const submitConfirmedRevoke = async () => {
    const sanctionId = revokeConfirmationId

    if (!sanctionId || controller.busy) {
      return
    }

    const succeeded = await controller.revoke(sanctionId)

    if (succeeded) {
      setRevokeConfirmationId(null)
    }
  }

  return (
    <>
      {canSanction ? (
        <section className="moderation-actions">
          {!activeBan ? (
            <button
              type="button"
              onClick={() => {
                controller.setSanctionType("BAN")
                setConfirmationVisible(false)
                setRevokeConfirmationId(null)
                controller.setSanctionFormOpen(true)
              }}
            >
              <Glyph name="moderation" size={19} />
              <span>
                <strong>{t("moderation.ban")}</strong>
                <small>{t("moderation.createBan")}</small>
              </span>
            </button>
          ) : (
            <button
              type="button"
              className="moderation-action-revoke"
              disabled={controller.busy}
              onClick={() => reviewRevoke(activeBan.id)}
            >
              <Glyph name="check" size={18} />
              <span>
                <strong>{t("moderation.unban")}</strong>
                <small>{activeBan.reason}</small>
              </span>
            </button>
          )}

          <button
            type="button"
            onClick={() => {
              controller.setSanctionType("NOTIFICATION_SUSPENSION")
              setConfirmationVisible(false)
              setRevokeConfirmationId(null)
              controller.setSanctionFormOpen(true)
            }}
          >
            <Glyph name="bell" size={19} />
            <span>
              <strong>{t("moderation.notifications")}</strong>
              <small>{t("moderation.suspendDelivery")}</small>
            </span>
          </button>
        </section>
      ) : null}

      {canSanction && revokeCandidate ? (
        <div
          className="moderation-sanction-confirmation moderation-sanction-confirmation--revoke"
          role="alertdialog"
          aria-label={
            revokeCandidate.type === "BAN" ? t("moderation.unban") : t("moderation.revoke")
          }
          onKeyDown={(event) => {
            if (event.key === "Escape") {
              event.preventDefault()
              dismissRevokeConfirmation()
            }
          }}
        >
          <span className="moderation-sanction-confirmation__mark" aria-hidden="true">
            <Glyph name={revokeCandidate.type === "BAN" ? "check" : "bell"} size={18} />
          </span>
          <span className="moderation-sanction-confirmation__copy">
            <small>{sanctionLabels[revokeCandidate.type]}</small>
            <strong>{revokeCandidate.reason}</strong>
            <em>
              {revokeCandidate.expiresAt
                ? formatModerationDate(revokeCandidate.expiresAt, language)
                : t("moderation.indefinite")}
            </em>
          </span>
          <div className="moderation-sanction-confirmation__actions">
            <button
              ref={revokeConfirmButtonRef}
              type="button"
              className="moderation-control-button moderation-control-button--danger"
              disabled={controller.busy}
              onClick={() => void submitConfirmedRevoke()}
            >
              {controller.busy
                ? t("moderation.saving")
                : revokeCandidate.type === "BAN"
                  ? t("moderation.unban")
                  : t("moderation.revoke")}
            </button>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--neutral"
              disabled={controller.busy}
              onClick={dismissRevokeConfirmation}
            >
              {t("common.cancel")}
            </button>
          </div>
        </div>
      ) : null}

      <Section title={t("moderation.sanctions")}>
        <div className="sanction-list">
          {controller.sanctions.map((sanction) => (
            <article
              key={sanction.id}
              className={`sanction-card sanction-card--${sanction.status.toLowerCase()}`}
            >
              <span className={`sanction-mark sanction-mark--${sanction.type.toLowerCase()}`} />
              <span>
                <strong>{sanctionLabels[sanction.type]}</strong>
                <small>{sanction.reason}</small>
                <em>
                  {sanction.status} ·{" "}
                  {sanction.expiresAt
                    ? formatModerationDate(sanction.expiresAt, language)
                    : t("moderation.indefinite")}
                </em>
              </span>
              {canSanction && sanction.status === "ACTIVE" ? (
                <button
                  type="button"
                  disabled={controller.busy}
                  onClick={() => reviewRevoke(sanction.id)}
                >
                  {t("moderation.revoke")}
                </button>
              ) : null}
            </article>
          ))}

          {!controller.sanctions.length ? (
            <p className="inline-hint inline-hint--left">{t("moderation.noSanctions")}</p>
          ) : null}
        </div>
      </Section>

      {canSanction && controller.sanctionFormOpen ? (
        <section className="moderation-form">
          <div className="section__head">
            <h2>{t("moderation.newSanction")}</h2>
            <button type="button" aria-label={t("moderation.close")} onClick={closeSanctionForm}>
              <Glyph name="close" size={15} />
            </button>
          </div>

          <Field label={t("moderation.type")}>
            <ChoiceGrid
              value={controller.sanctionType}
              options={SANCTION_TYPES}
              onChange={(value) => {
                setConfirmationVisible(false)
                controller.setSanctionType(value)
              }}
              labels={sanctionLabels}
            />
          </Field>

          <Field label={t("moderation.reason")}>
            <input
              value={controller.reason}
              disabled={confirmationVisible || controller.busy}
              onChange={(event) => {
                setConfirmationVisible(false)
                controller.setReason(event.target.value)
              }}
            />
          </Field>

          <Field label={t("moderation.internalNote")}>
            <input
              value={controller.internalNote}
              disabled={confirmationVisible || controller.busy}
              onChange={(event) => {
                setConfirmationVisible(false)
                controller.setInternalNote(event.target.value)
              }}
            />
          </Field>

          <Field
            label={t("moderation.durationHours")}
            hint={
              controller.duration && !controller.durationValid
                ? t("moderation.durationInvalid")
                : t("moderation.durationHint")
            }
          >
            <input
              type="number"
              min={1}
              step={1}
              value={controller.duration}
              disabled={confirmationVisible || controller.busy}
              onChange={(event) => {
                setConfirmationVisible(false)
                controller.setDuration(event.target.value)
              }}
            />
          </Field>

          {confirmationVisible ? (
            <div
              className="moderation-sanction-confirmation"
              role="alertdialog"
              aria-label={t("moderation.createSanction")}
              onKeyDown={(event) => {
                if (event.key === "Escape") {
                  event.preventDefault()
                  dismissConfirmation()
                }
              }}
            >
              <span className="moderation-sanction-confirmation__mark" aria-hidden="true">
                <Glyph name={controller.sanctionType === "BAN" ? "moderation" : "bell"} size={18} />
              </span>
              <span className="moderation-sanction-confirmation__copy">
                <small>{sanctionLabels[controller.sanctionType]}</small>
                <strong>{controller.reason.trim()}</strong>
                <em>
                  {controller.duration
                    ? `${controller.duration} ${t("moderation.durationHours")}`
                    : t("moderation.indefinite")}
                </em>
              </span>
              <div className="moderation-sanction-confirmation__actions">
                <button
                  ref={confirmButtonRef}
                  type="button"
                  className="destructive-action"
                  disabled={controller.busy}
                  onClick={() => void submitConfirmedSanction()}
                >
                  {controller.busy ? t("moderation.saving") : t("common.confirm")}
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
              className="destructive-action"
              disabled={!controller.reason.trim() || !controller.durationValid || controller.busy}
              onClick={() => setConfirmationVisible(true)}
            >
              {controller.busy ? t("moderation.saving") : t("moderation.createSanction")}
            </button>
          )}
        </section>
      ) : null}
    </>
  )
}
