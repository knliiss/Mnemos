import { CircleDotDashed } from "lucide-react"
import type { ItemEvent } from "@/shared/types/domain"

export function ItemList({ items }: { items: ItemEvent[] }) {
  return <div className="item-list">{items.map(item => <article className="item-row" key={item.id}><div className="abstract-icon"><CircleDotDashed size={19} /></div><div><strong>{item.name}</strong><small>{item.description} {item.accountName}</small></div><time>{new Date(item.happenedAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</time></article>)}</div>
}
