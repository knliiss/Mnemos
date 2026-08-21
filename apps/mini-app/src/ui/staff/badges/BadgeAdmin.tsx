import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"
import { BadgeAssignmentGrid } from "./BadgeAssignmentGrid"
import { BadgeAssignmentSearch } from "./BadgeAssignmentSearch"
import { BadgeCreateForm } from "./BadgeCreateForm"
import "./badge-admin.css"
import { useBadgeAssignments } from "./useBadgeAssignments"
import { useBadgeCatalog } from "./useBadgeCatalog"

export function BadgeAdmin({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const [createOpen, setCreateOpen] = useState(false)
  const catalog = useBadgeCatalog()
  const assignments = useBadgeAssignments()
  const busy = catalog.creating || assignments.busy

  return (
    <div className="screen badge-admin-screen">
      <PageHeader
        title={t("badgeAdmin.title")}
        back={back}
        actions={
          <button
            type="button"
            className="bet-admin-header-action"
            onClick={() => setCreateOpen((value) => !value)}
          >
            <Glyph name={createOpen ? "close" : "plus"} size={15} />
            {createOpen ? t("badgeAdmin.close") : t("badgeAdmin.new")}
          </button>
        }
      />

      <section className="review-overview">
        <span className="review-overview__mark">
          <Glyph name="spark" size={20} />
        </span>
        <span>
          <small>{t("badgeAdmin.eyebrow")}</small>
          <strong>{t("badgeAdmin.overviewCount", { count: catalog.badges.length })}</strong>
          <em>{t("badgeAdmin.overviewDescription")}</em>
        </span>
      </section>

      {createOpen ? (
        <BadgeCreateForm
          busy={busy}
          onCreate={catalog.create}
          onCreated={() => setCreateOpen(false)}
        />
      ) : null}

      <BadgeAssignmentSearch
        query={assignments.query}
        normalizedQuery={assignments.normalizedQuery}
        results={assignments.visibleResults}
        searchFailed={assignments.searchFailed}
        selectedUser={assignments.selectedUser}
        busy={busy}
        onQueryChange={assignments.setQuery}
        onSelectUser={assignments.setSelectedUser}
      />

      {catalog.loading ? (
        <FeatureLoading label={t("moderation.loading")} />
      ) : catalog.loadError ? (
        <FeatureEmpty
          icon="spark"
          title={t("common.loadErrorTitle")}
          copy={t("common.loadErrorCopy")}
        >
          <button type="button" onClick={() => void catalog.refresh()}>
            {t("common.retry")}
          </button>
        </FeatureEmpty>
      ) : (
        <BadgeAssignmentGrid
          badges={catalog.badges}
          userBadges={assignments.userBadges}
          assignedIds={assignments.assignedIds}
          userSelected={Boolean(assignments.selectedUser)}
          busy={busy}
          onToggleAssignment={(badge) => void assignments.toggleAssignment(badge)}
        />
      )}
    </div>
  )
}
