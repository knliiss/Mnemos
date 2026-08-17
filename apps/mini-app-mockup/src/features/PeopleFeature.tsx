import { FormEvent, useMemo, useState } from "react"
import { LOCALIZATION_LANGUAGES, SPOTLIGHT_PRESENTATION_MODES } from "../contracts"
import { currentUser, feed, initialComments, profiles, userTransactions } from "../mockData"
import type { Account, DropSpotlightPresentationMode, LocalizationLanguage, ProfileComment, Route } from "../model"
import { transactionSourceLabels } from "../presentation"
import { CristalixHead } from "../components/CristalixHead"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, Field, IconButton, PageHeader, Row, Section, Segmented } from "../shared/Primitives"

type Nav = (route: Route) => void
type Props = { routeId: Route["id"]; params?: Record<string, string>; activeAccount: Account; navigate: Nav; back: () => void }
type ReplyMap = Record<string, ProfileComment[]>

export function PeopleFeature(props: Props) {
  if (props.routeId === "search") return <SearchPage {...props} />
  if (props.routeId === "public-profile") return <PublicProfilePage {...props} />
  if (props.routeId === "user-wallet") return <UserWalletPage {...props} />
  if (props.routeId === "settings") return <SettingsPage {...props} />
  return <MeHome {...props} />
}

function MeHome({ activeAccount, navigate }: Props) {
  const creator = currentUser.systemRoles.some((role) => ["PERSONALIZATION_CREATOR", "ADMIN", "SUPER_ADMIN"].includes(role))
  const staff = currentUser.systemRoles.some((role) => ["MODERATOR", "ADMIN", "SUPER_ADMIN", "PERSONALIZATION_MODERATOR"].includes(role))
  return <div className="screen me-screen"><PageHeader title={currentUser.username} actions={<IconButton icon="settings" label="Налаштування" onClick={() => navigate({ id: "settings" })} />} /><section className="user-identity"><span className="user-monogram">K</span><div><strong>@{currentUser.username}</strong><small>Telegram</small></div><button className="identity-account" onClick={() => navigate({ id: "public-profile", params: { accountId: activeAccount.id } })}><Glyph name="account" size={13} /><b>{activeAccount.nickname}</b><span>›</span></button></section><button className="wallet-strip wallet-strip--user" onClick={() => navigate({ id: "user-wallet" })}><Glyph name="person" size={15} /><CurrencyValue currency="COIN" value={2160} /><span className="wallet-strip__divider" /><CurrencyValue currency="PRIME" value={132} /><span className="wallet-strip__arrow">›</span></button><div className="feature-rail"><button onClick={() => navigate({ id: "personalization-store" })}><Glyph name="store" size={22} /><span>Магазин</span></button><button onClick={() => navigate({ id: "personalization-owned" })}><Glyph name="spark" size={22} /><span>Мої</span></button><button onClick={() => navigate({ id: "search" })}><Glyph name="search" size={22} /><span>Профілі</span></button></div>{creator || staff ? <div className="workspace-list">{creator ? <Row icon="creator" title="Creator" onClick={() => navigate({ id: "creator-studio" })} /> : null}{staff ? <Row icon="moderation" title="Staff" onClick={() => navigate({ id: "moderation" })} /> : null}</div> : null}</div>
}

function SearchPage({ back, navigate }: Props) {
  const [query, setQuery] = useState("")
  const results = useMemo(() => { const normalized = query.trim().toLowerCase(); if (normalized.length < 2) return []; return profiles.filter((profile) => profile.nickname.toLowerCase().includes(normalized)) }, [query])
  return <div className="screen search-screen"><PageHeader title="Пошук" back={back} /><div className="search-box"><Glyph name="search" size={18} /><input autoFocus value={query} onChange={(event) => setQuery(event.target.value)} placeholder="nickname" /><span>{query.length >= 2 ? results.length : ""}</span></div><div className="search-results">{results.map((profile) => <button key={profile.accountId} onClick={() => navigate({ id: "public-profile", params: { accountId: profile.accountId } })}><CristalixHead skinUrl={profile.skinUrl} size={44} /><span><strong>{profile.nickname}</strong><small>↑ {profile.likes} · ◌ {profile.comments}</small></span><span>›</span></button>)}</div>{query.trim().length > 0 && query.trim().length < 2 ? <p className="inline-hint">Ще 1 символ</p> : null}{query.trim().length >= 2 && !results.length ? <p className="inline-hint">Нічого не знайдено</p> : null}</div>
}

function PublicProfilePage({ back, params }: Props) {
  const profile = profiles.find((item) => item.accountId === params?.accountId) ?? profiles[0]
  const [reaction, setReaction] = useState<"LIKE" | "DISLIKE" | null>(null)
  const [comments, setComments] = useState<ProfileComment[]>([...initialComments, { id: "comment-me", authorAccountId: "me", author: "knalis", content: "Перевірив — профіль виглядає нормально.", likes: 2, replies: 0, createdAt: "4 хв", liked: false }])
  const [draft, setDraft] = useState("")
  const [editing, setEditing] = useState<string | null>(null)
  const [editDraft, setEditDraft] = useState("")
  const [replyingTo, setReplyingTo] = useState<string | null>(null)
  const [replyDraft, setReplyDraft] = useState("")
  const [openReplies, setOpenReplies] = useState<string | null>(null)
  const [replies, setReplies] = useState<ReplyMap>({ "comment-1": [{ id: "reply-1", authorAccountId: "profile-vex", author: "vex", content: "Так, drop був хороший.", likes: 1, replies: 0, createdAt: "10 хв", liked: false }, { id: "reply-2", authorAccountId: "me", author: "knalis", content: "Сам здивувався.", likes: 3, replies: 0, createdAt: "7 хв", liked: false }] })
  const drops = feed.filter((event) => event.kind === "ITEM_DROP" && event.detail.toLowerCase() === profile.nickname.toLowerCase()).slice(0, 3)

  const settleOptimistic = (id: string, content: string, setter: React.Dispatch<React.SetStateAction<ProfileComment[]>>) => window.setTimeout(() => setter((current) => current.map((comment) => comment.id === id ? { ...comment, pending: false, failed: content.toLowerCase().includes("!fail") } : comment)), 760)
  const submit = (event: FormEvent) => { event.preventDefault(); const content = draft.trim(); if (!content) return; const id = `pending-${Date.now()}`; const optimistic: ProfileComment = { id, authorAccountId: "me", author: "knalis", content, likes: 0, replies: 0, createdAt: "зараз", liked: false, pending: true }; setComments([optimistic, ...comments]); setDraft(""); settleOptimistic(id, content, setComments) }
  const submitReply = (parentId: string) => { const content = replyDraft.trim(); if (!content) return; const id = `reply-${Date.now()}`; const optimistic: ProfileComment = { id, authorAccountId: "me", author: "knalis", content, likes: 0, replies: 0, createdAt: "зараз", liked: false, pending: true }; setReplies((current) => ({ ...current, [parentId]: [optimistic, ...(current[parentId] ?? [])] })); setComments((current) => current.map((comment) => comment.id === parentId ? { ...comment, replies: comment.replies + 1 } : comment)); setReplyDraft(""); setReplyingTo(null); window.setTimeout(() => setReplies((current) => ({ ...current, [parentId]: (current[parentId] ?? []).map((reply) => reply.id === id ? { ...reply, pending: false, failed: content.toLowerCase().includes("!fail") } : reply) })), 760) }
  const toggleLike = (id: string) => setComments(comments.map((comment) => comment.id !== id ? comment : { ...comment, liked: !comment.liked, likes: comment.likes + (comment.liked ? -1 : 1) }))
  const removeComment = (id: string) => setComments(comments.filter((comment) => comment.id !== id))
  const startEdit = (comment: ProfileComment) => { setEditing(comment.id); setEditDraft(comment.content) }
  const saveEdit = (id: string) => { const content = editDraft.trim(); if (!content) return; setComments(comments.map((comment) => comment.id === id ? { ...comment, content } : comment)); setEditing(null) }
  const retry = (id: string) => { setComments(comments.map((comment) => comment.id === id ? { ...comment, failed: false, pending: true } : comment)); window.setTimeout(() => setComments((current) => current.map((comment) => comment.id === id ? { ...comment, pending: false } : comment)), 700) }
  const likeCount = profile.likes + (reaction === "LIKE" ? 1 : 0); const dislikeCount = profile.dislikes + (reaction === "DISLIKE" ? 1 : 0)

  return <div className="screen profile-screen"><PageHeader title={profile.nickname} back={back} /><section className="public-identity"><CristalixHead skinUrl={profile.skinUrl} size={88} /><div className="public-identity__copy"><small>{profile.cristalixUuid.slice(0, 8)}</small></div><div className="profile-reactions"><button className={reaction === "LIKE" ? "is-active" : ""} onClick={() => setReaction(reaction === "LIKE" ? null : "LIKE")}><span>↑</span><b>{likeCount}</b></button><button className={reaction === "DISLIKE" ? "is-active" : ""} onClick={() => setReaction(reaction === "DISLIKE" ? null : "DISLIKE")}><span>↓</span><b>{dislikeCount}</b></button></div></section><Section title="Дропи">{drops.length ? <div className="drop-history">{drops.map((drop) => <div key={drop.id}><Glyph name="item-drop" size={18} /><span><strong>{drop.title}</strong><small>{drop.rarity ?? drop.meta}</small></span><time>{drop.relative}</time></div>)}</div> : <p className="inline-hint inline-hint--left">Порожньо</p>}</Section><section className="comments-section"><div className="section__head"><h2>Коментарі · {comments.length}</h2></div><form className="comment-composer" onSubmit={submit}><input value={draft} maxLength={500} onChange={(event) => setDraft(event.target.value)} placeholder="Коментар" /><button disabled={!draft.trim()}><Glyph name="send" size={18} /></button></form><div className="comment-list">{comments.map((comment) => <article key={comment.id} className={`comment ${comment.pending ? "is-pending" : ""} ${comment.failed ? "is-failed" : ""}`}><span className="comment__avatar">{comment.author.slice(0, 1).toUpperCase()}</span><div className="comment__body"><header><strong>{comment.author}</strong><time>{comment.createdAt}</time>{comment.author === "knalis" && !comment.pending ? <span className="comment__owner-actions"><button aria-label="Редагувати" onClick={() => startEdit(comment)}><Glyph name="edit" size={13} /></button><button aria-label="Видалити" onClick={() => removeComment(comment.id)}><Glyph name="trash" size={13} /></button></span> : null}</header>{editing === comment.id ? <div className="comment-edit"><input value={editDraft} maxLength={500} onChange={(event) => setEditDraft(event.target.value)} /><button aria-label="Зберегти" onClick={() => saveEdit(comment.id)}><Glyph name="check" size={14} /></button></div> : <p>{comment.content}</p>}<footer><button className={comment.liked ? "is-active" : ""} onClick={() => toggleLike(comment.id)}>♡ <span>{comment.likes || ""}</span></button><button aria-label="Відповісти" onClick={() => setReplyingTo(replyingTo === comment.id ? null : comment.id)}>↳</button>{comment.replies ? <button onClick={() => setOpenReplies(openReplies === comment.id ? null : comment.id)}>{comment.replies}</button> : null}{comment.failed ? <button className="retry" onClick={() => retry(comment.id)}>Повторити</button> : null}</footer>{replyingTo === comment.id ? <div className="reply-composer"><input autoFocus value={replyDraft} maxLength={500} onChange={(event) => setReplyDraft(event.target.value)} placeholder={`@${comment.author}`} /><button disabled={!replyDraft.trim()} onClick={() => submitReply(comment.id)}><Glyph name="send" size={14} /></button></div> : null}{openReplies === comment.id ? <div className="reply-list">{(replies[comment.id] ?? []).map((reply) => <div className={reply.pending ? "is-pending" : ""} key={reply.id}><strong>{reply.author}</strong><span>{reply.content}</span><small>{reply.createdAt}</small></div>)}</div> : null}</div></article>)}</div></section></div>
}

function UserWalletPage({ back }: Props) {
  return <div className="screen"><PageHeader title="Wallet" eyebrow="USER" back={back} /><div className="balance-grid"><div><CurrencyValue currency="COIN" value={2160} /></div><div><CurrencyValue currency="PRIME" value={132} /></div></div><Section title="Історія"><div className="transaction-list">{userTransactions.map((transaction) => <div className="transaction-row" key={transaction.id}><span className={`transaction-sign ${transaction.type === "CREDIT" ? "is-credit" : ""}`}>{transaction.type === "CREDIT" ? "+" : "−"}</span><span><strong>{transactionSourceLabels[transaction.source]}</strong><small>{transaction.createdAt}</small></span><CurrencyValue currency={transaction.currency} value={transaction.amount} compact /></div>)}</div></Section></div>
}

function SettingsPage({ back }: Props) {
  const [username, setUsername] = useState("knalis")
  const [language, setLanguage] = useState<LocalizationLanguage>("UK")
  const [spotlightMode, setSpotlightMode] = useState<DropSpotlightPresentationMode>("FULL")

  return <div className="screen"><PageHeader title="Налаштування" back={back} /><div className="form-stack settings-form"><Field label="Username"><input value={username} onChange={(event) => setUsername(event.target.value)} maxLength={32} /></Field><Field label="Мова"><Segmented value={language} options={LOCALIZATION_LANGUAGES} onChange={setLanguage} label={(value) => ({ UK: "Українська", EN: "English", RU: "Русский" }[value])} /></Field><Field label="Spotlight"><Segmented value={spotlightMode} options={SPOTLIGHT_PRESENTATION_MODES} onChange={setSpotlightMode} label={(value) => value === "FULL" ? "Повний" : "Компактний"} /></Field><button className="primary-action">Зберегти</button></div></div>
}
