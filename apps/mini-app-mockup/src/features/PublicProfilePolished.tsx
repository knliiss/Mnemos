import { AnimatePresence, motion } from "framer-motion"
import { FormEvent, useState } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { EventArt } from "../components/EventArt"
import { currentUser, feed, initialComments, profiles } from "../mockData"
import type { Account, ProfileComment, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  activeAccount: Account
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type ReplyMap = Record<string, ProfileComment[]>

export function PublicProfilePolished({ activeAccount, params, navigate, back }: Props) {
  const profile = profiles.find((item) => item.accountId === params?.accountId) ?? profiles[0]
  const [reaction, setReaction] = useState<"LIKE" | "DISLIKE" | null>(null)
  const [comments, setComments] = useState<ProfileComment[]>([
    ...initialComments,
    {
      id: "comment-me",
      authorAccountId: activeAccount.id,
      author: currentUser.username,
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
  const [openCommentMenu, setOpenCommentMenu] = useState<string | null>(null)
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
        authorAccountId: activeAccount.id,
        author: currentUser.username,
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
    .slice(0, 3)

  const settleOptimistic = (id: string, content: string) => window.setTimeout(() => {
    setComments((current) => current.map((comment) => comment.id === id
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
      authorAccountId: activeAccount.id,
      author: currentUser.username,
      content,
      likes: 0,
      replies: 0,
      createdAt: "зараз",
      liked: false,
      pending: true,
    }

    setComments((current) => [optimistic, ...current])
    setDraft("")
    settleOptimistic(id, content)
  }

  const submitReply = (parentId: string) => {
    const content = replyDraft.trim()
    if (!content) return

    const id = `reply-${Date.now()}`
    const optimistic: ProfileComment = {
      id,
      authorAccountId: activeAccount.id,
      author: currentUser.username,
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
    setOpenReplies(parentId)

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
    setComments((current) => current.map((comment) => comment.id !== id
      ? comment
      : { ...comment, liked: !comment.liked, likes: comment.likes + (comment.liked ? -1 : 1) }))
  }

  const removeComment = (id: string) => {
    setComments((current) => current.filter((comment) => comment.id !== id))
    setOpenCommentMenu(null)
  }

  const startEdit = (comment: ProfileComment) => {
    setEditing(comment.id)
    setEditDraft(comment.content)
    setOpenCommentMenu(null)
  }

  const saveEdit = (id: string) => {
    const content = editDraft.trim()
    if (!content) return

    setComments((current) => current.map((comment) => comment.id === id ? { ...comment, content } : comment))
    setEditing(null)
  }

  const retry = (id: string) => {
    setComments((current) => current.map((comment) => comment.id === id ? { ...comment, failed: false, pending: true } : comment))
    window.setTimeout(() => {
      setComments((current) => current.map((comment) => comment.id === id ? { ...comment, pending: false } : comment))
    }, 700)
  }

  const likeCount = profile.likes + (reaction === "LIKE" ? 1 : 0)
  const dislikeCount = profile.dislikes + (reaction === "DISLIKE" ? 1 : 0)

  return (
    <div className="screen public-profile-polished">
      <PageHeader title="Профіль" back={back} />

      <section className="profile-polished-hero">
        <div className="profile-polished-hero__glow" />
        <CristalixHead skinUrl={profile.skinUrl} size={82} />
        <div className="profile-polished-hero__copy">
          <small>CRISTALIX ACCOUNT</small>
          <strong>{profile.nickname}</strong>
          <span>{profile.cristalixUuid.slice(0, 8)}</span>
        </div>
        <div className="profile-polished-reactions" aria-label="Реакція на аккаунт">
          <motion.button
            className={reaction === "LIKE" ? "is-active" : ""}
            whileTap={{ scale: 0.96 }}
            onClick={() => setReaction(reaction === "LIKE" ? null : "LIKE")}
          >
            <Glyph name="like" size={17} />
            <b>{likeCount}</b>
          </motion.button>
          <motion.button
            className={reaction === "DISLIKE" ? "is-active is-negative" : ""}
            whileTap={{ scale: 0.96 }}
            onClick={() => setReaction(reaction === "DISLIKE" ? null : "DISLIKE")}
          >
            <Glyph name="dislike" size={17} />
            <b>{dislikeCount}</b>
          </motion.button>
        </div>
      </section>

      <section className="profile-polished-section profile-polished-drops">
        <header>
          <span><Glyph name="item-drop" size={18} /><strong>Останні дропи</strong></span>
          <small>{drops.length}</small>
        </header>

        {drops.length ? (
          <div className="profile-polished-drop-list">
            {drops.map((drop) => (
              <button key={drop.id} onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}>
                <EventArt event={drop} size={38} />
                <span><strong>{drop.title}</strong><small>{localizeRarity(drop.rarity) ?? drop.meta}</small></span>
                <time>{formatOccurredAt(drop.occurredAt)}</time>
                <b>›</b>
              </button>
            ))}
          </div>
        ) : <p className="inline-hint inline-hint--left">Поки без дропів</p>}
      </section>

      <section className="profile-polished-section profile-polished-comments">
        <header>
          <span><Glyph name="chat" size={18} /><strong>Коментарі</strong></span>
          <small>{comments.length}</small>
        </header>

        <form className="profile-comment-composer" onSubmit={submit}>
          <span className="profile-comment-avatar">{currentUser.username.slice(0, 1).toUpperCase()}</span>
          <input value={draft} maxLength={500} onChange={(event) => setDraft(event.target.value)} placeholder="Написати коментар" />
          <motion.button whileTap={{ scale: 0.94 }} disabled={!draft.trim()} aria-label="Надіслати">
            <Glyph name="send" size={17} />
          </motion.button>
        </form>

        <div className="profile-comment-feed">
          {comments.map((comment) => {
            const own = comment.author === currentUser.username
            const menuOpen = openCommentMenu === comment.id

            return (
              <motion.article
                layout
                key={comment.id}
                className={`profile-comment ${comment.pending ? "is-pending" : ""} ${comment.failed ? "is-failed" : ""}`}
              >
                <span className="profile-comment-avatar">{comment.author.slice(0, 1).toUpperCase()}</span>
                <div className="profile-comment__body">
                  <header>
                    <span><strong>{comment.author}</strong><time>{comment.createdAt}</time></span>
                    {own && !comment.pending ? (
                      <button className="profile-comment__more" aria-label="Дії з коментарем" onClick={() => setOpenCommentMenu(menuOpen ? null : comment.id)}>
                        <Glyph name="more" size={17} />
                      </button>
                    ) : null}

                    <AnimatePresence>
                      {menuOpen ? (
                        <motion.div
                          className="profile-comment-menu"
                          initial={{ opacity: 0, y: -4, scale: 0.98 }}
                          animate={{ opacity: 1, y: 0, scale: 1 }}
                          exit={{ opacity: 0, y: -3, scale: 0.985 }}
                        >
                          <button onClick={() => startEdit(comment)}><Glyph name="edit" size={14} />Редагувати</button>
                          <button className="is-danger" onClick={() => removeComment(comment.id)}><Glyph name="trash" size={14} />Видалити</button>
                        </motion.div>
                      ) : null}
                    </AnimatePresence>
                  </header>

                  {editing === comment.id ? (
                    <div className="profile-comment-edit">
                      <input value={editDraft} maxLength={500} onChange={(event) => setEditDraft(event.target.value)} />
                      <button aria-label="Зберегти" onClick={() => saveEdit(comment.id)}><Glyph name="check" size={14} /></button>
                    </div>
                  ) : <p>{comment.content}</p>}

                  <footer>
                    <button className={comment.liked ? "is-active" : ""} onClick={() => toggleLike(comment.id)}>
                      <Glyph name="like" size={14} />
                      {comment.likes ? <span>{comment.likes}</span> : null}
                    </button>
                    <button onClick={() => setReplyingTo(replyingTo === comment.id ? null : comment.id)}>Відповісти</button>
                    {comment.replies ? <button onClick={() => setOpenReplies(openReplies === comment.id ? null : comment.id)}>{comment.replies} відповіді</button> : null}
                    {comment.failed ? <button className="is-retry" onClick={() => retry(comment.id)}>Повторити</button> : null}
                  </footer>

                  <AnimatePresence initial={false}>
                    {replyingTo === comment.id ? (
                      <motion.div className="profile-reply-composer" initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: "auto" }} exit={{ opacity: 0, height: 0 }}>
                        <input autoFocus value={replyDraft} maxLength={500} onChange={(event) => setReplyDraft(event.target.value)} placeholder={`@${comment.author}`} />
                        <button disabled={!replyDraft.trim()} onClick={() => submitReply(comment.id)}><Glyph name="send" size={14} /></button>
                      </motion.div>
                    ) : null}
                  </AnimatePresence>

                  <AnimatePresence initial={false}>
                    {openReplies === comment.id ? (
                      <motion.div className="profile-reply-list" initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: "auto" }} exit={{ opacity: 0, height: 0 }}>
                        {(replies[comment.id] ?? []).map((reply) => (
                          <div className={reply.pending ? "is-pending" : ""} key={reply.id}>
                            <span className="profile-comment-avatar is-small">{reply.author.slice(0, 1).toUpperCase()}</span>
                            <span><strong>{reply.author}</strong><p>{reply.content}</p><small>{reply.createdAt}</small></span>
                          </div>
                        ))}
                      </motion.div>
                    ) : null}
                  </AnimatePresence>
                </div>
              </motion.article>
            )
          })}
        </div>
      </section>
    </div>
  )
}

function localizeRarity(value?: string) {
  if (value === "MYTHICAL") return "Міфічний"
  if (value === "SECRET") return "Секретний"
  return value
}
