import { motion } from "framer-motion"
import { useCallback, useEffect, useMemo, useState } from "react"
import {
  createSanction,
  createWalletAdjustment,
  getWalletAdjustment,
  listSanctions,
  listWalletAdjustments,
  revokeSanction,
  searchIdentities,
  type IdentitySearchResponse,
  type SanctionResponse,
  type WalletAdjustmentResponse,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { SANCTION_TYPES, TRANSACTION_TYPES } from "../contracts"
import { useProductData } from "../data/ProductDataProvider"
import type {
  Route,
  SanctionType,
  TransactionType,
  WalletOwnerType,
} from "../model"
import { sanctionLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import {
  ChoiceGrid,
  Field,
  PageHeader,
  Section,
  Segmented,
} from "../shared/Primitives"

type HomeProps = {
  navigate: (route: Route) => void
  back: () => void
}

type UserProps = {
  params?: Record<string, string>
  back: () => void
}

export function ModerationHomeProduction({ navigate, back }: HomeProps) {
  const [query, setQuery] = useState("")
  const [results, setResults] = useState<IdentitySearchResponse[]>([])
  const [loading, setLoading] = useState(false)
  const normalized = query.trim()

  useEffect(() => {
    if (normalized.length < 2) {
      setResults([])
      setLoading(false)
      return
    }

    setLoading(true)
    const timer = window.setTimeout(() => {
      void searchIdentities(normalized)
        .then(setResults)
        .catch(handleError)
        .finally(() => setLoading(false))
    }, 250)

    return () => window.clearTimeout(timer)
  }, [normalized])

  return (
    <div className="screen moderation-polished-screen">
      <PageHeader title="Модерація" back={back} />

      <section className="moderation-polished-hero">
        <span className="moderation-polished-hero__icon"><Glyph name="moderation" size={23} /></span>
        <span>
          <small>INTERNAL</small>
          <strong>Користувачі та аккаунти</strong>
          <p>Пошук і дії модерації працюють напряму з account-service та moderation-service.</p>
        </span>
      </section>

      <label className="moderation-polished-search">
        <Glyph name="search" size={19} />
        <input
          autoFocus
          value={query}
          minLength={2}
          maxLength={64}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Username, Telegram ID, User ID або Cristalix"
        />
        {query ? <button aria-label="Очистити" onClick={() => setQuery("")}><Glyph name="close" size={14} /></button> : null}
      </label>

      {!normalized ? (
        <section className="moderation-polished-help">
          <span>Можна шукати за</span>
          <div><i>Username</i><i>Telegram ID</i><i>User ID</i><i>Cristalix</i></div>
        </section>
      ) : null}

      {normalized.length === 1 ? <p className="moderation-polished-empty">Введи ще 1 символ</p> : null}
      {loading ? <LoadingState /> : null}

      {!loading && results.length ? (
        <section className="moderation-polished-results">
          <header><strong>Результати</strong><small>{results.length}</small></header>
          <div>
            {results.map((identity) => (
              <motion.button
                key={identity.userId}
                whileTap={{ scale: 0.985 }}
                onClick={() => navigate({ id: "moderation-user", params: { userId: identity.userId } })}
              >
                <span className="moderation-polished-results__avatar">{identity.username.slice(0, 1).toUpperCase()}</span>
                <span><strong>@{identity.username}</strong><small>{identity.telegramId}</small></span>
                <span className="moderation-polished-results__accounts">{identity.accounts.length} акаунт.</span>
                <b>›</b>
              </motion.button>
            ))}
          </div>
        </section>
      ) : null}

      {!loading && normalized.length >= 2 && !results.length ? (
        <p className="moderation-polished-empty">Нічого не знайдено</p>
      ) : null}
    </div>
  )
}

export function ModerationUserProduction({ params, back }: UserProps) {
  const product = useProductData()
  const userId = params?.userId ?? ""
  const canAdjustWallet = product.user.systemRoles.some((role) => role === "ADMIN" || role === "SUPER_ADMIN")
  const [identity, setIdentity] = useState<IdentitySearchResponse | null>(null)
  const [sanctions, setSanctions] = useState<SanctionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [sanctionFormOpen, setSanctionFormOpen] = useState(false)
  const [sanctionType, setSanctionType] = useState<SanctionType>("BAN")
  const [reason, setReason] = useState("")
  const [internalNote, setInternalNote] = useState("")
  const [duration, setDuration] = useState("")
  const [walletTarget, setWalletTarget] = useState<WalletOwnerType>("USER")
  const [accountTargetId, setAccountTargetId] = useState("")
  const [operation, setOperation] = useState<TransactionType>("CREDIT")
  const [amount, setAmount] = useState("100")
  const [walletReason, setWalletReason] = useState("")
  const [adjustments, setAdjustments] = useState<WalletAdjustmentResponse[]>([])

  const refreshIdentity = useCallback(async () => {
    if (!userId) return

    const matches = await searchIdentities(userId, 5)
    const found = matches.find((item) => item.userId === userId) ?? matches[0] ?? null
    setIdentity(found)

    if (found && !accountTargetId) {
      setAccountTargetId(found.accounts.find((account) => account.active)?.accountId ?? found.accounts[0]?.accountId ?? "")
    }
  }, [accountTargetId, userId])

  const refreshSanctions = useCallback(async () => {
    if (!userId) return
    setSanctions(await listSanctions(userId))
  }, [userId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void Promise.all([refreshIdentity(), refreshSanctions()])
        .catch(handleError)
        .finally(() => setLoading(false))
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refreshIdentity, refreshSanctions])

  const targetId = walletTarget === "USER" ? userId : accountTargetId
  const currency = walletTarget === "USER" ? "PRIME" as const : "COIN" as const

  useEffect(() => {
    if (!canAdjustWallet || !targetId) {
      setAdjustments([])
      return
    }

    const frame = window.requestAnimationFrame(() => {
      void listWalletAdjustments(walletTarget, targetId)
        .then(setAdjustments)
        .catch(handleError)
    })

    return () => window.cancelAnimationFrame(frame)
  }, [canAdjustWallet, targetId, walletTarget])

  const numericDuration = Number(duration)
  const durationValid = !duration || (Number.isInteger(numericDuration) && numericDuration > 0)
  const numericAmount = Number(amount)
  const amountValid = Number.isInteger(numericAmount) && numericAmount > 0
  const activeBan = sanctions.find((sanction) => sanction.type === "BAN" && sanction.status === "ACTIVE")

  const submitSanction = async () => {
    if (!userId || !reason.trim() || !durationValid || busy) return

    setBusy(true)

    try {
      await createSanction({
        userId,
        type: sanctionType,
        reason: reason.trim(),
        internalNote: internalNote.trim() || null,
        durationHours: duration ? numericDuration : null,
      })
      setReason("")
      setInternalNote("")
      setDuration("")
      setSanctionFormOpen(false)
      await refreshSanctions()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const revoke = async (sanctionId: string) => {
    if (busy) return
    setBusy(true)

    try {
      await revokeSanction(sanctionId)
      await refreshSanctions()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const pollAdjustment = async (operationId: string) => {
    for (let attempt = 0; attempt < 15; attempt += 1) {
      await new Promise((resolve) => window.setTimeout(resolve, 800))
      const current = await getWalletAdjustment(operationId)

      if (current.status === "PENDING") continue

      setAdjustments(await listWalletAdjustments(walletTarget, targetId))
      return
    }
  }

  const submitAdjustment = async () => {
    if (!canAdjustWallet || !targetId || !amountValid || !walletReason.trim() || busy) return

    setBusy(true)

    try {
      const created = await createWalletAdjustment({
        targetType: walletTarget,
        targetId,
        currency,
        type: operation,
        amount: numericAmount,
        reason: walletReason.trim(),
      })
      setAdjustments((current) => [created, ...current.filter((item) => item.operationId !== created.operationId)])
      setWalletReason("")
      void pollAdjustment(created.operationId).catch(handleError)
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  if (loading) {
    return <div className="screen internal-user-screen"><PageHeader title="Модерація" back={back} /><LoadingState /></div>
  }

  if (!identity) {
    return <div className="screen internal-user-screen"><PageHeader title="Модерація" back={back} /><p className="inline-hint">Користувача не знайдено</p></div>
  }

  return (
    <div className="screen internal-user-screen">
      <PageHeader title={`@${identity.username}`} back={back} />

      <section className="internal-user-hero">
        <span className="internal-user-hero__avatar">{identity.username.slice(0, 1).toUpperCase()}</span>
        <span><small>USER</small><strong>@{identity.username}</strong><em>{identity.userId}</em></span>
        <span className={activeBan ? "internal-user-state is-banned" : "internal-user-state"}>{activeBan ? "BAN" : "OK"}</span>
      </section>

      <section className="internal-info-grid">
        <article><small>Telegram ID</small><strong>{identity.telegramId}</strong></article>
        <article><small>Аккаунтів</small><strong>{identity.accounts.length}</strong></article>
      </section>

      <Section title="Cristalix аккаунти">
        <div className="internal-account-list">
          {identity.accounts.map((account) => (
            <article key={account.accountId}>
              <span className={account.active ? "is-active" : ""}><i /></span>
              <div><strong>{account.cristalixNickname}</strong><small>{account.cristalixUuid}</small><em>{account.accountId}</em></div>
              <b>{account.active ? "активний" : "неактивний"}</b>
            </article>
          ))}
        </div>
      </Section>

      <section className="moderation-actions-final">
        {!activeBan ? (
          <button onClick={() => { setSanctionType("BAN"); setSanctionFormOpen(true) }}>
            <Glyph name="moderation" size={19} />
            <span><strong>Забанити</strong><small>Створити санкцію BAN</small></span>
          </button>
        ) : (
          <button className="moderation-action-revoke" disabled={busy} onClick={() => void revoke(activeBan.id)}>
            <Glyph name="check" size={18} />
            <span><strong>Зняти бан</strong><small>{activeBan.reason}</small></span>
          </button>
        )}
        <button onClick={() => { setSanctionType("NOTIFICATION_SUSPENSION"); setSanctionFormOpen(true) }}>
          <Glyph name="bell" size={19} />
          <span><strong>Сповіщення</strong><small>Призупинити доставку</small></span>
        </button>
      </section>

      <Section title="Санкції">
        <div className="sanction-list sanction-list--final">
          {sanctions.map((sanction) => (
            <article key={sanction.id} className={`sanction-final sanction-final--${sanction.status.toLowerCase()}`}>
              <span className={`sanction-mark sanction-mark--${sanction.type.toLowerCase()}`} />
              <span>
                <strong>{sanctionLabels[sanction.type]}</strong>
                <small>{sanction.reason}</small>
                <em>{sanction.status} · {sanction.expiresAt ? formatDate(sanction.expiresAt) : "без строку"}</em>
              </span>
              {sanction.status === "ACTIVE" ? <button disabled={busy} onClick={() => void revoke(sanction.id)}>Зняти</button> : null}
            </article>
          ))}
          {!sanctions.length ? <p className="inline-hint inline-hint--left">Санкцій немає</p> : null}
        </div>
      </Section>

      {sanctionFormOpen ? (
        <section className="moderation-form moderation-form--final">
          <div className="section__head"><h2>Нова санкція</h2><button aria-label="Закрити" onClick={() => setSanctionFormOpen(false)}><Glyph name="close" size={15} /></button></div>
          <Field label="Тип"><ChoiceGrid value={sanctionType} options={SANCTION_TYPES} onChange={setSanctionType} labels={sanctionLabels} /></Field>
          <Field label="Причина"><input value={reason} onChange={(event) => setReason(event.target.value)} /></Field>
          <Field label="Внутрішня нотатка"><input value={internalNote} onChange={(event) => setInternalNote(event.target.value)} /></Field>
          <Field label="Тривалість, год" hint={duration && !durationValid ? "Додатне ціле або порожньо" : "Порожньо — без строку"}>
            <input type="number" min={1} step={1} value={duration} onChange={(event) => setDuration(event.target.value)} />
          </Field>
          <button className="destructive-action" disabled={!reason.trim() || !durationValid || busy} onClick={() => void submitSanction()}>
            {busy ? "Зберігаємо…" : "Створити санкцію"}
          </button>
        </section>
      ) : null}

      {canAdjustWallet ? (
        <section className="admin-wallet-panel">
          <div className="admin-wallet-panel__head">
            <span className="admin-wallet-panel__mark"><Glyph name="wallet" size={20} /></span>
            <span><strong>Коригування гаманця</strong><small>ADMIN / SUPER_ADMIN</small></span>
          </div>

          <Field label="Ціль">
            <Segmented
              value={walletTarget}
              options={["USER", "ACCOUNT"] as const}
              onChange={setWalletTarget}
              label={(value) => value === "USER" ? "User · PRIME" : "Account · COIN"}
            />
          </Field>

          {walletTarget === "ACCOUNT" ? (
            <Field label="Аккаунт">
              <ChoiceGrid
                value={accountTargetId}
                options={identity.accounts.map((account) => account.accountId)}
                onChange={setAccountTargetId}
                labels={Object.fromEntries(identity.accounts.map((account) => [account.accountId, account.cristalixNickname]))}
              />
            </Field>
          ) : null}

          <div className="admin-wallet-scope">
            <CurrencyMark currency={currency} size={18} />
            <span><strong>{currency}</strong><small>{targetId}</small></span>
          </div>

          <Field label="Операція">
            <Segmented
              value={operation}
              options={TRANSACTION_TYPES}
              onChange={setOperation}
              label={(value) => value === "CREDIT" ? "Нарахувати" : "Списати"}
            />
          </Field>

          <Field label="Сума" hint={!amountValid && amount.length ? "Додатне ціле" : undefined}>
            <div className="amount-input"><span><CurrencyMark currency={currency} size={17} /></span><input type="number" min={1} step={1} value={amount} onChange={(event) => setAmount(event.target.value)} /></div>
          </Field>
          <Field label="Причина"><input value={walletReason} maxLength={256} onChange={(event) => setWalletReason(event.target.value)} /></Field>
          <button className="primary-action" disabled={!targetId || !amountValid || !walletReason.trim() || busy} onClick={() => void submitAdjustment()}>
            {busy ? "Надсилаємо…" : "Застосувати"}
          </button>

          <div className="wallet-adjustment-history">
            <h3>Операції</h3>
            {adjustments.map((item) => (
              <article key={item.operationId}>
                <i className={`is-${item.status.toLowerCase()}`} />
                <span><strong>{item.type === "CREDIT" ? "+" : "−"}{item.amount} {item.currency}</strong><small>{item.reason}</small></span>
                <span><strong>{item.status}</strong><small>{formatDate(item.createdAt)}</small></span>
              </article>
            ))}
            {!adjustments.length ? <p className="inline-hint inline-hint--left">Операцій немає</p> : null}
          </div>
        </section>
      ) : null}
    </div>
  )
}

function LoadingState() {
  return <div className="events-r2-loading" aria-label="Завантаження"><i /><i /><i /></div>
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("uk-UA", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}
