import { motion } from "framer-motion"
import { Glyph } from "../shared/MnemosIcon"

type Props = {
  requested?: string
  onHome: () => void
}

export function NotFoundFeature({ requested, onHome }: Props) {
  return (
    <div className="screen not-found-r5-screen">
      <section className="not-found-r5-hero">
        <span className="not-found-r5-code" aria-hidden="true">
          <i>4</i>
          <b>0</b>
          <i>4</i>
        </span>

        <motion.img
          className="not-found-r5-mascot"
          src="/ui-illustrations/mnemos-mascot-404.svg"
          alt="Збентежений маскот Mnemos з картою"
          initial={{ opacity: 0, y: 8, scale: 0.97 }}
          animate={{ opacity: 1, y: [0, -2, 0], scale: 1 }}
          transition={{
            opacity: { duration: 0.3 },
            scale: { duration: 0.35 },
            y: { duration: 4.8, repeat: Infinity, ease: "easeInOut" },
          }}
        />

        <div className="not-found-r5-copy">
          <small>МАРШРУТ ЗАГУБИВСЯ</small>
          <h1>Тут нічого немає</h1>
          <p>
            Схоже, навіть кіт Mnemos не розуміє, куди веде ця сторінка.
          </p>
          {requested ? <code>{requested}</code> : null}
        </div>

        <motion.button
          className="not-found-r5-home"
          whileTap={{ scale: 0.97 }}
          onClick={onHome}
        >
          <Glyph name="home" size={18} />
          На головну
        </motion.button>

        <span className="not-found-r5-signal" aria-hidden="true"><i /><i /><i /><i /></span>
      </section>
    </div>
  )
}
