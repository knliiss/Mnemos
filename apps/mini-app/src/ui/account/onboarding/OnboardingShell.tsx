import { motion } from "framer-motion"
import type { ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"
import "./account-onboarding.css"
import "./account-onboarding-layout.css"

type OnboardingMode = "create" | "join"

type OnboardingShellProps = {
  mode: OnboardingMode
  title: string
  eyebrow: string
  description: string
  back: () => void
  children: ReactNode
}

export function OnboardingShell({
  mode,
  title,
  eyebrow,
  description,
  back,
  children,
}: OnboardingShellProps) {
  const { t } = useTranslation()
  const translationKey = mode === "create" ? "accountOnboarding.create" : "accountOnboarding.join"

  return (
    <div className={`screen account-onboarding account-onboarding--${mode}`}>
      <header className="account-onboarding-header">
        <motion.button
          type="button"
          className="account-onboarding-back"
          onClick={back}
          whileTap={{ scale: 0.94 }}
          aria-label={t("accountOnboarding.back")}
        >
          <Glyph name="back" size={22} />
        </motion.button>

        <div className="account-onboarding-heading">
          <small>{eyebrow}</small>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
      </header>

      <motion.section
        className="account-onboarding-hero"
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.34, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className={`account-onboarding-hero__art account-onboarding-hero-art is-${mode}`}>
          <img
            src={
              mode === "create"
                ? "/ui-illustrations/mnemos-mascot-search.svg"
                : "/ui-illustrations/mnemos-mascot-cat.svg"
            }
            alt=""
          />

          {mode === "join" ? (
            <span className="account-onboarding-ticket" aria-hidden="true">
              CODE
            </span>
          ) : null}
        </div>

        <div className="account-onboarding-hero__copy">
          <small>{t(`${translationKey}.mascotEyebrow`)}</small>
          <strong>{t(`${translationKey}.mascotTitle`)}</strong>
          <p>{t(`${translationKey}.mascotCopy`)}</p>

          <span className="account-onboarding-route" aria-hidden="true">
            <i />
            <b>{mode === "create" ? "C7X" : "CODE"}</b>
            <em />
            <b>MNEMOS</b>
          </span>
        </div>
      </motion.section>

      {children}
    </div>
  )
}

export function ButtonSignal({ busy }: { busy: boolean }) {
  return (
    <span
      className={`account-onboarding-primary__signal ${busy ? "is-busy" : ""}`}
      aria-hidden="true"
    >
      <i />
      <i />
      <i />
    </span>
  )
}

export function OnboardingFact({
  mark,
  title,
  copy,
}: {
  mark: string
  title: string
  copy: string
}) {
  return (
    <article className="account-onboarding-fact">
      <span>{mark}</span>
      <div>
        <strong>{title}</strong>
        <small>{copy}</small>
      </div>
    </article>
  )
}
