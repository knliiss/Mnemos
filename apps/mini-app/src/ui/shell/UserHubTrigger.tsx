import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { UserAvatar } from "./UserAvatar"

type UserHubTriggerProps = {
  open: boolean
  onToggle: () => void
}

export function UserHubTrigger({ open, onToggle }: UserHubTriggerProps) {
  const { t } = useTranslation()

  return (
    <motion.button
      type="button"
      className={`global-user-button ${open ? "is-open" : ""}`}
      whileTap={{ scale: 0.95 }}
      onClick={onToggle}
      aria-label={t("productShell.userMenu.open")}
    >
      <UserAvatar className="global-user-button__avatar" size={34} />
      <i />
    </motion.button>
  )
}
