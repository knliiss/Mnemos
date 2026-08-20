import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createAdminBadge,
  grantAdminBadge,
  listAdminBadges,
  listAdminUserBadges,
  revokeAdminBadge,
  type BadgeResponse,
  type UserBadgeResponse,
} from "@/shared/api/badgeAdminApi"
import { searchIdentities, type IdentitySearchResponse } from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { Glyph } from "../../shared/MnemosIcon"
import { Field, PageHeader } from "../../shared/Primitives"

export function BadgeAdmin({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const [badges, setBadges] = useState<BadgeResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [createOpen, setCreateOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const [code, setCode] = useState("")
  const [name, setName] = useState("")
  const [icon, setIcon] = useState("✦")
  const [color, setColor] = useState("#C7F24A")
  const [query, setQuery] = useState("")
  const [searchResults, setSearchResults] = useState<IdentitySearchResponse[]>([])
  const [selectedUser, setSelectedUser] = useState<IdentitySearchResponse | null>(null)
  const [userBadges, setUserBadges] = useState<UserBadgeResponse[]>([])
  const normalized = query.trim()

  const refreshBadges = useCallback(async () => {
    try {
      setBadges(await listAdminBadges())
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refreshBadges()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refreshBadges])

  useEffect(() => {
    if (normalized.length < 2) {
      return
    }

    const timer = window.setTimeout(() => {
      void searchIdentities(normalized).then(setSearchResults).catch(handleError)
    }, 250)

    return () => window.clearTimeout(timer)
  }, [normalized])

  const refreshUserBadges = useCallback(async (userId: string) => {
    setUserBadges(await listAdminUserBadges(userId))
  }, [])

  useEffect(() => {
    if (!selectedUser) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      void refreshUserBadges(selectedUser.userId).catch(handleError)
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refreshUserBadges, selectedUser])

  const assignedIds = useMemo(() => new Set(userBadges.map((item) => item.badge.id)), [userBadges])
  const codeValid = /^[A-Z][A-Z0-9_]{1,31}$/.test(code)
  const createValid =
    codeValid &&
    name.trim().length > 0 &&
    name.trim().length <= 64 &&
    icon.trim().length > 0 &&
    icon.trim().length <= 64

  const create = async () => {
    if (!createValid || busy) {
      return
    }
    setBusy(true)

    try {
      await createAdminBadge({
        code,
        name: name.trim(),
        icon: icon.trim(),
        color,
      })
      setCode("")
      setName("")
      setIcon("✦")
      setColor("#C7F24A")
      setCreateOpen(false)
      await refreshBadges()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const toggleAssignment = async (badge: BadgeResponse) => {
    if (!selectedUser || busy) {
      return
    }
    setBusy(true)

    try {
      if (assignedIds.has(badge.id)) {
        await revokeAdminBadge(badge.id, selectedUser.userId)
      } else {
        await grantAdminBadge(badge.id, selectedUser.userId)
      }

      await refreshUserBadges(selectedUser.userId)
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const visibleResults = normalized.length >= 2 ? searchResults : []

  return (
    <div className="screen review-overhaul-screen">
      <PageHeader
        title={t("badgeAdmin.title")}
        back={back}
        actions={
          <button
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
          <small>BADGE ADMIN</small>
          <strong>{t("badgeAdmin.overviewCount", { count: badges.length })}</strong>
          <em>{t("badgeAdmin.overviewDescription")}</em>
        </span>
      </section>

      {createOpen ? (
        <section className="creator-r2-form-block">
          <header>
            <small>NEW BADGE</small>
            <strong>{t("badgeAdmin.newBadge")}</strong>
          </header>
          <Field label="Code" hint={code && !codeValid ? t("badgeAdmin.codeHint") : undefined}>
            <input
              value={code}
              maxLength={32}
              onChange={(event) =>
                setCode(event.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, ""))
              }
              placeholder="EVENT_MASTER"
            />
          </Field>
          <Field label={t("badgeAdmin.name")}>
            <input
              value={name}
              maxLength={64}
              onChange={(event) => setName(event.target.value)}
              placeholder="Event Master"
            />
          </Field>
          <Field label={t("badgeAdmin.icon")}>
            <input
              value={icon}
              maxLength={64}
              onChange={(event) => setIcon(event.target.value)}
              placeholder="✦"
            />
          </Field>
          <Field label={t("badgeAdmin.color")}>
            <input
              type="color"
              value={color}
              onChange={(event) => setColor(event.target.value.toUpperCase())}
            />
          </Field>
          <button
            className="primary-action"
            disabled={!createValid || busy}
            onClick={() => void create()}
          >
            {busy ? t("badgeAdmin.creating") : t("badgeAdmin.createBadge")}
          </button>
        </section>
      ) : null}

      <section className="creator-r2-form-block">
        <header>
          <small>ASSIGNMENT</small>
          <strong>{t("badgeAdmin.assignment")}</strong>
        </header>
        <label className="staff-search">
          <Glyph name="search" size={18} />
          <input
            value={query}
            minLength={2}
            maxLength={64}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={t("moderation.searchPlaceholder")}
          />
        </label>

        {visibleResults.length ? (
          <section className="staff-search-results">
            <div>
              {visibleResults.map((identity) => (
                <button
                  key={identity.userId}
                  className={selectedUser?.userId === identity.userId ? "is-selected" : ""}
                  onClick={() => setSelectedUser(identity)}
                >
                  <span className="staff-search-results__avatar">
                    {identity.username.slice(0, 1).toUpperCase()}
                  </span>
                  <span>
                    <strong>@{identity.username}</strong>
                    <small>{identity.telegramId}</small>
                  </span>
                  <span className="staff-search-results__accounts">
                    {t("moderation.accountCount", { count: identity.accounts.length })}
                  </span>
                </button>
              ))}
            </div>
          </section>
        ) : normalized.length >= 2 ? (
          <p className="inline-hint">{t("moderation.nothingFound")}</p>
        ) : null}
      </section>

      {loading ? (
        <LoadingState />
      ) : (
        <section className="review-queue-grid">
          {badges.map((badge) => {
            const assigned = assignedIds.has(badge.id)
            const userBadge = userBadges.find((item) => item.badge.id === badge.id)

            return (
              <button
                key={badge.id}
                className={assigned ? "review-queue-card is-selected" : "review-queue-card"}
                disabled={!selectedUser || busy}
                onClick={() => void toggleAssignment(badge)}
              >
                <span
                  className="review-overview__mark"
                  style={{ color: badge.color, borderColor: badge.color }}
                  aria-hidden="true"
                >
                  {badge.icon}
                </span>
                <span className="review-queue-card__copy">
                  <span className="review-queue-card__meta">
                    <small>{badge.code}</small>
                  </span>
                  <strong>{badge.name}</strong>
                  <em>
                    {selectedUser
                      ? assigned
                        ? t("badgeAdmin.assigned")
                        : t("badgeAdmin.clickToAssign")
                      : t("badgeAdmin.selectUserFirst")}
                  </em>
                </span>
                <span className="review-queue-card__side">
                  {userBadge?.active ? (
                    <b className="status-published">ACTIVE</b>
                  ) : assigned ? (
                    <b className="status-approved">OWNED</b>
                  ) : null}
                </span>
              </button>
            )
          })}
          {!badges.length ? <p className="inline-hint">{t("badgeAdmin.noBadges")}</p> : null}
        </section>
      )}
    </div>
  )
}

function LoadingState() {
  const { t } = useTranslation()

  return (
    <div className="events-r2-loading" aria-label={t("moderation.loading")}>
      <i />
      <i />
      <i />
    </div>
  )
}
