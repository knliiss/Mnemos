import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { searchPublicProfiles } from "@/shared/api/profileSocialApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { Account, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureError } from "../../system/FeatureError"
import { FeatureLoading } from "../../system/FeatureLoading"
import "./profile-state.css"
import { PublicProfilePage } from "./PublicProfilePage"

type Props = {
  activeAccount: Account
  accountId?: string
  nickname?: string
  navigate: (route: Route) => void
  back: () => void
}

export function PublicProfileRoute({
  activeAccount,
  accountId,
  nickname,
  navigate,
  back,
}: Props) {
  const { t } = useTranslation()
  const directAccountId = accountId?.trim() || null
  const normalizedNickname = nickname?.trim() ?? ""
  const [resolvedAccountId, setResolvedAccountId] = useState<string | null>(directAccountId)
  const [resolving, setResolving] = useState(!directAccountId && Boolean(normalizedNickname))
  const [notFound, setNotFound] = useState(!directAccountId && !normalizedNickname)
  const [resolutionFailed, setResolutionFailed] = useState(false)
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    if (directAccountId) {
      setResolvedAccountId(directAccountId)
      setResolving(false)
      setNotFound(false)
      setResolutionFailed(false)
      return
    }

    if (!normalizedNickname) {
      setResolvedAccountId(null)
      setResolving(false)
      setNotFound(true)
      setResolutionFailed(false)
      return
    }

    let cancelled = false
    const exactNickname = normalizedNickname.toLocaleLowerCase()

    setResolvedAccountId(null)
    setResolving(true)
    setNotFound(false)
    setResolutionFailed(false)

    void searchPublicProfiles(normalizedNickname, 0, 50)
      .then((profiles) => {
        if (cancelled) {
          return
        }

        const exactProfile = profiles.content.find(
          (profile) => profile.cristalixNickname.toLocaleLowerCase() === exactNickname,
        )

        if (!exactProfile) {
          setNotFound(true)
          return
        }

        setResolvedAccountId(exactProfile.accountId)
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
          setResolutionFailed(true)
        }
      })
      .finally(() => {
        if (!cancelled) {
          setResolving(false)
        }
      })

    return () => {
      cancelled = true
    }
  }, [directAccountId, normalizedNickname, revision])

  if (resolvedAccountId) {
    return (
      <PublicProfilePage
        key={resolvedAccountId}
        activeAccount={activeAccount}
        accountId={resolvedAccountId}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (resolutionFailed) {
    return <FeatureError back={back} onRetry={() => setRevision((value) => value + 1)} />
  }

  return (
    <div className="screen public-profile-screen">
      <PageHeader title={t("people.profile.title")} back={back} />

      {resolving ? (
        <FeatureLoading label={t("people.profile.loading")} />
      ) : notFound ? (
        <section className="public-profile-state">
          <Glyph name="profile-mark" size={24} />
          <strong>{t("people.profile.notFoundTitle")}</strong>
          <small>{t("people.profile.notFoundCopy")}</small>
        </section>
      ) : null}
    </div>
  )
}
