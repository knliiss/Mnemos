import { FormEvent, useMemo, useState } from "react"
import { feed, initialComments, profiles, userTransactions } from "../mockData"
import type { Account, LocalizationLanguage, ProfileComment, PublicProfile, Route } from "../model"
import { CristalixHead } from "../components/CristalixHead"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, Field, IconButton, PageHeader, Row, Section, Segmented } from "../shared/Primitives"

type Nav = (route: Route) => void

type PeopleFeatureProps = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: Nav
  back: () => void
}

export function PeopleFeature(props: PeopleFeatureProps) {
  if (props.routeId === "search") return <SearchPage {...props} />
  if (props.routeId === "public-profile") return <PublicProfilePage {...props} />
  if (props.routeId === "user-wallet") return <UserWalletPage {...props} />
  if (props.routeId === "settings") return <SettingsPage {...props} />
  return <MeHome {...props} />
}

function MeHome({ activeAccount, navigate }: PeopleFeatureProps) {
  return <div className="screen me-screen">
    <PageHeader title="knalis" eyebrow="USER" actions={<IconButton icon="settings" label="Налаштування" onClick={() => navigate({ id: "settings" })} />} />

    <section className="user-identity">
      <span className="user-monogram">K</span>
      <div><strong>@knalis</strong><small>Telegram identity</small></div>
      <button className="identity-account" onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}><span>Account</span><b>{activeAccount.nickname}</b><span>›</span></button>
    </section>

    <button className="wallet-strip wallet-strip--user" onClick={() => navigate({ id: "user-wallet" })}>
      <span className="scope-mark">USER</span><CurrencyValue currency="COIN" value={2160} /><span className="wallet-strip__divider" /><CurrencyValue currency="PRIME" value={132} /><span className="wallet-strip__arrow">›</span>
    </button>

    <div className="feature-rail">
      <button onClick={() => navigate({ id: "personalization-store" })}><Glyph name="store" size={22} /><span>Store</span></button>
      <button onClick={() => navigate({ id: "personalization-owned" })}><Glyph name="spark" size={22} /><span>Мої</span></button>
      <button onClick={() => navigate({ id: "search" })}><Glyph name="search" size={22} /><span>Профілі</span></button>
    </div>

    <Section title="Creator">
      <Row icon="creator" title="Студія" meta="drafts · review · publish" onClick={() => navigate({ id: "creator-studio" })} />
    </Section>

    <Section title="Керування">
      <Row icon="moderation" title="Модерація" meta="sanctions · wallet · review · bets" onClick={() => navigate({ id: "moderation" })} />
    </Section>
  </div>
}

function SearchPage({ back, navigate }: PeopleFeatureProps) {
  const [query, setQuery] = useState("")
  const results = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (normalized.length < 2) return []
    return profiles.filter((profile) => profile.nickname.toLowerCase().includes(normalized))
  }, [query])

  return <div className="screen search-screen">
    <PageHeader title="Профілі" back={back} />
    <div className="search-box"><Glyph name="search" size={18} /><input autoFocus value={query} onChange={(event) => setQuery(event.target.value)} placeholder="nickname" /><span>{query.length ? `${results.length}` : ""}</span></div>
    <div className="search-results">{results.map((profile) => <button key={profile.accountId} onClick={() => navigate({ id: "public-profile", params: { accountId: profile.accountId } })}><CristalixHead skinUrl={profile.skinUrl} size={44} /><span><strong>{profile.nickname}</strong><small>{profile.likes} ↑ · {profile.comments} comments</small></span><span>›</span></button>)}</div>
    {query.trim().length > 0 && query.trim().length < 2 ? <p className="inline-hint">Ще 1 символ</p> : null}
    {query.trim().length >= 2 && !results.length ? <p className="inline-hint">Нічого не знайдено</p> : null}
  </div>
}

function PublicProfilePage({ back, params }: PeopleFeatureProps) {
  const profile = profiles.find((item) => item.accountId === params?.accountId) ?? profiles[0]
  const [reaction, setReaction] = useState<"LIKE" | "DISLIKE" | null>(null)
  const [comments, setComments] = useState<ProfileComment[]>(initialComments)
  const [draft, setDraft] = useState("")
  const drops = feed.filter((event) => event.kind === "ITEM_DROP" && event.detail.toLowerCase() === profile.nickname.toLowerCase()).slice(0, 3)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const content = draft.trim()
    if (!content) return
    const id = `pending-${Date.now()}`
    const optimistic: ProfileComment = { id, authorAccountId: "me", author: "knalis", content, likes: 0, replies: 0, createdAt: "зараз", liked: false, pending: true }
    setComments([optimistic, ...comments])
    setDraft("")
    window.setTimeout(() => {
      const failed = content.toLowerCase().includes("!fail")
      setComments((current) => current.map((comment) => comment.id === id ? { ...comment, pending: false, failed } : comment))
    }, 760)
  }

  const toggleLike = (id: string) => setComments(comments.map((comment) => comment.id !== id ? comment : { ...comment, liked: !comment.liked, likes: comment.likes + (comment.liked ? -1 : 1) }))
  const retry = (id: string) => {
    setComments(comments.map((comment) => comment.id === id ? { ...comment, failed: false, pending: true } : comment))
    window.setTimeout(() => setComments((current) => current.map((comment) => comment.id === id ? { ...comment, pending: false } : comment)), 700)
  }

  const likeCount = profile.likes + (reaction === "LIKE" ? 1 : 0)
  const dislikeCount = profile.dislikes + (reaction === "DISLIKE" ? 1 : 0)

  return <div className="screen profile-screen">
    <PageHeader title="Профіль" back={back} actions={<IconButton icon="more" label="Дії" />} />
    <section className="public-identity">
      <CristalixHead skinUrl={profile.skinUrl} size={88} />
      <div className="public-identity__copy"><strong>{profile.nickname}</strong><small>{profile.cristalixUuid.slice(0, 8)}</small></div>
      <div className="profile-reactions">
        <button className={reaction === "LIKE" ? "is-active" : ""} onClick={() => setReaction(reaction === "LIKE" ? null : "LIKE")}><span>↑</span><b>{likeCount}</b></button>
        <button className={reaction === "DISLIKE" ? "is-active" : ""} onClick={() => setReaction(reaction === "DISLIKE" ? null : "DISLIKE")}><span>↓</span><b>{dislikeCount}</b></button>
      </div>
    </section>

    <Section title="Останні drops">
      {drops.length ? <div className="drop-history">{drops.map((drop) => <div key={drop.id}><Glyph name="item-drop" size={18} /><span><strong>{drop.title}</strong><small>{drop.rarity ?? drop.meta}</small></span><time>{drop.relative}</time></div>)}</div> : <p className="inline-hint inline-hint--left">Немає останніх drops</p>}
    </Section>

    <section className="comments-section">
      <div className="section__head"><h2>Коментарі · {comments.length}</h2></div>
      <form className="comment-composer" onSubmit={submit}><input value={draft} maxLength={500} onChange={(event) => setDraft(event.target.value)} placeholder="Коментар" /><button disabled={!draft.trim()}><Glyph name="send" size={18} /></button></form>
      <div className="comment-list">{comments.map((comment) => <article key={comment.id} className={`comment ${comment.pending ? "is-pending" : ""} ${comment.failed ? "is-failed" : ""}`}>
        <span className="comment__avatar">{comment.author.slice(0, 1).toUpperCase()}</span>
        <div className="comment__body"><header><strong>{comment.author}</strong><time>{comment.createdAt}</time></header><p>{comment.content}</p><footer><button className={comment.liked ? "is-active" : ""} onClick={() => toggleLike(comment.id)}>♡ <span>{comment.likes || ""}</span></button>{comment.replies ? <button>↳ {comment.replies}</button> : null}{comment.failed ? <button className="retry" onClick={() => retry(comment.id)}>Не відправлено · повторити</button> : null}</footer></div>
      </article>)}</div>
    </section>
  </div>
}

function UserWalletPage({ back }: PeopleFeatureProps) {
  return <div className="screen"><PageHeader title="Wallet" eyebrow="USER" back={back} /><div className="balance-grid"><div><CurrencyValue currency="COIN" value={2160} /><small>Coin</small></div><div><CurrencyValue currency="PRIME" value={132} /><small>Prime</small></div></div><Section title="Історія"><div className="transaction-list">{userTransactions.map((transaction) => <div className="transaction-row" key={transaction.id}><span className={`transaction-sign ${transaction.type === "CREDIT" ? "is-credit" : ""}`}>{transaction.type === "CREDIT" ? "+" : "−"}</span><span><strong>{transaction.source.replaceAll("_", " ").toLowerCase()}</strong><small>{transaction.createdAt}</small></span><CurrencyValue currency={transaction.currency} value={transaction.amount} compact /></div>)}</div></Section></div>
}

function SettingsPage({ back }: PeopleFeatureProps) {
  const [username, setUsername] = useState("knalis")
  const [language, setLanguage] = useState<LocalizationLanguage>("UK")
  return <div className="screen"><PageHeader title="Налаштування" back={back} /><div className="form-stack settings-form"><Field label="Username"><input value={username} onChange={(event) => setUsername(event.target.value)} maxLength={32} /></Field><Field label="Мова"><Segmented value={language} options={["UK", "EN", "RU"] as const} onChange={setLanguage} label={(value) => ({ UK: "Українська", EN: "English", RU: "Русский" }[value])} /></Field><button className="primary-action">Зберегти</button></div></div>
}
