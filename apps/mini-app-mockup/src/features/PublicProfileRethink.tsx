import { AnimatePresence, motion, useDragControls } from "framer-motion"
import { FormEvent, PointerEvent, useEffect, useRef, useState } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { EventArt } from "../components/EventArt"
import { eventVisualTone } from "../eventVisualTone"
import { feed, initialComments, profiles } from "../mockData"
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

type ProfileReply = {
  id: string
  parentCommentId: string
  replyToAuthor?: string
  authorAccountId: string
  author: string
  content: string
  likes: number
  createdAt: string
  liked: boolean
}

type ReplyTarget = {
  rootCommentId: string
  author: string
}

type CommentTone = "lime" | "plum" | "amber"

const initialReplies: ProfileReply[] = [
  {
    id: "reply-1",
    parentCommentId: "comment-1",
    authorAccountId: "profile-vex",
    author: "vex",
    content: "Та ще й без довгого фарму перед цим.",
    likes: 4,
    createdAt: "12 хв",
    liked: false,
  },
  {
    id: "reply-2",
    parentCommentId: "comment-1",
    replyToAuthor: "vex",
    authorAccountId: "profile-north",
    author: "north",
    content: "Саме тому й запамʼятався.",
    likes: 2,
    createdAt: "9 хв",
    liked: false,
  },
]

export function PublicProfileRethink({ activeAccount, params, navigate, back }: Props) {
  const profile = profiles.find((item) => item.accountId === params?.accountId) ?? profiles[0]
  const [reaction, setReaction] = useState<"LIKE" | "DISLIKE" | null>(null)
  const [draft, setDraft] = useState("")
  const [commentsOpen, setCommentsOpen] = useState(false)
  const [replyingTo, setReplyingTo] = useState<ReplyTarget | null>(null)
  const [expandedReplies, setExpandedReplies] = useState<Set<string>>(() => new Set())
  const [replies, setReplies] = useState<ProfileReply[]>(initialReplies)
  const composerInputRef = useRef<HTMLInputElement>(null)
  const dragControls = useDragControls()
  const [comments, setComments] = useState<ProfileComment[]>([
    {
      id: "comment-me",
      authorAccountId: activeAccount.id,
      author: activeAccount.nickname,
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
  const latestComment = comments[0] ?? null

  useEffect(() => {
    if (!commentsOpen) {
      setReplyingTo(null)
      return
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = "hidden"

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setCommentsOpen(false)
      }
    }

    window.addEventListener("keydown", handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener("keydown", handleKeyDown)
    }
  }, [commentsOpen])

  const submit = (event: FormEvent) => {
    event.preventDefault()

    const content = draft.trim()

    if (!content) {
      return
    }

    if (replyingTo) {
      const parentId = replyingTo.rootCommentId

      setReplies((current) => [
        ...current,
        {
          id: `reply-${Date.now()}`,
          parentCommentId: parentId,
          replyToAuthor: replyingTo.author,
          authorAccountId: activeAccount.id,
          author: activeAccount.nickname,
          content,
          likes: 0,
          createdAt: "зараз",
          liked: false,
        },
      ])
      setExpandedReplies((current) => new Set(current).add(parentId))
      setReplyingTo(null)
    } else {
      setComments((current) => [
        {
          id: `comment-${Date.now()}`,
          authorAccountId: activeAccount.id,
          author: activeAccount.nickname,
          content,
          likes: 0,
          replies: 0,
          createdAt: "зараз",
          liked: false,
        },
        ...current,
      ])
    }

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

  const toggleReplyLike = (id: string) => {
    setReplies((current) =>
      current.map((reply) =>
        reply.id === id
          ? {
              ...reply,
              liked: !reply.liked,
              likes: reply.likes + (reply.liked ? -1 : 1),
            }
          : reply,
      ),
    )
  }

  const startReply = (target: ReplyTarget) => {
    setReplyingTo(target)

    window.requestAnimationFrame(() => {
      composerInputRef.current?.focus()
    })
  }

  const toggleReplies = (commentId: string) => {
    setExpandedReplies((current) => {
      const next = new Set(current)

      if (next.has(commentId)) {
        next.delete(commentId)
      } else {
        next.add(commentId)
      }

      return next
    })
  }

  const openComments = () => {
    setCommentsOpen(true)
  }

  const startSheetDrag = (event: PointerEvent<HTMLButtonElement>) => {
    dragControls.start(event)
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

          <button type="button" onClick={openComments}>
            <span className="public-profile-metric-icon">
              <Glyph name="chat" size={14} />
            </span>
            <span>
              <small>КОМЕНТАРІ</small>
              <strong>{comments.length}</strong>
            </span>
          </button>

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
              <span className="public-profile-drop__open" aria-hidden="true">
                ›
              </span>
            </motion.button>
          ))}
        </div>
      </section>

      <section className="public-profile-comments-preview">
        <header className="public-profile-section-head public-profile-comments-preview__head">
          <span>
            <small>СОЦІАЛЬНЕ</small>
            <strong>Коментарі</strong>
          </span>
          <b>{comments.length}</b>
        </header>

        <motion.button
          className="public-profile-comments-preview__open"
          type="button"
          whileTap={{ scale: 0.993 }}
          onClick={openComments}
        >
          <span className="public-profile-comments-preview__art" aria-hidden="true">
            <CommentAvatar author={latestComment?.author ?? profile.nickname} compact />
            <span className="public-profile-comments-preview__spark is-lime" />
            <span className="public-profile-comments-preview__spark is-plum" />
          </span>

          <span className="public-profile-comments-preview__copy">
            <small>ОСТАННЯ РОЗМОВА</small>
            <strong>
              {latestComment ? (
                <>
                  <b>@{latestComment.author}</b> {latestComment.content}
                </>
              ) : (
                "Коментарів ще немає"
              )}
            </strong>
            <em>
              {comments.length > 1
                ? `Відкрити всі ${comments.length} коментарі`
                : "Відкрити коментарі"}
            </em>
          </span>

          <span className="public-profile-comments-preview__count">{comments.length}</span>
          <span className="public-profile-comments-preview__arrow" aria-hidden="true">
            ›
          </span>
        </motion.button>
      </section>

      <AnimatePresence>
        {commentsOpen ? (
          <motion.div
            className="public-profile-comments-layer"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onMouseDown={(event) => {
              if (event.currentTarget === event.target) {
                setCommentsOpen(false)
              }
            }}
          >
            <motion.section
              className="public-profile-comments-sheet"
              initial={{ y: "100%" }}
              animate={{ y: 0 }}
              exit={{ y: "100%" }}
              transition={{ duration: 0.28, ease: [0.22, 1, 0.36, 1] }}
              drag="y"
              dragListener={false}
              dragControls={dragControls}
              dragConstraints={{ top: 0, bottom: 280 }}
              dragElastic={{ top: 0, bottom: 0.18 }}
              dragSnapToOrigin
              onDragEnd={(_, info) => {
                if (info.offset.y > 110 || info.velocity.y > 720) {
                  setCommentsOpen(false)
                }
              }}
              aria-modal="true"
              role="dialog"
              aria-label="Коментарі профілю"
            >
              <button
                type="button"
                className="public-profile-comments-sheet__drag-zone"
                onPointerDown={startSheetDrag}
                aria-label="Потягнути вниз, щоб закрити коментарі"
              >
                <span className="public-profile-comments-sheet__grabber" />
              </button>

              <header className="public-profile-comments-sheet__header">
                <span className="public-profile-comments-sheet__mark" aria-hidden="true">
                  <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
                </span>

                <span className="public-profile-comments-sheet__title">
                  <small>ПРОФІЛЬ · {profile.nickname}</small>
                  <strong>
                    Коментарі <b>{comments.length}</b>
                  </strong>
                </span>

                <button
                  type="button"
                  className="public-profile-comments-sheet__close"
                  onClick={() => setCommentsOpen(false)}
                  aria-label="Закрити коментарі"
                >
                  <Glyph name="close" size={15} />
                </button>
              </header>

              <div className="public-profile-comments-sheet__scroll">
                <AnimatePresence initial={false}>
                  {comments.map((comment, index) => {
                    const commentReplies = replies.filter(
                      (reply) => reply.parentCommentId === comment.id,
                    )
                    const repliesExpanded = expandedReplies.has(comment.id)

                    return (
                      <motion.article
                        layout
                        key={comment.id}
                        className={`public-profile-thread is-${commentTone(comment.author)}`}
                        initial={{ opacity: 0, y: 5 }}
                        animate={{ opacity: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.985 }}
                        transition={{ delay: Math.min(index * 0.025, 0.1) }}
                      >
                        <div className="public-profile-comment">
                          <CommentAvatar author={comment.author} />

                          <div className="public-profile-comment__body">
                            <header>
                              <strong>{comment.author}</strong>
                              <time>{comment.createdAt}</time>
                            </header>

                            <p>{comment.content}</p>

                            <footer>
                              <button
                                type="button"
                                onClick={() =>
                                  startReply({
                                    rootCommentId: comment.id,
                                    author: comment.author,
                                  })
                                }
                              >
                                Відповісти
                              </button>

                              {commentReplies.length ? (
                                <button
                                  type="button"
                                  className="public-profile-thread__toggle"
                                  onClick={() => toggleReplies(comment.id)}
                                >
                                  <i />
                                  {repliesExpanded
                                    ? "Сховати відповіді"
                                    : `Показати ${commentReplies.length} відповіді`}
                                </button>
                              ) : null}
                            </footer>
                          </div>

                          <button
                            type="button"
                            className={`public-profile-comment__like ${comment.liked ? "is-active" : ""}`}
                            onClick={() => toggleCommentLike(comment.id)}
                            aria-label={comment.liked ? "Прибрати лайк" : "Поставити лайк"}
                          >
                            <Glyph name="like" size={15} />
                            <span>{comment.likes || ""}</span>
                          </button>
                        </div>

                        <AnimatePresence initial={false}>
                          {repliesExpanded ? (
                            <motion.div
                              className="public-profile-replies"
                              initial={{ opacity: 0, height: 0 }}
                              animate={{ opacity: 1, height: "auto" }}
                              exit={{ opacity: 0, height: 0 }}
                            >
                              {commentReplies.map((reply) => (
                                <div
                                  className={`public-profile-reply is-${commentTone(reply.author)}`}
                                  key={reply.id}
                                >
                                  <CommentAvatar author={reply.author} compact />

                                  <div className="public-profile-reply__body">
                                    <header>
                                      <strong>{reply.author}</strong>
                                      <time>{reply.createdAt}</time>
                                    </header>

                                    <p>
                                      {reply.replyToAuthor ? (
                                        <span className="public-profile-reply__mention">
                                          @{reply.replyToAuthor}
                                        </span>
                                      ) : null}
                                      {reply.content}
                                    </p>

                                    <footer>
                                      <button
                                        type="button"
                                        onClick={() =>
                                          startReply({
                                            rootCommentId: comment.id,
                                            author: reply.author,
                                          })
                                        }
                                      >
                                        Відповісти
                                      </button>
                                    </footer>
                                  </div>

                                  <button
                                    type="button"
                                    className={`public-profile-reply__like ${reply.liked ? "is-active" : ""}`}
                                    onClick={() => toggleReplyLike(reply.id)}
                                    aria-label={reply.liked ? "Прибрати лайк" : "Поставити лайк"}
                                  >
                                    <Glyph name="like" size={12} />
                                    <span>{reply.likes || ""}</span>
                                  </button>
                                </div>
                              ))}
                            </motion.div>
                          ) : null}
                        </AnimatePresence>
                      </motion.article>
                    )
                  })}
                </AnimatePresence>
              </div>

              <div className="public-profile-comments-sheet__composer-wrap">
                <AnimatePresence initial={false}>
                  {replyingTo ? (
                    <motion.div
                      className="public-profile-reply-context"
                      initial={{ opacity: 0, y: 4 }}
                      animate={{ opacity: 1, y: 0 }}
                      exit={{ opacity: 0, y: 4 }}
                    >
                      <span>
                        Відповідь <strong>@{replyingTo.author}</strong>
                      </span>
                      <button
                        type="button"
                        onClick={() => setReplyingTo(null)}
                        aria-label="Скасувати відповідь"
                      >
                        <Glyph name="close" size={12} />
                      </button>
                    </motion.div>
                  ) : null}
                </AnimatePresence>

                <form className="public-profile-composer" onSubmit={submit}>
                  <CommentAvatar author={activeAccount.nickname} compact />

                  <input
                    ref={composerInputRef}
                    value={draft}
                    maxLength={500}
                    onChange={(event) => setDraft(event.target.value)}
                    placeholder={
                      replyingTo
                        ? `Відповісти @${replyingTo.author}`
                        : "Додати коментар..."
                    }
                  />

                  <motion.button
                    type="submit"
                    whileTap={{ scale: 0.94 }}
                    disabled={!draft.trim()}
                    aria-label={replyingTo ? "Надіслати відповідь" : "Надіслати коментар"}
                  >
                    <Glyph name="send" size={15} />
                  </motion.button>
                </form>
              </div>
            </motion.section>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </div>
  )
}

function CommentAvatar({ author, compact = false }: { author: string; compact?: boolean }) {
  return (
    <span
      className={`public-profile-comment-avatar is-${commentTone(author)} ${compact ? "is-compact" : ""}`}
      aria-hidden="true"
    >
      <span className="public-profile-comment-avatar__halo" />
      <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
      <i />
    </span>
  )
}

function commentTone(author: string): CommentTone {
  const tones: CommentTone[] = ["lime", "plum", "amber"]
  const score = Array.from(author).reduce((sum, character) => sum + character.charCodeAt(0), 0)

  return tones[score % tones.length]
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