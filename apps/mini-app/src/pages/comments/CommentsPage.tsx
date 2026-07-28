import { Heart, Send } from "lucide-react"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { PageHeader } from "@/shared/ui/PageHeader"
import { getComments } from "@/shared/api/contentService"
import type { Comment } from "@/shared/types/domain"

export function CommentsPage() {
  const { t } = useTranslation()
  const [comments, setComments] = useState<Comment[]>([])
  const [text, setText] = useState("")
  useEffect(() => { void getComments().then(setComments) }, [])
  return <div className="subpage"><PageHeader title={t("comments.title")} /><div className="filter-select">{t("common.all")}</div><div className="comments-list">{comments.map(comment => <article key={comment.id}><div className="letter-avatar">{comment.author[0]}</div><div><strong>{comment.author}</strong><p>{comment.text}</p><time>{comment.createdAt}</time></div><button><Heart size={14} />{comment.likes}</button></article>)}</div><div className="comment-input"><input value={text} onChange={event => setText(event.target.value)} placeholder={t("comments.placeholder")} /><button onClick={() => { console.info("Add comment", text); setText("") }}><Send size={18} /></button></div></div>
}
