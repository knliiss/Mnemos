import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"

export function LeaderboardRows({ entries }: { entries: LeaderboardDisplayEntry[] }) {
  return (
    <div className="leaderboard-rows">
      {entries.map((entry) => (
        <article key={`${entry.rank}:${entry.nickname}`} className="leaderboard-row">
          <span className="leaderboard-row__rank">#{entry.rank}</span>
          <span className="leaderboard-row__identity">
            <strong>{entry.nickname}</strong>
            <small>{entry.detail}</small>
          </span>
          <b>{entry.metric}</b>
        </article>
      ))}
    </div>
  )
}
