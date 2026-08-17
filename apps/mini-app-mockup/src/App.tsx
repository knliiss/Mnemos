import { AnimatePresence, MotionConfig, motion } from "framer-motion"
import {
  Activity,
  Bell,
  Check,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  CircleUserRound,
  Crown,
  Eye,
  Gift,
  Globe2,
  History,
  Link2,
  MapPin,
  MessageCircle,
  Package,
  Plus,
  Radio,
  RotateCcw,
  Send,
  Shield,
  Sparkles,
  Target,
  ThumbsDown,
  ThumbsUp,
  Trash2,
  UserRound,
  Users,
  Wallet,
  X,
  Zap,
} from "lucide-react"
import { FormEvent, ReactNode, useMemo, useState } from "react"
import { CristalixHead } from "./components/CristalixHead"
import { CurrencyMark } from "./components/CurrencyMark"

type RootScreen = "events" | "account" | "me"
type DetailScreen =
  | "subscriptions"
  | "bets"
  | "member"
  | "public-profile"
  | "account-wallet"
  | "user-wallet"
  | "personalization"
type Screen = RootScreen | DetailScreen

type Account = {
  id: string
  nickname: string
  shortId: string
  role: string
  skinUrl: string
  coin: number
  prime: number
}

type Member = {
  id: string
  name: string
  owner: boolean
  joinedAt: string
  roles: string[]
}

type EventKind = "ITEM_DROP" | "BOOSTER" | "GLOBAL" | "RAID"
type FeedEvent = {
  id: string
  kind: EventKind
  occurredAt: string
  relative: string
  title: string
  detail: string
  meta?: string
  rarity?: string
}

type CommentState = "sent" | "pending" | "failed"
type ProfileComment = {
  id: string
  author: string
  text: string
  time: string
  likes: number
  dislikes: number
  myLike: boolean | null
  state: CommentState
}

type SubscriptionKind = "ITEM_DROP" | "BOOSTER" | "DUNGEON" | "RAID" | "GLOBAL"
type SubscriptionRule = {
  id: string
  kind: SubscriptionKind
  title: string
  detail: string
}

const skinUrl = "https://webdata.c7x.dev/textures/skin/46d7a80e-74f5-11eb-acca-1cb72caa35fd"

const accounts: Account[] = [
  {
    id: "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
    nickname: "knalis",
    shortId: "46d7a80e",
    role: "Owner",
    skinUrl,
    coin: 12480,
    prime: 340,
  },
  {
    id: "0f4e8d3c-2a9a-4e9d-9d18-7cb58ec00f18",
    nickname: "Knaliss",
    shortId: "0f4e8d3c",
    role: "Member",
    skinUrl,
    coin: 4860,
    prime: 96,
  },
]

const initialMembers: Member[] = [
  { id: "member-1", name: "knalis", owner: true, joinedAt: "12 лип. 2026", roles: ["Owner"] },
  { id: "member-2", name: "north", owner: false, joinedAt: "18 лип. 2026", roles: ["Admin"] },
  { id: "member-3", name: "vex", owner: false, joinedAt: "2 серп. 2026", roles: ["Member"] },
  { id: "member-4", name: "mare", owner: false, joinedAt: "9 серп. 2026", roles: ["Member"] },
]

const roleCatalog = [
  { id: "owner", name: "Owner", color: "#a8d6b2", rank: 100, locked: true },
  { id: "admin", name: "Admin", color: "#9daab8", rank: 80, locked: false },
  { id: "moderator", name: "Moderator", color: "#80b89a", rank: 60, locked: false },
  { id: "member", name: "Member", color: "#68736c", rank: 10, locked: false },
]

const feed: FeedEvent[] = [
  {
    id: "ev-1",
    kind: "ITEM_DROP",
    occurredAt: "2026-08-17T16:48:12Z",
    relative: "2 хв тому",
    title: "Eternal Blade",
    detail: "Вибив knalis",
    meta: "Sword",
    rarity: "Mythical",
  },
  {
    id: "ev-2",
    kind: "BOOSTER",
    occurredAt: "2026-08-17T16:42:41Z",
    relative: "8 хв тому",
    title: "Loot booster",
    detail: "Активував vex",
    meta: "x2",
  },
  {
    id: "ev-3",
    kind: "GLOBAL",
    occurredAt: "2026-08-17T16:31:03Z",
    relative: "19 хв тому",
    title: "Eclipse",
    detail: "Глобальна подія",
  },
  {
    id: "ev-4",
    kind: "RAID",
    occurredAt: "2026-08-17T16:12:20Z",
    relative: "38 хв тому",
    title: "Raid",
    detail: "Локації 2, 4, 7",
    meta: "3 locations",
  },
  {
    id: "ev-5",
    kind: "ITEM_DROP",
    occurredAt: "2026-08-17T15:58:51Z",
    relative: "52 хв тому",
    title: "Moon Necklace",
    detail: "Вибив north",
    meta: "Jewelry",
    rarity: "Secret",
  },
]

const accountTransactions = [
  { id: "t1", source: "Daily reward", amount: 420, currency: "coin" as const, time: "сьогодні · 10:14" },
  { id: "t2", source: "Bet position", amount: -750, currency: "coin" as const, time: "вчора · 23:51" },
  { id: "t3", source: "Personalization", amount: -30, currency: "prime" as const, time: "вчора · 19:40" },
]

const userTransactions = [
  { id: "ut1", source: "Personalization", amount: -45, currency: "prime" as const, time: "16 серп. · 18:32" },
  { id: "ut2", source: "Admin adjustment", amount: 100, currency: "prime" as const, time: "14 серп. · 12:05" },
]

const initialSubscriptions: SubscriptionRule[] = [
  { id: "sub-1", kind: "ITEM_DROP", title: "Item drop", detail: "droppedFor = knalis" },
  { id: "sub-2", kind: "ITEM_DROP", title: "Item drop", detail: "itemName = Eternal Blade" },
  { id: "sub-3", kind: "GLOBAL", title: "Global", detail: "ECLIPSE" },
  { id: "sub-4", kind: "RAID", title: "Raid", detail: "locations = 2, 4, 7" },
  { id: "sub-5", kind: "DUNGEON", title: "Dungeon", detail: "EXPERT" },
]

const initialComments: ProfileComment[] = [
  {
    id: "comment-1",
    author: "north",
    text: "Нормальний профіль. За останній дроп окремий респект.",
    time: "18 хв тому",
    likes: 14,
    dislikes: 1,
    myLike: null,
    state: "sent",
  },
  {
    id: "comment-2",
    author: "vex",
    text: "Міфік за один вечір — це вже підозріло.",
    time: "1 год тому",
    likes: 8,
    dislikes: 3,
    myLike: true,
    state: "sent",
  },
]

const recentDrops = [
  { id: "drop-1", name: "Eternal Blade", rarity: "Mythical", type: "Sword", time: "2 хв тому" },
  { id: "drop-2", name: "Crimson Aura", rarity: "Secret", type: "Aura", time: "2 дні тому" },
  { id: "drop-3", name: "Fenrir", rarity: "Mythical", type: "Pet", time: "6 днів тому" },
]

function isRootScreen(screen: Screen): screen is RootScreen {
  return screen === "events" || screen === "account" || screen === "me"
}

function eventIcon(kind: EventKind, size = 19) {
  if (kind === "ITEM_DROP") return <Package size={size} strokeWidth={1.7} />
  if (kind === "BOOSTER") return <Zap size={size} strokeWidth={1.7} />
  if (kind === "GLOBAL") return <Globe2 size={size} strokeWidth={1.7} />
  return <MapPin size={size} strokeWidth={1.7} />
}

function subscriptionIcon(kind: SubscriptionKind) {
  if (kind === "ITEM_DROP") return <Package size={18} strokeWidth={1.7} />
  if (kind === "BOOSTER") return <Zap size={18} strokeWidth={1.7} />
  if (kind === "GLOBAL") return <Globe2 size={18} strokeWidth={1.7} />
  if (kind === "RAID") return <MapPin size={18} strokeWidth={1.7} />
  return <Activity size={18} strokeWidth={1.7} />
}

function GlassHeader({
  title,
  eyebrow,
  onBack,
  action,
}: {
  title: string
  eyebrow?: string
  onBack: () => void
  action?: ReactNode
}) {
  return (
    <header className="detail-header glass-header">
      <motion.button className="round-button" onClick={onBack} whileTap={{ scale: 0.92 }} aria-label="Назад">
        <ChevronLeft size={20} strokeWidth={1.8} />
      </motion.button>
      <div className="detail-header-copy">
        {eyebrow && <span>{eyebrow}</span>}
        <strong>{title}</strong>
      </div>
      <div className="detail-header-action">{action ?? <span />}</div>
    </header>
  )
}

function BottomNavigation({ screen, onChange }: { screen: RootScreen; onChange: (screen: RootScreen) => void }) {
  const items: Array<{ id: RootScreen; label: string; icon: ReactNode }> = [
    { id: "events", label: "Події", icon: <Radio size={19} strokeWidth={1.75} /> },
    { id: "account", label: "Аккаунт", icon: <Users size={19} strokeWidth={1.75} /> },
    { id: "me", label: "Профіль", icon: <CircleUserRound size={19} strokeWidth={1.75} /> },
  ]

  return (
    <nav className="bottom-navigation glass-surface" aria-label="Основна навігація">
      {items.map((item) => (
        <button key={item.id} className={screen === item.id ? "nav-tab nav-tab--active" : "nav-tab"} onClick={() => onChange(item.id)}>
          {screen === item.id && <motion.span layoutId="nav-active" className="nav-tab-glow" transition={{ type: "spring", stiffness: 430, damping: 36 }} />}
          <span className="nav-tab-content">{item.icon}<small>{item.label}</small></span>
        </button>
      ))}
    </nav>
  )
}

function AccountSwitcher({
  open,
  activeAccount,
  onClose,
  onSelect,
}: {
  open: boolean
  activeAccount: Account
  onClose: () => void
  onSelect: (account: Account) => void
}) {
  return (
    <AnimatePresence>
      {open && (
        <>
          <motion.button className="sheet-backdrop" aria-label="Закрити" onClick={onClose} initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} />
          <motion.section className="bottom-sheet glass-sheet" initial={{ y: "100%" }} animate={{ y: 0 }} exit={{ y: "100%" }} transition={{ type: "spring", stiffness: 310, damping: 32 }}>
            <div className="sheet-handle" />
            <div className="sheet-heading"><div><span>CONTEXT</span><h2>Аккаунт</h2></div><button onClick={onClose}><X size={19} /></button></div>
            <div className="account-choice-list">
              {accounts.map((account) => (
                <button key={account.id} onClick={() => onSelect(account)} className={account.id === activeAccount.id ? "account-choice account-choice--active" : "account-choice"}>
                  <CristalixHead skinUrl={account.skinUrl} size={42} />
                  <div><strong>{account.nickname}</strong><span>{account.shortId} · {account.role}</span></div>
                  {account.id === activeAccount.id ? <Check size={17} /> : <ChevronRight size={17} />}
                </button>
              ))}
            </div>
          </motion.section>
        </>
      )}
    </AnimatePresence>
  )
}

function EventsPage({ onNavigate }: { onNavigate: (screen: DetailScreen) => void }) {
  const [expandedEventId, setExpandedEventId] = useState<string | null>(feed[0].id)
  const latest = feed[0]

  return (
    <div className="page root-page events-page">
      <section className="root-heading">
        <div>
          <div className="live-label"><span className="live-dot" /> REALTIME</div>
          <h1>Події</h1>
        </div>
        <motion.button className="round-button glass-control" onClick={() => onNavigate("subscriptions")} whileTap={{ scale: 0.92 }} aria-label="Підписки">
          <Bell size={20} strokeWidth={1.75} />
          <span className="control-indicator" />
        </motion.button>
      </section>

      <motion.button className="latest-event glass-surface" onClick={() => setExpandedEventId(expandedEventId === latest.id ? null : latest.id)} whileTap={{ scale: 0.99 }}>
        <div className="latest-event-topline">
          <span className="event-symbol event-symbol--drop">{eventIcon(latest.kind, 21)}</span>
          <span className="event-time-relative">{latest.relative}</span>
        </div>
        <div className="latest-event-copy">
          <span className="section-kicker">ОСТАННЯ ПОДІЯ</span>
          <h2>{latest.title}</h2>
          <p>{latest.detail}</p>
        </div>
        <div className="latest-event-meta">
          {latest.rarity && <span className="rarity-chip">{latest.rarity}</span>}
          {latest.meta && <span>{latest.meta}</span>}
        </div>
        <AnimatePresence initial={false}>
          {expandedEventId === latest.id && (
            <motion.div className="event-expanded" initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }} exit={{ height: 0, opacity: 0 }}>
              <div><span>occurredAt</span><strong>17 серп. · 19:48</strong></div>
              <div><span>type</span><strong>ITEM_DROP</strong></div>
            </motion.div>
          )}
        </AnimatePresence>
      </motion.button>

      <div className="event-actions">
        <motion.button className="context-action glass-surface" onClick={() => onNavigate("bets")} whileTap={{ scale: 0.98 }}>
          <span className="context-action-icon"><Target size={18} strokeWidth={1.7} /></span>
          <div><strong>Ставки</strong><span>2 відкриті</span></div>
          <ChevronRight size={17} />
        </motion.button>
        <motion.button className="context-action glass-surface" onClick={() => onNavigate("subscriptions")} whileTap={{ scale: 0.98 }}>
          <span className="context-action-icon"><Bell size={18} strokeWidth={1.7} /></span>
          <div><strong>Підписки</strong><span>5 правил</span></div>
          <ChevronRight size={17} />
        </motion.button>
      </div>

      <section className="section-block">
        <div className="section-heading"><div><span className="section-kicker">FEED</span><h2>Останні</h2></div><span className="feed-state"><Activity size={13} /> SSE</span></div>
        <div className="event-feed glass-list">
          {feed.slice(1).map((event, index) => (
            <motion.button key={event.id} className="event-row" onClick={() => setExpandedEventId(expandedEventId === event.id ? null : event.id)} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: index * 0.035 }}>
              <span className={`event-symbol event-symbol--${event.kind.toLowerCase()}`}>{eventIcon(event.kind)}</span>
              <div className="event-row-copy"><div><strong>{event.title}</strong><span>{event.relative}</span></div><p>{event.detail}</p></div>
              <div className="event-row-tail">{event.rarity && <span className="micro-chip">{event.rarity}</span>}{event.meta && <small>{event.meta}</small>}<ChevronRight size={15} /></div>
              <AnimatePresence initial={false}>
                {expandedEventId === event.id && (
                  <motion.div className="event-row-expanded" initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }} exit={{ height: 0, opacity: 0 }}>
                    <span>{event.kind}</span><span>{event.occurredAt}</span>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.button>
          ))}
        </div>
      </section>
    </div>
  )
}

function AccountPage({
  account,
  members,
  onOpenSwitcher,
  onMember,
  onNavigate,
}: {
  account: Account
  members: Member[]
  onOpenSwitcher: () => void
  onMember: (member: Member) => void
  onNavigate: (screen: DetailScreen) => void
}) {
  const [claimed, setClaimed] = useState(false)
  const [invitesOpen, setInvitesOpen] = useState(false)
  const [inviteCreated, setInviteCreated] = useState(false)

  return (
    <div className="page root-page account-page">
      <section className="root-heading account-heading">
        <div><span className="section-kicker">ACTIVE ACCOUNT</span><h1>{account.nickname}</h1></div>
        <motion.button className="account-context-button glass-control" onClick={onOpenSwitcher} whileTap={{ scale: 0.96 }}>
          <CristalixHead skinUrl={account.skinUrl} size={34} />
          <ChevronDown size={16} />
        </motion.button>
      </section>

      <section className="account-overview glass-surface">
        <div className="account-overview-head">
          <CristalixHead skinUrl={account.skinUrl} size={78} />
          <div><span className="account-role"><Shield size={13} /> {account.role}</span><strong>{account.nickname}</strong><small>{account.shortId}</small></div>
          <motion.button className="ghost-icon" onClick={() => onNavigate("public-profile")} whileTap={{ scale: 0.92 }} aria-label="Публічний профіль"><Eye size={18} /></motion.button>
        </div>
        <button className="balance-strip" onClick={() => onNavigate("account-wallet")}>
          <div><CurrencyMark currency="coin" /><strong>{account.coin.toLocaleString("uk-UA")}</strong></div>
          <span className="vertical-rule" />
          <div><CurrencyMark currency="prime" /><strong>{account.prime.toLocaleString("uk-UA")}</strong></div>
          <ChevronRight size={16} />
        </button>
      </section>

      <section className="daily-reward glass-surface">
        <div className="daily-reward-main">
          <span className="context-action-icon"><Gift size={20} strokeWidth={1.7} /></span>
          <div><span className="section-kicker">DAILY REWARD</span><h2>{claimed ? "Нагороду отримано" : "Серія 17 днів"}</h2><p>{claimed ? "Наступна — завтра" : "Наступна: 380–520 coin · Prime 4%"}</p></div>
        </div>
        <div className="daily-reward-footer">
          <span><RotateCcw size={13} /> 2 відновлення</span>
          <motion.button disabled={claimed} onClick={() => setClaimed(true)} whileTap={{ scale: claimed ? 1 : 0.97 }}>{claimed ? <><Check size={15} /> Готово</> : "Забрати"}</motion.button>
        </div>
      </section>

      <section className="section-block members-section">
        <div className="section-heading">
          <div><span className="section-kicker">ACCOUNT</span><h2>Учасники</h2></div>
          <button className="text-action" onClick={() => setInvitesOpen((value) => !value)}><Link2 size={15} /> Запрошення</button>
        </div>

        <AnimatePresence initial={false}>
          {invitesOpen && (
            <motion.div className="invite-panel glass-surface" initial={{ opacity: 0, y: -8, height: 0 }} animate={{ opacity: 1, y: 0, height: "auto" }} exit={{ opacity: 0, y: -8, height: 0 }}>
              <div><span className="invite-code">MN-8HF3-2A</span><small>діє ще 18 год</small><button aria-label="Відкликати"><Trash2 size={15} /></button></div>
              {inviteCreated && <div><span className="invite-code">MN-K7Q9-Z1</span><small>без строку</small><button aria-label="Відкликати"><Trash2 size={15} /></button></div>}
              <button className="create-invite" onClick={() => setInviteCreated(true)} disabled={inviteCreated}><Plus size={15} /> {inviteCreated ? "Створено" : "Створити invite"}</button>
            </motion.div>
          )}
        </AnimatePresence>

        <div className="member-list glass-list">
          {members.map((member) => {
            const highestRole = member.roles[0] ?? "Member"
            const role = roleCatalog.find((item) => item.name === highestRole)
            return (
              <motion.button key={member.id} className="member-row" onClick={() => onMember(member)} whileTap={{ scale: 0.992 }}>
                <span className="member-avatar">{member.name.slice(0, 1).toUpperCase()}</span>
                <div><div><strong>{member.name}</strong>{member.owner && <Crown size={13} />}</div><span>приєднався {member.joinedAt}</span></div>
                <span className="role-label" style={{ color: role?.color }}>{highestRole}</span>
                <ChevronRight size={16} />
              </motion.button>
            )
          })}
        </div>
      </section>

      <section className="section-block">
        <div className="section-heading"><div><span className="section-kicker">WALLET</span><h2>Останні операції</h2></div><button className="icon-text-action" onClick={() => onNavigate("account-wallet")}><History size={15} /> Усі</button></div>
        <div className="transaction-preview glass-list">
          {accountTransactions.map((transaction) => (
            <div key={transaction.id} className="transaction-row"><span className="transaction-icon"><CurrencyMark currency={transaction.currency} /></span><div><strong>{transaction.source}</strong><span>{transaction.time}</span></div><b className={transaction.amount >= 0 ? "amount-positive" : ""}>{transaction.amount > 0 ? "+" : ""}{transaction.amount.toLocaleString("uk-UA")}</b></div>
          ))}
        </div>
      </section>
    </div>
  )
}

function MemberDetailPage({ member, onBack }: { member: Member; onBack: () => void }) {
  const [assignedRoles, setAssignedRoles] = useState(member.roles)

  const toggleRole = (roleName: string) => {
    if (member.owner || roleName === "Owner") return
    setAssignedRoles((current) => current.includes(roleName) ? current.filter((role) => role !== roleName) : [...current, roleName])
  }

  return (
    <div className="page detail-page">
      <GlassHeader title={member.name} eyebrow="MEMBER" onBack={onBack} />
      <section className="member-detail-hero glass-surface">
        <span className="large-member-avatar">{member.name.slice(0, 1).toUpperCase()}</span>
        <div><div className="member-name-line"><h1>{member.name}</h1>{member.owner && <Crown size={17} />}</div><p>Учасник з {member.joinedAt}</p></div>
      </section>

      <section className="section-block">
        <div className="section-heading"><div><span className="section-kicker">ROLES</span><h2>Ролі</h2></div><span className="subtle-count">{assignedRoles.length}</span></div>
        <div className="role-editor glass-list">
          {roleCatalog.map((role) => {
            const active = assignedRoles.includes(role.name)
            const disabled = member.owner || role.locked
            return (
              <button key={role.id} className={active ? "role-row role-row--active" : "role-row"} onClick={() => toggleRole(role.name)} disabled={disabled}>
                <span className="role-dot" style={{ background: role.color }} />
                <div><strong>{role.name}</strong><span>Hierarchy {role.rank}</span></div>
                <span className={active ? "role-check role-check--active" : "role-check"}>{active && <Check size={14} />}</span>
              </button>
            )
          })}
        </div>
        <p className="section-note">Зміни ролей у реальному UI доступні тільки коли поточний користувач має відповідні permissions і проходить hierarchy policy.</p>
      </section>

      {!member.owner && <button className="danger-action"><Trash2 size={16} /> Виключити з аккаунта</button>}
    </div>
  )
}

function ProfilePage({ account, onNavigate }: { account: Account; onNavigate: (screen: DetailScreen) => void }) {
  const [username, setUsername] = useState("knalis")
  const [editing, setEditing] = useState(false)
  const [language, setLanguage] = useState("UK")

  return (
    <div className="page root-page me-page">
      <section className="root-heading">
        <div><span className="section-kicker">USER</span><h1>Профіль</h1></div>
        <span className="user-orb">K</span>
      </section>

      <section className="user-hero glass-surface">
        <div className="user-hero-top">
          <div><span className="user-orb user-orb--large">K</span><div><span className="section-kicker">MNEMOS USER</span>{editing ? <input className="username-input" value={username} onChange={(event) => setUsername(event.target.value)} onBlur={() => setEditing(false)} autoFocus /> : <h2>{username}</h2>}<small>Локалізація: {language}</small></div></div>
          <button className="ghost-icon" onClick={() => setEditing(true)} aria-label="Змінити username"><Settings2Icon /></button>
        </div>
        <div className="user-actions-inline"><button onClick={() => setLanguage((value) => value === "UK" ? "EN" : "UK")}>Мова <strong>{language}</strong></button><span /><button onClick={() => onNavigate("user-wallet")}>User wallet <ChevronRight size={15} /></button></div>
      </section>

      <section className="section-block">
        <div className="section-heading"><div><span className="section-kicker">ACTIVE ACCOUNT</span><h2>Пов'язаний профіль</h2></div></div>
        <motion.button className="linked-account glass-surface" onClick={() => onNavigate("public-profile")} whileTap={{ scale: 0.99 }}>
          <CristalixHead skinUrl={account.skinUrl} size={62} />
          <div><strong>{account.nickname}</strong><span>{account.shortId} · {account.role}</span><small>Відкрити публічний профіль</small></div>
          <ChevronRight size={18} />
        </motion.button>
      </section>

      <section className="section-block profile-functions">
        <div className="section-heading"><div><span className="section-kicker">PERSONAL</span><h2>Персоналізація</h2></div></div>
        <motion.button className="personalization-summary glass-surface" onClick={() => onNavigate("personalization")} whileTap={{ scale: 0.99 }}>
          <span className="personalization-glyph"><Sparkles size={20} /></span>
          <div><strong>Graphite · Default pack</strong><span>Theme, frame, effects, badges та notification pack</span></div>
          <ChevronRight size={17} />
        </motion.button>
      </section>

      <section className="section-block">
        <div className="section-heading"><div><span className="section-kicker">USER WALLET</span><h2>Останні операції</h2></div><button className="icon-text-action" onClick={() => onNavigate("user-wallet")}><History size={15} /> Усі</button></div>
        <div className="transaction-preview glass-list">
          {userTransactions.map((transaction) => (
            <div key={transaction.id} className="transaction-row"><span className="transaction-icon"><CurrencyMark currency={transaction.currency} /></span><div><strong>{transaction.source}</strong><span>{transaction.time}</span></div><b className={transaction.amount >= 0 ? "amount-positive" : ""}>{transaction.amount > 0 ? "+" : ""}{transaction.amount}</b></div>
          ))}
        </div>
      </section>
    </div>
  )
}

function Settings2Icon() {
  return <UserRound size={18} strokeWidth={1.7} />
}

function PublicProfilePage({ account, onBack }: { account: Account; onBack: () => void }) {
  const [comments, setComments] = useState<ProfileComment[]>(initialComments)
  const [commentText, setCommentText] = useState("")
  const [reaction, setReaction] = useState("fire")

  const submitComment = (event: FormEvent) => {
    event.preventDefault()
    const text = commentText.trim()
    if (!text) return

    const id = `pending-${Date.now()}`
    const pending: ProfileComment = {
      id,
      author: "knalis",
      text,
      time: "щойно",
      likes: 0,
      dislikes: 0,
      myLike: null,
      state: "pending",
    }

    setComments((current) => [pending, ...current])
    setCommentText("")

    window.setTimeout(() => {
      const failed = text.toLowerCase().includes("error")
      setComments((current) => current.map((comment) => comment.id === id ? { ...comment, state: failed ? "failed" : "sent" } : comment))
    }, 900)
  }

  const retryComment = (id: string) => {
    setComments((current) => current.map((comment) => comment.id === id ? { ...comment, state: "pending" } : comment))
    window.setTimeout(() => setComments((current) => current.map((comment) => comment.id === id ? { ...comment, state: "sent" } : comment)), 700)
  }

  const setLike = (id: string, value: boolean) => {
    setComments((current) => current.map((comment) => {
      if (comment.id !== id || comment.state !== "sent") return comment
      if (comment.myLike === value) {
        return {
          ...comment,
          myLike: null,
          likes: value ? Math.max(0, comment.likes - 1) : comment.likes,
          dislikes: !value ? Math.max(0, comment.dislikes - 1) : comment.dislikes,
        }
      }
      return {
        ...comment,
        myLike: value,
        likes: comment.likes + (value ? 1 : 0) - (comment.myLike === true ? 1 : 0),
        dislikes: comment.dislikes + (!value ? 1 : 0) - (comment.myLike === false ? 1 : 0),
      }
    }))
  }

  const reactions = [
    { id: "fire", symbol: "🔥", count: 128 },
    { id: "heart", symbol: "♥", count: 43 },
    { id: "eyes", symbol: "◉", count: 19 },
  ]

  return (
    <div className="page detail-page public-profile-page">
      <GlassHeader title="Публічний профіль" eyebrow="ACCOUNT" onBack={onBack} />

      <section className="public-profile-hero glass-surface">
        <div className="public-profile-ambient" />
        <CristalixHead skinUrl={account.skinUrl} size={104} />
        <div className="public-profile-identity"><span className="section-kicker">CRISTALIX</span><h1>{account.nickname}</h1><p>раніше knalisss · {account.shortId}</p><div className="profile-meta"><span><Users size={13} /> 4 учасники</span><span>з липня 2026</span></div></div>
      </section>

      <section className="reaction-deck glass-surface" aria-label="Реакції">
        {reactions.map((item) => (
          <motion.button key={item.id} className={reaction === item.id ? "reaction-item reaction-item--active" : "reaction-item"} onClick={() => setReaction((current) => current === item.id ? "" : item.id)} whileTap={{ scale: 0.94 }}>
            <span>{item.symbol}</span><strong>{item.count + (reaction === item.id ? 1 : 0)}</strong>
          </motion.button>
        ))}
      </section>

      <section className="section-block drops-section">
        <div className="section-heading"><div><span className="section-kicker">ITEM DROPS</span><h2>Останні дропи</h2></div></div>
        <div className="recent-drops glass-surface">
          {recentDrops.map((drop, index) => (
            <motion.div key={drop.id} className="drop-row" initial={{ opacity: 0, x: -6 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: index * 0.045 }}>
              <span className="drop-rarity-mark" />
              <div><strong>{drop.name}</strong><span>{drop.rarity} · {drop.type}</span></div>
              <small>{drop.time}</small>
            </motion.div>
          ))}
        </div>
      </section>

      <section className="section-block comments-section">
        <div className="section-heading"><div><span className="section-kicker">SOCIAL</span><h2>Коментарі</h2></div><span className="subtle-count">{comments.length}</span></div>

        <form className="comment-composer glass-surface" onSubmit={submitComment}>
          <span className="comment-avatar">K</span>
          <input value={commentText} onChange={(event) => setCommentText(event.target.value)} maxLength={500} placeholder="Написати коментар…" />
          <motion.button type="submit" disabled={!commentText.trim()} whileTap={{ scale: 0.92 }} aria-label="Відправити"><Send size={17} strokeWidth={1.8} /></motion.button>
        </form>

        <div className="comments-list">
          <AnimatePresence initial={false}>
            {comments.map((comment) => (
              <motion.article key={comment.id} layout className={`comment ${comment.state === "pending" ? "comment--pending" : ""} ${comment.state === "failed" ? "comment--failed" : ""}`} initial={{ opacity: 0, y: -7 }} animate={{ opacity: comment.state === "pending" ? 0.48 : 1, y: 0 }} exit={{ opacity: 0, height: 0 }} transition={{ duration: 0.22 }}>
                <span className="comment-avatar">{comment.author.slice(0, 1).toUpperCase()}</span>
                <div className="comment-body">
                  <div className="comment-author"><strong>{comment.author}</strong><span>{comment.time}</span>{comment.state === "pending" && <span className="sending-label">відправляється</span>}</div>
                  <p>{comment.text}</p>
                  {comment.state === "failed" ? (
                    <button className="comment-retry" onClick={() => retryComment(comment.id)}><RotateCcw size={13} /> Не відправлено · повторити</button>
                  ) : (
                    <div className="comment-actions">
                      <button className={comment.myLike === true ? "active" : ""} onClick={() => setLike(comment.id, true)} disabled={comment.state !== "sent"}><ThumbsUp size={14} /><span>{comment.likes}</span></button>
                      <button className={comment.myLike === false ? "active" : ""} onClick={() => setLike(comment.id, false)} disabled={comment.state !== "sent"}><ThumbsDown size={14} /><span>{comment.dislikes}</span></button>
                    </div>
                  )}
                </div>
              </motion.article>
            ))}
          </AnimatePresence>
        </div>
      </section>
    </div>
  )
}

function SubscriptionsPage({ onBack }: { onBack: () => void }) {
  const [rules, setRules] = useState(initialSubscriptions)
  const [creatorOpen, setCreatorOpen] = useState(false)
  const [kind, setKind] = useState<SubscriptionKind>("ITEM_DROP")
  const [filterValue, setFilterValue] = useState("knalis")

  const kindLabels: Record<SubscriptionKind, string> = {
    ITEM_DROP: "Item drop",
    BOOSTER: "Booster",
    DUNGEON: "Dungeon",
    RAID: "Raid",
    GLOBAL: "Global",
  }

  const placeholder: Record<SubscriptionKind, string> = {
    ITEM_DROP: "droppedFor або itemName",
    BOOSTER: "boosterType",
    DUNGEON: "dungeonType",
    RAID: "locations: 2, 4, 7",
    GLOBAL: "eventType",
  }

  const createRule = () => {
    const value = filterValue.trim()
    if (!value) return
    setRules((current) => [...current, { id: `sub-${Date.now()}`, kind, title: kindLabels[kind], detail: value }])
    setFilterValue("")
    setCreatorOpen(false)
  }

  return (
    <div className="page detail-page subscriptions-page">
      <GlassHeader title="Підписки" eyebrow="EVENT NOTIFICATIONS" onBack={onBack} action={<motion.button className="round-button round-button--small" onClick={() => setCreatorOpen(true)} whileTap={{ scale: 0.92 }}><Plus size={18} /></motion.button>} />
      <p className="page-intro">Кожна підписка — окреме правило конкретного типу події. Тут немає загального пошуку або розкладу.</p>

      <div className="subscription-rules glass-list">
        {rules.map((rule) => (
          <motion.div key={rule.id} layout className="subscription-rule">
            <span className="subscription-symbol">{subscriptionIcon(rule.kind)}</span>
            <div><strong>{rule.title}</strong><span>{rule.detail}</span></div>
            <button onClick={() => setRules((current) => current.filter((item) => item.id !== rule.id))} aria-label="Видалити"><Trash2 size={16} /></button>
          </motion.div>
        ))}
      </div>

      <AnimatePresence>
        {creatorOpen && (
          <motion.section className="rule-creator glass-surface" initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: 8 }}>
            <div className="rule-creator-heading"><div><span className="section-kicker">NEW RULE</span><h2>Нова підписка</h2></div><button onClick={() => setCreatorOpen(false)}><X size={18} /></button></div>
            <div className="type-selector">
              {(Object.keys(kindLabels) as SubscriptionKind[]).map((item) => <button key={item} className={kind === item ? "active" : ""} onClick={() => { setKind(item); setFilterValue("") }}>{kindLabels[item]}</button>)}
            </div>
            <label className="field-label"><span>{placeholder[kind]}</span><input value={filterValue} onChange={(event) => setFilterValue(event.target.value)} placeholder={placeholder[kind]} /></label>
            {kind === "ITEM_DROP" && <p className="field-hint">Backend дозволяє droppedFor, itemName або обидва фільтри.</p>}
            <motion.button className="primary-action" onClick={createRule} disabled={!filterValue.trim()} whileTap={{ scale: 0.98 }}><Bell size={16} /> Створити правило</motion.button>
          </motion.section>
        )}
      </AnimatePresence>
    </div>
  )
}

function BetsPage({ onBack }: { onBack: () => void }) {
  const [selectedBet, setSelectedBet] = useState<string | null>("bet-moon")
  const [side, setSide] = useState<"YES" | "NO">("YES")
  const [amount, setAmount] = useState("250")
  const [placed, setPlaced] = useState(false)

  const bets = [
    { id: "bet-moon", type: "EVENT", title: "Moon", status: "OPEN", closesAt: "21:55", totalPool: 18420, yes: 58, no: 42 },
    { id: "bet-race", type: "ITEM_RACE", title: "Secret drop race", status: "OPEN", closesAt: "22:30", totalPool: 26750, yes: 0, no: 0 },
  ]

  return (
    <div className="page detail-page bets-page">
      <GlassHeader title="Ставки" eyebrow="EVENTS" onBack={onBack} />
      <div className="bets-list">
        {bets.map((bet) => (
          <motion.article key={bet.id} className={selectedBet === bet.id ? "bet-card glass-surface bet-card--open" : "bet-card glass-surface"} layout>
            <button className="bet-card-header" onClick={() => { setSelectedBet((current) => current === bet.id ? null : bet.id); setPlaced(false) }}>
              <span className="context-action-icon"><Target size={18} /></span>
              <div><span className="section-kicker">{bet.type}</span><strong>{bet.title}</strong><small>Закриття позицій · {bet.closesAt}</small></div>
              <div className="bet-pool"><CurrencyMark currency="coin" /><strong>{bet.totalPool.toLocaleString("uk-UA")}</strong></div>
            </button>
            <AnimatePresence initial={false}>
              {selectedBet === bet.id && (
                <motion.div className="bet-editor" initial={{ height: 0, opacity: 0 }} animate={{ height: "auto", opacity: 1 }} exit={{ height: 0, opacity: 0 }}>
                  {bet.type === "EVENT" && <div className="odds-bar"><button className={side === "YES" ? "active" : ""} onClick={() => setSide("YES")}>YES <strong>{bet.yes}%</strong></button><button className={side === "NO" ? "active" : ""} onClick={() => setSide("NO")}>NO <strong>{bet.no}%</strong></button></div>}
                  <label className="stake-input"><CurrencyMark currency="coin" /><input value={amount} onChange={(event) => setAmount(event.target.value.replace(/\D/g, ""))} inputMode="numeric" /><span>COIN</span></label>
                  <motion.button className={placed ? "primary-action primary-action--done" : "primary-action"} onClick={() => setPlaced(true)} disabled={!amount} whileTap={{ scale: 0.98 }}>{placed ? <><Check size={16} /> Позицію прийнято</> : "Поставити"}</motion.button>
                </motion.div>
              )}
            </AnimatePresence>
          </motion.article>
        ))}
      </div>
    </div>
  )
}

function WalletPage({ account, scope, onBack }: { account: Account; scope: "account" | "user"; onBack: () => void }) {
  const transactions = scope === "account" ? accountTransactions : userTransactions
  const coin = scope === "account" ? account.coin : 0
  const prime = scope === "account" ? account.prime : 510

  return (
    <div className="page detail-page wallet-page">
      <GlassHeader title="Гаманець" eyebrow={scope === "account" ? "ACCOUNT" : "USER"} onBack={onBack} />
      <section className="wallet-hero glass-surface">
        <div><span>Coin</span><strong><CurrencyMark currency="coin" />{coin.toLocaleString("uk-UA")}</strong></div>
        <div><span>Prime</span><strong><CurrencyMark currency="prime" />{prime.toLocaleString("uk-UA")}</strong></div>
      </section>
      <section className="section-block"><div className="section-heading"><div><span className="section-kicker">TRANSACTIONS</span><h2>Історія</h2></div></div><div className="transaction-preview glass-list">{transactions.map((transaction) => <div key={transaction.id} className="transaction-row"><span className="transaction-icon"><CurrencyMark currency={transaction.currency} /></span><div><strong>{transaction.source}</strong><span>{transaction.time}</span></div><b className={transaction.amount >= 0 ? "amount-positive" : ""}>{transaction.amount > 0 ? "+" : ""}{transaction.amount}</b></div>)}</div></section>
    </div>
  )
}

function PersonalizationPage({ onBack }: { onBack: () => void }) {
  const [scope, setScope] = useState<"USER" | "ACCOUNT">("USER")
  const items = [
    { type: "PROFILE_THEME", name: "Graphite", active: true, price: 0 },
    { type: "AVATAR_FRAME", name: "Thin Sage", active: true, price: 25 },
    { type: "NICK_STYLE", name: "Quiet", active: false, price: 20 },
    { type: "PROFILE_EFFECT", name: "Drift", active: false, price: 35 },
    { type: "NOTIFICATION_PACK", name: "Default", active: true, price: 0 },
  ]

  return (
    <div className="page detail-page personalization-page">
      <GlassHeader title="Персоналізація" eyebrow="OWNERSHIP & CATALOG" onBack={onBack} />
      <div className="scope-switch glass-control"><button className={scope === "USER" ? "active" : ""} onClick={() => setScope("USER")}>User</button><button className={scope === "ACCOUNT" ? "active" : ""} onClick={() => setScope("ACCOUNT")}>Account</button></div>
      <section className="personalization-showcase glass-surface"><div className="personalization-preview"><span /><span /><i /></div><div><span className="section-kicker">ACTIVE LOOK</span><h2>Graphite</h2><p>{scope === "USER" ? "Глобальна персоналізація юзера" : "Вигляд активного аккаунта"}</p></div></section>
      <section className="section-block"><div className="section-heading"><div><span className="section-kicker">CATALOG</span><h2>Елементи</h2></div></div><div className="personalization-list glass-list">{items.map((item) => <button key={item.type} className="personalization-row"><span className="personalization-dot"><Sparkles size={16} /></span><div><strong>{item.name}</strong><span>{item.type.replaceAll("_", " ")}</span></div>{item.active ? <span className="active-label"><Check size={13} /> active</span> : <span className="price-label"><CurrencyMark currency="prime" />{item.price}</span>}</button>)}</div></section>
      {scope === "USER" && <section className="creator-pack glass-surface"><div><span className="section-kicker">CREATOR</span><h2>Custom notification pack</h2><p>Окремий backend flow для створення, редагування та публікації.</p></div><ChevronRight size={18} /></section>}
    </div>
  )
}

export default function App() {
  const [stack, setStack] = useState<Screen[]>(["events"])
  const [activeAccountId, setActiveAccountId] = useState(accounts[0].id)
  const [accountSwitcherOpen, setAccountSwitcherOpen] = useState(false)
  const [members] = useState(initialMembers)
  const [selectedMember, setSelectedMember] = useState<Member>(initialMembers[1])

  const screen = stack[stack.length - 1]
  const rootScreen = stack.find(isRootScreen) ?? "events"
  const activeAccount = useMemo(() => accounts.find((account) => account.id === activeAccountId) ?? accounts[0], [activeAccountId])

  const setRoot = (next: RootScreen) => setStack([next])
  const navigate = (next: DetailScreen) => setStack((current) => [...current, next])
  const back = () => setStack((current) => current.length > 1 ? current.slice(0, -1) : current)
  const openMember = (member: Member) => { setSelectedMember(member); navigate("member") }

  return (
    <MotionConfig reducedMotion="user">
      <div className="preview-stage">
        <main className="app-shell">
          <div className="ambient ambient--one" />
          <div className="ambient ambient--two" />
          <div className="noise-layer" />

          <div className="screen-scroll">
            <AnimatePresence mode="wait" initial={false}>
              <motion.div key={screen} className="screen-motion" initial={{ opacity: 0, y: 8, scale: 0.995 }} animate={{ opacity: 1, y: 0, scale: 1 }} exit={{ opacity: 0, y: -5, scale: 0.998 }} transition={{ duration: 0.22, ease: [0.22, 1, 0.36, 1] }}>
                {screen === "events" && <EventsPage onNavigate={navigate} />}
                {screen === "account" && <AccountPage account={activeAccount} members={members} onOpenSwitcher={() => setAccountSwitcherOpen(true)} onMember={openMember} onNavigate={navigate} />}
                {screen === "me" && <ProfilePage account={activeAccount} onNavigate={navigate} />}
                {screen === "member" && <MemberDetailPage member={selectedMember} onBack={back} />}
                {screen === "public-profile" && <PublicProfilePage account={activeAccount} onBack={back} />}
                {screen === "subscriptions" && <SubscriptionsPage onBack={back} />}
                {screen === "bets" && <BetsPage onBack={back} />}
                {screen === "account-wallet" && <WalletPage account={activeAccount} scope="account" onBack={back} />}
                {screen === "user-wallet" && <WalletPage account={activeAccount} scope="user" onBack={back} />}
                {screen === "personalization" && <PersonalizationPage onBack={back} />}
              </motion.div>
            </AnimatePresence>
          </div>

          {isRootScreen(screen) && <BottomNavigation screen={screen} onChange={setRoot} />}

          <AccountSwitcher
            open={accountSwitcherOpen}
            activeAccount={activeAccount}
            onClose={() => setAccountSwitcherOpen(false)}
            onSelect={(account) => { setActiveAccountId(account.id); setAccountSwitcherOpen(false) }}
          />
        </main>
      </div>
    </MotionConfig>
  )
}
