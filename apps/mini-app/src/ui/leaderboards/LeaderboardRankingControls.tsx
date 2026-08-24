import { useTranslation } from "react-i18next"
import type {
  DropLeaderboardSort,
  LeaderboardItemRarity,
  LeaderboardItemType,
  PredictorLeaderboardSort,
} from "@/shared/api/leaderboardApi"

type ChoiceGroupProps<T extends string> = {
  label: string
  value: T
  options: readonly T[]
  optionLabel: (value: T) => string
  onChange: (value: T) => void
  compact?: boolean
}

type DropRankingControlsProps = {
  sort: DropLeaderboardSort
  itemType: LeaderboardItemType | ""
  rarity: LeaderboardItemRarity | ""
  nickname: string
  itemName: string
  onSortChange: (value: DropLeaderboardSort) => void
  onItemTypeChange: (value: LeaderboardItemType | "") => void
  onRarityChange: (value: LeaderboardItemRarity | "") => void
  onNicknameChange: (value: string) => void
  onItemNameChange: (value: string) => void
  onApply: () => void
  onReset: () => void
}

type PredictorRankingControlsProps = {
  sort: PredictorLeaderboardSort
  minimumSample: number
  onSortChange: (value: PredictorLeaderboardSort) => void
  onMinimumSampleChange: (value: number) => void
}

const dropSorts: DropLeaderboardSort[] = ["TOTAL", "MYTHICAL", "SECRET", "RECENT"]
const predictorSorts: PredictorLeaderboardSort[] = ["PROFIT", "WIN_RATE", "WINS", "TURNOVER"]
const itemTypes: LeaderboardItemType[] = ["SWORD", "AURA", "PET", "BOOK", "JEWELRY"]
const rarities: LeaderboardItemRarity[] = ["MYTHICAL", "SECRET"]

function ChoiceGroup<T extends string>({
  label,
  value,
  options,
  optionLabel,
  onChange,
  compact = false,
}: ChoiceGroupProps<T>) {
  return (
    <div className="leaderboard-choice-group">
      <span>{label}</span>
      <div
        className={compact ? "leaderboard-choice-row is-compact" : "leaderboard-choice-row"}
        role="group"
        aria-label={label}
      >
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
    </div>
  )
}

export function DropRankingControls({
  sort,
  itemType,
  rarity,
  nickname,
  itemName,
  onSortChange,
  onItemTypeChange,
  onRarityChange,
  onNicknameChange,
  onItemNameChange,
  onApply,
  onReset,
}: DropRankingControlsProps) {
  const { t } = useTranslation()
  const itemTypeOptions: Array<LeaderboardItemType | ""> = ["", ...itemTypes]
  const rarityOptions: Array<LeaderboardItemRarity | ""> = ["", ...rarities]

  return (
    <>
      <section className="leaderboard-ranking-panel">
        <div className="leaderboard-ranking-panel__head">
          <div>
            <span>{t("leaderboards.rankBy")}</span>
            <strong>{t("leaderboards.dropRankingTitle")}</strong>
          </div>
          <small>{t("leaderboards.dropRankingDescription")}</small>
        </div>

        <ChoiceGroup
          label={t("leaderboards.rankBy")}
          value={sort}
          options={dropSorts}
          optionLabel={(value) => t(`leaderboards.dropSort.${value.toLowerCase()}`)}
          onChange={onSortChange}
        />
      </section>

      <form
        className="leaderboard-filter-panel"
        onSubmit={(event) => {
          event.preventDefault()
          onApply()
        }}
      >
        <div className="leaderboard-filter-panel__head">
          <div>
            <span>{t("leaderboards.filtersEyebrow")}</span>
            <strong>{t("leaderboards.filtersTitle")}</strong>
          </div>
          <button type="button" onClick={onReset}>
            {t("leaderboards.reset")}
          </button>
        </div>

        <ChoiceGroup
          compact
          label={t("leaderboards.itemType")}
          value={itemType}
          options={itemTypeOptions}
          optionLabel={(value) =>
            value ? t(`leaderboards.itemTypes.${value.toLowerCase()}`) : t("common.all")
          }
          onChange={onItemTypeChange}
        />

        <ChoiceGroup
          compact
          label={t("leaderboards.rarity")}
          value={rarity}
          options={rarityOptions}
          optionLabel={(value) =>
            value ? t(`leaderboards.rarities.${value.toLowerCase()}`) : t("common.all")
          }
          onChange={onRarityChange}
        />

        <div className="leaderboard-filter-panel__search">
          <label>
            <span>{t("leaderboards.nickname")}</span>
            <input
              value={nickname}
              maxLength={20}
              placeholder={t("leaderboards.nicknamePlaceholder")}
              onChange={(event) => onNicknameChange(event.target.value)}
            />
          </label>

          <label>
            <span>{t("leaderboards.item")}</span>
            <input
              value={itemName}
              maxLength={24}
              placeholder={t("leaderboards.itemPlaceholder")}
              onChange={(event) => onItemNameChange(event.target.value)}
            />
          </label>
        </div>

        <button type="submit" className="leaderboard-filter-panel__apply">
          {t("leaderboards.apply")}
        </button>
      </form>
    </>
  )
}

export function PredictorRankingControls({
  sort,
  minimumSample,
  onSortChange,
  onMinimumSampleChange,
}: PredictorRankingControlsProps) {
  const { t } = useTranslation()

  return (
    <section className="leaderboard-ranking-panel">
      <div className="leaderboard-ranking-panel__head">
        <div>
          <span>{t("leaderboards.rankBy")}</span>
          <strong>{t("leaderboards.predictorRankingTitle")}</strong>
        </div>
        <small>{t("leaderboards.predictorRankingDescription")}</small>
      </div>

      <ChoiceGroup
        label={t("leaderboards.rankBy")}
        value={sort}
        options={predictorSorts}
        optionLabel={(value) => t(`leaderboards.predictorSort.${value.toLowerCase()}`)}
        onChange={onSortChange}
      />

      {sort === "WIN_RATE" ? (
        <label className="leaderboard-minimum-sample">
          <span>{t("leaderboards.minimumSample")}</span>
          <input
            type="number"
            min={1}
            max={1000}
            value={minimumSample}
            onChange={(event) => onMinimumSampleChange(Number(event.target.value) || 1)}
          />
          <small>{t("leaderboards.minimumSampleDescription")}</small>
        </label>
      ) : null}
    </section>
  )
}
