import { useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { Field } from "../../shared/Primitives"
import { AdminColorControl } from "../shared/AdminColorControl"
import { validateBadgeImageFile } from "./badgeImageValidation"
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
  const [image, setImage] = useState<File | null>(null)
  const [imageValid, setImageValid] = useState(false)
  const [checkingImage, setCheckingImage] = useState(false)
  const [color, setColor] = useState("#C7F24A")

  const codeValid = /^[A-Z][A-Z0-9_]{1,31}$/.test(code)
  const colorValid = /^#[0-9A-F]{6}$/.test(color.trim().toUpperCase())
  const createValid =
    codeValid &&
    name.trim().length > 0 &&
    name.trim().length <= 64 &&
    image !== null &&
    imageValid &&
    colorValid &&
    !checkingImage
  const imagePreviewUrl = useMemo(() => (image ? URL.createObjectURL(image) : null), [image])

  useEffect(() => {
    if (!image) {
      return
    }

    let cancelled = false

    void validateBadgeImageFile(image)
      .catch(() => false)
      .then((valid) => {
        if (!cancelled) {
          setImageValid(valid)
          setCheckingImage(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [image])

  useEffect(
    () => () => {
      if (imagePreviewUrl) {
        URL.revokeObjectURL(imagePreviewUrl)
      }
    },
    [imagePreviewUrl],
  )

  const selectImage = (nextImage: File | null) => {
    setImage(nextImage)
    setImageValid(false)
    setCheckingImage(nextImage !== null)
  }

  const create = async () => {
    if (!createValid || !image || busy) {
      return
    }

    const created = await onCreate({
      code,
      name: name.trim(),
      image,
      color: color.trim().toUpperCase(),
    })

    if (created) {
      onCreated()
    }
  }

  return (
    <section className="staff-form-block badge-create-form">
      <header className="badge-create-form__header">
        <span className="badge-create-form__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-event.svg" alt="" />
        </span>
        <span>
          <small>{t("badgeAdmin.newEyebrow")}</small>
          <strong>{t("badgeAdmin.newBadge")}</strong>
        </span>
      </header>

      <Field
        label={t("badgeAdmin.code")}
        hint={code && !codeValid ? t("badgeAdmin.codeHint") : undefined}
      >
        <input
          type="text"
          value={code}
          maxLength={32}
          autoComplete="off"
          spellCheck={false}
          onChange={(event) => setCode(event.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, ""))}
          placeholder={t("badgeAdmin.codePlaceholder")}
        />
      </Field>

      <Field label={t("badgeAdmin.name")}>
        <input
          type="text"
          value={name}
          maxLength={64}
          autoComplete="off"
          onChange={(event) => setName(event.target.value)}
          placeholder={t("badgeAdmin.namePlaceholder")}
        />
      </Field>

      <Field label={t("badgeAdmin.image")} hint={t("badgeAdmin.imageHint")}>
        <label className={`badge-image-picker ${imageValid ? "has-image" : ""}`}>
          <input
            type="file"
            accept="image/png,.png"
            onChange={(event) => selectImage(event.target.files?.[0] ?? null)}
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
        <AdminColorControl
          value={color}
          label={t("badgeAdmin.color")}
          disabled={busy}
          onChange={setColor}
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
