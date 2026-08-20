import { useTranslation } from "react-i18next"
import type { Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"

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

export function UserHubWorkspaces({ systemRoles, navigate }: UserHubWorkspacesProps) {
  const { t } = useTranslation()
  const access = resolveWorkspaceAccess(systemRoles)

  return (
    <>
      <div className="user-hub-workspace-label">{t("appShell.userMenu.workspaces")}</div>

      <nav className="user-hub-workspace-grid">
        {access.creator ? (
          <button
            type="button"
            className="is-creator"
            onClick={() => navigate({ id: "creator-studio" })}
          >
            <span>
              <Glyph name="creator" size={20} />
            </span>
            <strong>{t("appShell.userMenu.creator")}</strong>
            <small>{t("appShell.userMenu.creatorDescription")}</small>
            <b>›</b>
          </button>
        ) : null}

        {access.moderation && !access.administration ? (
          <button
            type="button"
            className="is-moderation"
            onClick={() => navigate({ id: "moderation" })}
          >
            <span>
              <Glyph name="moderation" size={20} />
            </span>
            <strong>{t("appShell.userMenu.moderation")}</strong>
            <small>{t("appShell.userMenu.moderationDescription")}</small>
            <b>›</b>
          </button>
        ) : null}

        {access.reviewerOnly ? (
          <button
            type="button"
            className="is-creator"
            onClick={() => navigate({ id: "personalization-review" })}
          >
            <span>
              <Glyph name="creator" size={20} />
            </span>
            <strong>{t("appShell.userMenu.review")}</strong>
            <small>{t("appShell.userMenu.reviewDescription")}</small>
            <b>›</b>
          </button>
        ) : null}

        {access.administration ? (
          <button
            type="button"
            className="is-admin"
            onClick={() => navigate({ id: "administration" })}
          >
            <span>
              <Glyph name="admin" size={20} />
            </span>
            <strong>{t("appShell.userMenu.administration")}</strong>
            <small>{t("appShell.userMenu.administrationDescription")}</small>
            <b>›</b>
          </button>
        ) : null}
      </nav>
    </>
  )
}
