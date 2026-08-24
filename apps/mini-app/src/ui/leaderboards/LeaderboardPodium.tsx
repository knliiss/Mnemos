import { motion } from "framer-motion"
import { Glyph } from "../shared/MnemosIcon"
import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"

const podiumOrder = [2, 1, 3]

function PodiumCrown() {
  return (
    <span className="leaderboard-podium__crown" aria-hidden="true">
      <Glyph name="crown" size={34} />
    </span>
  )
}

function PodiumCardContent({ entry }: { entry: LeaderboardDisplayEntry | undefined }) {
  if (!entry) {
    return <span className="leaderboard-podium__empty-mark">?</span>
  }

  return (
    <>
      <span className="leaderboard-podium__player-mark" aria-hidden="true">
        <Glyph name="person" size={20} />
      </span>

      <span className="leaderboard-podium__identity">
        <strong>{entry.nickname}</strong>
        {entry.detail ? <small>{entry.detail}</small> : null}
      </span>

      <b className="leaderboard-podium__metric">{entry.metric}</b>
    </>
  )
}

export function LeaderboardPodium({
  entries,
  onSelect,
}: {
  entries: LeaderboardDisplayEntry[]
  onSelect: (entry: LeaderboardDisplayEntry) => void
}) {
  const byRank = new Map(entries.map((entry) => [entry.rank, entry]))

  return (
    <section className="leaderboard-podium" aria-label="Podium">
      {podiumOrder.map((rank) => {
        const entry = byRank.get(rank)

        return (
          <div key={rank} className={`leaderboard-podium__slot is-rank-${rank}`}>
            <PodiumCrown />

            {entry ? (
              <motion.button
                type="button"
                className="leaderboard-podium__place leaderboard-podium__place--interactive"
                whileTap={{ scale: 0.985 }}
                onClick={() => onSelect(entry)}
                aria-label={`#${rank} ${entry.nickname}`}
              >
                <PodiumCardContent entry={entry} />
              </motion.button>
            ) : (
              <article
                className="leaderboard-podium__place leaderboard-podium__place--empty"
                aria-label={`#${rank}`}
              >
                <PodiumCardContent entry={undefined} />
              </article>
            )}
          </div>
        )
      })}
    </section>
  )
}
