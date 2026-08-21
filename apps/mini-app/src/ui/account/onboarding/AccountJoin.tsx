import { motion } from "framer-motion"
import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { ButtonSignal, OnboardingFact, OnboardingShell } from "./OnboardingShell"

const INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

type Props = {
  back: () => void
}

export function AccountJoin({ back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [rawCode, setRawCode] = useState("")
  const [busy, setBusy] = useState(false)
  const [pasteFailed, setPasteFailed] = useState(false)
  const submitLocked = useRef(false)
  const mountedRef = useRef(true)
  const normalizedCode = useMemo(() => normalizeInviteCode(rawCode), [rawCode])
  const formattedCode = useMemo(() => formatInviteCode(normalizedCode), [normalizedCode])
  const valid =
    normalizedCode.length === 16 &&
    [...normalizedCode].every((symbol) => INVITE_ALPHABET.includes(symbol))
  const touched = rawCode.length > 0

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  const submit = async () => {
    if (!valid || submitLocked.current) {
      return
    }

    submitLocked.current = true
    setBusy(true)

    try {
      const succeeded = await data.joinAccount(formatInviteCode(normalizedCode))

      if (succeeded && mountedRef.current) {
        back()
      }
    } finally {
      submitLocked.current = false

      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  const paste = async () => {
    setPasteFailed(false)

    try {
      const value = await navigator.clipboard?.readText()

      if (value) {
        setRawCode(value)
      }
    } catch {
      setPasteFailed(true)
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
              value={formattedCode}
              maxLength={19}
              placeholder="XXXX-XXXX-XXXX-XXXX"
              onChange={(event) => {
                setPasteFailed(false)
                setRawCode(event.target.value)
              }}
            />
          </span>

          <span className="account-onboarding-field__hint">
            <i className={valid ? "is-ready" : ""} />
            {pasteFailed
              ? t("accountOnboarding.join.pasteUnavailable")
              : valid
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
          <span>{busy ? t("accountOnboarding.join.busy") : t("accountOnboarding.join.submit")}</span>
          <ButtonSignal busy={busy} />
        </motion.button>
      </form>

      <div className="account-onboarding-facts">
        <OnboardingFact
          mark="01"
          title={t("accountOnboarding.join.factSingleUseTitle")}
          copy={t("accountOnboarding.join.factSingleUseCopy")}
        />
        <OnboardingFact
          mark="02"
          title={t("accountOnboarding.join.factAccessTitle")}
          copy={t("accountOnboarding.join.factAccessCopy")}
        />
      </div>
    </OnboardingShell>
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
