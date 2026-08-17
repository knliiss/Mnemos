import { AnimatePresence, motion } from "framer-motion"
import { ChevronRight, Newspaper } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"

const news = [
  {
    id: "news-1",
    tag: "UPDATE",
    title: "Mnemos Mini App",
    body: "Новий мобільний інтерфейс, профілі та швидша навігація між сервісами.",
    details: "Інтерфейс розділяє user та account context, підтримує компактну навігацію, safe-area Telegram Mini App і плавні переходи між усіма основними сценаріями.",
    date: "17 серп.",
  },
  {
    id: "news-2",
    tag: "EVENTS",
    title: "Realtime події",
    body: "Події та collector presence тепер представлені одним компактним live feed.",
    details: "Dungeon, Global, Raid, Booster та Item drop події зібрані в єдиній часовій стрічці з швидким переходом до деталей та підписки.",
    date: "16 серп.",
  },
  {
    id: "news-3",
    tag: "PERSONAL",
    title: "Notification packs",
    body: "Паки можуть змінювати вигляд сповіщень та snapshot глобального alert-а.",
    details: "Активний pack належить user scope. Для платного drop alert його вигляд фіксується в момент покупки, тому подальша зміна активного pack не змінює вже створений alert.",
    date: "15 серп.",
  },
]

export function NewsPage({ onBack }: { onBack: () => void }) {
  const [expandedId, setExpandedId] = useState<string | null>(news[0].id)

  return (
    <PageFrame page="news">
      <PageHeader
        title="Новини"
        eyebrow="MNEMOS"
        onBack={onBack}
        action={<Newspaper size={18} strokeWidth={1.7} />}
      />

      <section className="news-feed">
        {news.map((item, index) => {
          const expanded = expandedId === item.id

          return (
            <motion.article
              key={item.id}
              className={`${index === 0 ? "news-card news-card--featured" : "news-card"} ${expanded ? "news-card--expanded" : ""}`}
              layout
              transition={{ type: "spring", stiffness: 360, damping: 34 }}
            >
              <div className="news-card-topline">
                <span>{item.tag}</span>
                <small>{item.date}</small>
              </div>
              <h2>{item.title}</h2>
              <p>{item.body}</p>

              <AnimatePresence initial={false}>
                {expanded && (
                  <motion.p
                    className="news-card-details"
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: "auto" }}
                    exit={{ opacity: 0, height: 0 }}
                    transition={{ duration: 0.2, ease: [0.22, 1, 0.36, 1] }}
                  >
                    {item.details}
                  </motion.p>
                )}
              </AnimatePresence>

              <motion.button
                className="news-card-more"
                onClick={() => setExpandedId(expanded ? null : item.id)}
                whileTap={{ scale: 0.98 }}
              >
                <span>{expanded ? "Згорнути" : "Детальніше"}</span>
                <motion.span animate={{ rotate: expanded ? 90 : 0 }}>
                  <ChevronRight size={15} strokeWidth={1.7} />
                </motion.span>
              </motion.button>
            </motion.article>
          )
        })}
      </section>
    </PageFrame>
  )
}
