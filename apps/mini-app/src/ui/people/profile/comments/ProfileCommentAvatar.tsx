import { profileCommentTone } from "./profileCommentPresentation"

type ProfileCommentAvatarProps = {
  author: string
  compact?: boolean
}

export function ProfileCommentAvatar({ author, compact = false }: ProfileCommentAvatarProps) {
  return (
    <span
      className={`public-profile-comment-avatar is-${profileCommentTone(author)} ${compact ? "is-compact" : ""}`}
      aria-hidden="true"
    >
      <span className="public-profile-comment-avatar__halo" />
      <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" />
      <i />
    </span>
  )
}
