import { useTranslation } from "react-i18next"
import type { DropLeaderboardSort, PredictorLeaderboardSort } from "@/shared/api/leaderboardApi"

type SortChoiceProps<T extends string> = {
  value: T
  options: readonly T[]
  optionLabel: (value: T) => string
  onChange: (value: T) => void
}

type DropRankingControlsProps = {
  sort: DropLeaderboardSort
  onSortChange: (value: DropLeaderboardSort) => void
}

type PredictorRankingControlsProps = {
  sort: PredictorLeaderboardSort
  onSortChange: (value: PredictorLeaderboardSort) => void
}

const dropSorts: DropLeaderboardSort[] = ["TOTAL", "MYTHICAL", "SECRET", "RECENT"]
const predictorSorts: PredictorLeaderboardSort[] = ["PROFIT", "WIN_RATE", "WINS", "TURNOVER"]

function SortChoice<T extends string>({
  value,
  options,
  optionLabel,
  onChange,
}: SortChoiceProps<T>) {
  return (
    <div className="leaderboard-sortbar__choices" role="group">
      {options.map((option) => {
        const active = option === value

        return (
          <button
            key={option}
            type="button"
            className={active ? "is-active" : ""}
            aria-pressed={active}
            onClick={() => onChange(option)}
          >
            {optionLabel(option)}
          </button>
        )
      })}
    </div>
  )
}

export function DropRankingControls({ sort, onSortChange }: DropRankingControlsProps) {
  const { t } = useTranslation()

  return (
    <section className="leaderboard-sortbar">
      <span>{t("leaderboards.sort")}</span>
      <SortChoice
        value={sort}
        options={dropSorts}
        optionLabel={(value) => t(`leaderboards.dropSort.${value.toLowerCase()}`)}
        onChange={onSortChange}
      />
    </section>
  )
}

export function PredictorRankingControls({ sort, onSortChange }: PredictorRankingControlsProps) {
  const { t } = useTranslation()

  return (
    <section className="leaderboard-sortbar">
      <span>{t("leaderboards.sort")}</span>
      <SortChoice
        value={sort}
        options={predictorSorts}
        optionLabel={(value) => t(`leaderboards.predictorSort.${value.toLowerCase()}`)}
        onChange={onSortChange}
      />
    </section>
  )
}
