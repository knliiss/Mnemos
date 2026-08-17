import { ChevronRight, Newspaper } from "lucide-react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"

const news = [
  {
    id: "news-1",
    tag: "UPDATE",
    title: "Mnemos Mini App",
    body: "Новий мобільний інтерфейс, профілі та швидша навігація між сервісами.",
    date: "17 серп.",
  },
  {
    id: "news-2",
    tag: "EVENTS",
    title: "Realtime події",
    body: "Події та collector presence тепер представлені одним компактним live feed.",
    date: "16 серп.",
  },
  {
    id: "news-3",
    tag: "PERSONAL",
    title: "Notification packs",
    body: "Паки можуть змінювати вигляд сповіщень та snapshot глобального alert-а.",
    date: "15 серп.",
  },
]

export function NewsPage({ onBack }: { onBack: () => void }) {
  return (
    <PageFrame page="news">
      <PageHeader title="Новини" eyebrow="MNEMOS" onBack={onBack} action={<Newspaper size={18} strokeWidth={1.7} />} />

      <section className="news-feed">
        {news.map((item, index) => (
          <article key={item.id} className={index === 0 ? "news-card news-card--featured" : "news-card"}>
            <div className="news-card-topline"><span>{item.tag}</span><small>{item.date}</small></div>
            <h2>{item.title}</h2>
            <p>{item.body}</p>
            <span className="news-card-more"><span>Детальніше</span><ChevronRight size={15} strokeWidth={1.7} /></span>
          </article>
        ))}
      </section>
    </PageFrame>
  )
}
