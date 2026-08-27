import { useState } from "react"
import { useTranslation } from "react-i18next"
import type {
  LeaderboardExclusionScope,
  LeaderboardExclusionSubjectType,
} from "@/shared/api/leaderboardExclusionApi"
import { PageHeader } from "../../../shared/Primitives"
import { FeatureLoading } from "../../../system/FeatureLoading"
import { LeaderboardScopeSelect } from "./LeaderboardScopeSelect"
import { useLeaderboardExclusions } from "./useLeaderboardExclusions"
import "./leaderboard-exclusions.css"

const subjectTypes: LeaderboardExclusionSubjectType[] = ["ACCOUNT", "PLAYER"]

export function LeaderboardExclusionAdmin({ back }: { back(): void }) {
  const { t } = useTranslation()
  const exclusions = useLeaderboardExclusions()
  const [subjectType, setSubjectType] = useState<LeaderboardExclusionSubjectType>("ACCOUNT")
  const [scope, setScope] = useState<LeaderboardExclusionScope>("COINS")
  const [target, setTarget] = useState("")
  const [reason, setReason] = useState("")

  const submit = async () => {
    const created = await exclusions.create({
      subjectType,
      target,
      scope,
      reason,
    })

    if (created) {
      setTarget("")
      setReason("")
    }
  }

  return (
    <div className="screen leaderboard-exclusion-admin">
      <PageHeader title={t("leaderboardExclusions.title")} back={back} />

      <section className="leaderboard-exclusion-hero">
        <img src="/ui-illustrations/admin-leaderboards.svg" alt="" aria-hidden="true" />
        <span>
          <small>{t("leaderboardExclusions.eyebrow")}</small>
          <strong>{t("leaderboardExclusions.heroTitle")}</strong>
          <p>{t("leaderboardExclusions.heroCopy")}</p>
        </span>
      </section>

      <section className="leaderboard-exclusion-form">
        <header>
          <small>{t("leaderboardExclusions.newEyebrow")}</small>
          <strong>{t("leaderboardExclusions.newTitle")}</strong>
        </header>

        <div className="leaderboard-exclusion-segmented">
          {subjectTypes.map((value) => (
            <button
              key={value}
              type="button"
              className={subjectType === value ? "is-active" : ""}
              onClick={() => {
                setSubjectType(value)
                setScope(value === "ACCOUNT" ? "COINS" : "DROPS")
              }}
            >
              {t(`leaderboardExclusions.subjectTypes.${value}`)}
            </button>
          ))}
        </div>

        <label>
          <span>{t("leaderboardExclusions.target")}</span>
          <input
            value={target}
            maxLength={64}
            onChange={(event) => setTarget(event.target.value)}
            placeholder={t(
              subjectType === "PLAYER"
                ? "leaderboardExclusions.playerPlaceholder"
                : "leaderboardExclusions.accountPlaceholder",
            )}
          />
          <small>{t(`leaderboardExclusions.targetHint.${subjectType}`)}</small>
        </label>

        <LeaderboardScopeSelect subjectType={subjectType} value={scope} onChange={setScope} />

        <label>
          <span>{t("leaderboardExclusions.reason")}</span>
          <textarea
            value={reason}
            maxLength={256}
            onChange={(event) => setReason(event.target.value)}
            placeholder={t("leaderboardExclusions.reasonPlaceholder")}
          />
        </label>

        {exclusions.targetNotFound ? (
          <p className="leaderboard-exclusion-form__error">
            {t("leaderboardExclusions.targetNotFound")}
          </p>
        ) : null}

        <button
          type="button"
          className="leaderboard-exclusion-form__submit"
          disabled={!target.trim() || exclusions.busy}
          onClick={() => void submit()}
        >
          {exclusions.busy ? t("leaderboardExclusions.saving") : t("leaderboardExclusions.create")}
        </button>
      </section>

      <section className="leaderboard-exclusion-list">
        <header>
          <span>
            <small>{t("leaderboardExclusions.listEyebrow")}</small>
            <strong>{t("leaderboardExclusions.listTitle")}</strong>
          </span>
          <b>{exclusions.items.length}</b>
        </header>

        {exclusions.loading ? <FeatureLoading label={t("common.loading")} /> : null}

        {!exclusions.loading && exclusions.items.length === 0 ? (
          <div className="leaderboard-exclusion-empty">
            <img src="/ui-illustrations/admin-leaderboards.svg" alt="" aria-hidden="true" />
            <strong>{t("leaderboardExclusions.emptyTitle")}</strong>
            <small>{t("leaderboardExclusions.emptyCopy")}</small>
          </div>
        ) : null}

        {exclusions.items.map((item) => (
          <article key={item.id} className="leaderboard-exclusion-row">
            <span className="leaderboard-exclusion-row__identity">
              <small>{t(`leaderboardExclusions.subjectTypes.${item.subjectType}`)}</small>
              <strong>{item.subjectId}</strong>
              <em>{item.reason ?? t("leaderboardExclusions.noReason")}</em>
            </span>

            <span className="leaderboard-exclusion-row__scope">
              {t(`leaderboardExclusions.scopes.${item.scope}`)}
            </span>

            <button
              type="button"
              disabled={exclusions.busy}
              onClick={() => void exclusions.remove(item.id)}
            >
              {t("leaderboardExclusions.remove")}
            </button>
          </article>
        ))}
      </section>
    </div>
  )
}
