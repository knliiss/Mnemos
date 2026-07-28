import { CircleDotDashed, Coins } from "lucide-react"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { PageHeader } from "@/shared/ui/PageHeader"
import { Segmented } from "@/shared/ui/Segmented"
import { getBets } from "@/shared/api/contentService"
import type { Bet } from "@/shared/types/domain"

export function BetsPage() {
  const { t } = useTranslation()
  const [mode, setMode] = useState<"event" | "race">("event")
  const [bets, setBets] = useState<Bet[]>([])
  useEffect(() => { void getBets().then(setBets) }, [])
  const visible = bets.filter(bet => bet.mode === mode)
  return <div className="subpage"><PageHeader title={t("bets.title")} /><Segmented<"event" | "race"> value={mode} onChange={setMode} items={[{ value: "event", label: t("bets.events") }, { value: "race", label: t("bets.race") }]} /><h2 className="section-title">{t("bets.active")}</h2><div className="bet-list">{visible.map(bet => <article key={bet.id}><div className="bet-icon"><CircleDotDashed /></div><div className="bet-content"><strong>{bet.title}</strong><small>{bet.endsAt}</small>{bet.chance && <span>Шанс: {bet.chance}%</span>}<div><span>{t("bets.pool")}</span><b><Coins size={13} />{bet.pool.toLocaleString()}</b></div><div><span>{t("bets.participants")}</span><b>{bet.participants}</b></div><button onClick={() => console.info("Bet action", bet.id)}>{mode === "event" ? t("bets.place") : t("bets.enter")}</button></div></article>)}</div></div>
}
