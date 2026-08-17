import { FormEvent, useMemo, useState } from "react"
import { currentUser, feed, initialComments, profiles, userTransactions } from "../mockData"
import type { Account, ProfileComment, Route } from "../model"
import { transactionSourceLabels } from "../presentation"
import { CristalixHead } from "../components/CristalixHead"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader, Section } from "../shared/Primitives"

type Nav = (route: Route) => void
type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: Nav
  back: () => void
}
type ReplyMap = Record<string, ProfileComment[]>

export function PeopleFeature(props: Props) {
  if (props.routeId === "public-profile") return <PublicProfilePage {...props} />
  if (props.routeId === "user-wallet") return <UserWalletPage {...props} />

  return <SearchPage {...props} />
}

function SearchPage({ navigate }: Props) {
  const [query, setQuery] = useState("")
  const results = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (normalized.length < 2) return []

    return profiles.filter((profile) => profile.nickname.toLowerCase().includes(normalized))
  }, [query])

  return (
    <div className="screen search-root-screen root-screen">
      <header className="root-title-block">
        <h1>Пошук</h1>
        <p>Аккаунти Mnemos</p>
      </header>

      <label className="search-root-input">
        <Glyph name="search" size={20} />
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Cristalix nickname"
          autoCapitalize="off"
          autoCorrect="off"
        />
        {query ? <button onClick={() => setQuery("")} aria-label="Очистити"><Glyph name="close" size={15} /></button> : null}
      </label>

      {query.trim().length > 0 && query.trim().length < 2 ? <p className="search-root-hint">Введи ще один символ</p> : null}
      {query.trim().length >= 2 && !results.length ? <p className="search-root-hint">Нічого не знайдено</p> : null}

      {results.length ? (
        <section className="search-result-panel">
          <div className="search-result-panel__head"><span>Результати</span><b>{results.length}</b></div>
          {results.map((profile) => (
            <button key={profile.accountId} onClick={() => navigate({ id: "public-profile", params: { accountId: profile.accountId } })}>
              <CristalixHead skinUrl={profile.skinUrl} size={46} />
              <span><strong>{profile.nickname}</strong><small>{profile.comments} коментарів</small></span>
              <span className="search-result-stats"><b>↑ {profile.likes}</b><i>›</i></span>
            </button>
          ))}
        </section>
      ) : null}
    </div>
  )
}

function PublicProfilePage({ back, params }: Props) {
  const profile = profiles.find((item) => item.accountId === params?.accountId) ?? profiles[0]
  const [reaction, setReaction] = useState<"LIKE" | "DISLIKE" | null>(null)
  const [comments, setComments] = useState<ProfileComment[]>([
    ...initialComments,
    {
      id: "comment-me",
      authorAccountId: "me",
      author: "knalis",
      content: "Перевірив — профіль виглядає нормально.",
      likes: 2,
      replies: 0,
      createdAt: "4 хв",
      liked: false,
    },
  ])
  const [draft, setDraft] = useState("")
  const [editing, setEditing] = useState<string | null>(null)
  const [editDraft, setEditDraft] = useState("")
  const [replyingTo, setReplyingTo] = useState<string | null>(null)
  const [replyDraft, setReplyDraft] = useState("")
  const [openReplies, setOpenReplies] = useState<string | null>(null)
  const [replies, setReplies] = useState<ReplyMap>({
    "comment-1": [
      {
        id: "reply-1",
        authorAccountId: "profile-vex",
        author: "vex",
        content: "Так, дроп був хороший.",
        likes: 1,
        replies: 0,
        createdAt: "10 хв",
        liked: false,
      },
      {
        id: "reply-2",
        authorAccountId: "me",
        author: "knalis",
        content: "Сам здивувався.",
        likes: 3,
        replies: 0,
        createdAt: "7 хв",
        liked: false,
      },
    ],
  })

  const drops = feed
    .filter((event) => event.kind === "ITEM_DROP" && event.detail.toLowerCase() === profile.nickname.toLowerCase())
    .slice(0, 4)

  const settleOptimistic = (
    id: string,
    content: string,
    setter: React.Dispatch<React.SetStateAction<ProfileComment[]>>,
  ) => window.setTimeout(() => {
    setter((current) => current.map((comment) => comment.id === id
      ? { ...comment, pending: false, failed: content.toLowerCase().includes("!fail") }
      : comment))
  }, 760)

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const content = draft.trim()
    if (!content) return

    const id = `pending-${Date.now()}`
    const optimistic: ProfileComment = {
      id,
      authorAccountId: "me",
      author: "knalis",
      content,
      likes: 0,
      replies: 0,
      createdAt: "зараз",
      liked: false,
      pending: true,
    }

    setComments([optimistic, ...comments])
    setDraft("")
    settleOptimistic(id, content, setComments)
  }

  const submitReply = (parentId: string) => {
    const content = replyDraft.trim()
    if (!content) return

    const id = `reply-${Date.now()}`
    const optimistic: ProfileComment = {
      id,
      authorAccountId: "me",
      author: "knalis",
      content,
      likes: 0,
      replies: 0,
      createdAt: "зараз",
      liked: false,
      pending: true,
    }

    setReplies((current) => ({ ...current, [parentId]: [optimistic, ...(current[parentId] ?? [])] }))
    setComments((current) => current.map((comment) => comment.id === parentId ? { ...comment, replies: comment.replies + 1 } : comment))
    setReplyDraft("")
    setReplyingTo(null)

    window.setTimeout(() => {
      setReplies((current) => ({
        ...current,
        [parentId]: (current[parentId] ?? []).map((reply) => reply.id === id
          ? { ...reply, pending: false, failed: content.toLowerCase().includes("!fail") }
          : reply),
      }))
    }, 760)
  }

  const toggleLike = (id: string) => {
    setComments(comments.map((comment) => comment.id !== id
      ? comment
      : { ...comment, liked: !comment.liked, likes: comment.likes + (comment.liked ? -1 : 1) }))
  }

  const removeComment = (id: string) => setComments(comments.filter((comment) => comment.id !== id))
  const startEdit = (comment: ProfileComment) => {
    setEditing(comment.id)
    setEditDraft(comment.content)
  }
  const saveEdit = (id: string) => {
    const content = editDraft.trim()
    if (!content) return

    setComments(comments.map((comment) => comment.id === id ? { ...comment, content } : comment))
    setEditing(null)
  }
  const retry = (id: string) => {
    setComments(comments.map((comment) => comment.id === id ? { ...comment, failed: false, pending: true } : comment))
    window.setTimeout(() => {
      setComments((current) => current.map((comment) => comment.id === id ? { ...comment, pending: false } : comment))
    }, 700)
  }

  const likeCount = profile.likes + (reaction === "LIKE" ? 1 : 0)
  const dislikeCount = profile.dislikes + (reaction === "DISLIKE" ? 1 : 0)

  return (
    <div className="screen public-profile-redesign">
      <PageHeader title={profile.nickname} back={back} />

      <section className="public-profile-hero">
        <CristalixHead skinUrl={profile.skinUrl} size={92} />
        <div className="public-profile-hero__copy">
          <strong>{profile.nickname}</strong>
          <small>{profile.cristalixUuid.slice(0, 8)}</small>
          <div className="profile-reactions profile-reactions--redesign">
            <button className={reaction === "LIKE" ? "is-active" : ""} onClick={() => setReaction(reaction === "LIKE" ? null : "LIKE")}><span>↑</span><b>{likeCount}</b></button>
            <button className={reaction === "DISLIKE" ? "is-active" : ""} onClick={() => setReaction(reaction === "DISLIKE" ? null : "DISLIKE")}><span>↓</span><b>{dislikeCount}</b></button>
          </div>
        </div>
      </section>

      <section className="public-profile-section">
        <div className="public-profile-section__head"><h2>Останні дропи</h2><span>{drops.length}</span></div>
        {drops.length ? (
          <div className="public-drop-rail">
            {drops.map((drop) => (
              <article key={drop.id} className={`public-drop-card public-drop-card--${(drop.rarity ?? "default").toLowerCase()}`}>
                <span><Glyph name="item-drop" size={18} /></span>
                <strong>{drop.title}</strong>
                <small>{drop.rarity ?? drop.meta}</small>
                <time>{drop.relative}</time>
              </article>
            ))}
          </div>
        ) : <p className="inline-hint inline-hint--left">Поки без дропів</p>}
      </section>

      <section className="comments-section comments-section--redesign">
        <div className="public-profile-section__head"><h2>Коментарі</h2><span>{comments.length}</span></div>

        <form className="comment-composer comment-composer--redesign" onSubmit={submit}>
          <span className="comment-composer__avatar">K</span>
          <input value={draft} maxLength={500} onChange={(event) => setDraft(event.target.value)} placeholder="Написати коментар" />
          <button disabled={!draft.trim()} aria-label="Надіслати"><Glyph name="send" size={17} /></button>
        </form>

        <div className="comment-list comment-list--redesign">
          {comments.map((comment) => (
            <article key={comment.id} className={`comment ${comment.pending ? "is-pending" : ""} ${comment.failed ? "is-failed" : ""}`}>
              <span className="comment__avatar">{comment.author.slice(0, 1).toUpperCase()}</span>
              <div className="comment__body">
                <header>
                  <strong>{comment.author}</strong>
                  <time>{comment.createdAt}</time>
                  {comment.author === "knalis" && !comment.pending ? (
                    <span className="comment__owner-actions">
                      <button aria-label="Редагувати" onClick={() => startEdit(comment)}><Glyph name="edit" size={13} /></button>
                      <button aria-label="Видалити" onClick={() => removeComment(comment.id)}><Glyph name="trash" size={13} /></button>
                    </span>
                  ) : null}
                </header>

                {editing === comment.id ? (
                  <div className="comment-edit"><input value={editDraft} maxLength={500} onChange={(event) => setEditDraft(event.target.value)} /><button aria-label="Зберегти" onClick={() => saveEdit(comment.id)}><Glyph name="check" size={14} /></button></div>
                ) : <p>{comment.content}</p>}

                <footer>
                  <button className={comment.liked ? "is-active" : ""} onClick={() => toggleLike(comment.id)}>♡ <span>{comment.likes || ""}</span></button>
                  <button onClick={() => setReplyingTo(replyingTo === comment.id ? null : comment.id)}>Відповісти</button>
                  {comment.replies ? <button onClick={() => setOpenReplies(openReplies === comment.id ? null : comment.id)}>{comment.replies} відповідей</button> : null}
                  {comment.failed ? <button className="retry" onClick={() => retry(comment.id)}>Повторити</button> : null}
                </footer>

                {replyingTo === comment.id ? (
                  <div className="reply-composer"><input autoFocus value={replyDraft} maxLength={500} onChange={(event) => setReplyDraft(event.target.value)} placeholder={`@${comment.author}`} /><button disabled={!replyDraft.trim()} onClick={() => submitReply(comment.id)}><Glyph name="send" size={14} /></button></div>
                ) : null}

                {openReplies === comment.id ? (
                  <div className="reply-list">{(replies[comment.id] ?? []).map((reply) => <div className={reply.pending ? "is-pending" : ""} key={reply.id}><strong>{reply.author}</strong><span>{reply.content}</span><small>{reply.createdAt}</small></div>)}</div>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}

function UserWalletPage({ back }: Props) {
  return (
    <div className="screen wallet-detail-screen">
      <PageHeader title="PRIME" eyebrow="USER" back={back} />
      <div className="single-balance-card single-balance-card--prime"><CurrencyValue currency="PRIME" value={currentUser.prime} /></div>
      <Section title="Історія">
        <div className="transaction-list">
          {userTransactions.map((transaction) => (
            <div className="transaction-row" key={transaction.id}>
              <span className={`transaction-sign ${transaction.type === "CREDIT" ? "is-credit" : ""}`}>{transaction.type === "CREDIT" ? "+" : "−"}</span>
              <span><strong>{transactionSourceLabels[transaction.source]}</strong><small>{transaction.createdAt}</small></span>
              <CurrencyValue currency="PRIME" value={transaction.amount} compact />
            </div>
          ))}
        </div>
      </Section>
    </div>
  )
}
