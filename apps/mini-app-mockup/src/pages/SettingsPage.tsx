import { motion } from "framer-motion"
import { BellRing, Languages, Moon, Smartphone, Vibrate } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"

function ToggleRow({
  icon,
  title,
  meta,
  value,
  onChange,
}: {
  icon: React.ReactNode
  title: string
  meta: string
  value: boolean
  onChange: () => void
}) {
  return (
    <motion.button className="settings-toggle-row" onClick={onChange} whileTap={{ scale: 0.994 }}>
      <span className="settings-icon">{icon}</span>
      <div>
        <strong>{title}</strong>
        <span>{meta}</span>
      </div>
      <span className={`mock-switch ${value ? "mock-switch--on" : ""}`}>
        <span />
      </span>
    </motion.button>
  )
}

export function SettingsPage({ onBack }: { onBack: () => void }) {
  const [haptics, setHaptics] = useState(true)
  const [motionEnabled, setMotionEnabled] = useState(true)
  const [alerts, setAlerts] = useState(true)

  return (
    <PageFrame page="settings">
      <PageHeader title="Налаштування" eyebrow="USER" onBack={onBack} />

      <section className="detail-context-row">
        <ScopeBadge scope="user" />
        <span>knalis</span>
      </section>

      <section className="settings-block">
        <div className="settings-static-row">
          <span className="settings-icon">
            <Languages size={17} strokeWidth={1.7} />
          </span>
          <div>
            <strong>Мова</strong>
            <span>Українська</span>
          </div>
          <button>UK</button>
        </div>

        <div className="settings-static-row">
          <span className="settings-icon">
            <Moon size={17} strokeWidth={1.7} />
          </span>
          <div>
            <strong>Тема</strong>
            <span>Темна</span>
          </div>
          <span className="settings-value">Dark</span>
        </div>
      </section>

      <section className="settings-block settings-block--separate">
        <ToggleRow
          icon={<Vibrate size={17} strokeWidth={1.7} />}
          title="Haptics"
          meta="Тактильний відгук на ключові дії"
          value={haptics}
          onChange={() => setHaptics((value) => !value)}
        />
        <ToggleRow
          icon={<Smartphone size={17} strokeWidth={1.7} />}
          title="Motion"
          meta="Плавні переходи та spring-анімації"
          value={motionEnabled}
          onChange={() => setMotionEnabled((value) => !value)}
        />
        <ToggleRow
          icon={<BellRing size={17} strokeWidth={1.7} />}
          title="In-app alerts"
          meta="Події та результати через Mini App"
          value={alerts}
          onChange={() => setAlerts((value) => !value)}
        />
      </section>

      <section className="telegram-shell-note">
        <span>Telegram Mini App</span>
        <strong>safe-area · viewport · theme</strong>
      </section>
    </PageFrame>
  )
}
