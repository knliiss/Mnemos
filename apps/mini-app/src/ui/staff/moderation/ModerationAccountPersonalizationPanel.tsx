import { useCallback, useEffect, useState } from "react"
import {
  activateAdminAccountPersonalization,
  deactivateAdminAccountPersonalization,
  getAdminAccountPersonalizations,
  grantAdminAccountPersonalization,
  revokeAdminAccountPersonalization,
} from "@/shared/api/adminPersonalizationApi"
import type { OwnedPersonalizationResponse } from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import "./moderation-account-personalizations.css"

type Props = {
  accountId: string
  deleted: boolean
}

export function ModerationAccountPersonalizationPanel({ accountId, deleted }: Props) {
  const [items, setItems] = useState<OwnedPersonalizationResponse[]>([])
  const [personalizationId, setPersonalizationId] = useState("")
  const [loading, setLoading] = useState(true)
  const [busyKey, setBusyKey] = useState<string | null>(null)

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

  const grant = async () => {
    const id = personalizationId.trim()

    if (!id || deleted) {
      return
    }

    await runMutation(`grant:${id}`, async () => {
      await grantAdminAccountPersonalization(accountId, id)
      setPersonalizationId("")
    })
  }

  return (
    <section className="moderation-admin-personalizations">
      <header className="moderation-admin-roles__heading">
        <span>
          <small>PERSONALIZATION</small>
          <strong>Account personalizations</strong>
        </span>
        <em>{items.length}</em>
      </header>

      {!deleted ? (
        <div className="moderation-admin-personalization-grant">
          <input
            className="moderation-admin-input"
            placeholder="Personalization ID"
            value={personalizationId}
            onChange={(event) => setPersonalizationId(event.target.value)}
          />
          <button
            type="button"
            className="moderation-control-button moderation-control-button--positive"
            disabled={!personalizationId.trim() || Boolean(busyKey)}
            onClick={() => void grant()}
          >
            Grant
          </button>
        </div>
      ) : null}

      {loading ? (
        <div className="moderation-admin-role-empty">Loading…</div>
      ) : items.length === 0 ? (
        <div className="moderation-admin-role-empty">No account personalizations</div>
      ) : (
        <div className="moderation-admin-personalization-list">
          {items.map((item) => (
            <article key={item.personalizationId} className="moderation-admin-personalization-row">
              <span>
                <strong>{item.type}</strong>
                <small>{item.personalizationId}</small>
              </span>
              <em className={item.active ? "is-active" : ""}>
                {item.active ? "EQUIPPED" : "OWNED"}
              </em>
              {!deleted ? (
                <div>
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
                    {item.active ? "Unequip" : "Equip"}
                  </button>
                  <button
                    type="button"
                    className="is-danger"
                    disabled={Boolean(busyKey)}
                    onClick={() =>
                      void runMutation(`revoke:${item.personalizationId}`, () =>
                        revokeAdminAccountPersonalization(accountId, item.personalizationId),
                      )
                    }
                  >
                    Revoke
                  </button>
                </div>
              ) : null}
            </article>
          ))}
        </div>
      )}
    </section>
  )
}
