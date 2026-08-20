import { useMemo, useState } from "react"
import {
  BET_ITEM_TYPES,
  CURRENCIES,
  EVENT_BET_OUTCOMES,
  SANCTION_TYPES,
  TRANSACTION_TYPES,
  WALLET_OWNER_TYPES,
} from "../contracts"
import { betRounds, currentUser, profiles, reviewQueue } from "../mockData"
import type {
  BetItemType,
  Currency,
  EventBetOutcome,
  Personalization,
  Route,
  SanctionType,
  TransactionType,
  WalletOwnerType,
} from "../model"
import {
  itemTypeLabels,
  personalizationLabels,
  personalizationStatusLabels,
  sanctionLabels,
} from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import {
  ChoiceGrid,
  CurrencyValue,
  Field,
  PageHeader,
  Row,
  Section,
  Segmented,
} from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type Sanction = {
  id: string
  type: SanctionType
  reason: string
  duration?: number
  active: boolean
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

export function StaffFeature(props: Props) {
  if (props.routeId === "moderation-user") return <UserModeration {...props} />
  if (props.routeId === "personalization-review") return <Review {...props} />
  if (props.routeId === "bet-admin") return <BetAdmin {...props} />
  if (props.routeId === "badge-admin") return <Badges {...props} />

  return <StaffHome {...props} />
}

function StaffHome({ navigate, back }: Props) {
  const [query, setQuery] = useState("")
  const results = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return normalized.length < 2
      ? []
      : profiles.filter((profile) => profile.nickname.toLowerCase().includes(normalized))
  }, [query])

  return (
    <div className="screen moderation-screen">
      <PageHeader title="Модерація" back={back} />

      <div className="moderation-search">
        <Glyph name="search" size={18} />
        <input
          value={query}
          minLength={2}
          maxLength={64}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="користувач / аккаунт"
        />
      </div>

      {results.length ? (
        <div className="moderation-results">
          {results.map((profile) => (
            <button
              key={profile.accountId}
              onClick={() => navigate({ id: "moderation-user", params: { accountId: profile.accountId } })}
            >
              <span className="user-monogram user-monogram--small">{profile.nickname[0].toUpperCase()}</span>
              <span><strong>{profile.nickname}</strong><small>{profile.accountId.slice(0, 12)}</small></span>
              <span>›</span>
            </button>
          ))}
        </div>
      ) : null}

      <Section title="Черги">
        <Row
          icon="creator"
          title="Персоналізації"
          value={reviewQueue.length}
          onClick={() => navigate({ id: "personalization-review" })}
        />
      </Section>
    </div>
  )
}

function UserModeration({ params, back }: Props) {
  const profile = profiles.find((item) => item.accountId === params?.accountId) ?? profiles[0]
  const [sanctions, setSanctions] = useState<Sanction[]>([
    {
      id: "s1",
      type: "NOTIFICATION_SUSPENSION",
      reason: "spam",
      duration: 24,
      active: true,
    },
  ])
  const [sanctionType, setSanctionType] = useState<SanctionType>("BAN")
  const [reason, setReason] = useState("")
  const [note, setNote] = useState("")
  const [duration, setDuration] = useState("24")
  const [target, setTarget] = useState<WalletOwnerType>("USER")
  const [operation, setOperation] = useState<TransactionType>("CREDIT")
  const [amount, setAmount] = useState("100")
  const [walletReason, setWalletReason] = useState("")

  const numericDuration = Number(duration)
  const durationValid = !duration || (Number.isInteger(numericDuration) && numericDuration > 0)
  const numericAmount = Number(amount)
  const amountValid = Number.isInteger(numericAmount) && numericAmount > 0
  const adjustmentCurrency: Currency = target === "USER" ? "PRIME" : "COIN"

  const addSanction = () => {
    if (!reason.trim() || !durationValid) return

    setSanctions([
      {
        id: `s${Date.now()}`,
        type: sanctionType,
        reason: reason.trim(),
        duration: duration ? numericDuration : undefined,
        active: true,
      },
      ...sanctions,
    ])
    setReason("")
    setNote("")
  }

  return (
    <div className="screen moderation-user-redesign">
      <PageHeader title={profile.nickname} back={back} />

      <section className="moderation-target">
        <span className="user-monogram">{profile.nickname[0].toUpperCase()}</span>
        <div><strong>{profile.nickname}</strong><small>{profile.accountId}</small></div>
      </section>

      <Section title="Санкції">
        <div className="sanction-list">
          {sanctions.map((sanction) => (
            <article key={sanction.id} className={!sanction.active ? "is-inactive" : ""}>
              <span className={`sanction-mark sanction-mark--${sanction.type.toLowerCase()}`} />
              <span>
                <strong>{sanctionLabels[sanction.type]}</strong>
                <small>{sanction.reason}{sanction.duration ? ` · ${sanction.duration} год` : ""}</small>
              </span>
              {sanction.active ? (
                <button onClick={() => setSanctions(sanctions.map((item) => item.id === sanction.id ? { ...item, active: false } : item))}>Зняти</button>
              ) : <small>знято</small>}
            </article>
          ))}
        </div>
      </Section>

      <div className="moderation-form">
        <h2>Нова санкція</h2>
        <Field label="Тип">
          <ChoiceGrid value={sanctionType} options={SANCTION_TYPES} onChange={setSanctionType} labels={sanctionLabels} />
        </Field>
        <Field label="Причина"><input value={reason} onChange={(event) => setReason(event.target.value)} /></Field>
        <Field label="Внутрішня нотатка"><input value={note} onChange={(event) => setNote(event.target.value)} /></Field>
        <Field label="Тривалість, год" hint={duration && !durationValid ? "Додатне ціле або порожньо" : "Порожньо — без строку"}>
          <input type="number" min={1} step={1} value={duration} onChange={(event) => setDuration(event.target.value)} />
        </Field>
        <button className="destructive-action" disabled={!reason.trim() || !durationValid} onClick={addSanction}>Застосувати</button>
      </div>

      <div className="moderation-form moderation-wallet-form">
        <h2>Коригування гаманця</h2>
        <Field label="Ціль">
          <Segmented
            value={target}
            options={WALLET_OWNER_TYPES}
            onChange={setTarget}
            label={(value) => value === "USER" ? "User · PRIME" : "Account · COIN"}
          />
        </Field>

        <div className="moderation-wallet-scope">
          <span>{target === "USER" ? "Гаманець User" : "Гаманець Account"}</span>
          <CurrencyValue currency={adjustmentCurrency} value={0} compact />
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
            <span><CurrencyValue currency={adjustmentCurrency} value={0} compact /></span>
            <input type="number" min={1} step={1} value={amount} onChange={(event) => setAmount(event.target.value)} />
          </div>
        </Field>

        <Field label="Причина">
          <input value={walletReason} maxLength={256} onChange={(event) => setWalletReason(event.target.value)} />
        </Field>

        <button className="primary-action" disabled={!walletReason.trim() || !amountValid}>Застосувати</button>
      </div>
    </div>
  )
}

function Review({ back }: Props) {
  const [items, setItems] = useState<Personalization[]>(reviewQueue)
  const [selected, setSelected] = useState<Personalization | null>(null)
  const [currency, setCurrency] = useState<Currency>("PRIME")
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
          <button
            key={item.id}
            onClick={() => {
              setSelected(item)
              setCurrency(item.currency)
              setPrice(String(item.price))
            }}
          >
            <span className="review-type">{personalizationLabels[item.type]}</span>
            <span><strong>{item.name}</strong><small>{item.creator}</small></span>
            <span>{personalizationStatusLabels[item.status]}</span>
          </button>
        ))}
      </div>

      {selected ? (
        <section className="review-panel">
          <div className="section__head">
            <h2>{selected.name}</h2>
            <button aria-label="Закрити" onClick={() => setSelected(null)}><Glyph name="close" size={16} /></button>
          </div>

          <Field label="Ціна" hint={!priceValid && price.length ? "Додатне ціле" : undefined}>
            <div className="price-editor">
              <Segmented value={currency} options={CURRENCIES} onChange={setCurrency} />
              <input type="number" min={1} step={1} value={price} onChange={(event) => setPrice(event.target.value)} />
            </div>
          </Field>

          <button
            className="primary-action"
            disabled={!priceValid}
            onClick={() => {
              patch(selected.id, { status: "APPROVED", currency, price: numericPrice })
              setSelected(null)
            }}
          >
            Схвалити
          </button>

          <Field label="Причина відмови"><input value={reason} onChange={(event) => setReason(event.target.value)} /></Field>
          <button
            className="destructive-action"
            disabled={!reason.trim()}
            onClick={() => {
              patch(selected.id, { status: "REJECTED" })
              setSelected(null)
              setReason("")
            }}
          >
            Відхилити
          </button>

          {selected.status === "PUBLISHED" ? (
            <button className="secondary-action" onClick={() => patch(selected.id, { status: "UNLISTED" })}>Приховати</button>
          ) : selected.status === "UNLISTED" ? (
            <button className="secondary-action" onClick={() => patch(selected.id, { status: "PUBLISHED" })}>Повернути</button>
          ) : null}
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
            <Field label="Години" hint={!hoursValid && hours.length ? "1–168, ціле" : undefined}>
              <input type="number" min={1} max={168} step={1} value={hours} onChange={(event) => setHours(event.target.value)} />
            </Field>
            <Field label="Вхід" hint={!entryValid && entry.length ? "1000–5000, ціле" : undefined}>
              <input type="number" min={1000} max={5000} step={1} value={entry} onChange={(event) => setEntry(event.target.value)} />
            </Field>
          </div>
          <button
            className="primary-action"
            disabled={!itemRaceValid}
            onClick={() => {
              setCreated([`${itemName.trim()} · ${itemTypeLabels[itemType]}`, ...created])
              setItemName("")
            }}
          >
            Створити
          </button>
          {created.map((item) => <div className="inline-result" key={item}><Glyph name="check" size={14} />{item}</div>)}
        </div>
      </Section>
    </div>
  )
}

function Badges({ back }: Props) {
  const [badges, setBadges] = useState<Badge[]>([
    {
      id: "badge-1",
      code: "CREATOR",
      name: "Creator",
      icon: "◇",
      color: "#A9AFAB",
      granted: [currentUser.id],
    },
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

    setBadges(badges.map((item) => item.id === badge.id
      ? { ...item, granted: [...item.granted, cleanUserId] }
      : item))
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
