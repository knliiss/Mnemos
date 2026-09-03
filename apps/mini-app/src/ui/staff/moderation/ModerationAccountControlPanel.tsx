import { useCallback, useEffect, useState } from "react"
import {
  addAccountMember,
  deleteAccountPermanently,
  getAdminAccount,
  removeAccountMember,
  transferAccountOwnership,
  type AdminAccountDetailsResponse,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"

type Props = {
  accountId: string
  canDelete: boolean
  copy: ReturnType<typeof resolveModerationDirectoryCopy>
  onChanged(): Promise<void>
}

export function ModerationAccountControlPanel({ accountId, canDelete, copy, onChanged }: Props) {
  const [details, setDetails] = useState<AdminAccountDetailsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [busyAction, setBusyAction] = useState<string | null>(null)
  const [newMemberUserId, setNewMemberUserId] = useState("")
  const [deleteConfirmationVisible, setDeleteConfirmationVisible] = useState(false)

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      setDetails(await getAdminAccount(accountId))
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [accountId])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const runMutation = useCallback(
    async (actionId: string, mutation: () => Promise<void>) => {
      if (busyAction) {
        return
      }

      setBusyAction(actionId)

      try {
        await mutation()
        await Promise.all([refresh(), onChanged()])
      } catch (error) {
        handleError(error)
      } finally {
        setBusyAction(null)
      }
    },
    [busyAction, onChanged, refresh],
  )

  if (loading && !details) {
    return <div className="moderation-account-control__loading">{copy.loadingAccount}</div>
  }

  if (!details) {
    return null
  }

  const deleted = details.status === "DELETED"
  const normalizedNewMemberUserId = newMemberUserId.trim()

  return (
    <section className="moderation-account-control" aria-busy={Boolean(busyAction)}>
      <header className="moderation-account-control__header">
        <span>
          <small>{details.status}</small>
          <strong>{copy.accountMembers}</strong>
        </span>
        <b>
          {details.members.length}/{details.maxMembers}
        </b>
      </header>

      <div className="moderation-account-members">
        {details.members.map((member) => (
          <article key={member.memberId} className="moderation-account-member">
            <div>
              <strong>@{member.username}</strong>
              <small>{member.userId}</small>
              <em>{member.memberId}</em>
            </div>

            <span className="moderation-account-member__actions">
              {member.owner ? <b>{copy.owner}</b> : null}

              {!deleted && !member.owner ? (
                <>
                  <button
                    type="button"
                    className="moderation-control-button moderation-control-button--primary"
                    disabled={Boolean(busyAction)}
                    onClick={() =>
                      void runMutation(`owner:${member.memberId}`, () =>
                        transferAccountOwnership(accountId, member.memberId),
                      )
                    }
                  >
                    {copy.transferOwner}
                  </button>
                  <button
                    type="button"
                    className="moderation-control-button moderation-control-button--danger"
                    disabled={Boolean(busyAction)}
                    onClick={() =>
                      void runMutation(`remove:${member.memberId}`, () =>
                        removeAccountMember(accountId, member.memberId),
                      )
                    }
                  >
                    {copy.removeMember}
                  </button>
                </>
              ) : null}
            </span>
          </article>
        ))}
      </div>

      {!deleted ? (
        <div className="moderation-account-control__add-member">
          <input
            type="text"
            value={newMemberUserId}
            className="moderation-control-input"
            placeholder={copy.userIdPlaceholder}
            autoComplete="off"
            spellCheck={false}
            onChange={(event) => setNewMemberUserId(event.target.value)}
          />
          <button
            type="button"
            className="moderation-control-button moderation-control-button--primary"
            disabled={!normalizedNewMemberUserId || Boolean(busyAction)}
            onClick={() =>
              void runMutation("add-member", async () => {
                await addAccountMember(accountId, normalizedNewMemberUserId)
                setNewMemberUserId("")
              })
            }
          >
            {busyAction === "add-member" ? copy.addingMember : copy.addMember}
          </button>
        </div>
      ) : null}

      {canDelete && !deleted ? (
        <div className="moderation-account-control__danger-zone">
          {deleteConfirmationVisible ? (
            <>
              <p>{copy.deleteWarning}</p>
              <div>
                <button
                  type="button"
                  className="moderation-control-button moderation-control-button--danger"
                  disabled={Boolean(busyAction)}
                  onClick={() =>
                    void runMutation("delete-account", async () => {
                      await deleteAccountPermanently(accountId)
                      setDeleteConfirmationVisible(false)
                    })
                  }
                >
                  {copy.confirmDelete}
                </button>
                <button
                  type="button"
                  className="moderation-control-button moderation-control-button--neutral"
                  disabled={Boolean(busyAction)}
                  onClick={() => setDeleteConfirmationVisible(false)}
                >
                  {copy.cancel}
                </button>
              </div>
            </>
          ) : (
            <button
              type="button"
              className="moderation-control-button moderation-control-button--danger"
              disabled={Boolean(busyAction)}
              onClick={() => setDeleteConfirmationVisible(true)}
            >
              {copy.irreversibleDelete}
            </button>
          )}
        </div>
      ) : null}
    </section>
  )
}
