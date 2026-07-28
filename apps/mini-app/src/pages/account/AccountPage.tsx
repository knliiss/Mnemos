import { ChevronRight, MessageCircle, Pencil, Sparkles, UserRound, UsersRound } from "lucide-react"
import { useTranslation } from "react-i18next"
import { useNavigate, useParams } from "react-router-dom"
import { useEffect, useState } from "react"
import { useApp } from "@/app/providers/AppProvider"
import { AccountName } from "@/entities/account/ui/AccountName"
import { MinecraftHead } from "@/entities/account/ui/MinecraftHead"
import { getAccount } from "@/shared/api/contentService"
import type { Account } from "@/shared/types/domain"

export function AccountPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { id } = useParams()
  const app = useApp()
  const [external, setExternal] = useState<Account | null>(null)
  useEffect(() => { if (id) void getAccount(id).then(value => setExternal(value ?? null)) }, [id])
  const account = id ? external : app.account
  if (!account) return null
  const own = !id
  const currentAccount = account

  async function leave() {
    if (currentAccount.ownerUserId === app.user?.id) { alert(t("account.transferFirst")); return }
    await app.leaveAccount()
    navigate("/login")
  }

  return <div className="account-page">
    <div className="profile-hero"><MinecraftHead size={74} /><button className="edit-fab" onClick={() => console.info("Edit account")}><Pencil size={17} /></button><AccountName account={account} /><div className="role-badge" style={{ color: account.role.color }}>{account.personalization.badge} {account.role.name}</div><small><span className={account.online ? "online-dot" : "offline-dot"} />{account.online ? t("home.online") : "Offline"}</small></div>
    <div className="stats-row"><div><strong>{account.likes}</strong><small>{t("account.likes")}</small></div><div><strong>{account.members}</strong><small>{t("account.members")}</small></div><div><strong>{account.comments}</strong><small>{t("account.comments")}</small></div></div>
    <section className="panel about-card"><small>{t("account.about")}</small><p>{account.about}</p></section>
    <div className="menu-list">
      <button onClick={() => navigate(`/comments${id ? `/${id}` : ""}`)}><MessageCircle /><span>{t("account.comments")}<small>{account.comments}</small></span><ChevronRight /></button>
      <button onClick={() => console.info("Members", account.id)}><UsersRound /><span>{t("account.members")}<small>{account.members}</small></span><ChevronRight /></button>
      {own && <button onClick={() => navigate("/personalization")}><Sparkles /><span>{t("account.personalization")}<small>{account.personalization.prefix} {account.cristalixNickname} {account.personalization.suffix}</small></span><ChevronRight /></button>}
      {!own && <button onClick={() => console.info("Like", account.id)}><UserRound /><span>Поставить лайк</span><ChevronRight /></button>}
    </div>
    {own && <div className="danger-actions"><button onClick={() => void leave()}>{t("account.leave")}</button><button onClick={() => console.info("Delete account", account.id)}>{t("account.delete")}</button></div>}
  </div>
}
