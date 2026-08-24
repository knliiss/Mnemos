import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"

const podiumOrder = [2, 1, 3]

export function LeaderboardPodium({ entries }: { entries: LeaderboardDisplayEntry[] }) {
  const byRank = new Map(entries.map((entry) => [entry.rank, entry]))

  return (
    <section className="leaderboard-podium" aria-label="Top 3">
      {podiumOrder.map((rank) => {
        const entry = byRank.get(rank)

        if (!entry) {
          return null
        }

        return (
          <article key={rank} className={`leaderboard-podium__place is-rank-${rank}`}>
            <span className="leaderboard-podium__rank">#{rank}</span>

            <div className="leaderboard-podium__identity">
              <span className="leaderboard-podium__medal" aria-hidden="true">
                {rank}
              </span>
              <strong>{entry.nickname}</strong>
              <small>{entry.detail}</small>
            </div>

            <b className="leaderboard-podium__metric">{entry.metric}</b>
            <span className="leaderboard-podium__glow" aria-hidden="true" />
          </article>
        )
      })}
    </section>
  )
}
