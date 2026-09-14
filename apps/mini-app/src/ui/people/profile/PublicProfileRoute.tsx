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

type NicknameResolution =
  | { key: string; status: "loading" }
  | { key: string; status: "account"; accountId: string }
  | { key: string; status: "external"; player: PlayerResponse }
  | { key: string; status: "not-found" }
  | { key: string; status: "error" }

function NicknameProfileRoute({
  activeAccount,
  nickname,
  navigate,
  back,
}: NicknameProfileRouteProps) {
  const { t } = useTranslation()
  const [revision, setRevision] = useState(0)
  const requestKey = `${nickname.toLocaleLowerCase()}:${revision}`
  const [resolution, setResolution] = useState<NicknameResolution>({
    key: requestKey,
    status: "loading",
  })
  const activeResolution: NicknameResolution =
    resolution.key === requestKey ? resolution : { key: requestKey, status: "loading" }

  useEffect(() => {
    let cancelled = false
    const exactNickname = nickname.toLocaleLowerCase()

    void searchPublicProfiles(nickname, 0, 50)
      .then(async (profiles) => {
        if (cancelled) {
          return
        }

        const exactProfile = profiles.content.find(
          (profile) => profile.cristalixNickname.toLocaleLowerCase() === exactNickname,
        )

        if (exactProfile) {
          setResolution({ key: requestKey, status: "account", accountId: exactProfile.accountId })
          return
        }

        try {
          const player = await resolvePlayerByNickname(nickname)

          if (!cancelled) {
            setResolution({ key: requestKey, status: "external", player })
          }
        } catch (error) {
          if (cancelled) {
            return
          }

          if (error instanceof ApiError && error.status === 404) {
            setResolution({ key: requestKey, status: "not-found" })
            return
          }

          handleError(error)
          setResolution({ key: requestKey, status: "error" })
        }
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
          setResolution({ key: requestKey, status: "error" })
        }
      })

    return () => {
      cancelled = true
    }
  }, [nickname, requestKey])

  if (activeResolution.status === "account") {
    return (
      <PublicProfilePage
        activeAccount={activeAccount}
        accountId={activeResolution.accountId}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (activeResolution.status === "external") {
    return (
      <UnregisteredPlayerPage player={activeResolution.player} navigate={navigate} back={back} />
    )
  }

  if (activeResolution.status === "error") {
    return <FeatureError back={back} onRetry={() => setRevision((value) => value + 1)} />
  }

  return (
    <div className="screen public-profile-screen">
      <PageHeader title={t("people.profile.title")} back={back} />

      {activeResolution.status === "not-found" ? (
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
