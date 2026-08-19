import { motion } from "framer-motion"
import { useMemo, useState } from "react"
import { useProductData } from "../data/ProductDataProvider"
import { Glyph } from "../shared/MnemosIcon"

const INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

type Props = {
  back: () => void
}

export function AccountCreateProduction({ back }: Props) {
  const product = useProductData()
  const [nickname, setNickname] = useState("")
  const [busy, setBusy] = useState(false)
  const clean = nickname.trim()
  const valid = clean.length >= 4 && clean.length <= 20
  const touched = nickname.length > 0

  const submit = async () => {
    if (!valid || busy) return

    setBusy(true)

    try {
      await product.createAccount(clean)
      back()
    } catch {
      // The global backend error presenter handles the error. Keep the form open.
    } finally {
      setBusy(false)
    }
  }

  return (
    <OnboardingShell
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
          void submit()
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
          disabled={!valid || busy}
          whileTap={valid && !busy ? { scale: 0.975 } : undefined}
        >
          <span>{busy ? "Шукаю профіль…" : "Створити аккаунт"}</span>
          <ButtonSignal busy={busy} />
        </motion.button>
      </form>

      <div className="account-onboarding-facts">
        <Fact mark="01" title="Ти станеш власником" copy="Ролі та доступи можна налаштувати пізніше." />
        <Fact mark="02" title="Cristalix буде джерелом профілю" copy="Mnemos привʼяже UUID до знайденого nickname." />
      </div>
    </OnboardingShell>
  )
}

export function AccountJoinProduction({ back }: Props) {
  const product = useProductData()
  const [rawCode, setRawCode] = useState("")
  const [busy, setBusy] = useState(false)
  const normalized = useMemo(() => normalizeInviteCode(rawCode), [rawCode])
  const formatted = useMemo(() => formatInviteCode(normalized), [normalized])
  const valid = normalized.length === 16 && [...normalized].every((symbol) => INVITE_ALPHABET.includes(symbol))
  const touched = rawCode.length > 0

  const submit = async () => {
    if (!valid || busy) return

    setBusy(true)

    try {
      await product.joinAccount(formatInviteCode(normalized))
      back()
    } catch {
      // The global backend error presenter handles the error. Keep the form open.
    } finally {
      setBusy(false)
    }
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
    <OnboardingShell
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
          void submit()
        }}
      >
        <label className={`account-onboarding-field account-onboarding-field--code ${touched && !valid ? "is-invalid" : ""}`}>
          <span className="account-onboarding-field__label">
            <small>КОД ІНВАЙТА</small>
            <button type="button" className="account-onboarding-paste" onClick={() => void paste()}>
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
          disabled={!valid || busy}
          whileTap={valid && !busy ? { scale: 0.975 } : undefined}
        >
          <span>{busy ? "Перевіряю інвайт…" : "Увійти за кодом"}</span>
          <ButtonSignal busy={busy} />
        </motion.button>
      </form>

      <div className="account-onboarding-facts">
        <Fact mark="01" title="Код одноразовий" copy="Після успішного входу інвайт буде використано." />
        <Fact mark="02" title="Доступ визначає аккаунт" copy="Початкова роль та дозволи прийдуть від його налаштувань." />
      </div>
    </OnboardingShell>
  )
}

function OnboardingShell({
  mode,
  title,
  eyebrow,
  description,
  back,
  children,
}: {
  mode: "create" | "join"
  title: string
  eyebrow: string
  description: string
  back: () => void
  children: React.ReactNode
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
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.34, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className={`account-onboarding-hero__art account-onboarding-production-art is-${mode}`}>
          <img
            src={mode === "create"
              ? "/ui-illustrations/mnemos-mascot-search.svg"
              : "/ui-illustrations/mnemos-mascot-cat.svg"}
            alt=""
          />
          {mode === "join" ? <span className="account-onboarding-production-ticket" aria-hidden="true">CODE</span> : null}
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

function ButtonSignal({ busy }: { busy: boolean }) {
  return (
    <span className={`account-onboarding-primary__signal ${busy ? "is-busy" : ""}`} aria-hidden="true">
      <i />
      <i />
      <i />
    </span>
  )
}

function Fact({ mark, title, copy }: { mark: string; title: string; copy: string }) {
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

function normalizeInviteCode(value: string) {
  return value
    .trim()
    .toUpperCase()
    .replace(/[\s-]/g, "")
    .split("")
    .filter((symbol) => INVITE_ALPHABET.includes(symbol))
    .join("")
    .slice(0, 16)
}

function formatInviteCode(value: string) {
  return value.match(/.{1,4}/g)?.join("-") ?? value
}
