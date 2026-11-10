package com.mnemos.telegram.bot.application.widget.online

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.online.SaoOnlineSearchQuery
import com.mnemos.telegram.bot.application.online.SaoOnlinePlayerMatcher
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import com.mnemos.telegram.bot.application.online.port.out.SaoOnlineSnapshotGateway
import com.mnemos.telegram.bot.application.widget.model.WidgetTextLimiter
import com.mnemos.telegram.bot.application.widget.online.port.out.OnlineWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenOnlineWidgetService(
    private val properties: TelegramWidgetProperties,
    private val snapshots: SaoOnlineSnapshotGateway,
    private val publisher: OnlineWidgetPublisher,
) : OpenOnlineWidgetUseCase,
    OnlineWidgetSnapshotQuery {
    override val enabled: Boolean
        get() = properties.enabled

    override fun execute(
        telegramId: Long,
        realmNumber: Int?,
    ): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val result = load(telegramId, realmNumber)) {
            is OnlineWidgetSnapshotResult.Available -> publisher.publish(telegramId, result.view)
            OnlineWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(
        telegramId: Long,
        realmNumber: Int?,
        search: String?,
    ): OnlineWidgetSnapshotResult {
        if ((realmNumber != null && realmNumber !in 1..MAX_REALM_NUMBER) ||
            (search != null && (SaoOnlineSearchQuery.normalize(search) != search || realmNumber != null))
        ) {
            return OnlineWidgetSnapshotResult.Unavailable
        }

        val result = snapshots.getSnapshot() as? SaoOnlineSnapshotResult.Available
            ?: return OnlineWidgetSnapshotResult.Unavailable
        val snapshot = result.snapshot
        val ordered = snapshot.realms.sortedBy { it.number }
        val matches = search?.let { SaoOnlinePlayerMatcher.findMatches(snapshot, it) }.orEmpty()
        val selected =
            if (realmNumber == null || search != null) {
                null
            } else {
                ordered.firstOrNull { it.number == realmNumber }
                    ?: return OnlineWidgetSnapshotResult.Unavailable
            }

        return OnlineWidgetSnapshotResult.Available(
            OnlineWidgetView(
                trackedOnline = snapshot.trackedOnline,
                totalRealms = ordered.size,
                selectedRealmNumber = selected?.number,
                selectedRealmName = selected?.name?.let { WidgetTextLimiter.truncateUtf8(it, 28) },
                selectedRealmPlayerCount = if (search != null) matches.size else selected?.playerCount,
                generatedAt = snapshot.generatedAt.toString().substring(11, 16) + " UTC",
                search = search,
                realms =
                    (if (selected == null && search == null) ordered.take(MAX_VISIBLE_REALMS) else emptyList()).map { realm ->
                        OnlineWidgetRealm(
                            number = realm.number,
                            name = WidgetTextLimiter.truncateUtf8(realm.name, 28),
                            playerCount = realm.playerCount,
                        )
                    },
                players =
                    if (search != null) {
                        matches.take(MAX_VISIBLE_PLAYERS).map { match ->
                            OnlineWidgetPlayer(WidgetTextLimiter.truncateUtf8(match.nickname + " · " + match.realmName, 48))
                        }
                    } else {
                        selected?.players.orEmpty()
                            .sortedWith(compareBy<com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayer, String>(String.CASE_INSENSITIVE_ORDER) { it.nickname })
                            .take(MAX_VISIBLE_PLAYERS)
                            .map { player ->
                                OnlineWidgetPlayer(WidgetTextLimiter.truncateUtf8(player.nickname, 32))
                            }
                    },
            ),
        )
    }

    private companion object {
        const val MAX_REALM_NUMBER = 1_000
        const val MAX_VISIBLE_REALMS = 8
        const val MAX_VISIBLE_PLAYERS = 6
    }
}
