import { motion } from "framer-motion"
import { useMemo, useState } from "react"
import { internalIdentities } from "../moderationMockData"
import type { Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function ModerationHomePolished({ navigate, back }: Props) {
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
    <div className="screen moderation-polished-screen">
      <PageHeader title="Модерація" back={back} />

      <section className="moderation-polished-hero">
        <span className="moderation-polished-hero__icon"><Glyph name="moderation" size={23} /></span>
        <span>
          <small>INTERNAL</small>
          <strong>Користувачі та аккаунти</strong>
          <p>Пошук і дії модерації без змішування з іншими адміністративними областями.</p>
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

      {normalized.length === 0 ? (
        <section className="moderation-polished-help">
          <span>Можна шукати за</span>
          <div>
            <i>Username</i>
            <i>Telegram ID</i>
            <i>User ID</i>
            <i>Cristalix</i>
          </div>
        </section>
      ) : null}

      {normalized.length > 0 && normalized.length < 2 ? <p className="moderation-polished-empty">Введи ще 1 символ</p> : null}

      {results.length ? (
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
                <span>
                  <strong>@{identity.username}</strong>
                  <small>{identity.telegramId}</small>
                </span>
                <span className="moderation-polished-results__accounts">{identity.accounts.length} акаунт.</span>
                <b>›</b>
              </motion.button>
            ))}
          </div>
        </section>
      ) : normalized.length >= 2 ? <p className="moderation-polished-empty">Нічого не знайдено</p> : null}
    </div>
  )
}
