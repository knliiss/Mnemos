import { useMemo, useState } from "react"
import { BET_ITEM_TYPES, EVENT_BET_OUTCOMES, SANCTION_TYPES, TRANSACTION_TYPES } from "../contracts"
import { internalIdentities, sanctionsByUser, walletAdjustmentsByTarget } from "../moderationMockData"
import { betRounds, currentUser, reviewQueue } from "../mockData"
import type {
  BetItemType,
  EventBetOutcome,
  InternalIdentity,
  ModerationSanction,
  Personalization,
  Route,
  SanctionType,
  TransactionType,
  WalletAdjustment,
  WalletOwnerType,
} from "../model"
import { itemTypeLabels, personalizationLabels, personalizationStatusLabels, sanctionLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, CurrencyValue, Field, PageHeader, Row, Section, Segmented } from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type Badge = {
  id: string
  code: string
  name: string
  icon: string
  color: string
  granted: string[]
}

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i

export function StaffFeatureFinal(props: Props) {
  if (props.routeId === "moderation-user") return <InternalUserPage {...props} />
  if (props.routeId === "personalization-review") return <Review {...props} />
  if (props.routeId === "bet-admin") return <BetAdmin {...props} />
  if (props.routeId === "badge-admin") return <Badges {...props} />

  return <ModerationHome {...props} />
}

function ModerationHome({ navigate, back }: Props) {
  const [query, setQuery] = useState("")
  const normalized = query.trim().toLowerCase()
  const results = useMemo(() => {
    if (normalized.length < 2) return []

    return internalIdentities.filter((identity) => {
      const byUser = identity.username.toLowerCase().includes(normalized)
      const byTelegram = String(identity.telegramId).includes(normalized)
      const byId = identity.userId.toLowerCase().includes(normalized)
      const byAccount = identity.accounts.some((account) =>
        account.cristalixNickname.toLowerCase().includes(normalized) ||
        account.cristalixUuid.toLowerCase().includes(normalized) ||
        account.accountId.toLowerCase().includes(normalized),
      )

      return byUser || byTelegram || byId || byAccount
    }).slice(0, 20)
  }, [normalized])

  return (
    <div className="screen moderation-final-screen">
      <PageHeader title="Модерація" back={back} />

      <section className="internal-search-hero">
        <span className="internal-search-hero__mark"><Glyph name="search" size={20} /></span>
        <div>
          <strong>Користувачі та аккаунти</strong>
          <small>Username, Telegram ID, User ID або Cristalix</small>
        </div>
      </section>

      <div className="moderation-search moderation-search--final">
        <Glyph name="search" size={18} />
        <input
          value={query}
          minLength={2}
          maxLength={64}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Пошук"
        />
        {query ? <button aria-label="Очистити" onClick={() => setQuery("")}><Glyph name="close" size={14} /></button> : null}
      </div>

      {normalized.length > 0 && normalized.length < 2 ? <p className="inline-hint">Введи ще 1 символ</p> : null}

      {results.length ? (
        <div className="internal-search-results">
          {results.map((identity) => (
            <button key={identity.userId} onClick={() => navigate({ id: "moderation-user", params: { userId: identity.userId } })}>
              <span className="internal-search-results__avatar">{identity.username.slice(0, 1).toUpperCase()}</span>
              <span>
                <strong>@{identity.username}</strong>
                <small>{identity.telegramId} · {identity.accounts.length} акаунт.</small>
              </span>
              <b>›</b>
            </button>
          ))}
        </div>
      ) : normalized.length >= 2 ? <p className="inline-hint">Нічого не знайдено</p> : null}

      <Section title="Черги">
        <Row icon="creator" title="Персоналізації" value={reviewQueue.length} onClick={() => navigate({ id: "personalization-review" })} />
      </Section>
    </div>
  )
}

function InternalUserPage({ params, back }: Props) {
  const identity = resolveIdentity(params)
  const canAdjustWallet = currentUser.systemRoles.some((role) => role === "ADMIN" || role === "SUPER_ADMIN")
  const [sanctions, setSanctions] = useState<ModerationSanction[]>(() => sanctionsByUser[identity.userId] ?? [])
  const [sanctionFormOpen, setSanctionFormOpen] = useState(false)
  const [sanctionType, setSanctionType] = useState<SanctionType>("BAN")
  const [reason, setReason] = useState("")
  const [internalNote, setInternalNote] = useState("")
  const [duration, setDuration] = useState("")
  const [walletTarget, setWalletTarget] = useState<WalletOwnerType>("USER")
  const [accountTargetId, setAccountTargetId] = useState(identity.accounts.find((account) => account.active)?.accountId ?? identity.accounts[0]?.accountId ?? "")
  const [operation, setOperation] = useState<TransactionType>("CREDIT")
  const [amount, setAmount] = useState("100")
  const [walletReason, setWalletReason] = useState("")
  const [adjustments, setAdjustments] = useState<WalletAdjustment[]>(() => collectAdjustments(identity))

  const targetId = walletTarget === "USER" ? identity.userId : accountTargetId
  const currency = walletTarget === "USER" ? "PRIME" : "COIN"
  const numericDuration = Number(duration)
  const durationValid = !duration || (Number.isInteger(numericDuration) && numericDuration > 0)
  const numericAmount = Number(amount)
  const amountValid = Number.isInteger(numericAmount) && numericAmount > 0
  const activeBan = sanctions.find((sanction) => sanction.type === "BAN" && sanction.status === "ACTIVE")

  const openSanction = (type: SanctionType) => {
    setSanctionType(type)
    setSanctionFormOpen(true)
  }

  const createSanction = () => {
    if (!reason.trim() || !durationValid) return

    const created: ModerationSanction = {
      id: crypto.randomUUID(),
      userId: identity.userId,
      type: sanctionType,
      reason: reason.trim(),
      internalNote: internalNote.trim() || undefined,
      createdByUserId: currentUser.id,
      createdAt: "зараз",
      expiresAt: duration ? `через ${numericDuration} год` : undefined,
      status: "ACTIVE",
    }

    setSanctions([created, ...sanctions])
    setReason("")
    setInternalNote("")
    setDuration("")
    setSanctionFormOpen(false)
  }

  const revokeSanction = (id: string) => {
    setSanctions(sanctions.map((sanction) => sanction.id === id ? { ...sanction, status: "REVOKED" } : sanction))
  }

  const createAdjustment = () => {
    if (!canAdjustWallet || !targetId || !amountValid || !walletReason.trim()) return

    const operationId = crypto.randomUUID()
    const adjustment: WalletAdjustment = {
      operationId,
      requestId: crypto.randomUUID(),
      targetType: walletTarget,
      targetId,
      actorUserId: currentUser.id,
      currency,
      type: operation,
      amount: numericAmount,
      reason: walletReason.trim(),
      status: "PENDING",
      createdAt: "зараз",
    }

    setAdjustments([adjustment, ...adjustments])
    setWalletReason("")

    window.setTimeout(() => {
      setAdjustments((current) => current.map((item) => item.operationId === operationId
        ? { ...item, status: "COMPLETED", walletTransactionId: crypto.randomUUID() }
        : item))
    }, 900)
  }

  return (
    <div className="screen internal-user-screen">
      <PageHeader title={`@${identity.username}`} back={back} />

      <section className="internal-user-hero">
        <span className="internal-user-hero__avatar">{identity.username.slice(0, 1).toUpperCase()}</span>
        <span>
          <small>USER</small>
          <strong>@{identity.username}</strong>
          <em>{identity.userId}</em>
        </span>
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
              <div>
                <strong>{account.cristalixNickname}</strong>
                <small>{account.cristalixUuid}</small>
                <em>{account.accountId}</em>
              </div>
              <b>{account.active ? "активний" : "неактивний"}</b>
            </article>
          ))}
        </div>
      </Section>

      <section className="moderation-actions-final">
        {!activeBan ? (
          <button className="moderation-action-ban" onClick={() => openSanction("BAN")}>
            <Glyph name="moderation" size={19} />
            <span><strong>Забанити</strong><small>Створити санкцію BAN</small></span>
          </button>
        ) : (
          <button className="moderation-action-revoke" onClick={() => revokeSanction(activeBan.id)}>
            <Glyph name="check" size={18} />
            <span><strong>Зняти бан</strong><small>{activeBan.reason}</small></span>
          </button>
        )}
        <button onClick={() => openSanction("NOTIFICATION_SUSPENSION")}>
          <Glyph name="bell" size={19} />
          <span><strong>Сповіщення</strong><small>Призупинити доставку</small></span>
        </button>
      </section>

      <Section title="Санкції">
        <div className="sanction-list sanction-list--final">
          {sanctions.length ? sanctions.map((sanction) => (
            <article key={sanction.id} className={`sanction-final sanction-final--${sanction.status.toLowerCase()}`}>
              <span className={`sanction-mark sanction-mark--${sanction.type.toLowerCase()}`} />
              <span>
                <strong>{sanctionLabels[sanction.type]}</strong>
                <small>{sanction.reason}</small>
                <em>{sanction.status} · {sanction.expiresAt ?? "без строку"}</em>
              </span>
              {sanction.status === "ACTIVE" ? <button onClick={() => revokeSanction(sanction.id)}>Зняти</button> : null}
            </article>
          )) : <p className="inline-hint inline-hint--left">Санкцій немає</p>}
        </div>
      </Section>

      {sanctionFormOpen ? (
        <section className="moderation-form moderation-form--final">
          <div className="section__head">
            <h2>Нова санкція</h2>
            <button aria-label="Закрити" onClick={() => setSanctionFormOpen(false)}><Glyph name="close" size={15} /></button>
          </div>
          <Field label="Тип"><ChoiceGrid value={sanctionType} options={SANCTION_TYPES} onChange={setSanctionType} labels={sanctionLabels} /></Field>
          <Field label="Причина"><input value={reason} onChange={(event) => setReason(event.target.value)} /></Field>
          <Field label="Внутрішня нотатка"><input value={internalNote} onChange={(event) => setInternalNote(event.target.value)} /></Field>
          <Field label="Тривалість, год" hint={duration && !durationValid ? "Додатне ціле або порожньо" : "Порожньо — без строку"}>
            <input type="number" min={1} step={1} value={duration} onChange={(event) => setDuration(event.target.value)} />
          </Field>
          <button className="destructive-action" disabled={!reason.trim() || !durationValid} onClick={createSanction}>Створити санкцію</button>
        </section>
      ) : null}

      {canAdjustWallet ? (
        <section className="admin-wallet-panel">
          <div className="admin-wallet-panel__head">
            <span className="admin-wallet-panel__mark"><Glyph name="wallet" size={20} /></span>
            <span><strong>Коригування гаманця</strong><small>Тільки ADMIN / SUPER_ADMIN</small></span>
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
            <span><strong>{currency}</strong><small>{walletTarget === "USER" ? identity.userId : accountTargetId}</small></span>
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
            <div className="amount-input">
              <span><CurrencyMark currency={currency} size={17} /></span>
              <input type="number" min={1} step={1} value={amount} onChange={(event) => setAmount(event.target.value)} />
            </div>
          </Field>

          <Field label="Причина"><input value={walletReason} maxLength={256} onChange={(event) => setWalletReason(event.target.value)} /></Field>
          <button className="primary-action" disabled={!targetId || !amountValid || !walletReason.trim()} onClick={createAdjustment}>Застосувати</button>

          <div className="wallet-adjustment-history">
            <h3>Операції</h3>
            {adjustments.filter((item) => item.targetId === targetId).map((item) => (
              <article key={item.operationId}>
                <span className={`wallet-adjustment-status wallet-adjustment-status--${item.status.toLowerCase()}`} />
                <span><strong>{item.reason}</strong><small>{item.createdAt} · {item.status}</small></span>
                <span className={item.type === "CREDIT" ? "is-credit" : ""}>{item.type === "CREDIT" ? "+" : "−"}<CurrencyValue currency={item.currency} value={item.amount} compact /></span>
              </article>
            ))}
          </div>
        </section>
      ) : null}
    </div>
  )
}

function resolveIdentity(params?: Record<string, string>): InternalIdentity {
  const requested = params?.userId ?? params?.accountId

  return internalIdentities.find((identity) =>
    identity.userId === requested ||
    identity.username === requested ||
    identity.accounts.some((account) =>
      account.accountId === requested ||
      account.cristalixUuid === requested ||
      account.cristalixNickname === requested),
  ) ?? internalIdentities[0]
}

function collectAdjustments(identity: InternalIdentity): WalletAdjustment[] {
  return [
    ...(walletAdjustmentsByTarget[identity.userId] ?? []),
    ...identity.accounts.flatMap((account) => walletAdjustmentsByTarget[account.accountId] ?? []),
  ].sort((left, right) => right.createdAt.localeCompare(left.createdAt))
}

function Review({ back }: Props) {
  const [items, setItems] = useState<Personalization[]>(reviewQueue)
  const [selected, setSelected] = useState<Personalization | null>(null)
  const [price, setPrice] = useState("25")
  const [reason, setReason] = useState("")
  const numericPrice = Number(price)
  const priceValid = Number.isInteger(numericPrice) && numericPrice > 0

  const patch = (id: string, values: Partial<Personalization>) => {
    setItems(items.map((item) => item.id === id ? { ...item, ...values } : item))
  }

  return (
    <div className="screen">
      <PageHeader title="Перевірка" back={back} />
      <div className="review-list">
        {items.map((item) => (
          <button key={item.id} onClick={() => { setSelected(item); setPrice(String(item.price)) }}>
            <span className="review-type">{personalizationLabels[item.type]}</span>
            <span><strong>{item.name}</strong><small>{item.creator}</small></span>
            <span>{personalizationStatusLabels[item.status]}</span>
          </button>
        ))}
      </div>
      {selected ? (
        <section className="review-panel">
          <div className="section__head"><h2>{selected.name}</h2><button aria-label="Закрити" onClick={() => setSelected(null)}><Glyph name="close" size={16} /></button></div>
          <Field label="Ціна" hint={!priceValid && price.length ? "Додатне ціле" : undefined}>
            <div className="amount-input"><CurrencyMark currency={selected.currency} size={17} /><input type="number" min={1} step={1} value={price} onChange={(event) => setPrice(event.target.value)} /></div>
          </Field>
          <button className="primary-action" disabled={!priceValid} onClick={() => { patch(selected.id, { status: "APPROVED", price: numericPrice }); setSelected(null) }}>Схвалити</button>
          <Field label="Причина відмови"><input value={reason} onChange={(event) => setReason(event.target.value)} /></Field>
          <button className="destructive-action" disabled={!reason.trim()} onClick={() => { patch(selected.id, { status: "REJECTED" }); setSelected(null); setReason("") }}>Відхилити</button>
        </section>
      ) : null}
    </div>
  )
}

function BetAdmin({ back }: Props) {
  const [round, setRound] = useState(betRounds[0].id)
  const [outcome, setOutcome] = useState<EventBetOutcome>("YES")
  const [resolveReason, setResolveReason] = useState("")
  const [itemName, setItemName] = useState("")
  const [itemType, setItemType] = useState<BetItemType>("SWORD")
  const [hours, setHours] = useState("24")
  const [entry, setEntry] = useState("1000")
  const [created, setCreated] = useState<string[]>([])
  const roundLabels = Object.fromEntries(betRounds.map((item) => [item.id, `${item.eventType} · ${item.status}`]))
  const numericHours = Number(hours)
  const numericEntry = Number(entry)
  const hoursValid = Number.isInteger(numericHours) && numericHours >= 1 && numericHours <= 168
  const entryValid = Number.isInteger(numericEntry) && numericEntry >= 1000 && numericEntry <= 5000
  const itemRaceValid = Boolean(itemName.trim()) && hoursValid && entryValid

  return (
    <div className="screen">
      <PageHeader title="Ставки" back={back} />
      <Section title="Результат">
        <div className="form-stack">
          <Field label="Раунд"><ChoiceGrid value={round} options={betRounds.map((item) => item.id)} onChange={setRound} labels={roundLabels} /></Field>
          <Field label="Підсумок"><ChoiceGrid value={outcome} options={EVENT_BET_OUTCOMES} onChange={setOutcome} labels={{ YES: "Так", NO: "Ні", REFUND: "Повернення" }} /></Field>
          <Field label="Причина"><input value={resolveReason} onChange={(event) => setResolveReason(event.target.value)} /></Field>
          <button className="primary-action" disabled={!resolveReason.trim()}>Застосувати</button>
        </div>
      </Section>
      <Section title="Item race">
        <div className="form-stack">
          <Field label="Предмет"><input value={itemName} maxLength={24} onChange={(event) => setItemName(event.target.value)} /></Field>
          <Field label="Тип"><ChoiceGrid value={itemType} options={BET_ITEM_TYPES} onChange={setItemType} labels={itemTypeLabels} /></Field>
          <div className="two-fields">
            <Field label="Години" hint={!hoursValid && hours.length ? "1–168, ціле" : undefined}><input type="number" min={1} max={168} step={1} value={hours} onChange={(event) => setHours(event.target.value)} /></Field>
            <Field label="Вхід" hint={!entryValid && entry.length ? "1000–5000, ціле" : undefined}><input type="number" min={1000} max={5000} step={1} value={entry} onChange={(event) => setEntry(event.target.value)} /></Field>
          </div>
          <button className="primary-action" disabled={!itemRaceValid} onClick={() => { setCreated([`${itemName.trim()} · ${itemTypeLabels[itemType]}`, ...created]); setItemName("") }}>Створити</button>
          {created.map((item) => <div className="inline-result" key={item}><Glyph name="check" size={14} />{item}</div>)}
        </div>
      </Section>
    </div>
  )
}

function Badges({ back }: Props) {
  const [badges, setBadges] = useState<Badge[]>([
    { id: "badge-1", code: "CREATOR", name: "Creator", icon: "◇", color: "#A9AFAB", granted: [currentUser.id] },
  ])
  const [code, setCode] = useState("")
  const [name, setName] = useState("")
  const [icon, setIcon] = useState("")
  const [color, setColor] = useState("#A9AFAB")
  const [user, setUser] = useState("")
  const [selected, setSelected] = useState("badge-1")
  const validCode = /^[A-Z][A-Z0-9_]{1,31}$/.test(code)
  const cleanUserId = user.trim()
  const validUserId = UUID_PATTERN.test(cleanUserId)

  const create = () => {
    if (!validCode || !name.trim() || !icon.trim()) return

    const id = `badge-${Date.now()}`
    setBadges([...badges, { id, code, name: name.trim(), icon: icon.trim(), color, granted: [] }])
    setSelected(id)
    setCode("")
    setName("")
    setIcon("")
  }

  const badge = badges.find((item) => item.id === selected) ?? badges[0]
  const grant = () => {
    if (!validUserId || !badge || badge.granted.includes(cleanUserId)) return

    setBadges(badges.map((item) => item.id === badge.id ? { ...item, granted: [...item.granted, cleanUserId] } : item))
    setUser("")
  }

  return (
    <div className="screen">
      <PageHeader title="Бейджі" back={back} />
      <div className="moderation-form">
        <h2>Створити</h2>
        <Field label="Код" hint="A-Z, 0-9, _"><input value={code} maxLength={32} onChange={(event) => setCode(event.target.value.toUpperCase())} /></Field>
        <Field label="Назва"><input value={name} maxLength={64} onChange={(event) => setName(event.target.value)} /></Field>
        <Field label="Іконка"><input value={icon} maxLength={64} onChange={(event) => setIcon(event.target.value)} /></Field>
        <Field label="Колір"><div className="color-field"><input type="color" value={color} onChange={(event) => setColor(event.target.value.toUpperCase())} /><code>{color}</code></div></Field>
        <button className="primary-action" disabled={!validCode || !name.trim() || !icon.trim()} onClick={create}>Створити</button>
      </div>
      <Section title="Видача">
        <Field label="Бейдж"><ChoiceGrid value={selected} options={badges.map((item) => item.id)} onChange={setSelected} labels={Object.fromEntries(badges.map((item) => [item.id, item.name]))} /></Field>
        <div className="badge-sample" style={{ borderColor: badge?.color }}><span>{badge?.icon}</span><strong>{badge?.name}</strong><small>{badge?.code}</small></div>
        <Field label="User ID" hint={cleanUserId && !validUserId ? "UUID" : undefined}><input value={user} spellCheck={false} onChange={(event) => setUser(event.target.value)} /></Field>
        <button className="secondary-action full-action" disabled={!validUserId} onClick={grant}>Видати</button>
        <div className="granted-list">
          {badge?.granted.map((value) => (
            <div key={value}>
              <span>{value}</span>
              <button onClick={() => setBadges(badges.map((item) => item.id === badge.id ? { ...item, granted: item.granted.filter((granted) => granted !== value) } : item))}>Зняти</button>
            </div>
          ))}
        </div>
      </Section>
    </div>
  )
}
