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

type NicknameProfileRouteProps = Omit<Props, "accountId"> & {
  nickname: string
}

function NicknameProfileRoute({
  activeAccount,
  nickname,
  navigate,
  back,
}: NicknameProfileRouteProps) {
  const { t } = useTranslation()
  const [resolvedAccountId, setResolvedAccountId] = useState<string | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [resolutionFailed, setResolutionFailed] = useState(false)
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    let cancelled = false
    const exactNickname = nickname.toLocaleLowerCase()

    void searchPublicProfiles(nickname, 0, 50)
      .then((profiles) => {
        if (cancelled) {
          return
        }

        setResolutionFailed(false)

        const exactProfile = profiles.content.find(
          (profile) => profile.cristalixNickname.toLocaleLowerCase() === exactNickname,
        )

        if (!exactProfile) {
          setNotFound(true)
          return
        }

        setNotFound(false)
        setResolvedAccountId(exactProfile.accountId)
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
          setNotFound(false)
          setResolutionFailed(true)
        }
      })

    return () => {
      cancelled = true
    }
  }, [nickname, revision])

  if (resolvedAccountId) {
    return (
      <PublicProfilePage
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

      {notFound ? (
        <section className="public-profile-state">
          <Glyph name="profile-mark" size={24} />
          <strong>{t("people.profile.notFoundTitle")}</strong>
          <small>{t("people.profile.notFoundCopy")}</small>
        </section>
      ) : (
        <FeatureLoading label={t("people.profile.loading")} />
      )}
    </div>
  )
}

export function PublicProfileRoute({ activeAccount, accountId, nickname, navigate, back }: Props) {
  const directAccountId = accountId?.trim()

  if (directAccountId) {
    return (
      <PublicProfilePage
        activeAccount={activeAccount}
        accountId={directAccountId}
        navigate={navigate}
        back={back}
      />
    )
  }

  const normalizedNickname = nickname?.trim()

  if (normalizedNickname) {
    return (
      <NicknameProfileRoute
        key={normalizedNickname.toLocaleLowerCase()}
        activeAccount={activeAccount}
        nickname={normalizedNickname}
        navigate={navigate}
        back={back}
      />
    )
  }

  return (
    <PublicProfilePage activeAccount={activeAccount} accountId="" navigate={navigate} back={back} />
  )
}
