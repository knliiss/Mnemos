import {
  Bell,
  Gift,
  Layers,
  MessageCircle,
  Settings,
  Sparkles,
  Target,
  UserRound,
  Wallet,
} from "lucide-react"
import { CurrencyMark } from "../components/CurrencyMark"
import { NavRow } from "../components/NavRow"
import { PageFrame } from "../components/PageFrame"
import { ScopeBadge } from "../components/ScopeBadge"
import type { MockAccount, Page } from "../types"

export function ServicesPage({
  activeAccount,
  onNavigate,
}: {
  activeAccount: MockAccount
  onNavigate: (page: Page) => void
}) {
  return (
    <PageFrame page="services">
      <section className="page-title-row page-title-row--plain">
        <div>
          <span className="eyebrow">MNEMOS</span>
          <h1>Сервіси</h1>
        </div>
      </section>

      <section className="service-group">
        <div className="service-group-heading">
          <ScopeBadge scope="account" />
          <span>{activeAccount.nickname}</span>
        </div>

        <div className="nav-list">
          <NavRow
            icon={<Wallet size={18} strokeWidth={1.75} />}
            title="Гаманець"
            value={
              <span className="inline-balance">
                <CurrencyMark currency="coin" />
                {activeAccount.coinBalance.toLocaleString("uk-UA")}
              </span>
            }
            onClick={() => onNavigate("wallet")}
          />
          <NavRow
            icon={<Gift size={18} strokeWidth={1.75} />}
            title="Нагороди"
            value={`${activeAccount.streak}d`}
            onClick={() => onNavigate("rewards")}
          />
          <NavRow
            icon={<Target size={18} strokeWidth={1.75} />}
            title="Ставки"
            value="2 open"
            onClick={() => onNavigate("bets")}
          />
          <NavRow
            icon={<MessageCircle size={18} strokeWidth={1.75} />}
            title="Соціальне"
            value="24"
            onClick={() => onNavigate("social")}
          />
        </div>
      </section>

      <section className="service-group">
        <div className="service-group-heading">
          <ScopeBadge scope="user" />
          <span>knalis</span>
        </div>

        <div className="nav-list">
          <NavRow
            icon={<Sparkles size={18} strokeWidth={1.75} />}
            title="Персоналізація"
            value="Default"
            onClick={() => onNavigate("personalization")}
          />
          <NavRow
            icon={<Bell size={18} strokeWidth={1.75} />}
            title="Підписки"
            value="8"
            onClick={() => onNavigate("subscriptions")}
          />
          <NavRow
            icon={<Bell size={18} strokeWidth={1.75} />}
            title="Сповіщення"
            value={<span className="status-dot" />}
            onClick={() => onNavigate("notifications")}
          />
          <NavRow
            icon={<Settings size={18} strokeWidth={1.75} />}
            title="Налаштування"
            onClick={() => onNavigate("settings")}
          />
        </div>
      </section>

      <section className="scope-explainer">
        <div>
          <Layers size={15} strokeWidth={1.75} />
          <span>Аккаунт</span>
        </div>
        <div>
          <UserRound size={15} strokeWidth={1.75} />
          <span>Юзер</span>
        </div>
      </section>
    </PageFrame>
  )
}
