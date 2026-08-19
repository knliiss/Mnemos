import { motion } from "framer-motion"
import type { ReactNode } from "react"
import { CurrencyMark, Glyph, type GlyphName } from "./MnemosIcon"

export function PageHeader({
  title,
  eyebrow,
  back,
  actions,
}: {
  title: string
  eyebrow?: string
  back?: () => void
  actions?: ReactNode
}) {
  return (
    <header className="page-header">
      <div className="page-header__lead">
        {back ? (
          <button className="icon-button icon-button--back" onClick={back} aria-label="Назад">
            <Glyph name="back" size={20} />
          </button>
        ) : null}
        <div className="page-header__copy">
          {eyebrow ? <span className="eyebrow">{eyebrow}</span> : null}
          <h1>{title}</h1>
        </div>
      </div>
      {actions ? <div className="page-header__actions">{actions}</div> : null}
    </header>
  )
}

export function IconButton({
  icon,
  label,
  onClick,
  active,
}: {
  icon: GlyphName
  label: string
  onClick?: () => void
  active?: boolean
}) {
  return (
    <button
      className={`icon-button ${active ? "is-active" : ""}`}
      onClick={onClick}
      aria-label={label}
      title={label}
    >
      <Glyph name={icon} size={19} />
    </button>
  )
}

export function CurrencyValue({
  currency,
  value,
  compact = false,
}: {
  currency: "COIN" | "PRIME"
  value: number
  compact?: boolean
}) {
  return (
    <span className={`currency-value ${compact ? "currency-value--compact" : ""}`}>
      <CurrencyMark currency={currency} size={compact ? 15 : 18} />
      <span>{value.toLocaleString("uk-UA")}</span>
    </span>
  )
}

export function Section({
  title,
  action,
  children,
  className,
}: {
  title?: string
  action?: ReactNode
  children: ReactNode
  className?: string
}) {
  return (
    <section className={`section ${className ?? ""}`}>
      {title || action ? (
        <div className="section__head">
          {title ? <h2>{title}</h2> : <span />}
          {action}
        </div>
      ) : null}
      {children}
    </section>
  )
}

export function Row({
  icon,
  title,
  meta,
  value,
  onClick,
  danger,
  children,
}: {
  icon?: GlyphName
  title: string
  meta?: string
  value?: ReactNode
  onClick?: () => void
  danger?: boolean
  children?: ReactNode
}) {
  const content = (
    <>
      {icon ? (
        <span className="row__icon">
          <Glyph name={icon} size={19} />
        </span>
      ) : null}
      <span className="row__copy">
        <strong>{title}</strong>
        {meta ? <small>{meta}</small> : null}
      </span>
      {children}
      {value ? <span className="row__value">{value}</span> : null}
      {onClick ? <span className="row__chevron">›</span> : null}
    </>
  )
  return onClick ? (
    <button className={`row row--button ${danger ? "row--danger" : ""}`} onClick={onClick}>
      {content}
    </button>
  ) : (
    <div className={`row ${danger ? "row--danger" : ""}`}>{content}</div>
  )
}

export function Segmented<T extends string>({
  value,
  options,
  onChange,
  label,
}: {
  value: T
  options: readonly T[]
  onChange: (value: T) => void
  label?: (value: T) => string
}) {
  return (
    <div className="segmented" role="group">
      {options.map((option) => (
        <button
          key={option}
          className={value === option ? "is-selected" : ""}
          onClick={() => onChange(option)}
          type="button"
        >
          {label ? label(option) : option}
        </button>
      ))}
    </div>
  )
}

export function ChoiceGrid<T extends string>({
  value,
  options,
  onChange,
  labels,
}: {
  value: T
  options: readonly T[]
  onChange: (value: T) => void
  labels?: Partial<Record<T, string>>
}) {
  return (
    <div className="choice-grid">
      {options.map((option) => (
        <button
          key={option}
          type="button"
          onClick={() => onChange(option)}
          className={value === option ? "is-selected" : ""}
        >
          <span className="choice-grid__mark">
            {value === option ? <Glyph name="check" size={14} /> : null}
          </span>
          <span>{labels?.[option] ?? option}</span>
        </button>
      ))}
    </div>
  )
}

export function Field({
  label,
  hint,
  children,
}: {
  label: string
  hint?: string
  children: ReactNode
}) {
  return (
    <label className="field">
      <span className="field__label">{label}</span>
      {children}
      {hint ? <small>{hint}</small> : null}
    </label>
  )
}

export function Toggle({
  checked,
  onChange,
  label,
}: {
  checked: boolean
  onChange: (checked: boolean) => void
  label: string
}) {
  return (
    <button
      className={`toggle ${checked ? "is-on" : ""}`}
      onClick={() => onChange(!checked)}
      type="button"
      aria-label={label}
      aria-pressed={checked}
    >
      <span />
    </button>
  )
}

export function BottomSheet({
  title,
  children,
  onClose,
}: {
  title: string
  children: ReactNode
  onClose: () => void
}) {
  return (
    <motion.div
      className="sheet-backdrop"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      onClick={onClose}
    >
      <motion.section
        className="sheet"
        initial={{ y: 32, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        exit={{ y: 32, opacity: 0 }}
        transition={{ type: "spring", stiffness: 390, damping: 36 }}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="sheet__grip" />
        <div className="sheet__head">
          <h2>{title}</h2>
          <IconButton icon="close" label="Закрити" onClick={onClose} />
        </div>
        {children}
      </motion.section>
    </motion.div>
  )
}

export function StatusDot({ tone = "neutral" }: { tone?: "live" | "neutral" | "warning" }) {
  return <span className={`status-dot status-dot--${tone}`} />
}
export function EmptyState({ title, note }: { title: string; note?: string }) {
  return (
    <div className="empty-state">
      <span className="empty-state__mark" />
      <strong>{title}</strong>
      {note ? <small>{note}</small> : null}
    </div>
  )
}
