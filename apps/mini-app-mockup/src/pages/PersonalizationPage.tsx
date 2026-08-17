import { AnimatePresence, motion } from "framer-motion"
import { Check, Lock, Plus, Sparkles, X } from "lucide-react"
import { useState } from "react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import { mockPacks } from "../mockData"
import type { MockPack, Scope } from "../types"

export function PersonalizationPage({
  onBack,
  onNotify,
}: {
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [scope, setScope] = useState<Scope>("user")
  const [packs, setPacks] = useState<MockPack[]>(mockPacks)
  const [creatorOpen, setCreatorOpen] = useState(false)
  const [customPackName, setCustomPackName] = useState("Quiet")

  const usePack = (pack: MockPack) => {
    if (!pack.owned) {
      setPacks((current) => current.map((item) => item.id === pack.id ? { ...item, owned: true } : item))
      onNotify(`${pack.name} придбано`)
      return
    }

    setPacks((current) => current.map((item) => ({ ...item, active: item.scope === pack.scope ? item.id === pack.id : item.active })))
    onNotify(`${pack.name} активовано`)
  }

  const saveCreatorPack = () => {
    const name = customPackName.trim()

    if (!name) {
      onNotify("Вкажи назву пака")
      return
    }

    const existing = packs.some((pack) => pack.id === "creator-private")

    if (existing) {
      setPacks((current) => current.map((pack) => pack.id === "creator-private" ? { ...pack, name } : pack))
    } else {
      setPacks((current) => [
        ...current,
        {
          id: "creator-private",
          name,
          author: "knalis",
          scope: "user",
          price: 0,
          owned: true,
          active: false,
          preview: "paper",
        },
      ])
    }

    setCreatorOpen(false)
    onNotify("Private pack збережено")
  }

  const visiblePacks = packs.filter((pack) => pack.scope === scope)
  const creatorPack = packs.find((pack) => pack.id === "creator-private")

  return (
    <PageFrame page="personalization">
      <PageHeader title="Персоналізація" eyebrow="PERSONAL" onBack={onBack} />

      <div className="segmented-control segmented-control--detail">
        <button className={scope === "user" ? "segmented-control__active" : ""} onClick={() => setScope("user")}>Юзер</button>
        <button className={scope === "account" ? "segmented-control__active" : ""} onClick={() => setScope("account")}>Аккаунт</button>
      </div>

      <section className="personalization-intro">
        <ScopeBadge scope={scope} />
        <p>{scope === "user" ? "Notification packs впливають на сповіщення та глобальні alert-и." : "Візуальні налаштування, прив'язані до активного аккаунта."}</p>
      </section>

      <section className="pack-grid">
        {visiblePacks.map((pack, index) => (
          <motion.article
            key={pack.id}
            className={`pack-card pack-card--${pack.preview} ${pack.active ? "pack-card--active" : ""}`}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.04, duration: 0.22 }}
          >
            <div className="pack-preview">
              <span className="pack-preview-line" />
              <span className="pack-preview-dot" />
              <span className="pack-preview-line pack-preview-line--short" />
            </div>

            <div className="pack-meta">
              <div>
                <strong>{pack.name}</strong>
                <span>{pack.author}</span>
              </div>
              {pack.active && <span className="pack-active-mark"><Check size={13} strokeWidth={2} /></span>}
            </div>

            <motion.button className="pack-action" onClick={() => usePack(pack)} whileTap={{ scale: 0.98 }}>
              {pack.active ? (
                <><Check size={15} strokeWidth={2} /><span>Активний</span></>
              ) : pack.owned ? (
                <><Sparkles size={15} strokeWidth={1.8} /><span>Активувати</span></>
              ) : (
                <><CurrencyMark currency="prime" /><span>{pack.price}</span></>
              )}
            </motion.button>
          </motion.article>
        ))}
      </section>

      {scope === "user" && (
        <section className="creator-card">
          <span className="creator-card-icon"><Lock size={17} strokeWidth={1.7} /></span>
          <div><strong>{creatorPack?.name ?? "Private pack"}</strong><span>{creatorPack ? "Приватний · knalis" : "1 безкоштовний для creator"}</span></div>
          <button onClick={() => setCreatorOpen(true)} aria-label="Редагувати private pack"><Plus size={17} strokeWidth={1.8} /></button>
        </section>
      )}

      <AnimatePresence>
        {creatorOpen && (
          <motion.div className="inline-editor" initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: 8 }}>
            <div className="inline-editor-title">
              <div><span className="eyebrow">CREATOR</span><h2>Private pack</h2></div>
              <button onClick={() => setCreatorOpen(false)} aria-label="Закрити"><X size={17} strokeWidth={1.7} /></button>
            </div>
            <label><span>Назва</span><input value={customPackName} onChange={(event) => setCustomPackName(event.target.value)} maxLength={20} /></label>
            <div className="mini-preview"><span /><span /><span /></div>
            <button className="primary-action primary-action--embedded" onClick={saveCreatorPack}><Check size={16} strokeWidth={2} /><span>Зберегти</span></button>
          </motion.div>
        )}
      </AnimatePresence>
    </PageFrame>
  )
}
