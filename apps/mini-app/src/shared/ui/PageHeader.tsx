import { ArrowLeft } from "lucide-react"
import { useNavigate } from "react-router-dom"

export function PageHeader({ title }: { title: string }) {
  const navigate = useNavigate()
  return <header className="page-header"><button className="icon-button" onClick={() => navigate(-1)}><ArrowLeft size={20} /></button><h1>{title}</h1></header>
}
