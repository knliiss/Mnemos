import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { Badge } from "@/shared/api/badgeApi"
import { UserBadgeMark } from "./badges/UserBadgeMark"

type UserHubTriggerProps = {
  open: boolean
  activeBadge: Badge | null
  onToggle: () => void
}

export function UserHubTrigger({ open, activeBadge, onToggle }: UserHubTriggerProps) {
  const { t } = useTranslation()

  return (
    <motion.button
      type="button"
      className={`global-user-button ${open ? "is-open" : ""}`}
      whileTap={{ scale: 0.95 }}
      onClick={onToggle}
      aria-label={t("appShell.userMenu.open")}
    >
      <img
        className="global-user-button__avatar global-user-button__mnemos-mark"
        src="/ui-illustrations/mnemos-user-mark.svg"
        alt=""
        aria-hidden="true"
      />
      {activeBadge ? <UserBadgeMark badge={activeBadge} compact /> : <i />}
    </motion.button>
  )
}
