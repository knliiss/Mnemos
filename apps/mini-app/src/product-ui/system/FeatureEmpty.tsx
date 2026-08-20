import type { ReactNode } from "react"
import { Glyph, type GlyphName } from "../shared/MnemosIcon"
import "./system.css"

type Props = {
  icon: GlyphName
  title: string
  copy?: string
  children?: ReactNode
}

export function FeatureEmpty({ icon, title, copy, children }: Props) {
  return (
    <div className="feature-empty">
      <Glyph name={icon} size={24} />
      <strong>{title}</strong>
      {copy ? <small>{copy}</small> : null}
      {children}
    </div>
  )
}
