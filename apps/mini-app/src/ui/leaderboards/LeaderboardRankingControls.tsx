import { useTranslation } from "react-i18next"
import type {
  DropLeaderboardDirection,
  DropLeaderboardSort,
  PredictorLeaderboardSort,
} from "@/shared/api/leaderboardApi"

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
const dropDirections: DropLeaderboardDirection[] = ["DESC", "ASC"]

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

function DropDirectionChoice({
  direction,
  onChange,
}: {
  direction: DropLeaderboardDirection
  onChange: (value: DropLeaderboardDirection) => void
}) {
  const { t } = useTranslation()

  return (
    <div className="leaderboard-sortbar__direction" role="group">
      {dropDirections.map((option) => {
        const active = option === direction
        const label = t(`leaderboards.sortDirection.${option.toLowerCase()}`)

        return (
          <button
            key={option}
            type="button"
            className={active ? "is-active" : ""}
            aria-label={label}
            aria-pressed={active}
            title={label}
            onClick={() => onChange(option)}
          >
            <span aria-hidden="true">{option === "ASC" ? "↑" : "↓"}</span>
          </button>
        )
      })}
    </div>
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
      <span>{t("leaderboards.sort")}</span>
      <SortChoice
        value={sort}
        options={dropSorts}
        optionLabel={(value) => t(`leaderboards.dropSort.${value.toLowerCase()}`)}
        onChange={onSortChange}
      />
      <DropDirectionChoice direction={direction} onChange={onDirectionChange} />
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
