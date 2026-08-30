package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.adapter.out.notification.NotificationSubscriptionApiMapper.toContract
import com.mnemos.telegram.bot.adapter.out.notification.NotificationSubscriptionApiMapper.toModel
import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationRaidSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionCreationResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionCreationGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateSubscriptionResponse
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NotificationSubscriptionGatewayAdapter(
    private val client: NotificationServiceClient,
) : NotificationSubscriptionGateway,
    NotificationSubscriptionCreationGateway {
    override fun list(
        telegramId: Long,
        page: Int,
        size: Int,
    ): NotificationSubscriptionResult =
        try {
            NotificationSubscriptionResult.Available(
                client
                    .listSubscriptions(
                        telegramId = telegramId,
                        page = page,
                        size = size,
                    ).toModel(),
            )
        } catch (_: RuntimeException) {
            NotificationSubscriptionResult.Unavailable
        }

    override fun createItemDrop(
        telegramId: Long,
        draft: NotificationItemDropSubscriptionDraft,
    ): NotificationSubscriptionCreationResult =
        create {
            client.createItemDropSubscription(
                telegramId = telegramId,
                request = draft.toContract(),
            )
        }

    override fun createBooster(
        telegramId: Long,
        boosterType: NotificationBoosterType,
    ): NotificationSubscriptionCreationResult =
        create {
            client.createBoosterSubscription(
                telegramId = telegramId,
                request = boosterType.toContract(),
            )
        }

    override fun createDungeon(
        telegramId: Long,
        dungeonType: NotificationDungeonType,
    ): NotificationSubscriptionCreationResult =
        create {
            client.createDungeonSubscription(
                telegramId = telegramId,
                request = dungeonType.toContract(),
            )
        }

    override fun createRaid(
        telegramId: Long,
        draft: NotificationRaidSubscriptionDraft,
    ): NotificationSubscriptionCreationResult =
        create {
            client.createRaidSubscription(
                telegramId = telegramId,
                request = draft.toContract(),
            )
        }

    override fun createGlobal(
        telegramId: Long,
        eventType: NotificationGlobalType,
    ): NotificationSubscriptionCreationResult =
        create {
            client.createGlobalSubscription(
                telegramId = telegramId,
                request = eventType.toContract(),
            )
        }

    override fun delete(
        telegramId: Long,
        subscriptionId: UUID,
    ): Boolean =
        try {
            client.deleteSubscription(
                telegramId = telegramId,
                subscriptionId = subscriptionId,
            )
            true
        } catch (_: RuntimeException) {
            false
        }

    private fun create(request: () -> TelegramCreateSubscriptionResponse): NotificationSubscriptionCreationResult =
        try {
            if (request().subscriptionId != null) {
                NotificationSubscriptionCreationResult.APPLIED
            } else {
                NotificationSubscriptionCreationResult.USER_NOT_FOUND
            }
        } catch (_: RuntimeException) {
            NotificationSubscriptionCreationResult.UNAVAILABLE
        }
}
