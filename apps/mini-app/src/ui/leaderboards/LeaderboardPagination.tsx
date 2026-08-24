import { useTranslation } from "react-i18next"
import { Glyph } from "../shared/MnemosIcon"

type LeaderboardPaginationProps = {
  page: number
  totalPages: number
  onChange: (page: number) => void
}

export function LeaderboardPagination({ page, totalPages, onChange }: LeaderboardPaginationProps) {
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
        <Glyph name="back" size={16} />
      </button>

      <span>
        <b>{page}</b>
        <i>/</i>
        {totalPages}
      </span>

      <button
        type="button"
        className="leaderboard-pagination__next"
        disabled={page >= totalPages}
        aria-label={t("leaderboards.nextPage")}
        onClick={() => onChange(page + 1)}
      >
        <Glyph name="back" size={16} />
      </button>
    </nav>
  )
}
