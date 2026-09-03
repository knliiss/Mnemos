import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  activateAdminAccountPersonalization,
  deactivateAdminAccountPersonalization,
  getAdminAccountPersonalizations,
  grantAdminAccountPersonalization,
  revokeAdminAccountPersonalization,
} from "@/shared/api/adminPersonalizationApi"
import type {
  OwnedPersonalizationResponse,
  PersonalizationResponse,
} from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { Glyph } from "../../shared/MnemosIcon"
import { AdminPersonalizationPicker } from "./AdminPersonalizationPicker"
import "./moderation-account-personalizations.css"

type Props = {
  accountId: string
  deleted: boolean
}

type GrantMode = "CATALOG" | "ID"

export function ModerationAccountPersonalizationPanel({ accountId, deleted }: Props) {
  const { t } = useTranslation()
  const [items, setItems] = useState<OwnedPersonalizationResponse[]>([])
  const [grantMode, setGrantMode] = useState<GrantMode>("CATALOG")
  const [selectedPersonalization, setSelectedPersonalization] = useState<PersonalizationResponse | null>(null)
  const [personalizationId, setPersonalizationId] = useState("")
  const [loading, setLoading] = useState(true)
  const [busyKey, setBusyKey] = useState<string | null>(null)
  const [query, setQuery] = useState("")
  const [revokeCandidateId, setRevokeCandidateId] = useState<string | null>(null)
  const revokeConfirmRef = useRef<HTMLButtonElement>(null)

  const load = useCallback(async () => getAdminAccountPersonalizations(accountId), [accountId])

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      setItems(await load())
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [load])

  useEffect(() => {
    let active = true

    void load()
      .then((owned) => {
        if (active) {
          setItems(owned)
        }
      })
      .catch((error: unknown) => {
        if (active) {
          handleError(error)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [load])

  useEffect(() => {
    if (!revokeCandidateId) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      revokeConfirmRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [revokeCandidateId])

  const visibleItems = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    if (!normalized) {
      return items
    }

    return items.filter((item) =>
      [item.personalizationId, item.type]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(normalized)),
    )
  }, [items, query])

  const ownedIds = useMemo(() => items.map((item) => item.personalizationId), [items])

  const runMutation = async (key: string, mutation: () => Promise<unknown>) => {
    if (busyKey) {
      return
    }

    setBusyKey(key)

    try {
      await mutation()
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusyKey(null)
    }
  }

  const grantId = grantMode === "CATALOG" ? selectedPersonalization?.id ?? "" : personalizationId.trim()

  const grant = async () => {
    if (!grantId || deleted) {
      return
    }

    await runMutation(`grant:${grantId}`, async () => {
      await grantAdminAccountPersonalization(accountId, grantId)
      setSelectedPersonalization(null)
      setPersonalizationId("")
    })
  }

  const dismissRevokeConfirmation = () => {
    if (!busyKey) {
      setRevokeCandidateId(null)
    }
  }

  return (
    <section className="moderation-admin-personalizations">
      <header className="moderation-admin-personalizations__hero">
        <span className="moderation-admin-personalizations__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
        </span>
        <span>
          <small>{t("administration.personalizations")}</small>
          <strong>{t("moderation.accountPersonalizations", "Account personalizations")}</strong>
          <p>
            {t(
              "moderation.accountPersonalizationsDescription",
              "Manage ownership and the equipped account appearance without using the member-facing store.",
            )}
          </p>
        </span>
        <em>{items.length}</em>
      </header>

      {!deleted ? (
        <div className="moderation-admin-personalization-grant-workspace">
          <div className="moderation-admin-personalization-grant-mode">
            <button
              type="button"
              className={grantMode === "CATALOG" ? "is-active" : ""}
              onClick={() => setGrantMode("CATALOG")}
            >
              {t("moderation.personalizationCatalog", "Catalog")}
            </button>
            <button
              type="button"
              className={grantMode === "ID" ? "is-active" : ""}
              onClick={() => setGrantMode("ID")}
            >
              {t("moderation.personalizationAdvancedId", "Advanced ID")}
            </button>
          </div>

          <div className="moderation-admin-personalization-grant">
            {grantMode === "CATALOG" ? (
              <AdminPersonalizationPicker
                disabled={Boolean(busyKey)}
                excludedIds={ownedIds}
                selected={selectedPersonalization}
                onSelect={setSelectedPersonalization}
              />
            ) : (
              <label className="moderation-admin-field">
                <span>{t("moderation.personalizationId", "Personalization ID")}</span>
                <input
                  className="moderation-admin-input"
                  placeholder={t(
                    "moderation.personalizationIdPlaceholder",
                    "Paste personalization ID",
                  )}
                  value={personalizationId}
                  autoComplete="off"
                  spellCheck={false}
                  onChange={(event) => setPersonalizationId(event.target.value)}
                />
              </label>
            )}

            <button
              type="button"
              className="moderation-control-button moderation-control-button--positive"
              disabled={!grantId || Boolean(busyKey)}
              onClick={() => void grant()}
            >
              <Glyph name="plus" size={14} />
              {t("moderation.grantPersonalization", "Grant")}
            </button>
          </div>
        </div>
      ) : null}

      {items.length > 0 ? (
        <label className="moderation-admin-personalization-search">
          <Glyph name="search" size={16} />
          <input
            value={query}
            placeholder={t("moderation.filterPersonalizations", "Filter owned personalizations")}
            onChange={(event) => setQuery(event.target.value)}
          />
          {query ? (
            <button type="button" onClick={() => setQuery("")} aria-label={t("moderation.clear")}>
              <Glyph name="close" size={12} />
            </button>
          ) : null}
        </label>
      ) : null}

      {loading ? (
        <div className="moderation-admin-role-empty">{t("common.loading")}</div>
      ) : visibleItems.length === 0 ? (
        <div className="moderation-admin-personalization-empty">
          <img src="/ui-illustrations/admin-review.svg" alt="" aria-hidden="true" />
          <strong>{t("moderation.noAccountPersonalizations", "No account personalizations")}</strong>
          <small>
            {query
              ? t("moderation.nothingFound")
              : t(
                  "moderation.personalizationEmptyHint",
                  "Granted account cosmetics will appear here.",
                )}
          </small>
        </div>
      ) : (
        <div className="moderation-admin-personalization-list">
          {visibleItems.map((item) => (
            <article key={item.personalizationId} className="moderation-admin-personalization-row">
              <span className="moderation-admin-personalization-row__mark" aria-hidden="true">
                <Glyph name="spark" size={16} />
              </span>
              <span className="moderation-admin-personalization-row__copy">
                <strong>{item.type.replaceAll("_", " ")}</strong>
                <small>{item.personalizationId}</small>
              </span>
              <em className={item.active ? "is-active" : ""}>
                {item.active
                  ? t("moderation.personalizationEquipped", "Equipped")
                  : t("moderation.personalizationOwned", "Owned")}
              </em>
              {!deleted ? (
                <div className="moderation-admin-personalization-row__actions">
                  <button
                    type="button"
                    className={item.active ? "is-active" : ""}
                    disabled={Boolean(busyKey)}
                    onClick={() =>
                      void runMutation(`active:${item.personalizationId}`, async () => {
                        if (item.active) {
                          await deactivateAdminAccountPersonalization(accountId, item.personalizationId)
                        } else {
                          await activateAdminAccountPersonalization(accountId, item.personalizationId)
                        }
                      })
                    }
                  >
                    {item.active
                      ? t("moderation.unequipPersonalization", "Unequip")
                      : t("moderation.equipPersonalization", "Equip")}
                  </button>

                  {revokeCandidateId === item.personalizationId ? (
                    <span
                      className="moderation-admin-personalization-revoke-confirmation"
                      role="alertdialog"
                      aria-label={t("moderation.revokePersonalizationConfirm", "Revoke personalization?")}
                      onKeyDown={(event) => {
                        if (event.key === "Escape") {
                          event.preventDefault()
                          dismissRevokeConfirmation()
                        }
                      }}
                    >
                      <button
                        ref={revokeConfirmRef}
                        type="button"
                        className="is-danger"
                        disabled={Boolean(busyKey)}
                        onClick={() =>
                          void runMutation(`revoke:${item.personalizationId}`, async () => {
                            await revokeAdminAccountPersonalization(accountId, item.personalizationId)
                            setRevokeCandidateId(null)
                          })
                        }
                      >
                        {t("common.confirm", "Confirm")}
                      </button>
                      <button
                        type="button"
                        disabled={Boolean(busyKey)}
                        onClick={dismissRevokeConfirmation}
                      >
                        {t("common.cancel", "Cancel")}
                      </button>
                    </span>
                  ) : (
                    <button
                      type="button"
                      className="is-danger"
                      disabled={Boolean(busyKey)}
                      onClick={() => setRevokeCandidateId(item.personalizationId)}
                    >
                      {t("moderation.revokePersonalization", "Revoke")}
                    </button>
                  )}
                </div>
              ) : null}
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
