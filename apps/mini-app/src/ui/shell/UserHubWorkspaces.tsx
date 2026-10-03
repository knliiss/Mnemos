import { useTranslation } from "react-i18next"
import type { Route } from "../model"

type UserHubWorkspacesProps = {
  systemRoles: string[]
  navigate: (route: Route) => void
}

type WorkspaceAccess = {
  creator: boolean
  moderation: boolean
  administration: boolean
  reviewerOnly: boolean
}

function resolveWorkspaceAccess(systemRoles: string[]): WorkspaceAccess {
  const creator = systemRoles.some((role) =>
    ["PERSONALIZATION_CREATOR", "ADMIN", "SUPER_ADMIN"].includes(role),
  )
  const moderation = systemRoles.some((role) =>
    ["MODERATOR", "ADMIN", "SUPER_ADMIN"].includes(role),
  )
  const administration = systemRoles.some((role) => ["ADMIN", "SUPER_ADMIN"].includes(role))
  const personalizationReviewer = systemRoles.includes("PERSONALIZATION_MODERATOR")

  return {
    creator,
    moderation,
    administration,
    reviewerOnly: personalizationReviewer && !administration,
  }
}

function WorkspaceIllustration({ src }: { src: string }) {
  return (
    <span className="user-hub-workspace-art" aria-hidden="true">
      <img src={src} alt="" />
    </span>
  )
}

export function UserHubWorkspaces({ systemRoles, navigate }: UserHubWorkspacesProps) {
  const { t } = useTranslation()
  const access = resolveWorkspaceAccess(systemRoles)

  return (
    <>
      <div className="user-hub-workspace-label">{t("appShell.userMenu.workspaces")}</div>

      <nav className="user-hub-workspace-grid">
        <button
          type="button"
          className="is-notifications"
          onClick={() => navigate({ id: "subscriptions" })}
        >
          <WorkspaceIllustration src="/ui-illustrations/notifications-hub.svg" />
          <span className="user-hub-workspace-copy">
            <strong>{t("appShell.userMenu.notifications")}</strong>
            <small>{t("appShell.userMenu.notificationsDescription")}</small>
          </span>
          <b>›</b>
        </button>

        {access.creator ? (
          <button
            type="button"
            className="is-creator"
            onClick={() => navigate({ id: "creator-studio" })}
          >
            <WorkspaceIllustration src="/ui-illustrations/workspace-creator.svg" />
            <span className="user-hub-workspace-copy">
              <strong>{t("appShell.userMenu.creator")}</strong>
              <small>{t("appShell.userMenu.creatorDescription")}</small>
            </span>
            <b>›</b>
          </button>
        ) : null}

        {access.moderation && !access.administration ? (
          <button
            type="button"
            className="is-moderation"
            onClick={() => navigate({ id: "moderation" })}
          >
            <WorkspaceIllustration src="/ui-illustrations/admin-users.svg" />
            <span className="user-hub-workspace-copy">
              <strong>{t("appShell.userMenu.moderation")}</strong>
              <small>{t("appShell.userMenu.moderationDescription")}</small>
            </span>
            <b>›</b>
          </button>
        ) : null}

        {access.reviewerOnly ? (
          <button
            type="button"
            className="is-creator"
            onClick={() => navigate({ id: "personalization-review" })}
          >
            <WorkspaceIllustration src="/ui-illustrations/admin-review.svg" />
            <span className="user-hub-workspace-copy">
              <strong>{t("appShell.userMenu.review")}</strong>
              <small>{t("appShell.userMenu.reviewDescription")}</small>
            </span>
            <b>›</b>
          </button>
        ) : null}

        {access.administration ? (
          <button
            type="button"
            className="is-admin"
            onClick={() => navigate({ id: "administration" })}
          >
            <WorkspaceIllustration src="/ui-illustrations/workspace-admin.svg" />
            <span className="user-hub-workspace-copy">
              <strong>{t("appShell.userMenu.administration")}</strong>
              <small>{t("appShell.userMenu.administrationDescription")}</small>
            </span>
            <b>›</b>
          </button>
        ) : null}
      </nav>
    </>
  )
}
