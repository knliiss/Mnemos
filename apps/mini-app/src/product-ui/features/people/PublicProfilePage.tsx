import { motion } from "framer-motion"
import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createComment,
  deleteComment,
  getAccountComments,
  getAccountReaction,
  getAccountReactionSummary,
  getCommentLikeCount,
  getCommentLikeState,
  getCommentReplies,
  getPublicProfile,
  likeComment,
  removeAccountReaction,
  setAccountReaction,
  unlikeComment,
  type AccountReactionType,
  type CommentResponse,
  type PublicProfileResponse,
} from "@/shared/api/profileSocialApi"
import { getEventFeed } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { CristalixHead } from "../../components/CristalixHead"
import { EventArt } from "../../components/EventArt"
import { toProductFeedEvent } from "../../data/productMappers"
import { eventVisualTone } from "../../eventVisualTone"
import type { Account, FeedEvent, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { formatOccurredAt } from "../../timePresentation"
import { ProfileCommentsSheet } from "./comments/ProfileCommentsSheet"
import type {
  ProfileCommentView,
  ReplyTarget,
  ReplyThreadMap,
} from "./model"
import {
  localeForLanguage,
  nicknameStyle,
  profileSkinUrl,
  rarityLabel,
  shortAccountId,
} from "./profilePresentation"

const ROOT_COMMENT_PAGE_SIZE = 20
const REPLY_PAGE_SIZE = 3

type Props = {
  activeAccount: Account
  accountId: string
  navigate: (route: Route) => void
  back: () => void
}

export function PublicProfilePage({ activeAccount, accountId, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const [profile, setProfile] = useState<PublicProfileResponse | null>(null)
  const [reaction, setReaction] = useState<AccountReactionType | null>(null)
  const [comments, setComments] = useState<ProfileCommentView[]>([])
  const [commentsPage, setCommentsPage] = useState(0)
  const [commentsHasMore, setCommentsHasMore] = useState(false)
  const [commentsLoading, setCommentsLoading] = useState(false)
  const [commentsOpen, setCommentsOpen] = useState(false)
  const [drops, setDrops] = useState<FeedEvent[]>([])
  const [replies, setReplies] = useState<ReplyThreadMap>({})
  const [expandedReplies, setExpandedReplies] = useState<Set<string>>(() => new Set())
  const [loading, setLoading] = useState(true)
  const [reactionBusy, setReactionBusy] = useState(false)
  const [busyIds, setBusyIds] = useState<Set<string>>(() => new Set())
  const [submitting, setSubmitting] = useState(false)

  const setBusy = useCallback((id: string, busy: boolean) => {
    setBusyIds((current) => {
      const next = new Set(current)

      if (busy) {
        next.add(id)
      } else {
        next.delete(id)
      }

      return next
    })
  }, [])

  const refreshProfileSummary = useCallback(async () => {
    if (!accountId) {
      return
    }

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
        getAccountComments(accountId, 0, ROOT_COMMENT_PAGE_SIZE),
        getEventFeed({
          types: ["ITEM_DROP"],
          droppedFor: nextProfile.cristalixNickname,
          sort: "NEWEST",
          limit: 3,
        }),
      ])
      const authorNames = await resolveAuthorNames(
        commentPage.content,
        activeAccount,
        nextProfile,
      )

      setProfile(nextProfile)
      setReaction(currentReaction)
      setComments(
        commentPage.content.map((comment) => toCommentView(comment, authorNames, locale)),
      )
      setCommentsPage(commentPage.number)
      setCommentsHasMore(!commentPage.last)
      setDrops(feedResponse.items.map(toProductFeedEvent).slice(0, 3))
      setReplies({})
      setExpandedReplies(new Set())
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [accountId, activeAccount, locale])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void load()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [load])

  const loadMoreComments = useCallback(async () => {
    if (!profile || commentsLoading || !commentsHasMore) {
      return
    }

    const nextPage = commentsPage + 1
    setCommentsLoading(true)

    try {
      const page = await getAccountComments(accountId, nextPage, ROOT_COMMENT_PAGE_SIZE)
      const authorNames = await resolveAuthorNames(page.content, activeAccount, profile)
      const nextItems = page.content.map((comment) => toCommentView(comment, authorNames, locale))

      setComments((current) => mergeComments(current, nextItems))
      setCommentsPage(page.number)
      setCommentsHasMore(!page.last)
    } catch (error) {
      handleError(error)
    } finally {
      setCommentsLoading(false)
    }
  }, [
    accountId,
    activeAccount,
    commentsHasMore,
    commentsLoading,
    commentsPage,
    locale,
    profile,
  ])

  const fetchReplyPage = useCallback(
    async (root: ProfileCommentView, pageNumber: number, append: boolean) => {
      if (!profile) {
        return
      }

      setReplies((current) => ({
        ...current,
        [root.id]: {
          items: current[root.id]?.items ?? [],
          page: current[root.id]?.page ?? 0,
          hasMore: current[root.id]?.hasMore ?? true,
          loading: true,
        },
      }))

      try {
        const page = await getCommentReplies(root.id, pageNumber, REPLY_PAGE_SIZE)
        const authorNames = await resolveAuthorNames(page.content, activeAccount, profile)
        const nextItems = page.content.map((comment) => toCommentView(comment, authorNames, locale))

        setReplies((current) => ({
          ...current,
          [root.id]: {
            items: append
              ? mergeComments(current[root.id]?.items ?? [], nextItems)
              : nextItems,
            page: page.number,
            hasMore: !page.last,
            loading: false,
          },
        }))
      } catch (error) {
        handleError(error)
        setReplies((current) => ({
          ...current,
          [root.id]: {
            items: current[root.id]?.items ?? [],
            page: current[root.id]?.page ?? 0,
            hasMore: current[root.id]?.hasMore ?? false,
            loading: false,
          },
        }))
      }
    },
    [activeAccount, locale, profile],
  )

  const toggleReplies = useCallback(
    async (comment: ProfileCommentView) => {
      const isExpanded = expandedReplies.has(comment.id)

      setExpandedReplies((current) => {
        const next = new Set(current)

        if (next.has(comment.id)) {
          next.delete(comment.id)
        } else {
          next.add(comment.id)
        }

        return next
      })

      if (!isExpanded && !replies[comment.id]) {
        await fetchReplyPage(comment, 0, false)
      }
    },
    [expandedReplies, fetchReplyPage, replies],
  )

  const loadMoreReplies = useCallback(
    async (comment: ProfileCommentView) => {
      const thread = replies[comment.id]

      if (!thread || thread.loading || !thread.hasMore) {
        return
      }

      await fetchReplyPage(comment, thread.page + 1, true)
    },
    [fetchReplyPage, replies],
  )

  const toggleReaction = async (nextReaction: AccountReactionType) => {
    if (!profile || reactionBusy) {
      return
    }

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
      setProfile((current) => (current ? { ...current, ...summary } : current))
    } catch (error) {
      handleError(error)
    } finally {
      setReactionBusy(false)
    }
  }

  const toggleCommentLike = useCallback(
    async (comment: ProfileCommentView, rootCommentId?: string) => {
      if (busyIds.has(comment.id)) {
        return
      }

      setBusy(comment.id, true)

      try {
        const liked = await getCommentLikeState(comment.id)

        if (liked) {
          await unlikeComment(comment.id)
        } else {
          await likeComment(comment.id)
        }

        const likes = await getCommentLikeCount(comment.id)
        const update = (value: ProfileCommentView) =>
          value.id === comment.id ? { ...value, liked: !liked, likes } : value

        if (rootCommentId) {
          setReplies((current) => {
            const thread = current[rootCommentId]

            if (!thread) {
              return current
            }

            return {
              ...current,
              [rootCommentId]: {
                ...thread,
                items: thread.items.map(update),
              },
            }
          })
        } else {
          setComments((current) => current.map(update))
        }
      } catch (error) {
        handleError(error)
      } finally {
        setBusy(comment.id, false)
      }
    },
    [busyIds, setBusy],
  )

  const removeComment = useCallback(
    async (comment: ProfileCommentView, rootCommentId?: string) => {
      if (!comment.canDelete || busyIds.has(comment.id)) {
        return
      }

      setBusy(comment.id, true)

      try {
        await deleteComment(comment.id)

        if (rootCommentId) {
          setReplies((current) => {
            const thread = current[rootCommentId]

            if (!thread) {
              return current
            }

            return {
              ...current,
              [rootCommentId]: {
                ...thread,
                items: thread.items.filter((item) => item.id !== comment.id),
              },
            }
          })
          setComments((current) =>
            current.map((root) =>
              root.id === rootCommentId
                ? { ...root, replies: Math.max(0, root.replies - 1) }
                : root,
            ),
          )
        } else {
          setComments((current) => current.filter((item) => item.id !== comment.id))
          setReplies((current) => {
            const next = { ...current }
            delete next[comment.id]
            return next
          })
          setExpandedReplies((current) => {
            const next = new Set(current)
            next.delete(comment.id)
            return next
          })
        }

        await refreshProfileSummary()
      } catch (error) {
        handleError(error)
      } finally {
        setBusy(comment.id, false)
      }
    },
    [busyIds, refreshProfileSummary, setBusy],
  )

  const submitComment = useCallback(
    async (content: string, replyTarget: ReplyTarget | null) => {
      if (!profile || submitting) {
        return
      }

      setSubmitting(true)

      try {
        const created = await createComment(
          profile.accountId,
          content,
          replyTarget?.rootCommentId ?? null,
          replyTarget?.targetCommentId ?? null,
        )
        const authorNames = new Map<string, string>([
          [activeAccount.id, activeAccount.nickname],
          [profile.accountId, profile.cristalixNickname],
        ])

        if (created.replyToAuthorAccountId && replyTarget) {
          authorNames.set(created.replyToAuthorAccountId, replyTarget.author)
        }

        const view = toCommentView(created, authorNames, locale)

        if (replyTarget) {
          setReplies((current) => {
            const thread = current[replyTarget.rootCommentId]

            return {
              ...current,
              [replyTarget.rootCommentId]: {
                items: mergeComments(thread?.items ?? [], [view]),
                page: thread?.page ?? 0,
                hasMore: thread?.hasMore ?? false,
                loading: false,
              },
            }
          })
          setComments((current) =>
            current.map((comment) =>
              comment.id === replyTarget.rootCommentId
                ? { ...comment, replies: comment.replies + 1 }
                : comment,
            ),
          )
          setExpandedReplies((current) => new Set(current).add(replyTarget.rootCommentId))
        } else {
          setComments((current) => [view, ...current])
        }

        await refreshProfileSummary()
      } catch (error) {
        handleError(error)
        throw error
      } finally {
        setSubmitting(false)
      }
    },
    [activeAccount, locale, profile, refreshProfileSummary, submitting],
  )

  const latestComment = useMemo(
    () => comments.find((comment) => !comment.deleted) ?? null,
    [comments],
  )

  if (loading && !profile) {
    return (
      <div className="screen public-profile-screen">
        <PageHeader title={t("people.profile.title")} back={back} />
        <div className="events-r2-loading" aria-label={t("people.profile.loading")}>
          <i />
          <i />
          <i />
        </div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="screen public-profile-screen">
        <PageHeader title={t("people.profile.title")} back={back} />
        <section className="events-r2-empty">
          <Glyph name="profile-mark" size={24} />
          <strong>{t("people.profile.notFoundTitle")}</strong>
          <small>{t("people.profile.notFoundCopy")}</small>
        </section>
      </div>
    )
  }

  const reputation = profile.likes - profile.dislikes

  return (
    <div className="screen public-profile-screen">
      <PageHeader title={t("people.profile.title")} back={back} />

      <motion.section
        className="public-profile-card"
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="public-profile-card__main">
          <div className="public-profile-head">
            <CristalixHead skinUrl={profileSkinUrl(profile.cristalixUuid)} size={78} />
            <span className="public-profile-presence">
              <i />
              CRISTALIX
            </span>
          </div>

          <div className="public-profile-identity">
            <small>{t("people.profile.accountCard")}</small>
            <strong style={nicknameStyle(profile)}>{profile.cristalixNickname}</strong>
            <span>{profile.cristalixUuid.slice(0, 8)}</span>
          </div>

          <div className="public-profile-reactions" aria-label={t("people.profile.reactionAria")}>
            <motion.button
              type="button"
              className={reaction === "LIKE" ? "is-active" : ""}
              whileTap={{ scale: 0.95 }}
              disabled={reactionBusy}
              onClick={() => void toggleReaction("LIKE")}
            >
              <Glyph name="like" size={15} />
              <b>{profile.likes}</b>
            </motion.button>

            <motion.button
              type="button"
              className={reaction === "DISLIKE" ? "is-active is-negative" : ""}
              whileTap={{ scale: 0.95 }}
              disabled={reactionBusy}
              onClick={() => void toggleReaction("DISLIKE")}
            >
              <Glyph name="dislike" size={15} />
              <b>{profile.dislikes}</b>
            </motion.button>
          </div>
        </div>

        <div className="public-profile-metrics">
          <article>
            <span className="public-profile-metric-icon">
              <Glyph name="events" size={14} />
            </span>
            <span>
              <small>{t("people.profile.drops")}</small>
              <strong>{drops.length}</strong>
            </span>
          </article>

          <button type="button" onClick={() => setCommentsOpen(true)}>
            <span className="public-profile-metric-icon">
              <Glyph name="chat" size={14} />
            </span>
            <span>
              <small>{t("people.profile.comments")}</small>
              <strong>{profile.comments}</strong>
            </span>
          </button>

          <article>
            <span className="public-profile-metric-icon is-reputation">
              <Glyph name="like" size={14} />
            </span>
            <span>
              <small>{t("people.profile.reputation")}</small>
              <strong>{reputation}</strong>
            </span>
          </article>
        </div>
      </motion.section>

      <section className="public-profile-section">
        <header className="public-profile-section-head">
          <span>
            <small>{t("people.profile.activity")}</small>
            <strong>{t("people.profile.latestDrops")}</strong>
          </span>
          <b>{drops.length}</b>
        </header>

        <div className="public-profile-drops">
          {drops.map((drop, index) => (
            <motion.button
              key={drop.id}
              type="button"
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
                <small>{rarityLabel(drop.rarity, t)}</small>
                <strong>{drop.title}</strong>
                <em>{formatOccurredAt(drop.occurredAt, locale)}</em>
              </span>

              <span className="public-profile-drop__order">0{index + 1}</span>
              <span className="public-profile-drop__open" aria-hidden="true">
                ›
              </span>
            </motion.button>
          ))}

          {!drops.length ? (
            <div className="events-r2-empty">
              <Glyph name="item-drop" size={22} />
              <strong>{t("people.profile.noDrops")}</strong>
            </div>
          ) : null}
        </div>
      </section>

      <section className="public-profile-comments-preview">
        <header className="public-profile-section-head public-profile-comments-preview__head">
          <span>
            <small>{t("people.profile.social")}</small>
            <strong>{t("people.profile.comments")}</strong>
          </span>
          <b>{profile.comments}</b>
        </header>

        <motion.button
          className="public-profile-comments-preview__open"
          type="button"
          whileTap={{ scale: 0.993 }}
          onClick={() => setCommentsOpen(true)}
        >
          <span className="public-profile-comments-preview__art" aria-hidden="true">
            <img
              className="public-profile-comments-preview__illustration"
              src="/ui-illustrations/mnemos-comments.svg"
              alt=""
            />
            <span className="public-profile-comments-preview__spark is-lime" />
            <span className="public-profile-comments-preview__spark is-plum" />
          </span>

          <span className="public-profile-comments-preview__copy">
            <small>{t("people.profile.latestConversation")}</small>
            <strong>
              {latestComment ? (
                <>
                  <b>@{latestComment.author}</b> {latestComment.content}
                </>
              ) : (
                t("people.profile.noComments")
              )}
            </strong>
            <em>
              {profile.comments > 0
                ? t("people.profile.openAllComments", {
                    count: profile.comments,
                  })
                : t("people.profile.openComments")}
            </em>
          </span>

          <span className="public-profile-comments-preview__count">{profile.comments}</span>
          <span className="public-profile-comments-preview__arrow" aria-hidden="true">
            ›
          </span>
        </motion.button>
      </section>

      <ProfileCommentsSheet
        open={commentsOpen}
        profileNickname={profile.cristalixNickname}
        activeAccountNickname={activeAccount.nickname}
        totalCount={profile.comments}
        comments={comments}
        commentsHasMore={commentsHasMore}
        commentsLoading={commentsLoading}
        replies={replies}
        expandedReplies={expandedReplies}
        busyIds={busyIds}
        submitting={submitting}
        onClose={() => setCommentsOpen(false)}
        onLoadMoreComments={loadMoreComments}
        onToggleReplies={toggleReplies}
        onLoadMoreReplies={loadMoreReplies}
        onToggleLike={toggleCommentLike}
        onDelete={removeComment}
        onSubmit={submitComment}
      />
    </div>
  )
}

async function resolveAuthorNames(
  comments: CommentResponse[],
  activeAccount: Account,
  targetProfile: PublicProfileResponse,
): Promise<Map<string, string>> {
  const result = new Map<string, string>([
    [activeAccount.id, activeAccount.nickname],
    [targetProfile.accountId, targetProfile.cristalixNickname],
  ])
  const unknownIds = Array.from(
    new Set(
      comments
        .flatMap((comment) => [comment.authorAccountId, comment.replyToAuthorAccountId])
        .filter((value): value is string => Boolean(value))
        .filter((id) => !result.has(id)),
    ),
  )
  const profiles = await Promise.allSettled(
    unknownIds.map((id) => getPublicProfile(id)),
  )

  profiles.forEach((resolved, index) => {
    const id = unknownIds[index]

    if (resolved.status === "fulfilled") {
      result.set(resolved.value.accountId, resolved.value.cristalixNickname)
    } else {
      result.set(id, shortAccountId(id))
    }
  })

  return result
}

function toCommentView(
  comment: CommentResponse,
  authorNames: Map<string, string>,
  locale: string,
): ProfileCommentView {
  return {
    id: comment.id,
    authorAccountId: comment.authorAccountId,
    author: authorNames.get(comment.authorAccountId) ?? shortAccountId(comment.authorAccountId),
    replyToAuthor: comment.replyToAuthorAccountId
      ? (authorNames.get(comment.replyToAuthorAccountId) ?? shortAccountId(comment.replyToAuthorAccountId))
      : null,
    content: comment.deleted ? "" : (comment.content ?? ""),
    deleted: comment.deleted,
    canDelete: comment.canDelete,
    likes: comment.likes,
    replies: comment.replies,
    createdAt: formatOccurredAt(comment.createdAt, locale),
    liked: false,
  }
}

function mergeComments(
  current: ProfileCommentView[],
  incoming: ProfileCommentView[],
): ProfileCommentView[] {
  const next = [...current]
  const indexes = new Map(next.map((comment, index) => [comment.id, index]))

  incoming.forEach((comment) => {
    const index = indexes.get(comment.id)

    if (index === undefined) {
      indexes.set(comment.id, next.length)
      next.push(comment)
    } else {
      next[index] = comment
    }
  })

  return next
}
