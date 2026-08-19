import { motion } from "framer-motion"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { useProductData } from "../data/ProductDataProvider"
import { Glyph } from "../shared/MnemosIcon"

const INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

type Props = {
  back: () => void
}

export function AccountCreate({ back }: Props) {
  const { t } = useTranslation()
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
      title={t("accountOnboarding.create.title")}
      eyebrow={t("accountOnboarding.create.eyebrow")}
      description={t("accountOnboarding.create.description")}
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
            <small>{t("accountOnboarding.create.nicknameLabel")}</small>
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
              placeholder={t("accountOnboarding.create.nicknamePlaceholder")}
              onChange={(event) => setNickname(event.target.value)}
            />
          </span>
          <span className="account-onboarding-field__hint">
            <i className={valid ? "is-ready" : ""} />
            {valid
              ? t("accountOnboarding.create.readyHint", { nickname: clean })
              : touched
                ? t("accountOnboarding.create.invalidHint")
                : t("accountOnboarding.create.emptyHint")}
          </span>
        </label>

        <motion.button
          className="account-onboarding-primary"
          type="submit"
          disabled={!valid || busy}
          whileTap={valid && !busy ? { scale: 0.975 } : undefined}
        >
          <span>
            {busy ? t("accountOnboarding.create.busy") : t("accountOnboarding.create.submit")}
          </span>
          <ButtonSignal busy={busy} />
        </motion.button>
      </form>

      <div className="account-onboarding-facts">
        <Fact
          mark="01"
          title={t("accountOnboarding.create.factOwnerTitle")}
          copy={t("accountOnboarding.create.factOwnerCopy")}
        />
        <Fact
          mark="02"
          title={t("accountOnboarding.create.factProfileTitle")}
          copy={t("accountOnboarding.create.factProfileCopy")}
        />
      </div>
    </OnboardingShell>
  )
}

export function AccountJoin({ back }: Props) {
  const { t } = useTranslation()
  const product = useProductData()
  const [rawCode, setRawCode] = useState("")
  const [busy, setBusy] = useState(false)
  const normalized = useMemo(() => normalizeInviteCode(rawCode), [rawCode])
  const formatted = useMemo(() => formatInviteCode(normalized), [normalized])
  const valid =
    normalized.length === 16 && [...normalized].every((symbol) => INVITE_ALPHABET.includes(symbol))
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
      title={t("accountOnboarding.join.title")}
      eyebrow={t("accountOnboarding.join.eyebrow")}
      description={t("accountOnboarding.join.description")}
      back={back}
    >
      <form
        className="account-onboarding-form"
        onSubmit={(event) => {
          event.preventDefault()
          void submit()
        }}
      >
        <label
          className={`account-onboarding-field account-onboarding-field--code ${touched && !valid ? "is-invalid" : ""}`}
        >
          <span className="account-onboarding-field__label">
            <small>{t("accountOnboarding.join.codeLabel")}</small>
            <button type="button" className="account-onboarding-paste" onClick={() => void paste()}>
              {t("accountOnboarding.join.paste")}
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
              ? t("accountOnboarding.join.readyHint")
              : touched
                ? t("accountOnboarding.join.invalidHint")
                : t("accountOnboarding.join.emptyHint")}
          </span>
        </label>

        <motion.button
          className="account-onboarding-primary account-onboarding-primary--join"
          type="submit"
          disabled={!valid || busy}
          whileTap={valid && !busy ? { scale: 0.975 } : undefined}
        >
          <span>
            {busy ? t("accountOnboarding.join.busy") : t("accountOnboarding.join.submit")}
          </span>
          <ButtonSignal busy={busy} />
        </motion.button>
      </form>

      <div className="account-onboarding-facts">
        <Fact
          mark="01"
          title={t("accountOnboarding.join.factSingleUseTitle")}
          copy={t("accountOnboarding.join.factSingleUseCopy")}
        />
        <Fact
          mark="02"
          title={t("accountOnboarding.join.factAccessTitle")}
          copy={t("accountOnboarding.join.factAccessCopy")}
        />
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
  const { t } = useTranslation()
  const key = mode === "create" ? "accountOnboarding.create" : "accountOnboarding.join"

  return (
    <div className={`screen account-onboarding account-onboarding--${mode}`}>
      <header className="account-onboarding-header">
        <motion.button
          type="button"
          className="account-onboarding-back"
          onClick={back}
          whileTap={{ scale: 0.94 }}
          aria-label={t("accountOnboarding.back")}
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
        <div
          className={`account-onboarding-hero__art account-onboarding-production-art is-${mode}`}
        >
          <img
            src={
              mode === "create"
                ? "/ui-illustrations/mnemos-mascot-search.svg"
                : "/ui-illustrations/mnemos-mascot-cat.svg"
            }
            alt=""
          />
          {mode === "join" ? (
            <span className="account-onboarding-production-ticket" aria-hidden="true">
              CODE
            </span>
          ) : null}
        </div>
        <div className="account-onboarding-hero__copy">
          <small>{t(`${key}.mascotEyebrow`)}</small>
          <strong>{t(`${key}.mascotTitle`)}</strong>
          <p>{t(`${key}.mascotCopy`)}</p>
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
    <span
      className={`account-onboarding-primary__signal ${busy ? "is-busy" : ""}`}
      aria-hidden="true"
    >
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
