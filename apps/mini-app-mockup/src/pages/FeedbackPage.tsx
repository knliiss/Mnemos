import { Check, Send } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"

export function FeedbackPage({
  onBack,
  onNotify,
}: {
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [kind, setKind] = useState<"suggestion" | "complaint">("suggestion")
  const [text, setText] = useState("")
  const [sent, setSent] = useState(false)

  const submit = () => {
    if (!text.trim()) {
      onNotify("Напиши повідомлення")
      return
    }

    setSent(true)
    setText("")
    onNotify(kind === "suggestion" ? "Пропозицію надіслано" : "Скаргу надіслано")
  }

  const selectKind = (nextKind: "suggestion" | "complaint") => {
    setKind(nextKind)
    setSent(false)
  }

  return (
    <PageFrame page="feedback">
      <PageHeader title="Зворотний зв'язок" eyebrow="FEEDBACK" onBack={onBack} />

      <section className="detail-context-row">
        <ScopeBadge scope="user" />
        <span>knalis</span>
      </section>

      <div className="segmented-control segmented-control--detail">
        <button
          className={kind === "suggestion" ? "segmented-control__active" : ""}
          onClick={() => selectKind("suggestion")}
        >
          Пропозиція
        </button>
        <button
          className={kind === "complaint" ? "segmented-control__active" : ""}
          onClick={() => selectKind("complaint")}
        >
          Скарга
        </button>
      </div>

      <section className="feedback-composer">
        <textarea
          value={text}
          maxLength={500}
          onChange={(event) => {
            setText(event.target.value)
            setSent(false)
          }}
          placeholder={kind === "suggestion" ? "Що варто покращити?" : "Опиши проблему"}
          rows={6}
        />
        <div className="feedback-composer-meta">
          <span>{text.length} / 500</span>
          <span>{kind === "suggestion" ? "IDEA" : "REPORT"}</span>
        </div>
      </section>

      <button className={`primary-action ${sent ? "primary-action--done" : ""}`} onClick={submit}>
        {sent ? <Check size={18} strokeWidth={2} /> : <Send size={17} strokeWidth={1.8} />}
        <span>{sent ? "Надіслано" : "Надіслати"}</span>
      </button>

      <section className="detail-section">
        <div className="detail-section-heading">
          <h2>Мої звернення</h2>
          <span className="detail-counter">2</span>
        </div>
        <div className="feedback-history">
          <article>
            <span className="feedback-status feedback-status--done" />
            <div>
              <strong>Пошук предметів</strong>
              <span>Пропозиція · розглянуто</span>
            </div>
            <small>12 серп.</small>
          </article>
          <article>
            <span className="feedback-status" />
            <div>
              <strong>Notification delay</strong>
              <span>Скарга · в роботі</span>
            </div>
            <small>16 серп.</small>
          </article>
        </div>
      </section>
    </PageFrame>
  )
}
