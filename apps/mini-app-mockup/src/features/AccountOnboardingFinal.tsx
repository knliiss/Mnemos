import { motion } from "framer-motion"
import { useMemo, useState } from "react"
import type { ReactNode } from "react"
import type { Account } from "../model"
import { Glyph } from "../shared/MnemosIcon"

type CommonProps = {
  activeAccount: Account
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  setActiveAccount: (account: Account) => void
  back: () => void
}

type MascotMode = "create" | "join"

const INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

export function AccountCreateOnboarding({ activeAccount, accounts, setAccounts, setActiveAccount, back }: CommonProps) {
  const [nickname, setNickname] = useState("")
  const clean = nickname.trim()
  const valid = clean.length >= 4 && clean.length <= 20
  const touched = nickname.length > 0

  const create = () => {
    if (!valid) return

    const id = crypto.randomUUID()
    const account: Account = {
      id,
      cristalixUuid: id,
      nickname: clean,
      role: "Власник",
      skinUrl: activeAccount.skinUrl,
      memberCount: 1,
      maxMembers: 6,
      owner: true,
      active: true,
      coin: 0,
    }

    setAccounts([...accounts, account])
    setActiveAccount(account)
    back()
  }

  return (
    <AccountOnboardingShell
      mode="create"
      title="Новий аккаунт"
      eyebrow="CRISTALIX → MNEMOS"
      description="Знайдемо твій Cristalix профіль і створимо для нього окремий аккаунт Mnemos."
      back={back}
    >
      <form
        className="account-onboarding-form"
        onSubmit={(event) => {
          event.preventDefault()
          create()
        }}
      >
        <label className={`account-onboarding-field ${touched && !valid ? "is-invalid" : ""}`}>
          <span className="account-onboarding-field__label">
            <small>CRISTALIX NICKNAME</small>
            <b>{clean.length}/20</b>
          </span>
          <span className="account-onboarding-field__control">
            <Glyph name="search" size={19} />
            <input
              autoFocus
              autoComplete="off"
              autoCorrect="off"
              spellCheck={false}
              value={nickname}
              minLength={4}
              maxLength={20}
              placeholder="Наприклад, knalis"
              onChange={(event) => setNickname(event.target.value)}
            />
          </span>
          <span className="account-onboarding-field__hint">
            <i className={valid ? "is-ready" : ""} />
            {valid
              ? `Перевіримо ${clean} на Cristalix`
              : touched
                ? "Nickname має містити від 4 до 20 символів"
                : "Достатньо nickname — UUID підтягнеться автоматично"}
          </span>
        </label>

        <motion.button
          className="account-onboarding-primary"
          type="submit"
          disabled={!valid}
          whileTap={valid ? { scale: 0.975 } : undefined}
        >
          <span>Створити аккаунт</span>
          <span className="account-onboarding-primary__signal" aria-hidden="true">
            <i />
            <i />
            <i />
          </span>
        </motion.button>
      </form>

      <div className="account-onboarding-facts" aria-label="Що відбудеться після створення">
        <OnboardingFact mark="01" title="Ти станеш власником" copy="Ролі та доступи можна налаштувати пізніше." />
        <OnboardingFact mark="02" title="Cristalix буде джерелом профілю" copy="Mnemos привʼяже UUID до знайденого nickname." />
      </div>
    </AccountOnboardingShell>
  )
}

export function AccountJoinOnboarding({ activeAccount, accounts, setAccounts, setActiveAccount, back }: CommonProps) {
  const [rawCode, setRawCode] = useState("")
  const normalized = useMemo(() => normalizeInviteCode(rawCode), [rawCode])
  const formatted = useMemo(() => formatInviteCode(normalized), [normalized])
  const valid = normalized.length === 16 && [...normalized].every((symbol) => INVITE_ALPHABET.includes(symbol))
  const touched = rawCode.length > 0

  const join = () => {
    if (!valid) return

    const id = crypto.randomUUID()
    const joined: Account = {
      id,
      cristalixUuid: id,
      nickname: "north",
      role: "Учасник",
      skinUrl: activeAccount.skinUrl,
      memberCount: 5,
      maxMembers: 8,
      owner: false,
      active: true,
      coin: 0,
    }

    setAccounts([...accounts, joined])
    setActiveAccount(joined)
    back()
  }

  const paste = async () => {
    try {
      const value = await navigator.clipboard?.readText()
      if (value) setRawCode(value)
    } catch {
      // Clipboard permission is optional. Manual input remains available.
    }
  }

  return (
    <AccountOnboardingShell
      mode="join"
      title="Вхід за кодом"
      eyebrow="INVITE ACCESS"
      description="Встав код запрошення — Mnemos перевірить його і додасть тебе до потрібного аккаунта."
      back={back}
    >
      <form
        className="account-onboarding-form"
        onSubmit={(event) => {
          event.preventDefault()
          join()
        }}
      >
        <label className={`account-onboarding-field account-onboarding-field--code ${touched && !valid ? "is-invalid" : ""}`}>
          <span className="account-onboarding-field__label">
            <small>КОД ІНВАЙТА</small>
            <button
              type="button"
              className="account-onboarding-paste"
              onClick={paste}
            >
              Вставити
            </button>
          </span>
          <span className="account-onboarding-field__control account-onboarding-field__control--code">
            <input
              autoFocus
              autoCapitalize="characters"
              autoComplete="off"
              autoCorrect="off"
              spellCheck={false}
              inputMode="text"
              value={formatted}
              maxLength={19}
              placeholder="XXXX-XXXX-XXXX-XXXX"
              onChange={(event) => setRawCode(event.target.value)}
            />
          </span>
          <span className="account-onboarding-field__hint">
            <i className={valid ? "is-ready" : ""} />
            {valid
              ? "Формат правильний — код готовий до перевірки"
              : touched
                ? "16 символів · A–Z без I/O · цифри 2–9"
                : "Код складається з чотирьох груп по 4 символи"}
          </span>
        </label>

        <motion.button
          className="account-onboarding-primary account-onboarding-primary--join"
          type="submit"
          disabled={!valid}
          whileTap={valid ? { scale: 0.975 } : undefined}
        >
          <span>Увійти за кодом</span>
          <span className="account-onboarding-primary__signal" aria-hidden="true">
            <i />
            <i />
            <i />
          </span>
        </motion.button>
      </form>

      <div className="account-onboarding-facts" aria-label="Як працює код запрошення">
        <OnboardingFact mark="01" title="Код одноразовий" copy="Після успішного входу інвайт буде використано." />
        <OnboardingFact mark="02" title="Доступ визначає аккаунт" copy="Початкова роль та дозволи прийдуть від його налаштувань." />
      </div>
    </AccountOnboardingShell>
  )
}

function AccountOnboardingShell({
  mode,
  title,
  eyebrow,
  description,
  back,
  children,
}: {
  mode: MascotMode
  title: string
  eyebrow: string
  description: string
  back: () => void
  children: ReactNode
}) {
  return (
    <div className={`screen account-onboarding account-onboarding--${mode}`}>
      <header className="account-onboarding-header">
        <motion.button
          type="button"
          className="account-onboarding-back"
          onClick={back}
          whileTap={{ scale: 0.94 }}
          aria-label="Назад"
        >
          <Glyph name="back" size={22} />
        </motion.button>

        <div className="account-onboarding-heading">
          <small>{eyebrow}</small>
          <h1>{title}</h1>
          <p>{description}</p>
        </div>
      </header>

      <motion.section
        className="account-onboarding-hero"
        initial={false}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="account-onboarding-hero__art">
          <AccountOnboardingMascot mode={mode} />
        </div>
        <div className="account-onboarding-hero__copy">
          <small>{mode === "create" ? "Я ДОПОМОЖУ З ПРИВʼЯЗКОЮ" : "Я ПЕРЕВІРЮ ІНВАЙТ"}</small>
          <strong>{mode === "create" ? "Один nickname — і можна починати" : "Встав код без зайвих кроків"}</strong>
          <p>{mode === "create" ? "Спочатку знайдемо профіль, потім створимо Mnemos account." : "Після перевірки одразу перемкнемось на новий аккаунт."}</p>
          <span className="account-onboarding-route" aria-hidden="true">
            <i />
            <b>{mode === "create" ? "C7X" : "CODE"}</b>
            <em />
            <b>MNEMOS</b>
          </span>
        </div>
      </motion.section>

      {children}
    </div>
  )
}

function OnboardingFact({ mark, title, copy }: { mark: string; title: string; copy: string }) {
  return (
    <article className="account-onboarding-fact">
      <span>{mark}</span>
      <div>
        <strong>{title}</strong>
        <small>{copy}</small>
      </div>
    </article>
  )
}

function AccountOnboardingMascot({ mode }: { mode: MascotMode }) {
  const accent = mode === "create" ? "#B9DE38" : "#D6A653"
  const secondary = mode === "create" ? "#8CA66A" : "#B08347"

  return (
    <svg
      className={`account-onboarding-mascot account-onboarding-mascot--${mode}`}
      viewBox="0 0 168 132"
      shapeRendering="crispEdges"
      preserveAspectRatio="xMidYMid meet"
      aria-hidden="true"
    >
      <g className="account-onboarding-mascot__shadow">
        <rect x="36" y="118" width="76" height="4" fill="#080A08" opacity=".5" />
        <rect x="48" y="122" width="52" height="3" fill="#080A08" opacity=".22" />
      </g>

      <g className="account-onboarding-mascot__tail">
        <rect x="104" y="84" width="20" height="5" fill="#121512" />
        <rect x="120" y="88" width="12" height="5" fill="#343934" />
        <rect x="128" y="92" width="6" height="17" fill="#343934" />
        <rect x="122" y="105" width="10" height="5" fill="#444A44" />
      </g>

      <g className="account-onboarding-mascot__cat">
        <rect x="46" y="75" width="64" height="36" fill="#121512" />
        <rect x="52" y="79" width="52" height="28" fill="#343934" />
        <rect x="68" y="80" width="20" height="24" fill="#DDD6C8" />
        <rect x="73" y="80" width="10" height="24" fill="#EFE9DE" />
        <rect x="48" y="104" width="22" height="8" fill="#202420" />
        <rect x="86" y="104" width="22" height="8" fill="#202420" />

        <rect x="42" y="24" width="9" height="22" fill="#111411" />
        <rect x="47" y="19" width="10" height="22" fill="#111411" />
        <rect x="101" y="19" width="10" height="22" fill="#111411" />
        <rect x="107" y="24" width="9" height="22" fill="#111411" />
        <rect x="48" y="23" width="4" height="11" fill="#A87980" />
        <rect x="106" y="23" width="4" height="11" fill="#A87980" />

        <rect x="39" y="38" width="80" height="34" fill="#111411" />
        <rect x="45" y="36" width="68" height="32" fill="#383E38" />
        <rect x="52" y="40" width="54" height="5" fill="#454C45" />

        <g className="account-onboarding-mascot__eyes-open">
          <rect x="52" y="49" width="18" height="12" fill="#171A17" />
          <rect x="88" y="49" width="18" height="12" fill="#171A17" />
          <rect x="56" y="49" width="10" height="9" fill="#B9CE7D" />
          <rect x="92" y="49" width="10" height="9" fill="#B9CE7D" />
          <rect x="60" y="51" width="4" height="7" fill="#090B09" />
          <rect x="96" y="51" width="4" height="7" fill="#090B09" />
          <rect x="60" y="49" width="3" height="3" fill="#F4F6E8" />
          <rect x="96" y="49" width="3" height="3" fill="#F4F6E8" />
        </g>
        <g className="account-onboarding-mascot__eyes-closed">
          <rect x="55" y="55" width="12" height="3" fill="#AEBB78" />
          <rect x="91" y="55" width="12" height="3" fill="#AEBB78" />
        </g>

        <rect x="63" y="60" width="32" height="8" fill="#E4DDD1" />
        <rect x="70" y="68" width="18" height="5" fill="#D2C9BB" />
        <rect x="75" y="59" width="8" height="5" fill="#B68287" />
        <rect x="73" y="67" width="4" height="3" fill="#60635E" />
        <rect x="81" y="67" width="4" height="3" fill="#60635E" />

        <rect x="60" y="75" width="38" height="4" fill="#B58D4C" />
        <rect x="77" y="78" width="5" height="7" fill="#111411" />
        <rect x="78" y="80" width="3" height="3" fill={accent} />
      </g>

      {mode === "create" ? (
        <g className="account-onboarding-mascot__accessory account-onboarding-mascot__accessory--create">
          <rect x="118" y="42" width="38" height="48" fill="#101410" />
          <rect x="122" y="46" width="30" height="39" fill="#263026" />
          <rect x="127" y="51" width="20" height="16" fill="#D8D6C8" />
          <rect x="132" y="55" width="10" height="8" fill="#AEB5A8" />
          <rect x="127" y="72" width="15" height="4" fill="#697365" />
          <rect x="127" y="79" width="20" height="3" fill="#495347" />
          <rect x="118" y="63" width="38" height="4" fill={accent} opacity=".72" className="account-onboarding-mascot__scan" />
          <rect x="151" y="49" width="4" height="8" fill={secondary} />
          <rect x="157" y="58" width="4" height="4" fill={accent} />
          <rect x="115" y="35" width="4" height="4" fill="#9D7DA3" />
        </g>
      ) : (
        <g className="account-onboarding-mascot__accessory account-onboarding-mascot__accessory--join">
          <rect x="116" y="51" width="43" height="31" fill="#171812" />
          <rect x="120" y="47" width="35" height="34" fill={secondary} />
          <rect x="124" y="51" width="27" height="26" fill="#E0B767" />
          <rect x="128" y="56" width="5" height="5" fill="#5A4526" />
          <rect x="137" y="56" width="5" height="5" fill="#5A4526" />
          <rect x="146" y="56" width="5" height="5" fill="#5A4526" />
          <rect x="128" y="66" width="19" height="4" fill="#8A6632" />
          <rect x="155" y="54" width="4" height="5" fill={accent} />
          <rect x="159" y="63" width="4" height="4" fill="#B8D94A" />
          <rect x="112" y="42" width="4" height="4" fill="#9D7DA3" />
        </g>
      )}

      <g className="account-onboarding-mascot__pixels">
        <rect x="26" y="47" width="4" height="4" fill={accent} opacity=".75" />
        <rect x="20" y="57" width="3" height="3" fill="#9D7DA3" opacity=".75" />
        <rect x="28" y="95" width="4" height="4" fill={secondary} opacity=".65" />
      </g>
    </svg>
  )
}

function normalizeInviteCode(value: string) {
  return value
    .trim()
    .toUpperCase()
    .replace(/[\s-]/g, "")
    .slice(0, 16)
}

function formatInviteCode(value: string) {
  return value.match(/.{1,4}/g)?.join("-") ?? value
}
