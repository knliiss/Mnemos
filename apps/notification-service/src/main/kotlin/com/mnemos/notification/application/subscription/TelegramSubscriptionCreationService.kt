package com.mnemos.notification.application.subscription

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionCommand
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionUseCase
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionCommand
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionUseCase
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionCommand
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionUseCase
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionCommand
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionUseCase
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionCommand
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionUseCase
import com.mnemos.notification.application.recipient.TelegramUserIdentityService
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.stereotype.Service

@Service
class TelegramSubscriptionCreationService(
    private val userIdentityService: TelegramUserIdentityService,
    private val createItemDropSubscriptionUseCase: CreateItemDropSubscriptionUseCase,
    private val createBoosterSubscriptionUseCase: CreateBoosterSubscriptionUseCase,
    private val createDungeonSubscriptionUseCase: CreateDungeonSubscriptionUseCase,
    private val createRaidSubscriptionUseCase: CreateRaidSubscriptionUseCase,
    private val createGlobalSubscriptionUseCase: CreateGlobalSubscriptionUseCase,
) {
    fun createItemDrop(
        telegramId: TelegramId,
        droppedFor: String?,
        itemName: String?,
    ): SubscriptionId? {
        val userId = userIdentityService.resolve(telegramId) ?: return null
        val normalizedDroppedFor = normalizeDroppedFor(droppedFor)
        val normalizedItemName = normalizeItemName(itemName)

        require(normalizedDroppedFor != null || normalizedItemName != null) {
            "At least one item-drop subscription filter must be specified"
        }

        return createItemDropSubscriptionUseCase
            .execute(
                CreateItemDropSubscriptionCommand(
                    userId = userId,
                    droppedFor = normalizedDroppedFor,
                    itemName = normalizedItemName,
                ),
            ).subscriptionId
    }

    fun createBooster(
        telegramId: TelegramId,
        boosterType: DomainBoosterType,
    ): SubscriptionId? {
        val userId = userIdentityService.resolve(telegramId) ?: return null

        return createBoosterSubscriptionUseCase
            .execute(
                CreateBoosterSubscriptionCommand(
                    userId = userId,
                    boosterType = boosterType,
                ),
            ).subscriptionId
    }

    fun createDungeon(
        telegramId: TelegramId,
        dungeonType: DomainDungeonType,
    ): SubscriptionId? {
        val userId = userIdentityService.resolve(telegramId) ?: return null

        return createDungeonSubscriptionUseCase
            .execute(
                CreateDungeonSubscriptionCommand(
                    userId = userId,
                    dungeonType = dungeonType,
                ),
            ).subscriptionId
    }

    fun createRaid(
        telegramId: TelegramId,
        locations: Set<Int>,
    ): SubscriptionId? {
        require(locations.all { location -> location in MIN_RAID_LOCATION..MAX_RAID_LOCATION }) {
            "Raid locations must be between $MIN_RAID_LOCATION and $MAX_RAID_LOCATION"
        }

        val userId = userIdentityService.resolve(telegramId) ?: return null

        return createRaidSubscriptionUseCase
            .execute(
                CreateRaidSubscriptionCommand(
                    userId = userId,
                    locations = locations,
                ),
            ).subscriptionId
    }

    fun createGlobal(
        telegramId: TelegramId,
        eventType: DomainGlobalEventType,
    ): SubscriptionId? {
        val userId = userIdentityService.resolve(telegramId) ?: return null

        return createGlobalSubscriptionUseCase
            .execute(
                CreateGlobalSubscriptionCommand(
                    userId = userId,
                    eventType = eventType,
                ),
            ).subscriptionId
    }

    private fun normalizeDroppedFor(value: String?): String? {
        val normalized = value?.trim()?.takeIf(String::isNotEmpty) ?: return null

        require(normalized.length in MIN_NICKNAME_LENGTH..MAX_NICKNAME_LENGTH) {
            "Item-drop nickname filter has invalid length"
        }
        require(NICKNAME_PATTERN.matches(normalized)) {
            "Item-drop nickname filter has invalid characters"
        }

        return normalized
    }

    private fun normalizeItemName(value: String?): String? {
        val normalized = value?.trim()?.takeIf(String::isNotEmpty) ?: return null

        require(normalized.length <= MAX_ITEM_NAME_LENGTH) {
            "Item-drop item-name filter is too long"
        }

        return normalized
    }

    private companion object {
        const val MIN_NICKNAME_LENGTH = 4
        const val MAX_NICKNAME_LENGTH = 20
        const val MAX_ITEM_NAME_LENGTH = 24
        const val MIN_RAID_LOCATION = 1
        const val MAX_RAID_LOCATION = 28

        val NICKNAME_PATTERN = Regex("^[\\p{L}0-9_]+$")
    }
}
