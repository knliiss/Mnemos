package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.notification.NotificationRaidLocations
import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationRaidSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionCreationResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionCreationGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class UpdateWidgetSubscriptionService(
    private val subscriptions: NotificationSubscriptionGateway,
    private val creations: NotificationSubscriptionCreationGateway,
) : UpdateWidgetSubscriptionUseCase {
    override fun set(
        telegramId: Long,
        kind: WidgetSubscriptionKind,
        value: String,
        enabled: Boolean,
    ): WidgetSubscriptionUpdateResult {
        val current = load(telegramId) ?: return WidgetSubscriptionUpdateResult.UNAVAILABLE
        val selected =
            when (kind) {
                WidgetSubscriptionKind.BOOSTER -> {
                    val entry = NotificationBoosterType.entries.firstOrNull { it.name == value }
                        ?: return WidgetSubscriptionUpdateResult.INVALID
                    Selection(current.boosterSubscriptions[entry]) { creations.createBooster(telegramId, entry) }
                }
                WidgetSubscriptionKind.DUNGEON -> {
                    val entry = NotificationDungeonType.entries.firstOrNull { it.name == value }
                        ?: return WidgetSubscriptionUpdateResult.INVALID
                    Selection(current.dungeonSubscriptions[entry]) { creations.createDungeon(telegramId, entry) }
                }
                WidgetSubscriptionKind.GLOBAL -> {
                    val entry = NotificationGlobalType.entries.firstOrNull { it.name == value }
                        ?: return WidgetSubscriptionUpdateResult.INVALID
                    Selection(current.globalSubscriptions[entry]) { creations.createGlobal(telegramId, entry) }
                }
            }

        return when {
            enabled && selected.currentId != null -> WidgetSubscriptionUpdateResult.APPLIED
            !enabled && selected.currentId == null -> WidgetSubscriptionUpdateResult.APPLIED
            enabled -> selected.create().toWidgetResult()
            else -> removeOrRecheck(telegramId, kind, value, selected.currentId!!)
        }
    }

    override fun setRaidLocations(telegramId: Long, locations: Set<Int>): WidgetSubscriptionUpdateResult {
        if (locations.any { it !in NotificationRaidLocations.MIN..NotificationRaidLocations.MAX }) {
            return WidgetSubscriptionUpdateResult.INVALID
        }

        val current = load(telegramId) ?: return WidgetSubscriptionUpdateResult.UNAVAILABLE
        val currentLocations = NotificationRaidLocations.active(current.raidSubscription)
        if (currentLocations == locations) return WidgetSubscriptionUpdateResult.APPLIED

        if (locations.isEmpty()) {
            val id = current.raidSubscription?.id ?: return WidgetSubscriptionUpdateResult.APPLIED
            return if (subscriptions.delete(telegramId, id)) WidgetSubscriptionUpdateResult.APPLIED
            else if (load(telegramId)?.raidSubscription == null) WidgetSubscriptionUpdateResult.APPLIED
            else WidgetSubscriptionUpdateResult.UNAVAILABLE
        }

        return creations.updateRaid(
            telegramId,
            NotificationRaidSubscriptionDraft(NotificationRaidLocations.toBackendLocations(locations)),
        ).toWidgetResult()
    }

    private fun removeOrRecheck(
        telegramId: Long,
        kind: WidgetSubscriptionKind,
        value: String,
        id: UUID,
    ): WidgetSubscriptionUpdateResult {
        if (subscriptions.delete(telegramId, id)) return WidgetSubscriptionUpdateResult.APPLIED
        val refreshed = load(telegramId) ?: return WidgetSubscriptionUpdateResult.UNAVAILABLE
        val stillPresent =
            when (kind) {
                WidgetSubscriptionKind.BOOSTER -> refreshed.boosterSubscriptions.keys.any { it.name == value }
                WidgetSubscriptionKind.DUNGEON -> refreshed.dungeonSubscriptions.keys.any { it.name == value }
                WidgetSubscriptionKind.GLOBAL -> refreshed.globalSubscriptions.keys.any { it.name == value }
            }
        return if (stillPresent) WidgetSubscriptionUpdateResult.UNAVAILABLE else WidgetSubscriptionUpdateResult.APPLIED
    }

    private fun load(telegramId: Long): NotificationSubscriptionResult.Available? =
        subscriptions.list(telegramId, 0, 1) as? NotificationSubscriptionResult.Available

    private fun NotificationSubscriptionCreationResult.toWidgetResult(): WidgetSubscriptionUpdateResult =
        when (this) {
            NotificationSubscriptionCreationResult.APPLIED -> WidgetSubscriptionUpdateResult.APPLIED
            NotificationSubscriptionCreationResult.USER_NOT_FOUND -> WidgetSubscriptionUpdateResult.USER_NOT_FOUND
            NotificationSubscriptionCreationResult.UNAVAILABLE -> WidgetSubscriptionUpdateResult.UNAVAILABLE
        }

    private class Selection(
        val currentId: UUID?,
        val create: () -> NotificationSubscriptionCreationResult,
    )
}
