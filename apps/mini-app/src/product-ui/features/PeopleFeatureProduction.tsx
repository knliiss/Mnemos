import { AnimatePresence, motion } from "framer-motion"
import {
  type CSSProperties,
  type FormEvent,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react"
import {
  createComment,
  deleteComment,
  editComment,
  getAccountComments,
  getAccountReaction,
  getAccountReactionSummary,
  getCommentLikeCount,
  getCommentLikeState,
  getCommentReplies,
  getPublicProfile,
  likeComment,
  removeAccountReaction,
  searchPublicProfiles,
  setAccountReaction,
  unlikeComment,
  type AccountReactionType,
  type CommentResponse,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { getEventFeed } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { CristalixHead } from "../components/CristalixHead"
import { EventArt } from "../components/EventArt"
import { toProductFeedEvent } from "../data/productMappers"
import { eventVisualTone } from "../eventVisualTone"
import type { Account, FeedEvent, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { formatOccurredAt } from "../timePresentation"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

type CommentView = {
  id: string
  authorAccountId: string
  author: string
  content: string
  deleted: boolean
  likes: number
  replies: number
  createdAt: string
  liked: boolean
}

type ReplyMap = Record<string, CommentView[]>

const SKIN_BASE_URL = "https://webdata.c7x.dev/textures/skin"

export function PeopleFeatureProduction(props: Props) {
  if (props.routeId === "public-profile") {
    return <PublicProfileProduction {...props} />
  }

  return <SearchProduction {...props} />
}

function SearchProduction({ navigate }: Props) {
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<PublicProfileResponse[]>([])
  const [searching, setSearching] = useState(false)
  const normalizedQuery = query.trim()

  const runSearch = useCallback(async (value: string) => {
    setSearching(true)

    try {
      const page = await searchPublicProfiles(value)
      setResults(page.content)
    } catch (error) {
      handleError(error)
      setResults([])
    } finally {
      setSearching(false)
    }
  }, [])

  useEffect(() => {
    if (normalizedQuery.length < 2) return

    const timeout = window.setTimeout(() => {
      void runSearch(normalizedQuery)
    }, 260)

    return () => window.clearTimeout(timeout)
  }, [normalizedQuery, runSearch])

  const changeQuery = (value: string) => {
    setQuery(value)

    if (value.trim().length < 2) {
      setResults([])
      setSearching(false)
    }
  }

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
          onChange={(event) => changeQuery(event.target.value)}
          placeholder="Cristalix nickname"
          autoCapitalize="off"
          autoCorrect="off"
        />
        {query ? (
          <button onClick={() => changeQuery("")} aria-label="Очистити">
            <Glyph name="close" size={15} />
          </button>
        ) : null}
      </label>

      {!normalizedQuery ? (
        <section className="search-root-empty">
          <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
          <strong>Знайди Cristalix аккаунт</strong>
          <small>Пошук працює по nickname і відкриває публічний профіль Mnemos.</small>
        </section>
      ) : null}

      {normalizedQuery.length === 1 ? <p className="search-root-hint">Введи ще один символ</p> : null}
      {searching ? <p className="search-root-hint">Шукаємо…</p> : null}
      {normalizedQuery.length >= 2 && !searching && !results.length ? (
        <p className="search-root-hint">Нічого не знайдено</p>
      ) : null}

      {results.length ? (
        <section className="search-result-panel">
          <div className="search-result-panel__head">
            <span>Результати</span>
            <b>{results.length}</b>
          </div>

          {results.map((profile) => (
            <button
              key={profile.accountId}
              onClick={() => navigate({ id: "public-profile", params: { accountId: profile.accountId } })}
            >
              <CristalixHead skinUrl={skinUrl(profile.cristalixUuid)} size={46} />
              <span>
                <strong style={nicknameStyle(profile)}>{profile.cristalixNickname}</strong>
                <small>{profile.comments} коментарів</small>
              </span>
              <span className="search-result-stats">
                <b>↑ {profile.likes}</b>
                <i>›</i>
              </span>
            </button>
          ))}
        </section>
      ) : null}
    </div>
  )
}

function PublicProfileProduction({ activeAccount, params, navigate, back }: Props) {
  const accountId = params?.accountId ?? ""
  const [profile, setProfile] = useState<PublicProfileResponse | null>(null)
  const [reaction, setReaction] = useState<AccountReactionType | null>(null)
  const [comments, setComments] = useState<CommentView[]>([])
  const [drops, setDrops] = useState<FeedEvent[]>([])
  const [loading, setLoading] = useState(true)
  const [reactionBusy, setReactionBusy] = useState(false)
  const [draft, setDraft] = useState("")
  const [commentBusyId, setCommentBusyId] = useState<string | null>(null)
  const [editingId, setEditingId] = useState<string | null>(null)
  const [editDraft, setEditDraft] = useState("")
  const [replyingTo, setReplyingTo] = useState<string | null>(null)
  const [replyDraft, setReplyDraft] = useState("")
  const [openReplies, setOpenReplies] = useState<string | null>(null)
  const [replies, setReplies] = useState<ReplyMap>({})

  const refreshProfileSummary = useCallback(async () => {
    if (!accountId) return

    try {
      setProfile(await getPublicProfile(accountId))
    } catch (error) {
      handleError(error)
    }
  }, [accountId])

  const load = useCallback(async () => {
    if (!accountId) {
      setLoading(false)
      return
    }

    setLoading(true)

    try {
      const nextProfile = await getPublicProfile(accountId)
      const [currentReaction, commentPage, feedResponse] = await Promise.all([
        getAccountReaction(accountId),
        getAccountComments(accountId),
        getEventFeed({
          types: ["ITEM_DROP"],
          search: nextProfile.cristalixNickname,
          sort: "NEWEST",
          limit: 20,
        }),
      ])

      const authorNames = await resolveAuthorNames(
        commentPage.content,
        activeAccount,
        nextProfile,
      )

      setProfile(nextProfile)
      setReaction(currentReaction)
      setComments(commentPage.content.map((comment) => toCommentView(comment, authorNames)))
      setDrops(
        feedResponse.items
          .map(toProductFeedEvent)
          .filter((event) => event.kind === "ITEM_DROP"
            && event.detail.toLowerCase() === nextProfile.cristalixNickname.toLowerCase())
          .slice(0, 3),
      )
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [accountId, activeAccount])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void load()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [load])

  const toggleReaction = async (nextReaction: AccountReactionType) => {
    if (!profile || reactionBusy) return

    setReactionBusy(true)

    try {
      if (reaction === nextReaction) {
        await removeAccountReaction(profile.accountId)
        setReaction(null)
      } else {
        await setAccountReaction(profile.accountId, nextReaction)
        setReaction(nextReaction)
      }

      const summary = await getAccountReactionSummary(profile.accountId)
      setProfile((current) => current ? { ...current, ...summary } : current)
    } catch (error) {
      handleError(error)
    } finally {
      setReactionBusy(false)
    }
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    if (!profile || commentBusyId) return

    const content = draft.trim()
    if (!content) return

    setCommentBusyId("new")

    try {
      const created = await createComment(profile.accountId, content)
      const authorNames = new Map([[activeAccount.id, activeAccount.nickname]])

      setComments((current) => [toCommentView(created, authorNames), ...current])
      setDraft("")
      await refreshProfileSummary()
    } catch (error) {
      handleError(error)
    } finally {
      setCommentBusyId(null)
    }
  }

  const startEdit = (comment: CommentView) => {
    setEditingId(comment.id)
    setEditDraft(comment.content)
  }

  const saveEdit = async (commentId: string) => {
    const content = editDraft.trim()
    if (!content || commentBusyId) return

    setCommentBusyId(commentId)

    try {
      const updated = await editComment(commentId, content)

      setComments((current) => current.map((comment) => comment.id === commentId
        ? { ...comment, content: updated.content ?? "" }
        : comment))
      setEditingId(null)
    } catch (error) {
      handleError(error)
    } finally {
      setCommentBusyId(null)
    }
  }

  const removeComment = async (commentId: string) => {
    if (commentBusyId) return

    setCommentBusyId(commentId)

    try {
      await deleteComment(commentId)

      setComments((current) => current.map((comment) => comment.id === commentId
        ? { ...comment, content: "Коментар видалено", deleted: true }
        : comment))
      await refreshProfileSummary()
    } catch (error) {
      handleError(error)
    } finally {
      setCommentBusyId(null)
    }
  }

  const toggleCommentLike = async (commentId: string) => {
    if (commentBusyId) return

    setCommentBusyId(commentId)

    try {
      const liked = await getCommentLikeState(commentId)

      if (liked) {
        await unlikeComment(commentId)
      } else {
        await likeComment(commentId)
      }

      const likes = await getCommentLikeCount(commentId)
      setComments((current) => current.map((comment) => comment.id === commentId
        ? { ...comment, liked: !liked, likes }
        : comment))
    } catch (error) {
      handleError(error)
    } finally {
      setCommentBusyId(null)
    }
  }

  const toggleReplies = async (comment: CommentView) => {
    if (openReplies === comment.id) {
      setOpenReplies(null)
      return
    }

    setOpenReplies(comment.id)
    if (replies[comment.id]) return

    try {
      const page = await getCommentReplies(comment.id)
      const authorNames = await resolveAuthorNames(
        page.content,
        activeAccount,
        profile,
      )

      setReplies((current) => ({
        ...current,
        [comment.id]: page.content.map((reply) => toCommentView(reply, authorNames)),
      }))
    } catch (error) {
      handleError(error)
    }
  }

  const submitReply = async (parent: CommentView) => {
    if (!profile || commentBusyId) return

    const content = replyDraft.trim()
    if (!content) return

    setCommentBusyId(parent.id)

    try {
      const created = await createComment(profile.accountId, content, parent.id)
      const authorNames = new Map([[activeAccount.id, activeAccount.nickname]])

      setReplies((current) => ({
        ...current,
        [parent.id]: [...(current[parent.id] ?? []), toCommentView(created, authorNames)],
      }))
      setComments((current) => current.map((comment) => comment.id === parent.id
        ? { ...comment, replies: comment.replies + 1 }
        : comment))
      setReplyDraft("")
      setReplyingTo(null)
      setOpenReplies(parent.id)
      await refreshProfileSummary()
    } catch (error) {
      handleError(error)
    } finally {
      setCommentBusyId(null)
    }
  }

  const ownProfile = profile?.accountId === activeAccount.id

  if (loading && !profile) {
    return (
      <div className="screen profile-r2-screen">
        <PageHeader title="Профіль" back={back} />
        <div className="events-r2-loading" aria-label="Завантаження профілю"><i /><i /><i /></div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="screen profile-r2-screen">
        <PageHeader title="Профіль" back={back} />
        <section className="events-r2-empty">
          <Glyph name="profile-mark" size={24} />
          <strong>Профіль не знайдено</strong>
          <small>Аккаунт міг бути деактивований або видалений.</small>
        </section>
      </div>
    )
  }

  return (
    <div className="screen profile-r2-screen">
      <PageHeader title="Профіль" back={back} />

      <motion.section className="profile-r2-hero" initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}>
        <div className="profile-r2-hero__mesh" aria-hidden="true"><i /><i /><i /><i /></div>
        <div className="profile-r2-head-wrap">
          <CristalixHead skinUrl={skinUrl(profile.cristalixUuid)} size={96} />
          <span className="profile-r2-online"><i />CRISTALIX</span>
        </div>

        <div className="profile-r2-identity">
          <small>{ownProfile ? "ТВІЙ ACCOUNT" : "ACCOUNT CARD"}</small>
          <strong style={nicknameStyle(profile)}>{profile.cristalixNickname}</strong>
          <span>{profile.cristalixUuid.slice(0, 8)}</span>
        </div>

        <div className="profile-r2-reactions">
          <motion.button
            className={reaction === "LIKE" ? "is-active" : ""}
            whileTap={{ scale: 0.95 }}
            disabled={reactionBusy}
            onClick={() => void toggleReaction("LIKE")}
          >
            <Glyph name="like" size={16} />
            <b>{profile.likes}</b>
          </motion.button>
          <motion.button
            className={reaction === "DISLIKE" ? "is-active is-negative" : ""}
            whileTap={{ scale: 0.95 }}
            disabled={reactionBusy}
            onClick={() => void toggleReaction("DISLIKE")}
          >
            <Glyph name="dislike" size={16} />
            <b>{profile.dislikes}</b>
          </motion.button>
        </div>
      </motion.section>

      <section className="profile-r2-snapshot">
        <article><small>ДРОПИ</small><strong>{drops.length}</strong><em>останні</em></article>
        <article><small>КОМЕНТАРІ</small><strong>{profile.comments}</strong><em>на профілі</em></article>
        <article><small>РЕПУТАЦІЯ</small><strong>{profile.likes - profile.dislikes}</strong><em>баланс</em></article>
      </section>

      <header className="profile-r2-section-head">
        <span><small>АКТИВНІСТЬ</small><strong>Останні дропи</strong></span>
        <b>{drops.length}</b>
      </header>

      <section className="profile-r2-drops">
        {drops.map((drop, index) => (
          <motion.button
            key={drop.id}
            className={`profile-r2-drop event-surface--${eventVisualTone(drop)}`}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.045 }}
            whileTap={{ scale: 0.992 }}
            onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}
          >
            <span className="profile-r2-drop__art"><EventArt event={drop} size={54} /></span>
            <span className="profile-r2-drop__copy">
              <small>{localizeRarity(drop.rarity) ?? "Дроп"}</small>
              <strong>{drop.title}</strong>
              <em>{formatOccurredAt(drop.occurredAt)}</em>
            </span>
            <span className="profile-r2-drop__index">0{index + 1}</span>
          </motion.button>
        ))}

        {!drops.length ? (
          <div className="events-r2-empty">
            <Glyph name="item-drop" size={22} />
            <strong>Останніх дропів немає</strong>
          </div>
        ) : null}
      </section>

      <header className="profile-r2-section-head profile-r2-comments-head">
        <span><small>СОЦІАЛЬНЕ</small><strong>Коментарі</strong></span>
        <b>{profile.comments}</b>
      </header>

      <form className="profile-r2-composer" onSubmit={(event) => void submit(event)}>
        <span className="profile-r2-user-mark"><Glyph name="profile-mark" size={18} /></span>
        <input
          value={draft}
          maxLength={500}
          disabled={commentBusyId === "new"}
          onChange={(event) => setDraft(event.target.value)}
          placeholder="Написати щось про цей аккаунт"
        />
        <motion.button
          whileTap={{ scale: 0.94 }}
          disabled={!draft.trim() || commentBusyId === "new"}
          aria-label="Надіслати"
        >
          <Glyph name="send" size={16} />
        </motion.button>
      </form>

      <section className="profile-r2-comments">
        <AnimatePresence initial={false}>
          {comments.map((comment, index) => (
            <motion.article
              layout
              key={comment.id}
              className={`profile-r2-comment ${comment.deleted ? "is-deleted" : ""}`}
              initial={{ opacity: 0, y: 5 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.985 }}
              transition={{ delay: Math.min(index * 0.025, 0.1) }}
            >
              <span className="profile-r2-comment__avatar"><Glyph name="profile-mark" size={16} /></span>
              <div className="profile-r2-comment__body">
                <header>
                  <strong>{comment.author}</strong>
                  <time>{formatOccurredAt(comment.createdAt)}</time>
                  {!comment.deleted && comment.authorAccountId === activeAccount.id ? (
                    <span className="comment__owner-actions">
                      <button aria-label="Редагувати" onClick={() => startEdit(comment)}>
                        <Glyph name="edit" size={13} />
                      </button>
                      <button
                        aria-label="Видалити"
                        disabled={commentBusyId !== null}
                        onClick={() => void removeComment(comment.id)}
                      >
                        <Glyph name="trash" size={13} />
                      </button>
                    </span>
                  ) : null}
                </header>

                {editingId === comment.id ? (
                  <div className="comment-edit">
                    <input
                      value={editDraft}
                      maxLength={500}
                      onChange={(event) => setEditDraft(event.target.value)}
                    />
                    <button
                      aria-label="Зберегти"
                      disabled={!editDraft.trim() || commentBusyId !== null}
                      onClick={() => void saveEdit(comment.id)}
                    >
                      <Glyph name="check" size={14} />
                    </button>
                  </div>
                ) : <p>{comment.content}</p>}

                {!comment.deleted ? (
                  <footer>
                    <button
                      className={comment.liked ? "is-active" : ""}
                      disabled={commentBusyId !== null}
                      onClick={() => void toggleCommentLike(comment.id)}
                    >
                      <Glyph name="like" size={13} />
                      {comment.likes || ""}
                    </button>
                    <button onClick={() => setReplyingTo(replyingTo === comment.id ? null : comment.id)}>
                      Відповісти
                    </button>
                    {comment.replies ? (
                      <button onClick={() => void toggleReplies(comment)}>
                        {comment.replies} відповідей
                      </button>
                    ) : null}
                  </footer>
                ) : null}

                {replyingTo === comment.id ? (
                  <div className="reply-composer">
                    <input
                      autoFocus
                      value={replyDraft}
                      maxLength={500}
                      onChange={(event) => setReplyDraft(event.target.value)}
                      placeholder={`@${comment.author}`}
                    />
                    <button
                      disabled={!replyDraft.trim() || commentBusyId !== null}
                      onClick={() => void submitReply(comment)}
                    >
                      <Glyph name="send" size={14} />
                    </button>
                  </div>
                ) : null}

                {openReplies === comment.id ? (
                  <div className="reply-list">
                    {(replies[comment.id] ?? []).map((reply) => (
                      <div key={reply.id}>
                        <strong>{reply.author}</strong>
                        <span>{reply.content}</span>
                        <small>{formatOccurredAt(reply.createdAt)}</small>
                      </div>
                    ))}
                  </div>
                ) : null}
              </div>
              <span className="profile-r2-comment__rail" />
            </motion.article>
          ))}
        </AnimatePresence>

        {!comments.length ? (
          <div className="events-r2-empty">
            <Glyph name="profile-mark" size={22} />
            <strong>Коментарів ще немає</strong>
            <small>Можеш залишити перший.</small>
          </div>
        ) : null}
      </section>
    </div>
  )
}

async function resolveAuthorNames(
  comments: CommentResponse[],
  activeAccount: Account,
  targetProfile: PublicProfileResponse | null,
): Promise<Map<string, string>> {
  const result = new Map<string, string>([[activeAccount.id, activeAccount.nickname]])

  if (targetProfile) {
    result.set(targetProfile.accountId, targetProfile.cristalixNickname)
  }

  const unknownIds = Array.from(new Set(
    comments
      .map((comment) => comment.authorAccountId)
      .filter((accountId) => !result.has(accountId)),
  ))

  const profiles = await Promise.allSettled(unknownIds.map((accountId) => getPublicProfile(accountId)))

  profiles.forEach((profile, index) => {
    if (profile.status === "fulfilled") {
      result.set(profile.value.accountId, profile.value.cristalixNickname)
    } else {
      result.set(unknownIds[index], shortAccountId(unknownIds[index]))
    }
  })

  return result
}

function toCommentView(comment: CommentResponse, authorNames: Map<string, string>): CommentView {
  return {
    id: comment.id,
    authorAccountId: comment.authorAccountId,
    author: authorNames.get(comment.authorAccountId) ?? shortAccountId(comment.authorAccountId),
    content: comment.deleted ? "Коментар видалено" : comment.content ?? "",
    deleted: comment.deleted,
    likes: comment.likes,
    replies: comment.replies,
    createdAt: comment.createdAt,
    liked: false,
  }
}

function nicknameStyle(profile: PublicProfileResponse): CSSProperties | undefined {
  const style = profile.appearance.nickStyle
  if (!style || !style.colors.length) return undefined

  const stops = style.colors
    .map((stop) => `${stop.color} ${stop.position}%`)
    .join(", ")

  return {
    backgroundImage: `linear-gradient(${style.angleDegrees}deg, ${stops})`,
    backgroundClip: "text",
    WebkitBackgroundClip: "text",
    color: "transparent",
    textShadow: style.glow
      ? `0 0 ${Math.max(2, Math.round(style.glow.strength / 10))}px ${style.glow.color}`
      : undefined,
  }
}

function skinUrl(cristalixUuid: string) {
  return `${SKIN_BASE_URL}/${cristalixUuid}`
}

function shortAccountId(accountId: string) {
  return `#${accountId.slice(0, 8)}`
}

function localizeRarity(value?: string) {
  if (value === "MYTHICAL") return "Міфічний"
  if (value === "SECRET") return "Секретний"
  return value
}
