import { AnimatePresence, motion } from "framer-motion"
import { FormEvent, useState } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { EventArt } from "../components/EventArt"
import { eventVisualTone } from "../eventVisualTone"
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

export function PublicProfileRethink({ activeAccount, params, navigate, back }: Props) {
  const profile = profiles.find((item) => item.accountId === params?.accountId) ?? profiles[0]
  const [reaction, setReaction] = useState<"LIKE" | "DISLIKE" | null>(null)
  const [draft, setDraft] = useState("")
  const [comments, setComments] = useState<ProfileComment[]>([
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
    ...initialComments,
  ])

  const drops = feed
    .filter(
      (event) =>
        event.kind === "ITEM_DROP" &&
        event.detail.toLowerCase() === profile.nickname.toLowerCase(),
    )
    .sort((left, right) => Date.parse(right.occurredAt) - Date.parse(left.occurredAt))
    .slice(0, 3)

  const likeCount = profile.likes + (reaction === "LIKE" ? 1 : 0)
  const dislikeCount = profile.dislikes + (reaction === "DISLIKE" ? 1 : 0)
  const reputation = likeCount - dislikeCount

  const submit = (event: FormEvent) => {
    event.preventDefault()

    const content = draft.trim()

    if (!content) {
      return
    }

    setComments((current) => [
      {
        id: `comment-${Date.now()}`,
        authorAccountId: activeAccount.id,
        author: currentUser.username,
        content,
        likes: 0,
        replies: 0,
        createdAt: "зараз",
        liked: false,
      },
      ...current,
    ])
    setDraft("")
  }

  const toggleCommentLike = (id: string) => {
    setComments((current) =>
      current.map((comment) =>
        comment.id === id
          ? {
              ...comment,
              liked: !comment.liked,
              likes: comment.likes + (comment.liked ? -1 : 1),
            }
          : comment,
      ),
    )
  }

  return (
    <div className="screen public-profile-screen">
      <PageHeader title="Профіль" back={back} />

      <motion.section
        className="public-profile-card"
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="public-profile-card__main">
          <div className="public-profile-head">
            <CristalixHead skinUrl={profile.skinUrl} size={78} />
            <span className="public-profile-presence">
              <i />
              CRISTALIX
            </span>
          </div>

          <div className="public-profile-identity">
            <small>CRISTALIX ACCOUNT</small>
            <strong>{profile.nickname}</strong>
            <span>{profile.cristalixUuid.slice(0, 8)}</span>
          </div>

          <div className="public-profile-reactions" aria-label="Реакція на профіль">
            <motion.button
              className={reaction === "LIKE" ? "is-active" : ""}
              whileTap={{ scale: 0.95 }}
              onClick={() => setReaction(reaction === "LIKE" ? null : "LIKE")}
            >
              <Glyph name="like" size={15} />
              <b>{likeCount}</b>
            </motion.button>

            <motion.button
              className={reaction === "DISLIKE" ? "is-active is-negative" : ""}
              whileTap={{ scale: 0.95 }}
              onClick={() => setReaction(reaction === "DISLIKE" ? null : "DISLIKE")}
            >
              <Glyph name="dislike" size={15} />
              <b>{dislikeCount}</b>
            </motion.button>
          </div>
        </div>

        <div className="public-profile-metrics">
          <article>
            <span className="public-profile-metric-icon">
              <Glyph name="events" size={14} />
            </span>
            <span>
              <small>ДРОПИ</small>
              <strong>{drops.length}</strong>
            </span>
          </article>

          <article>
            <span className="public-profile-metric-icon">
              <Glyph name="chat" size={14} />
            </span>
            <span>
              <small>КОМЕНТАРІ</small>
              <strong>{comments.length}</strong>
            </span>
          </article>

          <article>
            <span className="public-profile-metric-icon is-reputation">
              <Glyph name="like" size={14} />
            </span>
            <span>
              <small>РЕПУТАЦІЯ</small>
              <strong>{reputation}</strong>
            </span>
          </article>
        </div>
      </motion.section>

      <section className="public-profile-section">
        <header className="public-profile-section-head">
          <span>
            <small>АКТИВНІСТЬ</small>
            <strong>Останні дропи</strong>
          </span>
          <b>{drops.length}</b>
        </header>

        <div className="public-profile-drops">
          {drops.map((drop, index) => (
            <motion.button
              key={drop.id}
              className={`public-profile-drop event-surface--${eventVisualTone(drop)}`}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.045 }}
              whileTap={{ scale: 0.992 }}
              onClick={() =>
                navigate({
                  id: "event-detail",
                  params: { eventId: drop.id },
                })
              }
            >
              <span className="public-profile-drop__art">
                <EventArt event={drop} size={46} />
              </span>

              <span className="public-profile-drop__copy">
                <small>{localizeRarity(drop.rarity) ?? "Дроп"}</small>
                <strong>{drop.title}</strong>
                <em>{formatOccurredAt(drop.occurredAt)}</em>
              </span>

              <span className="public-profile-drop__order">0{index + 1}</span>
              <span className="public-profile-drop__open" aria-hidden="true">›</span>
            </motion.button>
          ))}
        </div>
      </section>

      <section className="public-profile-social">
        <header className="public-profile-section-head public-profile-social__head">
          <span>
            <small>СОЦІАЛЬНЕ</small>
            <strong>Коментарі</strong>
          </span>
          <b>{comments.length}</b>
        </header>

        <form className="public-profile-composer" onSubmit={submit}>
          <span className="public-profile-composer__mark">
            <Glyph name="profile-mark" size={17} />
          </span>

          <input
            value={draft}
            maxLength={500}
            onChange={(event) => setDraft(event.target.value)}
            placeholder="Написати щось про цей аккаунт"
          />

          <motion.button
            whileTap={{ scale: 0.94 }}
            disabled={!draft.trim()}
            aria-label="Надіслати"
          >
            <Glyph name="send" size={15} />
          </motion.button>
        </form>

        <div className="public-profile-comments">
          <AnimatePresence initial={false}>
            {comments.map((comment, index) => (
              <motion.article
                layout
                key={comment.id}
                className="public-profile-comment"
                initial={{ opacity: 0, y: 5 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.985 }}
                transition={{ delay: Math.min(index * 0.025, 0.1) }}
              >
                <span className="public-profile-comment__avatar">
                  <Glyph name="profile-mark" size={15} />
                </span>

                <div className="public-profile-comment__body">
                  <header>
                    <strong>{comment.author}</strong>
                    <time>{comment.createdAt}</time>
                  </header>

                  <p>{comment.content}</p>

                  <footer>
                    <button
                      className={comment.liked ? "is-active" : ""}
                      onClick={() => toggleCommentLike(comment.id)}
                    >
                      <Glyph name="like" size={12} />
                      {comment.likes || ""}
                    </button>
                    <button>Відповісти</button>
                    {comment.replies ? <span>{comment.replies} відповіді</span> : null}
                  </footer>
                </div>
              </motion.article>
            ))}
          </AnimatePresence>
        </div>
      </section>
    </div>
  )
}

function localizeRarity(value?: string) {
  if (value === "MYTHICAL") {
    return "Міфічний"
  }

  if (value === "SECRET") {
    return "Секретний"
  }

  return value
}
