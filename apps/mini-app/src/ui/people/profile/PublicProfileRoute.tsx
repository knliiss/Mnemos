import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { resolvePlayerByNickname, type PlayerResponse } from "@/shared/api/playerDirectoryApi"
import { searchPublicProfiles } from "@/shared/api/profileSocialApi"
import { ApiError } from "@/shared/api/httpClient"
import { handleError } from "@/shared/errors/errorHandler"
import type { Account, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { FeatureError } from "../../system/FeatureError"
import { FeatureLoading } from "../../system/FeatureLoading"
import "./profile-state.css"
import { PublicProfilePage } from "./PublicProfilePage"
import { UnregisteredPlayerPage } from "./UnregisteredPlayerPage"

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
  const [externalPlayer, setExternalPlayer] = useState<PlayerResponse | null>(null)
  const [notFound, setNotFound] = useState(false)
  const [resolutionFailed, setResolutionFailed] = useState(false)
  const [revision, setRevision] = useState(0)

  useEffect(() => {
    let cancelled = false
    const exactNickname = nickname.toLocaleLowerCase()

    setResolvedAccountId(null)
    setExternalPlayer(null)
    setNotFound(false)
    setResolutionFailed(false)

    void searchPublicProfiles(nickname, 0, 50)
      .then(async (profiles) => {
        if (cancelled) {
          return
        }

        const exactProfile = profiles.content.find(
          (profile) => profile.cristalixNickname.toLocaleLowerCase() === exactNickname,
        )

        if (exactProfile) {
          setResolvedAccountId(exactProfile.accountId)
          return
        }

        try {
          const player = await resolvePlayerByNickname(nickname)

          if (!cancelled) {
            setExternalPlayer(player)
          }
        } catch (error) {
          if (cancelled) {
            return
          }

          if (error instanceof ApiError && error.status === 404) {
            setNotFound(true)
            return
          }

          handleError(error)
          setResolutionFailed(true)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
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

  if (externalPlayer) {
    return <UnregisteredPlayerPage player={externalPlayer} navigate={navigate} back={back} />
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
