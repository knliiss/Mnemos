import { useEffect, useRef } from "react"
import { useTranslation } from "react-i18next"
import type { CollectorCredentialResponse } from "@/shared/api/collectorAdminApi"
import { Glyph } from "../../shared/MnemosIcon"
import { collectorCredentialState } from "./collectorAdminPresentation"

type CollectorCredentialListProps = {
  credentials: CollectorCredentialResponse[]
  activeCollectorIds: ReadonlySet<string>
  busyId: string | null
  disableCandidate: string | null
  revokeCandidate: string | null
  dateFormatter: Intl.DateTimeFormat
  onEnable(credentialId: string): void
  onRequestDisable(credentialId: string): void
  onCancelDisable(): void
  onConfirmDisable(credentialId: string): void
  onRequestRevoke(credentialId: string): void
  onCancelRevoke(): void
  onConfirmRevoke(credentialId: string): void
}

export function CollectorCredentialList({
  credentials,
  activeCollectorIds,
  busyId,
  disableCandidate,
  revokeCandidate,
  dateFormatter,
  onEnable,
  onRequestDisable,
  onCancelDisable,
  onConfirmDisable,
  onRequestRevoke,
  onCancelRevoke,
  onConfirmRevoke,
}: CollectorCredentialListProps) {
  const { t } = useTranslation()
  const confirmButtonRef = useRef<HTMLButtonElement>(null)
  const activeConfirmation = disableCandidate ?? revokeCandidate

  useEffect(() => {
    if (!activeConfirmation) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      confirmButtonRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [activeConfirmation])

  const cancelConfirmation = () => {
    if (disableCandidate) {
      onCancelDisable()
      return
    }

    onCancelRevoke()
  }

  return (
    <div className="collector-credential-grid">
      {credentials.map((credential) => {
        const state = collectorCredentialState(credential)
        const active = activeCollectorIds.has(credential.id)
        const busy = busyId === credential.id
        const confirmingDisable = disableCandidate === credential.id
        const confirmingRevoke = revokeCandidate === credential.id
        const confirmingAction = confirmingDisable || confirmingRevoke
        const confirmationTitle = confirmingDisable
          ? t("collectorAdmin.disableConfirmTitle")
          : t("collectorAdmin.revokeConfirmTitle")
        const confirmationCopy = confirmingDisable
          ? t("collectorAdmin.disableConfirmCopy")
          : t("collectorAdmin.revokeConfirmCopy")

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

              <span className="collector-credential-card__statuses">
                <span className={`collector-status is-${state.toLowerCase()}`}>
                  {t(`collectorAdmin.status.${state.toLowerCase()}`)}
                </span>
                <span className={`collector-live-status ${active ? "is-active" : "is-inactive"}`}>
                  <i aria-hidden="true" />
                  {t(`collectorAdmin.presenceStatus.${active ? "active" : "inactive"}`)}
                </span>
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

            {confirmingAction ? (
              <div
                className={`collector-revoke-confirmation ${confirmingDisable ? "is-disable" : "is-revoke"}`}
                role="alertdialog"
                aria-label={confirmationTitle}
                onKeyDown={(event) => {
                  if (event.key === "Escape") {
                    event.preventDefault()
                    cancelConfirmation()
                  }
                }}
              >
                <span>
                  <strong>{confirmationTitle}</strong>
                  <small>{confirmationCopy}</small>
                </span>
                <div>
                  <button type="button" disabled={busy} onClick={cancelConfirmation}>
                    {t("collectorAdmin.cancel")}
                  </button>
                  <button
                    ref={confirmButtonRef}
                    type="button"
                    className={confirmingDisable ? "is-warning" : "is-danger"}
                    disabled={busy}
                    onClick={() =>
                      confirmingDisable
                        ? onConfirmDisable(credential.id)
                        : onConfirmRevoke(credential.id)
                    }
                  >
                    {busy
                      ? t("collectorAdmin.working")
                      : confirmingDisable
                        ? t("collectorAdmin.disable")
                        : t("collectorAdmin.revoke")}
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
                      state === "ENABLED"
                        ? onRequestDisable(credential.id)
                        : onEnable(credential.id)
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
