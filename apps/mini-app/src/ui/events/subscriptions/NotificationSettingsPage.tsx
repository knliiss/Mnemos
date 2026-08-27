import { PlayerOfflineSubscriptions } from "./PlayerOfflineSubscriptions"
import { Subscriptions } from "./Subscriptions"
import "./player-offline-subscriptions.css"

type Props = {
  back: () => void
}

export function NotificationSettingsPage({ back }: Props) {
  return (
    <div className="notification-settings-page">
      <Subscriptions back={back} />
      <div className="notification-settings-page__presence">
        <PlayerOfflineSubscriptions />
      </div>
    </div>
  )
}
