import { motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { useMnemosData } from "../../data/MnemosDataProvider"
import { Glyph } from "../../shared/MnemosIcon"
import { ButtonSignal, OnboardingFact, OnboardingShell } from "./OnboardingShell"

type Props = {
  back: () => void
}

export function AccountCreate({ back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [nickname, setNickname] = useState("")
  const [busy, setBusy] = useState(false)
  const submitLocked = useRef(false)
  const mountedRef = useRef(true)
  const cleanNickname = nickname.trim()
  const valid = cleanNickname.length >= 4 && cleanNickname.length <= 20
  const touched = nickname.length > 0

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
      const succeeded = await data.createAccount(cleanNickname)

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
            <b>{cleanNickname.length}/20</b>
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
              ? t("accountOnboarding.create.readyHint", { nickname: cleanNickname })
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
        <OnboardingFact
          mark="01"
          title={t("accountOnboarding.create.factOwnerTitle")}
          copy={t("accountOnboarding.create.factOwnerCopy")}
        />
        <OnboardingFact
          mark="02"
          title={t("accountOnboarding.create.factProfileTitle")}
          copy={t("accountOnboarding.create.factProfileCopy")}
        />
      </div>
    </OnboardingShell>
  )
}
