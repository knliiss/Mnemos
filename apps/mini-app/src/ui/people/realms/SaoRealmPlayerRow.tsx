import type { SaoRealmPlayerResponse } from "@/shared/api/playerDirectoryApi"
import { CristalixHead } from "../../components/profile/CristalixHead"
import type { Route } from "../../model"
import { profileSkinUrl } from "../profile/profilePresentation"

type Props = {
  player: SaoRealmPlayerResponse
  navigate: (route: Route) => void
}

export function SaoRealmPlayerRow({ player, navigate }: Props) {
  const content = (
    <>
      <CristalixHead skinUrl={profileSkinUrl(player.playerId)} size={30} />
      <strong>{player.nickname}</strong>
      {player.accountId ? <span>›</span> : null}
    </>
  )

  if (!player.accountId) {
    return <div className="sao-realm-player is-external">{content}</div>
  }

  return (
    <button
      type="button"
      className="sao-realm-player"
      onClick={() =>
        navigate({
          id: "public-profile",
          params: { accountId: player.accountId ?? "" },
        })
      }
    >
      {content}
    </button>
  )
}
