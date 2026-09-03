import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  addAccountMember,
  deleteAccountPermanently,
  getAdminAccount,
  removeAccountMember,
  transferAccountOwnership,
  type AdminAccountDetailsResponse,
  type UserIdentityDirectoryResponse,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { AdminUserPicker } from "./AdminUserPicker"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"

type Props = {
  accountId: string
  canDelete: boolean
  copy: ReturnType<typeof resolveModerationDirectoryCopy>
  onChanged(): Promise<void>
  onOpenUser?: (userId: string) => void
}

type MemberConfirmation =
  | { kind: "OWNER"; memberId: string; userId: string; username: string }
  | { kind: "REMOVE"; memberId: string; userId: string; username: string }
  | null

export function ModerationAccountControlPanel({
  accountId,
  canDelete,
  copy,
  onChanged,
  onOpenUser,
}: Props) {
  const { t } = useTranslation()
  const [details, setDetails] = useState<AdminAccountDetailsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [busyAction, setBusyAction] = useState<string | null>(null)
  const [newMember, setNewMember] = useState<UserIdentityDirectoryResponse | null>(null)
  const [newOwner, setNewOwner] = useState<UserIdentityDirectoryResponse | null>(null)
  const [memberConfirmation, setMemberConfirmation] = useState<MemberConfirmation>(null)
  const [ownerConfirmationVisible, setOwnerConfirmationVisible] = useState(false)
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
    let active = true

    void getAdminAccount(accountId)
      .then((accountDetails) => {
        if (active) {
          setDetails(accountDetails)
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
  }, [accountId])

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

  const memberUserIds = useMemo(
    () => details?.members.map((member) => member.userId) ?? [],
    [details?.members],
  )

  if (loading && !details) {
    return <div className="moderation-account-control__loading">{copy.loadingAccount}</div>
  }

  if (!details) {
    return null
  }

  const deleted = details.status === "DELETED"

  const confirmMemberAction = async () => {
    if (!memberConfirmation) {
      return
    }

    const confirmation = memberConfirmation
    setMemberConfirmation(null)

    if (confirmation.kind === "OWNER") {
      await runMutation(`owner:${confirmation.userId}`, () =>
        transferAccountOwnership(accountId, confirmation.userId),
      )
      return
    }

    await runMutation(`remove:${confirmation.memberId}`, () =>
      removeAccountMember(accountId, confirmation.memberId),
    )
  }

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
              {onOpenUser ? (
                <button
                  type="button"
                  className="moderation-account-member__user-link"
                  onClick={() => onOpenUser(member.userId)}
                >
                  @{member.username}
                </button>
              ) : (
                <strong>@{member.username}</strong>
              )}
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
                      setMemberConfirmation({
                        kind: "OWNER",
                        memberId: member.memberId,
                        userId: member.userId,
                        username: member.username,
                      })
                    }
                  >
                    {copy.transferOwner}
                  </button>
                  <button
                    type="button"
                    className="moderation-control-button moderation-control-button--danger"
                    disabled={Boolean(busyAction)}
                    onClick={() =>
                      setMemberConfirmation({
                        kind: "REMOVE",
                        memberId: member.memberId,
                        userId: member.userId,
                        username: member.username,
                      })
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

      {memberConfirmation ? (
        <div className="moderation-account-control__confirmation">
          <span>
            <strong>
              {memberConfirmation.kind === "OWNER"
                ? t("moderation.confirmOwnershipTransfer", "Transfer account ownership?")
                : t("moderation.confirmMemberRemoval", "Remove this account member?")}
            </strong>
            <small>@{memberConfirmation.username}</small>
          </span>
          <div>
            <button
              type="button"
              className={
                memberConfirmation.kind === "REMOVE"
                  ? "moderation-control-button moderation-control-button--danger"
                  : "moderation-control-button moderation-control-button--primary"
              }
              disabled={Boolean(busyAction)}
              onClick={() => void confirmMemberAction()}
            >
              {t("common.confirm", "Confirm")}
            </button>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--neutral"
              disabled={Boolean(busyAction)}
              onClick={() => setMemberConfirmation(null)}
            >
              {copy.cancel}
            </button>
          </div>
        </div>
      ) : null}

      {!deleted ? (
        <div className="moderation-account-control__admin-actions">
          <div className="moderation-account-control__picker-action">
            <AdminUserPicker
              label={t("moderation.addMember", copy.addMember)}
              placeholder={t(
                "moderation.userPickerPlaceholder",
                "Search by username or Telegram ID",
              )}
              disabled={Boolean(busyAction)}
              excludedUserIds={memberUserIds}
              selected={newMember}
              onSelect={setNewMember}
            />
            <button
              type="button"
              className="moderation-control-button moderation-control-button--primary"
              disabled={!newMember || Boolean(busyAction)}
              onClick={() =>
                void runMutation("add-member", async () => {
                  if (!newMember) {
                    return
                  }

                  await addAccountMember(accountId, newMember.userId)
                  setNewMember(null)
                })
              }
            >
              {busyAction === "add-member" ? copy.addingMember : copy.addMember}
            </button>
          </div>

          <div className="moderation-account-control__picker-action">
            <AdminUserPicker
              label={t("moderation.transferOwnership", copy.transferOwner)}
              placeholder={t(
                "moderation.ownerPickerPlaceholder",
                "Search for the new owner",
              )}
              disabled={Boolean(busyAction)}
              selected={newOwner}
              onSelect={(candidate) => {
                setNewOwner(candidate)
                setOwnerConfirmationVisible(false)
              }}
            />

            {ownerConfirmationVisible && newOwner ? (
              <div className="moderation-account-control__picker-confirmation">
                <span>
                  {t(
                    "moderation.ownerTransferWarning",
                    "The selected user will become the account owner immediately.",
                  )}
                </span>
                <div>
                  <button
                    type="button"
                    className="moderation-control-button moderation-control-button--primary"
                    disabled={Boolean(busyAction)}
                    onClick={() =>
                      void runMutation("transfer-owner", async () => {
                        await transferAccountOwnership(accountId, newOwner.userId)
                        setNewOwner(null)
                        setOwnerConfirmationVisible(false)
                      })
                    }
                  >
                    {t("common.confirm", "Confirm")}
                  </button>
                  <button
                    type="button"
                    className="moderation-control-button moderation-control-button--neutral"
                    disabled={Boolean(busyAction)}
                    onClick={() => setOwnerConfirmationVisible(false)}
                  >
                    {copy.cancel}
                  </button>
                </div>
              </div>
            ) : (
              <button
                type="button"
                className="moderation-control-button moderation-control-button--primary"
                disabled={!newOwner || Boolean(busyAction)}
                onClick={() => setOwnerConfirmationVisible(true)}
              >
                {copy.transferOwner}
              </button>
            )}
          </div>
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
