import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { Route } from "../../model"

type AccountQuickActionsProps = {
  navigate: (route: Route) => void
  canOpenGuides: boolean
  openGuides: () => void
}

type QuickAction = {
  key: string
  title: string
  copy: string
  illustration: string
  route?: Route
  action?: () => void
}

export function AccountQuickActions({
  navigate,
  canOpenGuides,
  openGuides,
}: AccountQuickActionsProps) {
  const { t } = useTranslation()
  const actions: QuickAction[] = [
    {
      key: "roles",
      title: t("accountHome.roles"),
      copy: t("accountHome.accessRights"),
      illustration: "/ui-illustrations/account-roles.svg",
      route: { id: "roles" },
    },
    {
      key: "invites",
      title: t("accountHome.invites"),
      copy: t("accountHome.invitations"),
      illustration: "/ui-illustrations/account-invites.svg",
      route: { id: "account-invites" },
    },
    {
      key: "guides",
      title: t("accountHome.guides"),
      copy: canOpenGuides ? "SAO" : t("accountHome.guidesSoon"),
      illustration: "/ui-illustrations/account-guides.svg",
      action: canOpenGuides ? openGuides : undefined,
    },
    {
      key: "leaderboards",
      title: t("accountHome.leaderboards"),
      copy: t("accountHome.leaderboardsSummary"),
      illustration: "/ui-illustrations/admin-leaderboards.svg",
      route: { id: "leaderboards" },
    },
    {
      key: "settings",
      title: t("accountHome.settings"),
      copy: t("accountHome.account"),
      illustration: "/ui-illustrations/account-settings.svg",
      route: { id: "account-settings" },
    },
  ]

  return (
    <nav
      className="account-home-actions account-home-actions--dashboard"
      aria-label={t("accountHome.managementAria")}
    >
      {actions.map((action) => {
        const enabled = Boolean(action.route || action.action)

        return (
          <motion.button
            type="button"
            key={action.key}
            className={`account-home-action account-home-action--${action.key}`}
            whileTap={enabled ? { scale: 0.975 } : undefined}
            disabled={!enabled}
            onClick={() => {
              if (action.route) {
                navigate(action.route)
                return
              }

              action.action?.()
            }}
          >
            <span className="account-home-action__art" aria-hidden="true">
              <img src={action.illustration} alt="" />
            </span>

            <span className="account-home-action__copy">
              <strong>{action.title}</strong>
              <small>{action.copy}</small>
            </span>
          </motion.button>
        )
      })}
    </nav>
  )
}
