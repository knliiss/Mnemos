import { motion } from "framer-motion"
import { useLayoutEffect, useRef, type ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { restoreScrollTop } from "../navigation/scrollRestoration"
import { CurrencyMark, Glyph, type GlyphName } from "./MnemosIcon"
import "./primitives.css"

const guideScrollPositions = new Map<string, number>()
let restoreGuideScrollOnNextMount = false

function guideScrollKey(screen: HTMLElement, title: string, eyebrow?: string) {
  return `${screen.className}:${eyebrow ?? ""}:${title}`
}

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
  const { t } = useTranslation()
  const headerRef = useRef<HTMLElement>(null)

  useLayoutEffect(() => {
    const header = headerRef.current
    const guideScreen = header?.closest<HTMLElement>(".guides-screen")

    if (!header || !guideScreen) {
      restoreGuideScrollOnNextMount = false
      return
    }

    const routeStage = header.closest<HTMLElement>(".route-stage")
    if (!routeStage) {
      return
    }

    const key = guideScrollKey(guideScreen, title, eyebrow)
    const restorePreviousPosition = restoreGuideScrollOnNextMount
    const targetTop = restorePreviousPosition ? (guideScrollPositions.get(key) ?? 0) : 0

    if (!restorePreviousPosition) {
      guideScrollPositions.set(key, 0)
    }

    const remember = () => {
      guideScrollPositions.set(key, routeStage.scrollTop)
    }

    routeStage.addEventListener("scroll", remember, { passive: true })

    const cancelRestore = restoreScrollTop(routeStage, targetTop)
    restoreGuideScrollOnNextMount = false

    return () => {
      routeStage.removeEventListener("scroll", remember)
      cancelRestore()
    }
  }, [eyebrow, title])

  const handleBack = () => {
    const guideScreen = headerRef.current?.closest<HTMLElement>(".guides-screen")

    if (guideScreen) {
      restoreGuideScrollOnNextMount = !guideScreen.classList.contains("guides-hub-screen")
    }

    back?.()
  }

  return (
    <header ref={headerRef} className="page-header">
      <div className="page-header__lead">
        {back ? (
          <button
            type="button"
            className="icon-button icon-button--back"
            onClick={handleBack}
            aria-label={t("common.back")}
          >
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

function IconButton({
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
      type="button"
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
  const { i18n } = useTranslation()
  const locale =
    i18n.resolvedLanguage === "en" ? "en-US" : i18n.resolvedLanguage === "ru" ? "ru-RU" : "uk-UA"

  return (
    <span className={`currency-value ${compact ? "currency-value--compact" : ""}`}>
      <CurrencyMark currency={currency} size={compact ? 15 : 18} />
      <span>{value.toLocaleString(locale)}</span>
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

export function BottomSheet({
  title,
  children,
  onClose,
}: {
  title: string
  children: ReactNode
  onClose: () => void
}) {
  const { t } = useTranslation()

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
        role="dialog"
        aria-modal="true"
        aria-label={title}
        initial={{ y: 32, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        exit={{ y: 32, opacity: 0 }}
        transition={{ type: "spring", stiffness: 390, damping: 36 }}
        onClick={(event) => event.stopPropagation()}
      >
        <div className="sheet__grip" />
        <div className="sheet__head">
          <h2>{title}</h2>
          <IconButton icon="close" label={t("common.close")} onClick={onClose} />
        </div>
        {children}
      </motion.section>
    </motion.div>
  )
}
