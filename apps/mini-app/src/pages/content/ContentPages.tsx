import { useEffect, useState } from "react"
import { PageHeader } from "@/shared/ui/PageHeader"
import { getNews, getNotifications, getProposals } from "@/shared/api/contentService"
import type { NewsItem, Notification, Proposal } from "@/shared/types/domain"

export function NewsPage() { const [items, setItems] = useState<NewsItem[]>([]); useEffect(() => { void getNews().then(setItems) }, []); return <Content title="Новости" items={items.map(x => ({ title: x.title, body: x.summary, meta: x.createdAt }))} /> }
export function ProposalsPage() { const [items, setItems] = useState<Proposal[]>([]); useEffect(() => { void getProposals().then(setItems) }, []); return <Content title="Предложения" items={items.map(x => ({ title: x.title, body: x.description, meta: `${x.author} · ${x.votes} голосов` }))} /> }
export function NotificationsPage() { const [items, setItems] = useState<Notification[]>([]); useEffect(() => { void getNotifications().then(setItems) }, []); return <Content title="Уведомления" items={items.map(x => ({ title: x.title, body: x.body, meta: x.read ? "Прочитано" : "Новое" }))} /> }
export function AdminPage() { return <Content title="Админ" items={[{ title: "Управление аккаунтами", body: "Доступно пользователям с соответствующим permission.", meta: "ACCOUNT_MANAGE" }, { title: "Модерация комментариев", body: "Инструменты автора и администратора.", meta: "COMMENT_MODERATE" }]} /> }
export function PersonalizationPage() { return <Content title="Персонализация" items={[{ title: "{-A-}", body: "Префикс", meta: "500 праймов" }, { title: "{ }", body: "Префикс", meta: "750 праймов" }, { title: "<ICE>", body: "Префикс", meta: "1 000 праймов" }, { title: "{DRAGON}", body: "Префикс", meta: "1 200 праймов" }]} /> }
function Content({ title, items }: { title: string; items: { title: string; body: string; meta: string }[] }) { return <div className="subpage"><PageHeader title={title} /><div className="content-cards">{items.map((item, index) => <article key={`${item.title}-${index}`}><strong>{item.title}</strong><p>{item.body}</p><small>{item.meta}</small></article>)}</div></div> }
