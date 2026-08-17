type AccountMarkProps = {
  compact?: boolean
}

export function AccountMark({ compact = false }: AccountMarkProps) {
  return (
    <span className={`account-mark ${compact ? "account-mark--compact" : ""}`} aria-hidden="true">
      <span />
      <span />
      <span />
      <span />
    </span>
  )
}
