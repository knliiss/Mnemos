package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.adapter.out.notification.NotificationSubscriptionApiMapper.toContract
import com.mnemos.telegram.bot.adapter.out.notification.NotificationSubscriptionApiMapper.toModel
import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationRaidSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionCreationResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionCreationGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateSubscriptionResponse
import org.slf4j.LoggerFactory
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
            val subscriptions = loadAllSubscriptions(telegramId)
            val itemDropSubscriptions =
                subscriptions.filter { subscription ->
                    subscription.type == NotificationSubscriptionType.ITEM_DROP
                }

            NotificationSubscriptionResult.Available(
                page = paginateItemDrops(itemDropSubscriptions, page, size),
                boosterSubscriptions =
                    enumSubscriptionIds<NotificationBoosterType>(subscriptions, NotificationSubscriptionType.BOOSTER) { subscription ->
                        subscription.boosterType
                    },
                dungeonSubscriptions =
                    enumSubscriptionIds<NotificationDungeonType>(subscriptions, NotificationSubscriptionType.DUNGEON) { subscription ->
                        subscription.dungeonType
                    },
                globalSubscriptions =
                    enumSubscriptionIds<NotificationGlobalType>(subscriptions, NotificationSubscriptionType.GLOBAL) { subscription ->
                        subscription.globalType
                    },
                raidSubscription =
                    subscriptions.firstOrNull { subscription ->
                        subscription.type == NotificationSubscriptionType.RAID
                    },
            )
        } catch (exception: RuntimeException) {
            logger.warn("Could not load notification subscriptions from notification-service", exception)
            NotificationSubscriptionResult.Unavailable
        }

    override fun createItemDrop(
        telegramId: Long,
        draft: NotificationItemDropSubscriptionDraft,
    ): NotificationSubscriptionCreationResult =
        create("create item-drop subscription") {
            client.createItemDropSubscription(
                telegramId = telegramId,
                request = draft.toContract(),
            )
        }

    override fun createBooster(
        telegramId: Long,
        boosterType: NotificationBoosterType,
    ): NotificationSubscriptionCreationResult =
        create("create booster subscription") {
            client.createBoosterSubscription(
                telegramId = telegramId,
                request = boosterType.toContract(),
            )
        }

    override fun createDungeon(
        telegramId: Long,
        dungeonType: NotificationDungeonType,
    ): NotificationSubscriptionCreationResult =
        create("create dungeon subscription") {
            client.createDungeonSubscription(
                telegramId = telegramId,
                request = dungeonType.toContract(),
            )
        }

    override fun updateRaid(
        telegramId: Long,
        draft: NotificationRaidSubscriptionDraft,
    ): NotificationSubscriptionCreationResult =
        create("update raid subscription") {
            client.updateRaidSubscription(
                telegramId = telegramId,
                request = draft.toContract(),
            )
        }

    override fun createGlobal(
        telegramId: Long,
        eventType: NotificationGlobalType,
    ): NotificationSubscriptionCreationResult =
        create("create global subscription") {
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
        } catch (exception: RuntimeException) {
            logger.warn("Could not delete notification subscription in notification-service", exception)
            false
        }

    private fun loadAllSubscriptions(telegramId: Long): List<NotificationSubscription> {
        val subscriptions = mutableListOf<NotificationSubscription>()
        var page = 0
        var hasNextPage: Boolean

        do {
            val response =
                client
                    .listSubscriptions(
                        telegramId = telegramId,
                        page = page,
                        size = INTERNAL_PAGE_SIZE,
                    ).toModel()

            subscriptions += response.items
            hasNextPage = response.hasNextPage
            page += 1
        } while (hasNextPage)

        return subscriptions
    }

    private fun paginateItemDrops(
        subscriptions: List<NotificationSubscription>,
        requestedPage: Int,
        size: Int,
    ): NotificationSubscriptionPage {
        require(size > 0)

        val totalPages =
            if (subscriptions.isEmpty()) {
                0
            } else {
                (subscriptions.size + size - 1) / size
            }
        val page =
            if (totalPages == 0) {
                0
            } else {
                requestedPage.coerceIn(0, totalPages - 1)
            }
        val items = subscriptions.drop(page * size).take(size)

        return NotificationSubscriptionPage(
            items = items,
            page = page,
            totalPages = totalPages,
            totalItems = subscriptions.size.toLong(),
            hasNextPage = page + 1 < totalPages,
        )
    }

    private inline fun <reified T : Enum<T>> enumSubscriptionIds(
        subscriptions: List<NotificationSubscription>,
        type: NotificationSubscriptionType,
        crossinline value: (NotificationSubscription) -> String?,
    ): Map<T, UUID> =
        subscriptions
            .asSequence()
            .filter { subscription -> subscription.type == type }
            .mapNotNull { subscription ->
                val rawValue = value(subscription) ?: return@mapNotNull null
                val enumValue = enumValueOrNull<T>(rawValue) ?: return@mapNotNull null

                enumValue to subscription.id
            }.toMap()

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { enumValue -> enumValue.name == value.uppercase() }

    private fun create(
        operation: String,
        request: () -> TelegramCreateSubscriptionResponse,
    ): NotificationSubscriptionCreationResult =
        try {
            if (request().subscriptionId != null) {
                NotificationSubscriptionCreationResult.APPLIED
            } else {
                NotificationSubscriptionCreationResult.USER_NOT_FOUND
            }
        } catch (exception: RuntimeException) {
            logger.warn("Could not {} in notification-service", operation, exception)
            NotificationSubscriptionCreationResult.UNAVAILABLE
        }

    private companion object {
        const val INTERNAL_PAGE_SIZE = 20

        val logger = LoggerFactory.getLogger(NotificationSubscriptionGatewayAdapter::class.java)
    }
}
