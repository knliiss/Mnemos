import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from "react"
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
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"

export function BadgeAdmin({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const [badges, setBadges] = useState<BadgeResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [createOpen, setCreateOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const [code, setCode] = useState("")
  const [name, setName] = useState("")
  const [icon, setIcon] = useState("✦")
  const [color, setColor] = useState("#C7F24A")
  const [query, setQuery] = useState("")
  const [searchResult, setSearchResult] = useState<{
    query: string
    items: IdentitySearchResponse[]
  }>({ query: "", items: [] })
  const [selectedUser, setSelectedUser] = useState<IdentitySearchResponse | null>(null)
  const [searchErrorQuery, setSearchErrorQuery] = useState<string | null>(null)
  const [userBadgeResult, setUserBadgeResult] = useState<{
    userId: string
    items: UserBadgeResponse[]
  }>({ userId: "", items: [] })
  const badgeRequestRevision = useRef(0)
  const userBadgeRequestRevision = useRef(0)
  const busyRef = useRef(false)
  const mountedRef = useRef(true)
  const selectedUserIdRef = useRef<string | null>(selectedUser?.userId ?? null)
  const normalized = query.trim()

  useLayoutEffect(() => {
    selectedUserIdRef.current = selectedUser?.userId ?? null
  }, [selectedUser?.userId])

  const refreshBadges = useCallback(async () => {
    const revision = badgeRequestRevision.current + 1

    badgeRequestRevision.current = revision
    setLoading(true)
    setLoadError(false)

    try {
      const nextBadges = await listAdminBadges()

      if (revision === badgeRequestRevision.current) {
        setBadges(nextBadges)
      }
    } catch (error) {
      if (revision === badgeRequestRevision.current) {
        setBadges([])
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (revision === badgeRequestRevision.current) {
        setLoading(false)
      }
    }
  }, [])

  useEffect(() => {
    mountedRef.current = true
    const frame = window.requestAnimationFrame(() => {
      void refreshBadges()
    })

    return () => {
      badgeRequestRevision.current += 1
      userBadgeRequestRevision.current += 1
      mountedRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [refreshBadges])

  useEffect(() => {
    if (normalized.length < 2) {
      return
    }

    let cancelled = false
    const timer = window.setTimeout(() => {
      setSearchErrorQuery(null)

      void searchIdentities(normalized)
        .then((items) => {
          if (!cancelled) {
            setSearchErrorQuery(null)
            setSearchResult({ query: normalized, items })
          }
        })
        .catch((error) => {
          if (!cancelled) {
            setSearchErrorQuery(normalized)
            handleError(error)
          }
        })
    }, 250)

    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [normalized])

  const refreshUserBadges = useCallback(async (userId: string) => {
    const revision = userBadgeRequestRevision.current + 1

    userBadgeRequestRevision.current = revision

    try {
      const items = await listAdminUserBadges(userId)

      if (revision === userBadgeRequestRevision.current) {
        setUserBadgeResult({ userId, items })
      }
    } catch (error) {
      if (revision === userBadgeRequestRevision.current) {
        setUserBadgeResult({ userId, items: [] })
        handleError(error)
      }
    }
  }, [])

  useEffect(() => {
    if (!selectedUser) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      void refreshUserBadges(selectedUser.userId)
    })

    return () => {
      userBadgeRequestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refreshUserBadges, selectedUser])

  const userBadges = useMemo(
    () =>
      selectedUser?.userId === userBadgeResult.userId ? userBadgeResult.items : [],
    [selectedUser?.userId, userBadgeResult],
  )
  const assignedIds = useMemo(() => new Set(userBadges.map((item) => item.badge.id)), [userBadges])
  const codeValid = /^[A-Z][A-Z0-9_]{1,31}$/.test(code)
  const createValid =
    codeValid &&
    name.trim().length > 0 &&
    name.trim().length <= 64 &&
    icon.trim().length > 0 &&
    icon.trim().length <= 64

  const create = async () => {
    if (!createValid || busyRef.current) {
      return
    }
    busyRef.current = true
    setBusy(true)

    try {
      await createAdminBadge({
        code,
        name: name.trim(),
        icon: icon.trim(),
        color,
      })

      if (!mountedRef.current) {
        return
      }

      setCode("")
      setName("")
      setIcon("✦")
      setColor("#C7F24A")
      setCreateOpen(false)
      await refreshBadges()
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      busyRef.current = false
      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  const toggleAssignment = async (badge: BadgeResponse) => {
    if (!selectedUser || busyRef.current) {
      return
    }
    busyRef.current = true
    setBusy(true)
    const selectedUserId = selectedUser.userId

    try {
      if (assignedIds.has(badge.id)) {
        await revokeAdminBadge(badge.id, selectedUserId)
      } else {
        await grantAdminBadge(badge.id, selectedUserId)
      }

      if (mountedRef.current && selectedUserIdRef.current === selectedUserId) {
        await refreshUserBadges(selectedUserId)
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      busyRef.current = false
      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  const visibleResults =
    normalized.length >= 2 && searchResult.query === normalized ? searchResult.items : []

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
          <strong>{t("badgeAdmin.overviewCount", { count: badges.length })}</strong>
          <em>{t("badgeAdmin.overviewDescription")}</em>
        </span>
      </section>

      {createOpen ? (
        <section className="staff-form-block">
          <header>
            <small>{t("badgeAdmin.newEyebrow")}</small>
            <strong>{t("badgeAdmin.newBadge")}</strong>
          </header>
          <Field
            label={t("badgeAdmin.code")}
            hint={code && !codeValid ? t("badgeAdmin.codeHint") : undefined}
          >
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
            type="button"
            className="primary-action"
            disabled={!createValid || busy}
            onClick={() => void create()}
          >
            {busy ? t("badgeAdmin.creating") : t("badgeAdmin.createBadge")}
          </button>
        </section>
      ) : null}

      <section className="staff-form-block">
        <header>
          <small>{t("badgeAdmin.assignmentEyebrow")}</small>
          <strong>{t("badgeAdmin.assignment")}</strong>
        </header>
        <label className="staff-search">
          <Glyph name="search" size={18} />
          <input
            value={query}
            disabled={busy}
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
                  type="button"
                  key={identity.userId}
                  disabled={busy}
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
        ) : searchErrorQuery === normalized ? (
          <p className="inline-hint">{t("common.loadErrorTitle")}</p>
        ) : normalized.length >= 2 ? (
          <p className="inline-hint">{t("moderation.nothingFound")}</p>
        ) : null}
      </section>

      {loading ? (
        <LoadingState />
      ) : loadError ? (
        <FeatureEmpty
          icon="spark"
          title={t("common.loadErrorTitle")}
          copy={t("common.loadErrorCopy")}
        >
          <button type="button" onClick={() => void refreshBadges()}>
            {t("common.retry")}
          </button>
        </FeatureEmpty>
      ) : (
        <section className="review-queue-grid">
          {badges.map((badge) => {
            const assigned = assignedIds.has(badge.id)
            const userBadge = userBadges.find((item) => item.badge.id === badge.id)

            return (
              <button
                type="button"
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
                    <b className="status-published">{t("badgeAdmin.active")}</b>
                  ) : assigned ? (
                    <b className="status-approved">{t("badgeAdmin.owned")}</b>
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

  return <FeatureLoading label={t("moderation.loading")} />
}
