import { Bell, Coins, Crown, Flame, Gift, Gavel, Lightbulb, Newspaper, ShoppingCart, ShieldCheck } from "lucide-react"
import { useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { useApp } from "@/app/providers/AppProvider"
import { AccountName } from "@/entities/account/ui/AccountName"
import { ItemList } from "@/entities/item/ui/ItemList"
import { MinecraftHead } from "@/entities/account/ui/MinecraftHead"
import { Segmented } from "@/shared/ui/Segmented"
import { getItems } from "@/shared/api/contentService"
import type { ItemEvent } from "@/shared/types/domain"

export function HomePage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { account } = useApp()
  const [scope, setScope] = useState<"all" | "mine">("all")
  const [items, setItems] = useState<ItemEvent[]>([])
  const [sheetOpen, setSheetOpen] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const carousel = useRef<HTMLDivElement>(null)
  const [progress, setProgress] = useState(0)
  const touchStart = useRef(0)

  useEffect(() => { void getItems(0, 8, scope === "mine").then(setItems) }, [scope])
  if (!account) return null

  const actions = [
    { label: t("home.bets"), icon: Gavel, path: "/bets" },
    { label: t("home.shop"), icon: ShoppingCart, path: "/personalization" },
    { label: t("home.admin"), icon: ShieldCheck, path: "/admin" },
    { label: t("home.news"), icon: Newspaper, path: "/news" },
    { label: t("home.proposals"), icon: Lightbulb, path: "/proposals" }
  ]

  async function loadMore() {
    if (loadingMore) return
    setLoadingMore(true)
    const next = await getItems(items.length, 8, scope === "mine")
    setItems(current => [...current, ...next])
    setLoadingMore(false)
  }

  return <div className="home-page">
    <header className="home-profile"><MinecraftHead size={47} /><div className="home-name"><AccountName account={account} compact /><small><span className="online-dot" />{t("home.online")}</small></div><button className="icon-button" onClick={() => navigate("/notifications")}><Bell size={20} /></button></header>
    <section className="reward-card"><div><Flame size={18} /><strong>{account.streak} {t("home.days")}</strong></div><button onClick={() => console.info("Daily reward") }><Gift size={17} />{t("home.reward")}</button></section>
    <section className="balance-grid"><div><Coins size={20} /><span><strong>{account.coins.toLocaleString()}</strong><small>{t("home.coins")}</small></span></div><div><Crown size={20} /><span><strong>{account.primes.toLocaleString()}</strong><small>{t("home.primes")}</small></span></div></section>
    <section className="quick-card">
      <div className="quick-carousel" ref={carousel} onScroll={event => { const el = event.currentTarget; setProgress(el.scrollLeft / Math.max(1, el.scrollWidth - el.clientWidth)) }}>
        {[...actions, ...actions].map((action, index) => <button key={`${action.label}-${index}`} onClick={() => navigate(action.path)}><span><action.icon size={20} /></span><small>{action.label}</small></button>)}
      </div>
      <div className="orbit-track"><i style={{ transform: `translateX(${progress * 520}%)` }} /></div>
    </section>
    <section className={`latest-sheet ${sheetOpen ? "expanded" : ""}`} onTouchStart={event => { touchStart.current = event.touches[0].clientY }} onTouchEnd={event => { const delta = touchStart.current - event.changedTouches[0].clientY; if (delta > 35) setSheetOpen(true); if (delta < -35) setSheetOpen(false) }}>
      <button className="sheet-handle" aria-label={t("home.swipe")} onClick={() => setSheetOpen(value => !value)}><span /></button>
      <div className="latest-header"><h2>{t("home.latest")}</h2><Segmented<"all" | "mine"> value={scope} onChange={setScope} items={[{ value: "all", label: t("common.all") }, { value: "mine", label: t("common.myAccount") }]} /></div>
      <div className="sheet-list" onScroll={event => { const el = event.currentTarget; if (sheetOpen && el.scrollTop + el.clientHeight > el.scrollHeight - 80) void loadMore() }}><ItemList items={items} />{loadingMore && <div className="loading-line" />}</div>
    </section>
    {sheetOpen && <button className="sheet-backdrop" aria-label={t("common.cancel")} onClick={() => setSheetOpen(false)} />}
  </div>
}
