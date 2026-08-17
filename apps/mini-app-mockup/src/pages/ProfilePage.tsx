import { motion } from "framer-motion"
import {
  ChevronRight,
  MessageCircle,
  Settings,
  Sparkles,
  ThumbsDown,
  ThumbsUp,
  Wallet,
} from "lucide-react"
import { CristalixHead } from "../components/CristalixHead"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { ScopeBadge } from "../components/ScopeBadge"
import type { MockAccount, Page } from "../types"

export function ProfilePage({
  activeAccount,
  onNavigate,
}: {
  activeAccount: MockAccount
  onNavigate: (page: Page) => void
}) {
  return (
    <PageFrame page="profile">
      <section className="profile-topline">
        <div className="user-identity">
          <span className="user-avatar">K</span>
          <div>
            <strong>knalis</strong>
            <span>@knalis</span>
          </div>
        </div>

        <motion.button
          className="icon-button"
          onClick={() => onNavigate("settings")}
          whileTap={{ scale: 0.92 }}
          aria-label="Налаштування"
        >
          <Settings size={19} strokeWidth={1.8} />
        </motion.button>
      </section>

      <motion.section
        className="profile-hero"
        initial={{ opacity: 0, scale: 0.985 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.28, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className="head-stage">
          <div className="head-shadow" />
          <CristalixHead skinUrl={activeAccount.skinUrl} size={112} />
        </div>

        <div className="profile-account-copy">
          <ScopeBadge scope="account" />
          <h1>{activeAccount.nickname}</h1>
          <span className="account-id">{activeAccount.shortId} · {activeAccount.role}</span>
        </div>
      </motion.section>

      <section className="profile-stats" aria-label="Статистика профілю">
        <button onClick={() => onNavigate("social")}>
          <ThumbsUp size={15} strokeWidth={1.7} />
          <strong>128</strong>
        </button>
        <span />
        <button onClick={() => onNavigate("social")}>
          <ThumbsDown size={15} strokeWidth={1.7} />
          <strong>9</strong>
        </button>
        <span />
        <button onClick={() => onNavigate("social")}>
          <MessageCircle size={15} strokeWidth={1.7} />
          <strong>24</strong>
        </button>
      </section>

      <section className="profile-section">
        <div className="profile-section-heading">
          <ScopeBadge scope="user" />
        </div>

        <div className="profile-grid">
          <motion.button
            className="profile-tile"
            onClick={() => onNavigate("personalization")}
            whileTap={{ scale: 0.98 }}
          >
            <Sparkles size={18} strokeWidth={1.7} />
            <div>
              <span>Notification pack</span>
              <strong>Default</strong>
            </div>
            <ChevronRight size={16} strokeWidth={1.7} />
          </motion.button>

          <motion.button
            className="profile-tile"
            onClick={() => onNavigate("subscriptions")}
            whileTap={{ scale: 0.98 }}
          >
            <span className="profile-tile-number">8</span>
            <div>
              <span>Підписки</span>
              <strong>Активні</strong>
            </div>
            <ChevronRight size={16} strokeWidth={1.7} />
          </motion.button>
        </div>
      </section>

      <section className="profile-section">
        <div className="profile-section-heading">
          <ScopeBadge scope="account" />
        </div>

        <div className="profile-account-summary">
          <button onClick={() => onNavigate("wallet")}>
            <Wallet size={17} strokeWidth={1.7} />
            <span className="inline-balance inline-balance--large">
              <CurrencyMark currency="coin" />
              {activeAccount.coinBalance.toLocaleString("uk-UA")}
            </span>
            <span className="inline-balance inline-balance--large">
              <CurrencyMark currency="prime" />
              {activeAccount.primeBalance.toLocaleString("uk-UA")}
            </span>
            <ChevronRight size={16} strokeWidth={1.7} />
          </button>

          <button onClick={() => onNavigate("social")}>
            <MessageCircle size={17} strokeWidth={1.7} />
            <span>Публічний профіль</span>
            <ChevronRight size={16} strokeWidth={1.7} />
          </button>
        </div>
      </section>
    </PageFrame>
  )
}
