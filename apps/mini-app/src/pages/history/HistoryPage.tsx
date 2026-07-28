import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { ItemList } from "@/entities/item/ui/ItemList"
import { Segmented } from "@/shared/ui/Segmented"
import { getItems } from "@/shared/api/contentService"
import type { ItemEvent } from "@/shared/types/domain"

export function HistoryPage() {
  const { t } = useTranslation()
  const [tab, setTab] = useState<"items" | "bets" | "transactions">("items")
  const [items, setItems] = useState<ItemEvent[]>([])
  useEffect(() => { void getItems(0, 12).then(setItems) }, [])
  return <div><h1 className="page-title">{t("history.title")}</h1><Segmented<"items" | "bets" | "transactions"> value={tab} onChange={setTab} items={[{ value: "items", label: t("history.items") }, { value: "bets", label: t("history.bets") }, { value: "transactions", label: t("history.transactions") }]} /><div className="filter-select">{t("common.all")}</div>{tab === "items" ? <ItemList items={items} /> : <div className="empty-card">{t("common.soon")}</div>}</div>
}
