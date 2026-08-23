import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { Glyph } from "../shared/MnemosIcon"
import "./not-found.css"

type Props = {
  requested?: string
  onHome: () => void
}

export function NotFoundFeature({ requested, onHome }: Props) {
  const { t } = useTranslation()

  return (
    <div className="screen not-found-screen">
      <section className="not-found-hero">
        <span className="not-found-code" aria-hidden="true">
          <i>4</i>
          <b>0</b>
          <i>4</i>
        </span>

        <img
          className="not-found-mascot"
          src="/ui-illustrations/mnemos-mascot-404.svg"
          alt={t("notFound.mascotAlt")}
        />

        <div className="not-found-copy">
          <small>{t("notFound.eyebrow")}</small>
          <h1>{t("notFound.title")}</h1>
          <p>{t("notFound.description")}</p>
          {requested ? <code>{requested}</code> : null}
        </div>

        <motion.button
          type="button"
          className="not-found-home"
          whileTap={{ scale: 0.97 }}
          onClick={onHome}
        >
          <Glyph name="home" size={18} />
          {t("notFound.home")}
        </motion.button>

        <span className="not-found-signal" aria-hidden="true">
          <i />
          <i />
          <i />
          <i />
        </span>
      </section>
    </div>
  )
}
