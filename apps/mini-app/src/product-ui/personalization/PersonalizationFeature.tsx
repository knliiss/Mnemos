import { motion } from "framer-motion"
import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  activatePersonalization,
  deactivatePersonalization,
  getPersonalization,
  getPurchaseOperation,
  listOwnedPersonalizations,
  listPersonalizations,
  purchasePersonalization,
  type PersonalizationResponse,
} from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { useErrorNotifications } from "../components/ErrorNotifications"
import { PersonalizationDetails, PersonalizationVisual } from "../components/PersonalizationPreview"
import { CURRENCIES, PERSONALIZATION_TYPES } from "../contracts"
import { useProductData } from "../data/ProductDataProvider"
import { toProductPersonalization } from "../data/personalizationMappers"
import type {
  Currency,
  Personalization,
  PersonalizationTarget,
  PersonalizationType,
  Route,
} from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader, Segmented } from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const targets: Record<PersonalizationType, PersonalizationTarget> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

const accountTypes = PERSONALIZATION_TYPES.filter((type) => targets[type] === "ACCOUNT")

export function PersonalizationFeature(props: Props) {
  const { t } = useTranslation()
  const product = useProductData()
  const refreshAccount = product.refreshAccount
  const errors = useErrorNotifications()
  const [items, setItems] = useState<Personalization[]>([])
  const [loading, setLoading] = useState(true)
  const [busyId, setBusyId] = useState<string | null>(null)
  const [pendingPurchases, setPendingPurchases] = useState<Record<string, string>>({})

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      const [ownedPage, ...catalogPages] = await Promise.all([
        listOwnedPersonalizations(undefined, 0, 100),
        ...PERSONALIZATION_TYPES.map((type) => listPersonalizations(type, undefined, 0, 100)),
      ])
      const ownedById = new Map(ownedPage.items.map((owned) => [owned.personalizationId, owned]))
      const responsesById = new Map<string, PersonalizationResponse>()

      catalogPages.forEach((page) => {
        page.items.forEach((item) => responsesById.set(item.id, item))
      })

      const missingOwnedIds = ownedPage.items
        .map((owned) => owned.personalizationId)
        .filter((id) => !responsesById.has(id))

      if (missingOwnedIds.length) {
        const missing = await Promise.all(missingOwnedIds.map((id) => getPersonalization(id)))
        missing.forEach((item) => responsesById.set(item.id, item))
      }

      setItems(
        [...responsesById.values()].map((response) =>
          toProductPersonalization(
            response,
            ownedById.get(response.id) ?? null,
            product.user.id,
            product.user.username,
          ),
        ),
      )
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [product.user.id, product.user.username])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refresh])

  const finishPurchase = useCallback(
    async (
      personalizationId: string,
      status: "CONFIRMED" | "FAILED",
      failureReason?: string | null,
    ) => {
      setPendingPurchases((current) => {
        const next = { ...current }
        delete next[personalizationId]
        return next
      })

      if (status === "CONFIRMED") {
        await Promise.all([refresh(), refreshAccount()])
        return
      }

      errors.showBackendError({
        code: "PERSONALIZATION_PURCHASE_FAILED",
        statusCode: 409,
        message: failureReason ?? t("personalization.purchaseFailed"),
      })
    },
    [errors, refresh, refreshAccount, t],
  )

  useEffect(
    () =>
      realtimeEvents.subscribe((event) => {
        if (event.type !== "personalization.purchase.completed") {
          return
        }

        void finishPurchase(
          event.payload.personalizationId,
          event.payload.status,
          event.payload.failureReason,
        )
      }),
    [finishPurchase],
  )

  const pollPurchase = useCallback(
    async (personalizationId: string, operationId: string) => {
      for (let attempt = 0; attempt < 15; attempt += 1) {
        await new Promise((resolve) => window.setTimeout(resolve, 900))

        try {
          const operation = await getPurchaseOperation(operationId)
          if (operation.status === "PENDING") {
            continue
          }

          await finishPurchase(personalizationId, operation.status, operation.failureReason)
          return
        } catch {
          // SSE is primary; polling only covers reconnect races.
        }
      }
    },
    [finishPurchase],
  )

  const purchase = useCallback(
    async (item: Personalization) => {
      if (item.owned || pendingPurchases[item.id] || busyId) {
        return
      }

      setBusyId(item.id)

      try {
        const operation = await purchasePersonalization(item.id)
        setPendingPurchases((current) => ({
          ...current,
          [item.id]: operation.operationId,
        }))
        void pollPurchase(item.id, operation.operationId)
      } catch (error) {
        handleError(error)
      } finally {
        setBusyId(null)
      }
    },
    [busyId, pendingPurchases, pollPurchase],
  )

  const toggleActive = useCallback(
    async (item: Personalization) => {
      if (!item.owned || busyId) {
        return
      }

      setBusyId(item.id)

      try {
        if (item.active) {
          await deactivatePersonalization(item.id)
        } else {
          await activatePersonalization(item.id)
        }

        await refresh()
      } catch (error) {
        handleError(error)
      } finally {
        setBusyId(null)
      }
    },
    [busyId, refresh],
  )

  if (props.routeId === "personalization-owned") {
    return (
      <OwnedPage
        {...props}
        items={items}
        loading={loading}
        busyId={busyId}
        onToggleActive={toggleActive}
      />
    )
  }

  if (props.routeId === "personalization-store-category") {
    return <StoreCategoryPage {...props} items={items} loading={loading} />
  }

  if (props.routeId === "personalization-detail") {
    return (
      <DetailPage
        {...props}
        items={items}
        loading={loading}
        busyId={busyId}
        pendingPurchases={pendingPurchases}
        onPurchase={purchase}
        onToggleActive={toggleActive}
      />
    )
  }

  return <StoreRoot {...props} items={items} loading={loading} prime={product.user.prime} />
}

function StoreRoot({
  navigate,
  items,
  loading,
  prime,
}: Props & { items: Personalization[]; loading: boolean; prime: number }) {
  const { t, i18n } = useTranslation()
  const published = items.filter((item) => item.status === "PUBLISHED")
  const locale = localeForLanguage(i18n.resolvedLanguage)

  return (
    <div className="screen personalization-store-screen root-screen">
      <header className="personalization-store-header">
        <span>
          <small>{t("personalization.store.eyebrow")}</small>
          <h1>{t("personalization.store.title")}</h1>
          <p>{t("personalization.store.description")}</p>
        </span>
        <button className="store-prime-pill" onClick={() => navigate({ id: "user-wallet" })}>
          <CurrencyMark currency="PRIME" size={17} />
          <strong>{prime.toLocaleString(locale)}</strong>
        </button>
      </header>

      <section className="personalization-store-hero">
        <span>
          <small>{t("personalization.store.catalogEyebrow")}</small>
          <strong>{t("personalization.store.heroTitle")}</strong>
          <em>{t("personalization.store.heroCopy")}</em>
        </span>
        <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
      </section>

      {loading ? (
        <LoadingState />
      ) : (
        <section className="personalization-store-categories">
          {PERSONALIZATION_TYPES.map((type) => {
            const typeItems = published.filter((item) => item.type === type)
            const owned = items.filter((item) => item.type === type && item.owned).length
            const sample = typeItems[0] ?? items.find((item) => item.type === type && item.owned)

            return (
              <motion.button
                key={type}
                className={`personalization-store-category-card personalization-store-category-card--${type.toLowerCase()}`}
                whileTap={{ scale: 0.988 }}
                onClick={() => navigate({ id: "personalization-store-category", params: { type } })}
              >
                <span className="personalization-store-category-card__icon">
                  <TypeGlyph type={type} />
                </span>
                <span className="personalization-store-category-card__copy">
                  <small>{t(`personalization.target.${targets[type]}`)}</small>
                  <strong>{typeTitle(t, type)}</strong>
                  <em>{typeNote(t, type)}</em>
                  <span>
                    {t("personalization.store.catalogCount", { count: typeItems.length })}
                    {owned ? ` · ${t("personalization.store.ownedCount", { count: owned })}` : ""}
                  </span>
                </span>
                <span className="personalization-store-category-card__preview">
                  {sample ? (
                    <PersonalizationVisual item={sample} small />
                  ) : (
                    <Glyph name="spark" size={20} />
                  )}
                </span>
              </motion.button>
            )
          })}
        </section>
      )}
    </div>
  )
}

function StoreCategoryPage({
  params,
  navigate,
  back,
  items,
  loading,
}: Props & { items: Personalization[]; loading: boolean }) {
  const { t } = useTranslation()
  const type = readType(params?.type) ?? "PROFILE_THEME"
  const [currency, setCurrency] = useState<"ALL" | Currency>("ALL")
  const [page, setPage] = useState(0)
  const pageSize = 6
  const visible = useMemo(
    () =>
      items.filter(
        (item) =>
          item.status === "PUBLISHED" &&
          item.type === type &&
          (currency === "ALL" || item.currency === currency),
      ),
    [currency, items, type],
  )
  const pageCount = Math.max(1, Math.ceil(visible.length / pageSize))
  const safePage = Math.min(page, pageCount - 1)
  const pageItems = visible.slice(safePage * pageSize, safePage * pageSize + pageSize)

  return (
    <div className="screen personalization-store-category">
      <PageHeader title={typeTitle(t, type)} back={back} />

      <section className="store-category-v5__intro">
        <span className="store-category-v5__icon">
          <TypeGlyph type={type} />
        </span>
        <span>
          <small>
            {targets[type] === "ACCOUNT"
              ? t("personalization.target.accountEyebrow")
              : t("personalization.target.userEyebrow")}
          </small>
          <strong>{typeNote(t, type)}</strong>
          <em>{typeImpact(t, type)}</em>
        </span>
      </section>

      <div className="store-category-v5__toolbar">
        <span>{t("personalization.category.positions", { count: visible.length })}</span>
        <Segmented
          value={currency}
          options={["ALL", ...CURRENCIES] as const}
          onChange={(value) => {
            setCurrency(value)
            setPage(0)
          }}
          label={(value) => (value === "ALL" ? t("personalization.category.all") : value)}
        />
      </div>

      {loading ? (
        <LoadingState />
      ) : (
        <div className="store-category-v5__list">
          {pageItems.map((item) => (
            <motion.button
              key={item.id}
              className="store-category-v5__card"
              whileTap={{ scale: 0.992 }}
              onClick={() =>
                navigate({ id: "personalization-detail", params: { personalizationId: item.id } })
              }
            >
              <span className="store-category-v5__visual">
                <PersonalizationVisual item={item} />
              </span>
              <span className="store-category-v5__copy">
                <small>{typeTitle(t, item.type)}</small>
                <strong>{item.name}</strong>
                <em>{item.creator}</em>
              </span>
              <span className="store-category-v5__price">
                {item.owned ? (
                  <span className="catalog-state">
                    <Glyph name="check" size={13} />
                    {item.active
                      ? t("personalization.category.active")
                      : t("personalization.category.owned")}
                  </span>
                ) : (
                  <CurrencyValue currency={item.currency} value={item.price} compact />
                )}
              </span>
            </motion.button>
          ))}
          {!pageItems.length ? (
            <p className="inline-hint">{t("personalization.category.empty")}</p>
          ) : null}
        </div>
      )}

      {pageCount > 1 ? <Pager page={safePage} pageCount={pageCount} onChange={setPage} /> : null}
    </div>
  )
}

function OwnedPage({
  params,
  navigate,
  back,
  items,
  loading,
  busyId,
  onToggleActive,
}: Props & {
  items: Personalization[]
  loading: boolean
  busyId: string | null
  onToggleActive: (item: Personalization) => Promise<void>
}) {
  const { t } = useTranslation()
  const target = params?.target === "ACCOUNT" || params?.target === "USER" ? params.target : null
  const type = readType(params?.type)

  if (target === "ACCOUNT" && type && targets[type] === "ACCOUNT") {
    const typeItems = items.filter(
      (item) => item.owned && item.target === "ACCOUNT" && item.type === type,
    )

    return (
      <div className="screen account-personalization-category">
        <PageHeader title={typeTitle(t, type)} back={back} />
        <section className="account-personalization-category-v5__intro">
          <span className="account-personalization-category-v5__icon">
            <TypeGlyph type={type} />
          </span>
          <span>
            <small>{t("personalization.owned.accountEyebrow")}</small>
            <strong>{typeNote(t, type)}</strong>
            <em>{typeImpact(t, type)}</em>
          </span>
        </section>
        {loading ? (
          <LoadingState />
        ) : (
          <section className="account-personalization-category-v5__list">
            {typeItems.map((item) => (
              <motion.button
                key={item.id}
                className={item.active ? "is-selected" : ""}
                whileTap={{ scale: 0.992 }}
                disabled={busyId === item.id}
                onClick={() => void onToggleActive(item)}
              >
                <PersonalizationVisual item={item} />
                <span>
                  <small>
                    {item.active
                      ? t("personalization.owned.activeEyebrow")
                      : t("personalization.owned.purchasedEyebrow")}
                  </small>
                  <strong>{item.name}</strong>
                  <em>{item.creator}</em>
                </span>
                <span className="catalog-state">
                  {busyId === item.id
                    ? "…"
                    : item.active
                      ? t("personalization.owned.deselect")
                      : t("personalization.owned.select")}
                </span>
              </motion.button>
            ))}
            {!typeItems.length ? (
              <p className="inline-hint">{t("personalization.owned.typeEmpty")}</p>
            ) : null}
          </section>
        )}
      </div>
    )
  }

  if (target === "ACCOUNT") {
    const owned = items.filter((item) => item.owned && item.target === "ACCOUNT")

    return (
      <div className="screen account-personalization-overview">
        <PageHeader title={t("personalization.owned.accountTitle")} back={back} />
        <section className="account-personalization-v5__hero">
          <span>
            <small>{t("personalization.owned.accountImageEyebrow")}</small>
            <strong>{t("personalization.owned.accountImageTitle")}</strong>
            <em>
              {t("personalization.owned.accountImageStats", {
                active: owned.filter((item) => item.active).length,
                owned: owned.length,
              })}
            </em>
          </span>
          <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
        </section>
        {loading ? (
          <LoadingState />
        ) : (
          <section className="account-personalization-v5__categories">
            {accountTypes.map((itemType) => {
              const typeItems = owned.filter((item) => item.type === itemType)
              const selected = typeItems.find((item) => item.active) ?? null

              return (
                <motion.button
                  key={itemType}
                  className={selected ? "is-selected" : ""}
                  whileTap={{ scale: 0.99 }}
                  onClick={() =>
                    navigate({
                      id: "personalization-owned",
                      params: { target: "ACCOUNT", type: itemType },
                    })
                  }
                >
                  <span className="account-personalization-v5__category-icon">
                    <TypeGlyph type={itemType} />
                  </span>
                  <span className="account-personalization-v5__category-copy">
                    <small>{typeTitle(t, itemType)}</small>
                    <strong>{selected?.name ?? t("personalization.owned.notSelected")}</strong>
                    <em>{typeNote(t, itemType)}</em>
                    <span>
                      {t("personalization.owned.purchasedCount", { count: typeItems.length })}
                    </span>
                  </span>
                  <span className="account-personalization-v5__category-preview">
                    {selected ? (
                      <PersonalizationVisual item={selected} small />
                    ) : (
                      <Glyph name="spark" size={18} />
                    )}
                  </span>
                </motion.button>
              )
            })}
          </section>
        )}
      </div>
    )
  }

  const visible = items.filter((item) => item.owned && (!target || item.target === target))

  return (
    <div className="screen personalization-owned-screen">
      <PageHeader
        title={
          target === "USER"
            ? t("personalization.owned.userTitle")
            : t("personalization.owned.myTitle")
        }
        back={back}
      />
      {loading ? (
        <LoadingState />
      ) : (
        <section className="personalization-owned-list">
          {visible.map((item) => (
            <motion.button
              key={item.id}
              whileTap={{ scale: 0.992 }}
              onClick={() =>
                navigate({ id: "personalization-detail", params: { personalizationId: item.id } })
              }
            >
              <PersonalizationVisual item={item} small />
              <span>
                <small>{typeTitle(t, item.type)}</small>
                <strong>{item.name}</strong>
                <em>
                  {item.active
                    ? t("personalization.owned.active")
                    : t("personalization.owned.purchased")}
                </em>
              </span>
            </motion.button>
          ))}
          {!visible.length ? (
            <p className="inline-hint">{t("personalization.owned.empty")}</p>
          ) : null}
        </section>
      )}
    </div>
  )
}

function DetailPage({
  params,
  back,
  items,
  loading,
  busyId,
  pendingPurchases,
  onPurchase,
  onToggleActive,
}: Props & {
  items: Personalization[]
  loading: boolean
  busyId: string | null
  pendingPurchases: Record<string, string>
  onPurchase: (item: Personalization) => Promise<void>
  onToggleActive: (item: Personalization) => Promise<void>
}) {
  const { t } = useTranslation()
  const item = items.find((candidate) => candidate.id === params?.personalizationId) ?? null

  if (loading) {
    return (
      <div className="screen">
        <PageHeader title={t("personalization.detail.title")} back={back} />
        <LoadingState />
      </div>
    )
  }

  if (!item) {
    return (
      <div className="screen">
        <PageHeader title={t("personalization.detail.title")} back={back} />
        <p className="inline-hint">{t("personalization.detail.notFound")}</p>
      </div>
    )
  }

  const pending = Boolean(pendingPurchases[item.id])
  const busy = busyId === item.id

  return (
    <div className="screen personalization-detail-screen">
      <PageHeader title={item.name} back={back} />
      <section className="personalization-detail-v5__hero">
        <PersonalizationVisual item={item} detail />
        <span>
          <small>
            {typeTitle(t, item.type)} · {t(`personalization.target.${item.target}`)}
          </small>
          <strong>{item.name}</strong>
          <em>{item.creator}</em>
        </span>
      </section>

      <PersonalizationDetails item={item} />

      <section className="personalization-detail-v5__purchase">
        <span>
          <small>
            {item.owned
              ? t("personalization.detail.inCollection")
              : t("personalization.detail.price")}
          </small>
          {item.owned ? (
            <strong>
              {item.active
                ? t("personalization.detail.active")
                : t("personalization.detail.purchased")}
            </strong>
          ) : (
            <CurrencyValue currency={item.currency} value={item.price} />
          )}
        </span>

        {item.owned ? (
          <button disabled={busy} onClick={() => void onToggleActive(item)}>
            {busy
              ? t("personalization.detail.saving")
              : item.active
                ? t("personalization.detail.disable")
                : t("personalization.detail.enable")}
          </button>
        ) : (
          <button disabled={busy || pending} onClick={() => void onPurchase(item)}>
            {pending
              ? t("personalization.detail.processing")
              : busy
                ? t("personalization.detail.sending")
                : t("personalization.detail.buy")}
          </button>
        )}
      </section>
    </div>
  )
}

function TypeGlyph({ type }: { type: PersonalizationType }) {
  const glyph =
    type === "NOTIFICATION_PACK"
      ? "bell"
      : type === "NICK_STYLE"
        ? "spark"
        : type === "PROFILE_THEME"
          ? "creator"
          : type === "AVATAR_FRAME"
            ? "profile-mark"
            : "events"

  return <Glyph name={glyph} size={20} />
}

function LoadingState() {
  const { t } = useTranslation()
  return (
    <div className="events-r2-loading" aria-label={t("personalization.loading")}>
      <i />
      <i />
      <i />
    </div>
  )
}

function Pager({
  page,
  pageCount,
  onChange,
}: {
  page: number
  pageCount: number
  onChange: (page: number) => void
}) {
  const { t } = useTranslation()

  return (
    <nav className="catalog-pager" aria-label={t("personalization.pages")}>
      <button disabled={page <= 0} onClick={() => onChange(page - 1)}>
        ‹
      </button>
      <span>
        {page + 1} / {pageCount}
      </span>
      <button disabled={page >= pageCount - 1} onClick={() => onChange(page + 1)}>
        ›
      </button>
    </nav>
  )
}

function typeTitle(t: (key: string) => string, type: PersonalizationType) {
  return t(`personalization.types.${type}.title`)
}

function typeNote(t: (key: string) => string, type: PersonalizationType) {
  return t(`personalization.types.${type}.note`)
}

function typeImpact(t: (key: string) => string, type: PersonalizationType) {
  return t(`personalization.types.${type}.impact`)
}

function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }
  if (language === "ru") {
    return "ru-RU"
  }
  return "uk-UA"
}

function readType(value?: string): PersonalizationType | null {
  return PERSONALIZATION_TYPES.find((type) => type === value) ?? null
}
