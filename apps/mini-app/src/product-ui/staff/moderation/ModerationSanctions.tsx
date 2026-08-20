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
}

export function ModerationSanctions({ controller, language }: Props) {
  const { t } = useTranslation()
  const activeBan = controller.activeBan
  const sanctionLabels: Record<SanctionType, string> = {
    BAN: t("moderation.sanctionBan"),
    NOTIFICATION_SUSPENSION: t("moderation.sanctionNotification"),
  }

  return (
    <>
      <section className="moderation-actions">
        {!activeBan ? (
          <button type="button"
            onClick={() => {
              controller.setSanctionType("BAN")
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
          <button type="button"
            className="moderation-action-revoke"
            disabled={controller.busy}
            onClick={() => void controller.revoke(activeBan.id)}
          >
            <Glyph name="check" size={18} />
            <span>
              <strong>{t("moderation.unban")}</strong>
              <small>{activeBan.reason}</small>
            </span>
          </button>
        )}

        <button type="button"
          onClick={() => {
            controller.setSanctionType("NOTIFICATION_SUSPENSION")
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
              {sanction.status === "ACTIVE" ? (
                <button type="button"
                  disabled={controller.busy}
                  onClick={() => void controller.revoke(sanction.id)}
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

      {controller.sanctionFormOpen ? (
        <section className="moderation-form">
          <div className="section__head">
            <h2>{t("moderation.newSanction")}</h2>
            <button type="button"
              aria-label={t("moderation.close")}
              onClick={() => controller.setSanctionFormOpen(false)}
            >
              <Glyph name="close" size={15} />
            </button>
          </div>

          <Field label={t("moderation.type")}>
            <ChoiceGrid
              value={controller.sanctionType}
              options={SANCTION_TYPES}
              onChange={controller.setSanctionType}
              labels={sanctionLabels}
            />
          </Field>

          <Field label={t("moderation.reason")}>
            <input
              value={controller.reason}
              onChange={(event) => controller.setReason(event.target.value)}
            />
          </Field>

          <Field label={t("moderation.internalNote")}>
            <input
              value={controller.internalNote}
              onChange={(event) => controller.setInternalNote(event.target.value)}
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
              onChange={(event) => controller.setDuration(event.target.value)}
            />
          </Field>

          <button type="button"
            className="destructive-action"
            disabled={!controller.reason.trim() || !controller.durationValid || controller.busy}
            onClick={() => void controller.submitSanction()}
          >
            {controller.busy ? t("moderation.saving") : t("moderation.createSanction")}
          </button>
        </section>
      ) : null}
    </>
  )
}
