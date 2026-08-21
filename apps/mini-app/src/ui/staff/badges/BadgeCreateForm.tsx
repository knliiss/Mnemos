import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Field } from "../../shared/Primitives"
import type { BadgeDraft } from "./useBadgeCatalog"

type BadgeCreateFormProps = {
  busy: boolean
  onCreate(draft: BadgeDraft): Promise<boolean>
  onCreated(): void
}

export function BadgeCreateForm({ busy, onCreate, onCreated }: BadgeCreateFormProps) {
  const { t } = useTranslation()
  const [code, setCode] = useState("")
  const [name, setName] = useState("")
  const [icon, setIcon] = useState("✦")
  const [color, setColor] = useState("#C7F24A")

  const codeValid = /^[A-Z][A-Z0-9_]{1,31}$/.test(code)
  const createValid =
    codeValid &&
    name.trim().length > 0 &&
    name.trim().length <= 64 &&
    icon.trim().length > 0 &&
    icon.trim().length <= 64

  const create = async () => {
    if (!createValid || busy) {
      return
    }

    const created = await onCreate({
      code,
      name: name.trim(),
      icon: icon.trim(),
      color,
    })

    if (created) {
      onCreated()
    }
  }

  return (
    <section className="staff-form-block">
      <header>
        <small>{t("badgeAdmin.newEyebrow")}</small>
        <strong>{t("badgeAdmin.newBadge")}</strong>
      </header>

      <Field
        label={t("badgeAdmin.code")}
        hint={code && !codeValid ? t("badgeAdmin.codeHint") : undefined}
      >
        <input
          value={code}
          maxLength={32}
          onChange={(event) => setCode(event.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, ""))}
          placeholder={t("badgeAdmin.codePlaceholder")}
        />
      </Field>

      <Field label={t("badgeAdmin.name")}>
        <input
          value={name}
          maxLength={64}
          onChange={(event) => setName(event.target.value)}
          placeholder={t("badgeAdmin.namePlaceholder")}
        />
      </Field>

      <Field label={t("badgeAdmin.icon")}>
        <input value={icon} maxLength={64} onChange={(event) => setIcon(event.target.value)} />
      </Field>

      <Field label={t("badgeAdmin.color")}>
        <input
          type="color"
          value={color}
          onChange={(event) => setColor(event.target.value.toUpperCase())}
        />
      </Field>

      <button
        type="button"
        className="primary-action"
        disabled={!createValid || busy}
        onClick={() => void create()}
      >
        {busy ? t("badgeAdmin.creating") : t("badgeAdmin.createBadge")}
      </button>
    </section>
  )
}
