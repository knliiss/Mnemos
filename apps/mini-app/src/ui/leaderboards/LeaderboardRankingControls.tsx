import { useTranslation } from "react-i18next"
import type {
  DropLeaderboardDirection,
  DropLeaderboardSort,
  PredictorLeaderboardSort,
} from "@/shared/api/leaderboardApi"
import { Glyph } from "../shared/MnemosIcon"

type SortChoiceProps<T extends string> = {
  value: T
  options: readonly T[]
  optionLabel: (value: T) => string
  onChange: (value: T) => void
}

type DropRankingControlsProps = {
  sort: DropLeaderboardSort
  direction: DropLeaderboardDirection
  onSortChange: (value: DropLeaderboardSort) => void
  onDirectionChange: (value: DropLeaderboardDirection) => void
}

type PredictorRankingControlsProps = {
  sort: PredictorLeaderboardSort
  onSortChange: (value: PredictorLeaderboardSort) => void
}

const dropSorts: DropLeaderboardSort[] = ["MYTHICAL", "TOTAL", "SECRET"]
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

function DropDirectionToggle({
  direction,
  onChange,
}: {
  direction: DropLeaderboardDirection
  onChange: (value: DropLeaderboardDirection) => void
}) {
  const { t } = useTranslation()
  const label = t(`leaderboards.sortDirection.${direction.toLowerCase()}`)
  const nextDirection: DropLeaderboardDirection = direction === "DESC" ? "ASC" : "DESC"

  return (
    <button
      type="button"
      className={`leaderboard-sortbar__direction ${
        direction === "ASC" ? "is-ascending" : "is-descending"
      }`}
      aria-label={label}
      title={label}
      onClick={() => onChange(nextDirection)}
    >
      <Glyph name="back" size={18} />
    </button>
  )
}

export function DropRankingControls({
  sort,
  direction,
  onSortChange,
  onDirectionChange,
}: DropRankingControlsProps) {
  const { t } = useTranslation()

  return (
    <section className="leaderboard-sortbar leaderboard-sortbar--drops">
      <SortChoice
        value={sort}
        options={dropSorts}
        optionLabel={(value) => t(`leaderboards.dropSort.${value.toLowerCase()}`)}
        onChange={onSortChange}
      />
      <DropDirectionToggle direction={direction} onChange={onDirectionChange} />
    </section>
  )
}

export function PredictorRankingControls({ sort, onSortChange }: PredictorRankingControlsProps) {
  const { t } = useTranslation()

  return (
    <section className="leaderboard-sortbar leaderboard-sortbar--predictors">
      <SortChoice
        value={sort}
        options={predictorSorts}
        optionLabel={(value) => t(`leaderboards.predictorSort.${value.toLowerCase()}`)}
        onChange={onSortChange}
      />
    </section>
  )
}
