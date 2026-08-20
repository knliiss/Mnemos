import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { CristalixHead } from "../components/profile/CristalixHead"

type UserHubTriggerProps = {
  open: boolean
  skinUrl: string
  onToggle: () => void
}

export function UserHubTrigger({ open, skinUrl, onToggle }: UserHubTriggerProps) {
  const { t } = useTranslation()

  return (
    <motion.button
      type="button"
      className={`global-user-button ${open ? "is-open" : ""}`}
      whileTap={{ scale: 0.95 }}
      onClick={onToggle}
      aria-label={t("appShell.userMenu.open")}
    >
      <CristalixHead className="global-user-button__avatar" skinUrl={skinUrl} size={34} />
      <i />
    </motion.button>
  )
}
