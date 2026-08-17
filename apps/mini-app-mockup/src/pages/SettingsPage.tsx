import { Bell, Languages, Moon, Vibrate } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"

export function SettingsPage({
  onBack,
  onNotify,
}: {
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [motionEnabled, setMotionEnabled] = useState(true)
  const [haptics, setHaptics] = useState(true)
  const [notifications, setNotifications] = useState(true)

  return (
    <PageFrame page="settings">
      <PageHeader title="Налаштування" eyebrow="USER" onBack={onBack} />

      <section className="detail-context-row"><ScopeBadge scope="user" /><span>knalis</span></section>

      <section className="settings-group">
        <button className="settings-item" onClick={() => onNotify("Мова: Українська")}>
          <span className="settings-item-icon"><Languages size={18} strokeWidth={1.7} /></span>
          <div><strong>Мова</strong><span>Українська</span></div>
          <span className="settings-value">UK</span>
        </button>

        <button className="settings-item" onClick={() => onNotify("Тема наслідує Telegram")}>
          <span className="settings-item-icon"><Moon size={18} strokeWidth={1.7} /></span>
          <div><strong>Тема</strong><span>Telegram</span></div>
          <span className="settings-value">Auto</span>
        </button>
      </section>

      <section className="settings-group">
        <button className="settings-item" onClick={() => setMotionEnabled((value) => !value)}>
          <span className="settings-item-icon"><Moon size={18} strokeWidth={1.7} /></span>
          <div><strong>Анімації</strong><span>Плавні переходи</span></div>
          <span className={`toggle ${motionEnabled ? "toggle--on" : ""}`}><span /></span>
        </button>
        <button className="settings-item" onClick={() => setHaptics((value) => !value)}>
          <span className="settings-item-icon"><Vibrate size={18} strokeWidth={1.7} /></span>
          <div><strong>Haptics</strong><span>Telegram Mini App</span></div>
          <span className={`toggle ${haptics ? "toggle--on" : ""}`}><span /></span>
        </button>
        <button className="settings-item" onClick={() => setNotifications((value) => !value)}>
          <span className="settings-item-icon"><Bell size={18} strokeWidth={1.7} /></span>
          <div><strong>Сповіщення</strong><span>Основний канал</span></div>
          <span className={`toggle ${notifications ? "toggle--on" : ""}`}><span /></span>
        </button>
      </section>

      <p className="settings-footnote">Це мокап. Значення не зберігаються та не відправляються в API.</p>
    </PageFrame>
  )
}
