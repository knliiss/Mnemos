export type ProfileCommentView = {
  id: string
  authorAccountId: string
  author: string
  replyToAuthor: string | null
  content: string
  deleted: boolean
  canDelete: boolean
  likes: number
  replies: number
  createdAt: string
  liked: boolean
}

export type ReplyTarget = {
  rootCommentId: string
  targetCommentId: string | null
  author: string
}

export type ReplyThreadState = {
  items: ProfileCommentView[]
  page: number
  hasMore: boolean
  loading: boolean
}

export type ReplyThreadMap = Record<string, ReplyThreadState>
