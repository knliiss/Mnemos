import type { LeaderboardDisplayEntry } from "./leaderboardPresentation"

const podiumOrder = [2, 1, 3]

function PodiumCrown({ rank }: { rank: number }) {
  return (
    <span className="leaderboard-podium__crown" aria-hidden="true">
      <svg viewBox="0 0 64 44" role="presentation">
        <path d="M7 34 3 10l18 12L32 5l11 17 18-12-4 24H7Z" fill="currentColor" />
        <path d="M9 36h46v5H9z" fill="currentColor" opacity="0.82" />
        <circle cx="4" cy="9" r="3" fill="currentColor" />
        <circle cx="32" cy="4" r="3" fill="currentColor" />
        <circle cx="60" cy="9" r="3" fill="currentColor" />
      </svg>
      <span>{rank}</span>
    </span>
  )
}

function PodiumCardContent({
  rank,
  entry,
}: {
  rank: number
  entry: LeaderboardDisplayEntry | undefined
}) {
  return (
    <>
      <span className="leaderboard-podium__rank">#{rank}</span>

      <div className="leaderboard-podium__identity">
        <span className="leaderboard-podium__medal" aria-hidden="true">
          {entry ? rank : "?"}
        </span>
        <strong>{entry?.nickname ?? "?"}</strong>
        <small>{entry?.detail ?? "?"}</small>
      </div>

      <b className="leaderboard-podium__metric">{entry?.metric ?? "?"}</b>
      <span className="leaderboard-podium__glow" aria-hidden="true" />
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
    <section className="leaderboard-podium" aria-label="Top 3">
      {podiumOrder.map((rank) => {
        const entry = byRank.get(rank)

        return (
          <div key={rank} className={`leaderboard-podium__slot is-rank-${rank}`}>
            <PodiumCrown rank={rank} />

            {entry ? (
              <button
                type="button"
                className="leaderboard-podium__place leaderboard-podium__place--interactive"
                onClick={() => onSelect(entry)}
                aria-label={entry.nickname}
              >
                <PodiumCardContent rank={rank} entry={entry} />
              </button>
            ) : (
              <article className="leaderboard-podium__place leaderboard-podium__place--empty">
                <PodiumCardContent rank={rank} entry={undefined} />
              </article>
            )}
          </div>
        )
      })}
    </section>
  )
}
