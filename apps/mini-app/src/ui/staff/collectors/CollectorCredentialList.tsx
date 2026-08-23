import { useTranslation } from "react-i18next"
import type { CollectorCredentialResponse } from "@/shared/api/collectorAdminApi"
import { Glyph } from "../../shared/MnemosIcon"
import { collectorCredentialState } from "./collectorAdminPresentation"

type CollectorCredentialListProps = {
  credentials: CollectorCredentialResponse[]
  busyId: string | null
  revokeCandidate: string | null
  dateFormatter: Intl.DateTimeFormat
  onEnable(credentialId: string): void
  onDisable(credentialId: string): void
  onRequestRevoke(credentialId: string): void
  onCancelRevoke(): void
  onConfirmRevoke(credentialId: string): void
}

export function CollectorCredentialList({
  credentials,
  busyId,
  revokeCandidate,
  dateFormatter,
  onEnable,
  onDisable,
  onRequestRevoke,
  onCancelRevoke,
  onConfirmRevoke,
}: CollectorCredentialListProps) {
  const { t } = useTranslation()

  return (
    <div className="collector-credential-grid">
      {credentials.map((credential) => {
        const state = collectorCredentialState(credential)
        const busy = busyId === credential.id
        const confirmingRevoke = revokeCandidate === credential.id

        return (
          <article
            key={credential.id}
            className={`collector-credential-card is-${state.toLowerCase()}`}
          >
            <header>
              <span className="collector-credential-card__icon" aria-hidden="true">
                <Glyph name="admin" size={19} />
              </span>

              <span className="collector-credential-card__identity">
                <strong>{credential.name}</strong>
                <small>{credential.id}</small>
              </span>

              <span className={`collector-status is-${state.toLowerCase()}`}>
                {t(`collectorAdmin.status.${state.toLowerCase()}`)}
              </span>
            </header>

            <dl className="collector-credential-card__meta">
              <div>
                <dt>{t("collectorAdmin.created")}</dt>
                <dd>{dateFormatter.format(new Date(credential.createdAt))}</dd>
              </div>
              <div>
                <dt>{t("collectorAdmin.updated")}</dt>
                <dd>{dateFormatter.format(new Date(credential.updatedAt))}</dd>
              </div>
            </dl>

            {confirmingRevoke ? (
              <div className="collector-revoke-confirmation">
                <span>
                  <strong>{t("collectorAdmin.revokeConfirmTitle")}</strong>
                  <small>{t("collectorAdmin.revokeConfirmCopy")}</small>
                </span>
                <div>
                  <button type="button" disabled={busy} onClick={onCancelRevoke}>
                    {t("collectorAdmin.cancel")}
                  </button>
                  <button
                    type="button"
                    className="is-danger"
                    disabled={busy}
                    onClick={() => onConfirmRevoke(credential.id)}
                  >
                    {busy ? t("collectorAdmin.working") : t("collectorAdmin.revoke")}
                  </button>
                </div>
              </div>
            ) : (
              <footer>
                {state === "REVOKED" ? (
                  <span>{t("collectorAdmin.revokedHint")}</span>
                ) : (
                  <button
                    type="button"
                    disabled={busy}
                    onClick={() =>
                      state === "ENABLED" ? onDisable(credential.id) : onEnable(credential.id)
                    }
                  >
                    <Glyph name={state === "ENABLED" ? "close" : "check"} size={15} />
                    {busy
                      ? t("collectorAdmin.working")
                      : state === "ENABLED"
                        ? t("collectorAdmin.disable")
                        : t("collectorAdmin.enable")}
                  </button>
                )}

                {state !== "REVOKED" ? (
                  <button
                    type="button"
                    className="is-danger"
                    disabled={busy}
                    onClick={() => onRequestRevoke(credential.id)}
                  >
                    <Glyph name="trash" size={15} />
                    {t("collectorAdmin.revoke")}
                  </button>
                ) : null}
              </footer>
            )}
          </article>
        )
      })}
    </div>
  )
}
