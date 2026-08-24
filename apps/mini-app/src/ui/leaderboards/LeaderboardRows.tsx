import { motion } from "framer-motion"
import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"

export function LeaderboardRows({
  entries,
  onSelect,
}: {
  entries: LeaderboardDisplayEntry[]
  onSelect: (entry: LeaderboardDisplayEntry) => void
}) {
  return (
    <div className="leaderboard-rows">
      {entries.map((entry) => (
        <motion.button
          key={`${entry.rank}:${entry.nickname}`}
          type="button"
          className="leaderboard-row"
          whileTap={{ scale: 0.992 }}
          onClick={() => onSelect(entry)}
          aria-label={`#${entry.rank} ${entry.nickname}`}
        >
          <span className="leaderboard-row__rank">#{entry.rank}</span>

          <span className="leaderboard-row__identity">
            <strong>{entry.nickname}</strong>
            {entry.detail ? <small>{entry.detail}</small> : null}
          </span>

          <span className="leaderboard-row__metric">
            <b>{entry.metric}</b>
          </span>

          <span className="leaderboard-row__chevron" aria-hidden="true">
            ›
          </span>
        </motion.button>
      ))}
    </div>
  )
}
