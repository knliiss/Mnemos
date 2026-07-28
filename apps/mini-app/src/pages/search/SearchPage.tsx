import { ChevronRight, Search } from "lucide-react"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { MinecraftHead } from "@/entities/account/ui/MinecraftHead"
import { searchAccounts } from "@/shared/api/contentService"
import type { Account } from "@/shared/types/domain"

export function SearchPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [query, setQuery] = useState("")
  const [accounts, setAccounts] = useState<Account[]>([])
  useEffect(() => { void searchAccounts(query).then(setAccounts) }, [query])
  return <div><div className="search-box"><Search size={18} /><input value={query} onChange={event => setQuery(event.target.value)} placeholder={t("search.placeholder")} /></div><h2 className="section-title">{t("search.recent")}</h2><div className="account-results">{accounts.map(account => <button key={account.id} onClick={() => navigate(`/account/${account.id}`)}><MinecraftHead size={42} /><span><strong style={{ color: account.personalization.nicknameColor }}>{account.cristalixNickname}</strong><small>{account.role.name}</small></span><i className={account.online ? "online-dot" : "offline-dot"} /><ChevronRight size={18} /></button>)}</div></div>
}
