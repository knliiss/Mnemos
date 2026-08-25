import { motion } from "framer-motion"
import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"
import { LeaderboardPlayerHead } from "./LeaderboardPlayerHead"
import { PixelCrown } from "./PixelCrown"

const podiumOrder = [2, 1, 3]

function headSize(rank: number): number {
  if (rank === 1) {
    return 74
  }

  return 62
}

function PodiumCardContent({
  entry,
  rank,
}: {
  entry: LeaderboardDisplayEntry | undefined
  rank: number
}) {
  if (!entry) {
    return <span className="leaderboard-podium__empty-mark">?</span>
  }

  return (
    <>
      <span className="leaderboard-podium__head" aria-hidden="true">
        <LeaderboardPlayerHead
          entry={entry}
          size={headSize(rank)}
          className="leaderboard-podium__head-image"
        />
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
            <span className="leaderboard-podium__crown" aria-hidden="true">
              <PixelCrown />
            </span>

            {entry ? (
              <motion.button
                type="button"
                className="leaderboard-podium__place leaderboard-podium__place--interactive"
                whileTap={{ scale: 0.985 }}
                onClick={() => onSelect(entry)}
                aria-label={`#${rank} ${entry.nickname}`}
              >
                <PodiumCardContent entry={entry} rank={rank} />
              </motion.button>
            ) : (
              <article
                className="leaderboard-podium__place leaderboard-podium__place--empty"
                aria-label={`#${rank}`}
              >
                <PodiumCardContent entry={undefined} rank={rank} />
              </article>
            )}
          </div>
        )
      })}
    </section>
  )
}
