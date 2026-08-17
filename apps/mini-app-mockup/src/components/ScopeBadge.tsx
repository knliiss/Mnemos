import { CircleUserRound, Layers } from "lucide-react"
import type { Scope } from "../types"

export function ScopeBadge({ scope }: { scope: Scope }) {
  const Icon = scope === "account" ? Layers : CircleUserRound

  return (
    <span className={`scope-badge scope-badge--${scope}`}>
      <Icon size={12} strokeWidth={1.8} />
      {scope === "account" ? "Аккаунт" : "Юзер"}
    </span>
  )
}
