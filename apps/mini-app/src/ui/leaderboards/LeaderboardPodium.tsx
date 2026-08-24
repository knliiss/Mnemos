import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"

export function LeaderboardPodium({ entries }: { entries: LeaderboardDisplayEntry[] }) {
  const byRank = new Map(entries.slice(0, 3).map((entry) => [entry.rank, entry]))
  const order = [2, 1, 3]

  return (
    <section className="leaderboard-podium" aria-label="Top 3">
      {order.map((rank) => {
        const entry = byRank.get(rank)

        return (
          <article key={rank} className={`leaderboard-podium__place is-rank-${rank}`}>
            <span className="leaderboard-podium__medal">{rank}</span>
            {entry ? (
              <>
                <strong>{entry.nickname}</strong>
                <span>{entry.metric}</span>
              </>
            ) : (
              <span className="leaderboard-podium__empty">—</span>
            )}
          </article>
        )
      })}
    </section>
  )
}
