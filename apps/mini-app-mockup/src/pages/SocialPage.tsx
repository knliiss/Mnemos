import { motion } from "framer-motion"
import { Megaphone, MessageCircle, Send, ThumbsDown, ThumbsUp } from "lucide-react"
import { useState } from "react"
import { CristalixHead } from "../components/CristalixHead"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import type { MockAccount } from "../types"

export function SocialPage({
  activeAccount,
  onBack,
  onNotify,
}: {
  activeAccount: MockAccount
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [liked, setLiked] = useState<"up" | "down" | null>(null)
  const [comment, setComment] = useState("")

  const sendComment = () => {
    if (!comment.trim()) {
      return
    }

    setComment("")
    onNotify("Коментар додано")
  }

  return (
    <PageFrame page="social">
      <PageHeader title="Публічний профіль" eyebrow="SOCIAL" onBack={onBack} />

      <section className="social-profile-card">
        <CristalixHead skinUrl={activeAccount.skinUrl} size={72} />
        <div><ScopeBadge scope="account" /><h2>{activeAccount.nickname}</h2><span>{activeAccount.shortId}</span></div>
      </section>

      <section className="reaction-bar">
        <motion.button className={liked === "up" ? "reaction-button reaction-button--active" : "reaction-button"} onClick={() => setLiked(liked === "up" ? null : "up")} whileTap={{ scale: 0.96 }}>
          <ThumbsUp size={17} strokeWidth={1.8} /><span>{liked === "up" ? 129 : 128}</span>
        </motion.button>
        <motion.button className={liked === "down" ? "reaction-button reaction-button--active" : "reaction-button"} onClick={() => setLiked(liked === "down" ? null : "down")} whileTap={{ scale: 0.96 }}>
          <ThumbsDown size={17} strokeWidth={1.8} /><span>{liked === "down" ? 10 : 9}</span>
        </motion.button>
      </section>

      <motion.button className="global-alert-card" onClick={() => onNotify("Global alert придбано · snapshot Default")} whileTap={{ scale: 0.99 }}>
        <span className="global-alert-icon"><Megaphone size={19} strokeWidth={1.8} /></span>
        <div><strong>Global drop alert</strong><span>Покаже твій вибитий дроп усім</span></div>
        <span className="inline-balance"><CurrencyMark currency="prime" />20</span>
      </motion.button>

      <section className="detail-section">
        <div className="detail-section-heading"><h2>Коментарі</h2><span className="detail-counter">24</span></div>
        <div className="comment-list">
          <article><span className="comment-avatar">N</span><div><div><strong>north</strong><span>18 хв</span></div><p>Швидко відповідає, все нормально.</p></div></article>
          <article><span className="comment-avatar">V</span><div><div><strong>vex</strong><span>2 год</span></div><p>Підтверджую дроп.</p></div></article>
          <article><span className="comment-avatar">M</span><div><div><strong>mare</strong><span>вчора</span></div><p>GG.</p></div></article>
        </div>
      </section>

      <div className="comment-composer">
        <MessageCircle size={17} strokeWidth={1.7} />
        <input value={comment} onChange={(event) => setComment(event.target.value)} placeholder="Коментар" />
        <motion.button onClick={sendComment} whileTap={{ scale: 0.9 }} aria-label="Надіслати"><Send size={17} strokeWidth={1.8} /></motion.button>
      </div>
    </PageFrame>
  )
}
