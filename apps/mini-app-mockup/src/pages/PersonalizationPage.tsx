import { AnimatePresence, motion } from "framer-motion"
import { Check, Sparkles } from "lucide-react"
import { useState } from "react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import { mockPacks } from "../mockData"

export function PersonalizationPage({
  onBack,
  onNotify,
}: {
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [activeId, setActiveId] = useState("default")
  const [ownedIds, setOwnedIds] = useState(() => new Set(mockPacks.filter((pack) => pack.owned).map((pack) => pack.id)))

  const usePack = (id: string, price: number) => {
    if (!ownedIds.has(id)) {
      setOwnedIds((current) => new Set([...current, id]))
      onNotify(`Пак придбано · −${price} Prime`)
      return
    }

    setActiveId(id)
    onNotify("Notification pack активовано")
  }

  return (
    <PageFrame page="personalization">
      <PageHeader title="Персоналізація" eyebrow="USER" onBack={onBack} />

      <section className="detail-context-row">
        <ScopeBadge scope="user" />
        <span>knalis</span>
      </section>

      <section className="personalization-preview">
        <div className="personalization-preview-copy">
          <span>Активний pack</span>
          <strong>{mockPacks.find((pack) => pack.id === activeId)?.name ?? "Default"}</strong>
        </div>
        <motion.div
          key={activeId}
          className={`pack-preview pack-preview--${mockPacks.find((pack) => pack.id === activeId)?.preview ?? "sage"}`}
          initial={{ opacity: 0, scale: 0.96 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.24 }}
        >
          <span />
          <span />
          <span />
        </motion.div>
      </section>

      <section className="detail-section">
        <div className="detail-section-heading">
          <h2>Notification packs</h2>
        </div>

        <div className="pack-list">
          {mockPacks.map((pack, index) => {
            const active = pack.id === activeId
            const owned = ownedIds.has(pack.id)

            return (
              <motion.article
                key={pack.id}
                className={`pack-row ${active ? "pack-row--active" : ""}`}
                initial={{ opacity: 0, y: 7 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.03, duration: 0.2 }}
              >
                <div className={`pack-swatch pack-swatch--${pack.preview}`}>
                  <span />
                  <span />
                </div>

                <div className="pack-copy">
                  <strong>{pack.name}</strong>
                  <span>{pack.author}</span>
                </div>

                <AnimatePresence mode="wait" initial={false}>
                  {active ? (
                    <motion.span
                      key="active"
                      className="pack-state"
                      initial={{ opacity: 0, scale: 0.9 }}
                      animate={{ opacity: 1, scale: 1 }}
                    >
                      <Check size={14} strokeWidth={2} />
                    </motion.span>
                  ) : (
                    <motion.button
                      key="action"
                      className="pack-action"
                      onClick={() => usePack(pack.id, pack.price)}
                      whileTap={{ scale: 0.96 }}
                    >
                      {owned ? (
                        "Активувати"
                      ) : (
                        <>
                          <CurrencyMark currency="prime" />
                          {pack.price}
                        </>
                      )}
                    </motion.button>
                  )}
                </AnimatePresence>
              </motion.article>
            )
          })}
        </div>
      </section>

      <section className="detail-section">
        <div className="info-card info-card--quiet">
          <Sparkles size={18} strokeWidth={1.7} />
          <div>
            <strong>Snapshot</strong>
            <span>Для платного alert фіксується pack, активний у момент покупки.</span>
          </div>
        </div>
      </section>
    </PageFrame>
  )
}
