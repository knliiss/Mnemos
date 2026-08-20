import { Glyph } from "../shared/MnemosIcon"

type UserAvatarProps = {
  className?: string
  placeholderClassName?: string
  size: number
}

export function UserAvatar({ className, placeholderClassName, size }: UserAvatarProps) {
  const classes = ["user-avatar-placeholder", className, placeholderClassName]
    .filter(Boolean)
    .join(" ")

  return (
    <span className={classes} style={{ width: size, height: size }} aria-hidden="true">
      <Glyph name="profile-mark" size={Math.round(size * 0.48)} />
    </span>
  )
}
