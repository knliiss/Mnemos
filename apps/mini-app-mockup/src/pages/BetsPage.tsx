import { motion } from "framer-motion"
import { ChevronRight, Clock3, Target } from "lucide-react"
import { useState } from "react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import { mockBets } from "../mockData"
import type { MockAccount, Page } from "../types"

export function BetsPage({
  activeAccount,
  onBack,
  onNavigate,
}: {
  activeAccount: MockAccount
  onBack: () => void
  onNavigate: (page: Page) => void
}) {
  const [kind, setKind] = useState<"event" | "race">("event")
  const visibleBets = mockBets.filter((bet) => bet.kind === kind)

  return (
    <PageFrame page="bets">
      <PageHeader title="Ставки" eyebrow="BET" onBack={onBack} />

      <section className="detail-context-row detail-context-row--between">
        <div>
          <ScopeBadge scope="account" />
          <span>{activeAccount.nickname}</span>
        </div>
        <span className="inline-balance">
          <CurrencyMark currency="coin" />
          {activeAccount.coinBalance.toLocaleString("uk-UA")}
        </span>
      </section>

      <div className="segmented-control segmented-control--detail">
        <button className={kind === "event" ? "segmented-control__active" : ""} onClick={() => setKind("event")}>
          Події
        </button>
        <button className={kind === "race" ? "segmented-control__active" : ""} onClick={() => setKind("race")}>
          Item race
        </button>
      </div>

      <section className="bet-list">
        {visibleBets.map((bet, index) => (
          <motion.button
            key={bet.id}
            className="bet-card"
            onClick={() => onNavigate("bet-detail")}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.04, duration: 0.22 }}
            whileTap={{ scale: 0.99 }}
          >
            <div className="bet-card-topline">
              <span className="bet-kind"><Target size={13} strokeWidth={1.8} />{bet.kind === "event" ? "EVENT" : "RACE"}</span>
              <span className="bet-close"><Clock3 size={13} strokeWidth={1.7} />{bet.closesAt}</span>
            </div>

            <div className="bet-card-title">
              <strong>{bet.title}</strong>
              <span>{bet.subtitle}</span>
            </div>

            {bet.kind === "event" && (
              <div className="bet-ratio">
                <div className="bet-ratio-bar">
                  <span style={{ width: `${bet.yesPercent}%` }} />
                </div>
                <div>
                  <span>YES {bet.yesPercent}%</span>
                  <span>NO {bet.noPercent}%</span>
                </div>
              </div>
            )}

            <div className="bet-card-bottom">
              <span className="inline-balance"><CurrencyMark currency="coin" />{bet.pot.toLocaleString("uk-UA")}</span>
              <ChevronRight size={17} strokeWidth={1.7} />
            </div>
          </motion.button>
        ))}
      </section>
    </PageFrame>
  )
}
