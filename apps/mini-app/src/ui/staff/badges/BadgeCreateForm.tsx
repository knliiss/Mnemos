import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { Field } from "../../shared/Primitives"
import type { BadgeDraft } from "./useBadgeCatalog"

const MAX_BADGE_IMAGE_BYTES = 256 * 1024

type BadgeCreateFormProps = {
  busy: boolean
  onCreate(draft: BadgeDraft): Promise<boolean>
  onCreated(): void
}

export function BadgeCreateForm({ busy, onCreate, onCreated }: BadgeCreateFormProps) {
  const { t } = useTranslation()
  const [code, setCode] = useState("")
  const [name, setName] = useState("")
  const [image, setImage] = useState<File | null>(null)
  const [color, setColor] = useState("#C7F24A")

  const codeValid = /^[A-Z][A-Z0-9_]{1,31}$/.test(code)
  const imageValid =
    image !== null && image.type === "image/png" && image.size <= MAX_BADGE_IMAGE_BYTES
  const createValid = codeValid && name.trim().length > 0 && name.trim().length <= 64 && imageValid
  const imagePreviewUrl = useMemo(() => (image ? URL.createObjectURL(image) : null), [image])

  useEffect(
    () => () => {
      if (imagePreviewUrl) {
        URL.revokeObjectURL(imagePreviewUrl)
      }
    },
    [imagePreviewUrl],
  )

  const create = async () => {
    if (!createValid || !image || busy) {
      return
    }

    const created = await onCreate({
      code,
      name: name.trim(),
      image,
      color,
    })

    if (created) {
      onCreated()
    }
  }

  return (
    <section className="staff-form-block badge-create-form">
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

      <Field label={t("badgeAdmin.image")} hint={t("badgeAdmin.imageHint")}>
        <label className={`badge-image-picker ${imageValid ? "has-image" : ""}`}>
          <input
            type="file"
            accept="image/png,.png"
            onChange={(event) => setImage(event.target.files?.[0] ?? null)}
          />

          <span className="badge-image-picker__preview">
            {imagePreviewUrl && imageValid ? (
              <img src={imagePreviewUrl} alt="" aria-hidden="true" />
            ) : (
              <img src="/ui-illustrations/admin-badges.svg" alt="" aria-hidden="true" />
            )}
          </span>

          <span className="badge-image-picker__copy">
            <strong>{image?.name ?? t("badgeAdmin.image")}</strong>
            <small>{t("badgeAdmin.imageHint")}</small>
          </span>
        </label>
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
