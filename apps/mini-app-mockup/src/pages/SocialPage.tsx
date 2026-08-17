import { motion } from "framer-motion"
import {
  MessageCircle,
  Send,
  ThumbsDown,
  ThumbsUp,
  Users,
  Zap,
} from "lucide-react"
import { useState } from "react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import type { MockAccount } from "../types"

const comments = [
  { id: "c1", author: "north", body: "Швидко відповідає, нормальний профіль.", time: "2 год", likes: 7 },
  { id: "c2", author: "vex", body: "Знайшли через item drop — усе ок.", time: "вчора", likes: 4 },
  { id: "c3", author: "rei", body: "Плюс за стабільні алерти.", time: "15 серп.", likes: 3 },
]

export function SocialPage({
  activeAccount,
  onBack,
  onNotify,
}: {
  activeAccount: MockAccount
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [mode, setMode] = useState<"profile" | "alert">("profile")
  const [liked, setLiked] = useState<"up" | "down" | null>(null)

  return (
    <PageFrame page="social">
      <PageHeader title="Соціальне" eyebrow="ACCOUNT" onBack={onBack} />

      <section className="detail-context-row">
        <ScopeBadge scope="account" />
        <span>{activeAccount.nickname}</span>
      </section>

      <div className="segmented-control segmented-control--wide">
        <button
          className={mode === "profile" ? "segmented-control__active" : ""}
          onClick={() => setMode("profile")}
        >
          Профіль
        </button>
        <button
          className={mode === "alert" ? "segmented-control__active" : ""}
          onClick={() => setMode("alert")}
        >
          Drop alert
        </button>
      </div>

      {mode === "profile" ? (
        <>
          <section className="social-reaction-bar">
            <motion.button
              className={liked === "up" ? "social-reaction social-reaction--active" : "social-reaction"}
              onClick={() => setLiked((value) => value === "up" ? null : "up")}
              whileTap={{ scale: 0.95 }}
            >
              <ThumbsUp size={17} strokeWidth={1.75} />
              <strong>{128 + (liked === "up" ? 1 : 0)}</strong>
            </motion.button>
            <motion.button
              className={liked === "down" ? "social-reaction social-reaction--active" : "social-reaction"}
              onClick={() => setLiked((value) => value === "down" ? null : "down")}
              whileTap={{ scale: 0.95 }}
            >
              <ThumbsDown size={17} strokeWidth={1.75} />
              <strong>{9 + (liked === "down" ? 1 : 0)}</strong>
            </motion.button>
            <span className="social-comment-count">
              <MessageCircle size={16} strokeWidth={1.7} />
              24
            </span>
          </section>

          <section className="comment-list">
            {comments.map((comment, index) => (
              <motion.article
                key={comment.id}
                className="comment-row"
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.035, duration: 0.2 }}
              >
                <span className="comment-avatar">{comment.author[0].toUpperCase()}</span>
                <div>
                  <div className="comment-meta">
                    <strong>{comment.author}</strong>
                    <span>{comment.time}</span>
                  </div>
                  <p>{comment.body}</p>
                  <button>
                    <ThumbsUp size={13} strokeWidth={1.7} />
                    {comment.likes}
                  </button>
                </div>
              </motion.article>
            ))}
          </section>

          <div className="comment-composer">
            <input placeholder="Коментар" />
            <motion.button
              onClick={() => onNotify("Mock: коментар додано")}
              whileTap={{ scale: 0.92 }}
              aria-label="Надіслати"
            >
              <Send size={17} strokeWidth={1.8} />
            </motion.button>
          </div>
        </>
      ) : (
        <section className="alert-composer">
          <div className="alert-preview-card">
            <span className="alert-preview-icon">
              <Zap size={20} strokeWidth={1.75} />
            </span>
            <div>
              <span>knalis вибив</span>
              <strong>Secret item</strong>
              <small>Default notification pack</small>
            </div>
          </div>

          <div className="alert-reach">
            <Users size={17} strokeWidth={1.7} />
            <div>
              <strong>Глобальна доставка</strong>
              <span>Онлайн одразу, офлайн — при наступному підключенні.</span>
            </div>
          </div>

          <div className="alert-snapshot-row">
            <div>
              <ScopeBadge scope="user" />
              <span>Default</span>
            </div>
            <span>snapshot</span>
          </div>

          <motion.button
            className="primary-action primary-action--prime"
            onClick={() => onNotify("Drop alert придбано · −25 Prime")}
            whileTap={{ scale: 0.985 }}
          >
            <CurrencyMark currency="prime" />
            <span>25</span>
            <span className="primary-action-spacer" />
            <span>Надіслати</span>
          </motion.button>
        </section>
      )}
    </PageFrame>
  )
}
