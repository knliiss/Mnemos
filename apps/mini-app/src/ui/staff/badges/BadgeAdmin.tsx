import { useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { BadgeResponse } from "@/shared/api/badgeAdminApi"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"
import { BadgeAssignmentGrid } from "./BadgeAssignmentGrid"
import { BadgeAssignmentSearch } from "./BadgeAssignmentSearch"
import { BadgeCreateForm } from "./BadgeCreateForm"
import "./badge-admin.css"
import "./badge-admin-control-polish.css"
import { useBadgeAssignments } from "./useBadgeAssignments"
import { useBadgeCatalog } from "./useBadgeCatalog"

type BadgeAssignmentConfirmation = {
  badge: BadgeResponse
  assigned: boolean
  userId: string
  username: string
}

export function BadgeAdmin({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const [createOpen, setCreateOpen] = useState(false)
  const [assignmentConfirmation, setAssignmentConfirmation] =
    useState<BadgeAssignmentConfirmation | null>(null)
  const assignmentConfirmRef = useRef<HTMLButtonElement>(null)
  const catalog = useBadgeCatalog()
  const assignments = useBadgeAssignments()
  const busy = catalog.creating || assignments.busy

  useEffect(() => {
    if (!assignmentConfirmation) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      assignmentConfirmRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [assignmentConfirmation])

  const confirmAssignmentChange = async () => {
    const confirmation = assignmentConfirmation

    if (
      !confirmation ||
      busy ||
      assignments.selectedUser?.userId !== confirmation.userId
    ) {
      return
    }

    setAssignmentConfirmation(null)
    await assignments.toggleAssignment(confirmation.badge)
  }

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
        onSelectUser={(user) => {
          setAssignmentConfirmation(null)
          assignments.setSelectedUser(user)
        }}
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
          onToggleAssignment={(badge) => {
            const selectedUser = assignments.selectedUser

            if (!selectedUser) {
              return
            }

            setAssignmentConfirmation({
              badge,
              assigned: assignments.assignedIds.has(badge.id),
              userId: selectedUser.userId,
              username: selectedUser.username,
            })
          }}
        />
      )}

      {assignmentConfirmation ? (
        <section
          className={
            assignmentConfirmation.assigned
              ? "badge-assignment-confirmation is-revoke"
              : "badge-assignment-confirmation"
          }
          role="alertdialog"
          aria-label={t("badgeAdmin.confirmAssignmentChange")}
          onKeyDown={(event) => {
            if (event.key === "Escape" && !busy) {
              event.preventDefault()
              setAssignmentConfirmation(null)
            }
          }}
        >
          <span className="badge-assignment-confirmation__art" aria-hidden="true">
            <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          </span>
          <span className="badge-assignment-confirmation__copy">
            <small>
              {assignmentConfirmation.assigned
                ? t("badgeAdmin.revokeBadge")
                : t("badgeAdmin.assignBadge")}
            </small>
            <strong>{assignmentConfirmation.badge.name}</strong>
            <em>
              {t("badgeAdmin.assignmentTarget", {
                username: assignmentConfirmation.username,
              })}
            </em>
          </span>
          <span className="badge-assignment-confirmation__actions">
            <button
              ref={assignmentConfirmRef}
              type="button"
              className={assignmentConfirmation.assigned ? "is-danger" : "is-primary"}
              disabled={busy}
              onClick={() => void confirmAssignmentChange()}
            >
              {assignmentConfirmation.assigned
                ? t("badgeAdmin.confirmRevokeBadge")
                : t("badgeAdmin.confirmAssignBadge")}
            </button>
            <button
              type="button"
              className="is-neutral"
              disabled={busy}
              onClick={() => setAssignmentConfirmation(null)}
            >
              {t("common.cancel")}
            </button>
          </span>
        </section>
      ) : null}
    </div>
  )
}
