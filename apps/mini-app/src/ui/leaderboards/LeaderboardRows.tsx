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
        <button
          key={`${entry.rank}:${entry.nickname}`}
          type="button"
          className="leaderboard-row leaderboard-row--interactive"
          onClick={() => onSelect(entry)}
          aria-label={entry.nickname}
        >
          <span className="leaderboard-row__rank">{entry.rank}</span>

          <span className="leaderboard-row__identity">
            <strong>{entry.nickname}</strong>
            <small>{entry.detail}</small>
          </span>

          <span className="leaderboard-row__metric">
            <b>{entry.metric}</b>
          </span>

          <span className="leaderboard-row__chevron" aria-hidden="true">
            ›
          </span>
        </button>
      ))}
    </div>
  )
}
