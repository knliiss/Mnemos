import { motion } from "framer-motion"
import {
  BellRing,
  ChevronRight,
  CircleUserRound,
  Languages,
  Link2,
  Settings2,
  SlidersHorizontal,
  UserRoundCog,
  WalletCards,
} from "lucide-react"
import { AccountMark } from "../components/AccountMark"
import { CristalixHead } from "../components/CristalixHead"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import type { MockAccount } from "../types"

type ProfilePageProps = {
  activeAccount: MockAccount
}

export function ProfilePage({ activeAccount }: ProfilePageProps) {
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
          whileTap={{ scale: 0.92 }}
          aria-label="Налаштування"
        >
          <Settings2 size={19} strokeWidth={1.8} />
        </motion.button>
      </section>

      <motion.section
        className="profile-hero"
        initial={{ opacity: 0, scale: 0.985, y: 7 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ delay: 0.04, duration: 0.28, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className="head-stage">
          <div className="head-shadow" />
          <CristalixHead skinUrl={activeAccount.skinUrl} size={116} />
        </div>

        <div className="profile-account-copy">
          <span className="linked-account-label">
            <Link2 size={13} strokeWidth={1.8} />
            Cristalix
          </span>
          <h1>{activeAccount.nickname}</h1>
          <span className="account-id">{activeAccount.shortId}</span>
        </div>
      </motion.section>

      <section className="scope-section">
        <div className="scope-heading scope-heading--user">
          <CircleUserRound size={16} strokeWidth={1.7} />
          <span>Юзер</span>
        </div>

        <div className="settings-list">
          <motion.button className="settings-row" whileTap={{ scale: 0.99 }}>
            <span className="settings-icon">
              <BellRing size={17} strokeWidth={1.65} />
            </span>
            <div>
              <strong>Notification pack</strong>
              <span>Default</span>
            </div>
            <ChevronRight size={17} strokeWidth={1.7} />
          </motion.button>

          <motion.button className="settings-row" whileTap={{ scale: 0.99 }}>
            <span className="settings-icon">
              <SlidersHorizontal size={17} strokeWidth={1.65} />
            </span>
            <div>
              <strong>Персоналізація</strong>
              <span>2 активні</span>
            </div>
            <ChevronRight size={17} strokeWidth={1.7} />
          </motion.button>

          <motion.button className="settings-row" whileTap={{ scale: 0.99 }}>
            <span className="settings-icon">
              <Languages size={17} strokeWidth={1.65} />
            </span>
            <div>
              <strong>Мова</strong>
              <span>Українська</span>
            </div>
            <ChevronRight size={17} strokeWidth={1.7} />
          </motion.button>
        </div>
      </section>

      <section className="scope-section scope-section--account">
        <div className="scope-heading scope-heading--account">
          <AccountMark compact />
          <span>Аккаунт</span>
        </div>

        <div className="account-summary">
          <div className="account-summary-main">
            <AccountMark />
            <div>
              <strong>{activeAccount.nickname}</strong>
              <span>active</span>
            </div>
          </div>

          <div className="account-summary-wallet" aria-label="Баланс аккаунта">
            <span>
              <CurrencyMark currency="coin" />
              12 480
            </span>
            <span>
              <CurrencyMark currency="prime" />
              340
            </span>
          </div>
        </div>

        <div className="settings-list settings-list--separate">
          <motion.button className="settings-row" whileTap={{ scale: 0.99 }}>
            <span className="settings-icon">
              <WalletCards size={17} strokeWidth={1.65} />
            </span>
            <div>
              <strong>Транзакції</strong>
              <span>Історія аккаунта</span>
            </div>
            <ChevronRight size={17} strokeWidth={1.7} />
          </motion.button>

          <motion.button className="settings-row" whileTap={{ scale: 0.99 }}>
            <span className="settings-icon">
              <UserRoundCog size={17} strokeWidth={1.65} />
            </span>
            <div>
              <strong>Доступ</strong>
              <span>Ролі та права</span>
            </div>
            <ChevronRight size={17} strokeWidth={1.7} />
          </motion.button>
        </div>
      </section>
    </PageFrame>
  )
}
