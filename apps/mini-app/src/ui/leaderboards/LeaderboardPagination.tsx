import { useTranslation } from "react-i18next"

type LeaderboardPaginationProps = {
  page: number
  totalPages: number
  onChange: (page: number) => void
}

export function LeaderboardPagination({
  page,
  totalPages,
  onChange,
}: LeaderboardPaginationProps) {
  const { t } = useTranslation()

  if (totalPages <= 1) {
    return null
  }

  return (
    <nav className="leaderboard-pagination" aria-label={t("leaderboards.pagination")}>
      <button
        type="button"
        disabled={page <= 1}
        aria-label={t("leaderboards.previousPage")}
        onClick={() => onChange(page - 1)}
      >
        ‹
      </button>

      <span>
        <b>{page}</b>
        <i>/</i>
        {totalPages}
      </span>

      <button
        type="button"
        disabled={page >= totalPages}
        aria-label={t("leaderboards.nextPage")}
        onClick={() => onChange(page + 1)}
      >
        ›
      </button>
    </nav>
  )
}
