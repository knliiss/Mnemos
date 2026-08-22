import { AnimatePresence, motion } from "framer-motion"
import { useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { useMnemosData } from "../data/MnemosDataProvider"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"

type RewardResult = {
  coin: number
  prime: number
}

type RewardParticleKind = "spark" | "orb" | "shard" | "coin"

type RewardParticle = {
  kind: RewardParticleKind
  tone: "coin" | "prime" | "lime"
  x: number
  y: number
  rotate: number
  delay: number
  duration: number
}

const rewardParticles: RewardParticle[] = [
  { kind: "coin", tone: "coin", x: -78, y: -44, rotate: -42, delay: 0.02, duration: 1.68 },
  { kind: "spark", tone: "lime", x: -48, y: -72, rotate: 38, delay: 0.08, duration: 1.55 },
  { kind: "shard", tone: "prime", x: -14, y: -82, rotate: -62, delay: 0.04, duration: 1.85 },
  { kind: "orb", tone: "coin", x: 34, y: -76, rotate: 24, delay: 0.11, duration: 1.72 },
  { kind: "spark", tone: "prime", x: 70, y: -48, rotate: 68, delay: 0.06, duration: 1.64 },
  { kind: "coin", tone: "coin", x: 82, y: -12, rotate: 96, delay: 0.14, duration: 1.9 },
  { kind: "shard", tone: "lime", x: 52, y: 18, rotate: 116, delay: 0.1, duration: 1.62 },
  { kind: "orb", tone: "prime", x: -54, y: 16, rotate: -108, delay: 0.13, duration: 1.78 },
]

export function AccountDailyReward() {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [rewardResult, setRewardResult] = useState<RewardResult | null>(null)
  const [rewardBusy, setRewardBusy] = useState(false)
  const rewardBusyRef = useRef(false)

  const reward = data.dailyReward
  const claimAvailable = Boolean(reward && !reward.claimedToday && !reward.recoverable)
  const restoreAvailable = Boolean(
    reward && !reward.claimedToday && reward.recoverable && reward.restoresRemaining > 0,
  )

  const claimDaily = async () => {
    if (!claimAvailable || rewardBusyRef.current) {
      return
    }

    rewardBusyRef.current = true
    setRewardBusy(true)

    try {
      const result = await data.claimReward()

      if (result) {
        setRewardResult(result)
      }
    } catch {
      // Backend errors are already routed through the global error handler.
    } finally {
      rewardBusyRef.current = false
      setRewardBusy(false)
    }
  }

  const restoreDaily = async () => {
    if (!restoreAvailable || rewardBusyRef.current) {
      return
    }

    rewardBusyRef.current = true
    setRewardBusy(true)

    try {
      await data.restoreReward()
    } catch {
      // Backend errors are already routed through the global error handler.
    } finally {
      rewardBusyRef.current = false
      setRewardBusy(false)
    }
  }

  return (
    <section className={`account-home-reward ${reward?.claimedToday ? "is-claimed" : ""}`}>
      <span className="account-home-reward__flame" aria-hidden="true">
        <i />
        <motion.img
          src="/ui-icons/flame.png"
          alt=""
          animate={{
            y: [0, -2, 1, -1, 0],
            scale: [1, 1.045, 0.985, 1.025, 1],
            rotate: [0, -1.5, 1.3, -0.7, 0],
          }}
          transition={{ duration: 1.45, repeat: Infinity, ease: "easeInOut" }}
        />
      </span>

      <span className="account-home-reward__copy">
        <small>{t("accountHome.dailyReward")}</small>

        <span className="account-home-reward__streak">
          <strong>{reward?.currentStreak || reward?.recoverableStreak || 0}</strong>
          <span>{t("accountHome.streakDays")}</span>
        </span>

        {reward && !rewardResult ? (
          <span className="account-home-reward__range">
            <span>
              <CurrencyMark currency="COIN" size={14} />
              {reward.nextCoinMin}–{reward.nextCoinMax}
            </span>
            <span>
              <CurrencyMark currency="PRIME" size={13} />
              {t("accountHome.primeChance", {
                chance: Math.round(reward.primeChance * 100),
              })}
            </span>
          </span>
        ) : null}
      </span>

      {claimAvailable ? (
        <motion.button
          type="button"
          className="account-home-reward__claim"
          whileTap={{ scale: 0.96 }}
          disabled={rewardBusy}
          onClick={() => void claimDaily()}
          aria-label={t("accountHome.claimAria")}
        >
          <motion.img
            src="/ui-illustrations/daily-claim.svg"
            alt=""
            aria-hidden="true"
            animate={{ y: [0, -2.5, 0] }}
            transition={{ duration: 1.9, repeat: Infinity, ease: "easeInOut" }}
          />
          <span>{t("accountHome.claim")}</span>
        </motion.button>
      ) : restoreAvailable ? (
        <motion.button
          type="button"
          className="is-restore"
          whileTap={{ scale: 0.96 }}
          disabled={rewardBusy}
          onClick={() => void restoreDaily()}
        >
          <Glyph name="swap" size={14} />
          {t("accountHome.restore")}
          <small>{reward?.restoresRemaining}×</small>
        </motion.button>
      ) : null}

      <AnimatePresence>
        {rewardResult ? (
          <>
            <span className="account-home-reward-particles" aria-hidden="true">
              {rewardParticles.map((particle, index) => (
                <motion.span
                  className={`reward-particle reward-particle--${particle.kind} is-${particle.tone}`}
                  key={`${particle.kind}-${particle.tone}-${index}`}
                  initial={{ opacity: 0, x: 0, y: 0, scale: 0.35, rotate: 0 }}
                  animate={{
                    opacity: [0, 0.96, 0.72, 0],
                    x: [0, particle.x * 0.24, particle.x * 0.68, particle.x],
                    y: [0, particle.y * 0.2, particle.y * 0.62, particle.y],
                    scale: [0.35, 1, 0.82, 0.18],
                    rotate: [0, particle.rotate * 0.28, particle.rotate * 0.7, particle.rotate],
                  }}
                  transition={{
                    duration: particle.duration,
                    delay: particle.delay,
                    ease: [0.2, 0.72, 0.22, 1],
                    times: [0, 0.2, 0.7, 1],
                  }}
                />
              ))}
            </span>

            <motion.div
              className="account-home-reward-result"
              initial={{ opacity: 0, scale: 0.94, y: 7 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              transition={{ duration: 0.42, ease: [0.22, 1, 0.36, 1] }}
            >
              <span>
                <small>{t("accountHome.received")}</small>
                <strong>
                  <CurrencyMark currency="COIN" size={17} />+{rewardResult.coin}
                </strong>
              </span>

              <span className={rewardResult.prime ? "is-prime" : "is-empty"}>
                <small>PRIME</small>
                <strong>
                  <CurrencyMark currency="PRIME" size={15} />
                  {rewardResult.prime ? `+${rewardResult.prime}` : t("accountHome.primeNotDropped")}
                </strong>
              </span>
            </motion.div>
          </>
        ) : null}
      </AnimatePresence>
    </section>
  )
}
