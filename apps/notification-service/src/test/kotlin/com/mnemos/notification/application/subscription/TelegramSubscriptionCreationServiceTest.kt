package com.mnemos.notification.application.subscription

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionCommand
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionResult
import com.mnemos.notification.application.booster.usecase.create.CreateBoosterSubscriptionUseCase
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionCommand
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionResult
import com.mnemos.notification.application.dungeon.usecase.create.CreateDungeonSubscriptionUseCase
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionCommand
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionResult
import com.mnemos.notification.application.global.usecase.create.CreateGlobalSubscriptionUseCase
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionCommand
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionResult
import com.mnemos.notification.application.itemdrop.usecase.create.CreateItemDropSubscriptionUseCase
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionCommand
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionResult
import com.mnemos.notification.application.raid.usecase.create.CreateRaidSubscriptionUseCase
import com.mnemos.notification.application.raid.usecase.update.UpdateRaidSubscriptionCommand
import com.mnemos.notification.application.raid.usecase.update.UpdateRaidSubscriptionResult
import com.mnemos.notification.application.raid.usecase.update.UpdateRaidSubscriptionUseCase
import com.mnemos.notification.application.recipient.TelegramUserIdentityService
import com.mnemos.notification.application.recipient.model.RecipientView
import com.mnemos.notification.application.recipient.port.out.RecipientReader
import com.mnemos.notification.application.recipient.port.out.TelegramUserIdentityGateway
import com.mnemos.notification.domain.recipient.model.RecipientPriority
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class TelegramSubscriptionCreationServiceTest {
    @Test
    fun `item drop creation resolves Telegram user and normalizes filters`() {
        val itemDropUseCase = RecordingItemDropUseCase()
        val service = createService(recipient = recipient(), itemDropUseCase = itemDropUseCase)

        val result =
            service.createItemDrop(
                telegramId = TELEGRAM_ID,
                droppedFor = "  knaliz  ",
                itemName = "  Sword Art  ",
            )

        assertEquals(SUBSCRIPTION_ID, result)
        assertEquals(USER_ID, itemDropUseCase.command?.userId)
        assertEquals("knaliz", itemDropUseCase.command?.droppedFor)
        assertEquals("Sword Art", itemDropUseCase.command?.itemName)
    }

    @Test
    fun `missing recipient projection bootstraps Telegram identity`() {
        val itemDropUseCase = RecordingItemDropUseCase()
        val service =
            createService(
                recipient = null,
                fallbackUserId = USER_ID,
                itemDropUseCase = itemDropUseCase,
            )

        val result =
            service.createItemDrop(
                telegramId = TELEGRAM_ID,
                droppedFor = "knaliz",
                itemName = null,
            )

        assertEquals(SUBSCRIPTION_ID, result)
        assertEquals(USER_ID, itemDropUseCase.command?.userId)
    }

    @Test
    fun `missing Telegram identity does not invoke item drop use case`() {
        val itemDropUseCase = RecordingItemDropUseCase()
        val service =
            createService(
                recipient = null,
                fallbackUserId = null,
                itemDropUseCase = itemDropUseCase,
            )

        val result =
            service.createItemDrop(
                telegramId = TELEGRAM_ID,
                droppedFor = "knaliz",
                itemName = null,
            )

        assertNull(result)
        assertNull(itemDropUseCase.command)
    }

    @Test
    fun `raid creation rejects locations outside public API bounds`() {
        val service = createService(recipient = recipient(), itemDropUseCase = RecordingItemDropUseCase())

        assertThrows(IllegalArgumentException::class.java) {
            service.createRaid(
                telegramId = TELEGRAM_ID,
                locations = setOf(29),
            )
        }
    }

    @Test
    fun `raid creation accepts empty locations as all locations subscription`() {
        val raidUseCase = RecordingRaidUseCase()
        val service =
            createService(
                recipient = recipient(),
                itemDropUseCase = RecordingItemDropUseCase(),
                raidUseCase = raidUseCase,
            )

        val result = service.createRaid(TELEGRAM_ID, emptySet())

        assertEquals(SUBSCRIPTION_ID, result)
        assertEquals(emptySet<Int>(), raidUseCase.command?.locations)
        assertFalse(raidUseCase.command == null)
    }

    @Test
    fun `raid update replaces locations through update use case`() {
        val updateRaidUseCase = RecordingUpdateRaidUseCase()
        val service =
            createService(
                recipient = recipient(),
                itemDropUseCase = RecordingItemDropUseCase(),
                updateRaidUseCase = updateRaidUseCase,
            )

        val result = service.updateRaid(TELEGRAM_ID, setOf(3, 12))

        assertEquals(SUBSCRIPTION_ID, result)
        assertEquals(USER_ID, updateRaidUseCase.command?.userId)
        assertEquals(setOf(3, 12), updateRaidUseCase.command?.locations)
    }

    @Test
    fun `raid update accepts empty locations as all locations`() {
        val updateRaidUseCase = RecordingUpdateRaidUseCase()
        val service =
            createService(
                recipient = recipient(),
                itemDropUseCase = RecordingItemDropUseCase(),
                updateRaidUseCase = updateRaidUseCase,
            )

        val result = service.updateRaid(TELEGRAM_ID, emptySet())

        assertEquals(SUBSCRIPTION_ID, result)
        assertEquals(emptySet<Int>(), updateRaidUseCase.command?.locations)
    }

    private fun createService(
        recipient: RecipientView?,
        itemDropUseCase: CreateItemDropSubscriptionUseCase,
        fallbackUserId: UserId? = null,
        raidUseCase: CreateRaidSubscriptionUseCase = RecordingRaidUseCase(),
        updateRaidUseCase: UpdateRaidSubscriptionUseCase = RecordingUpdateRaidUseCase(),
    ): TelegramSubscriptionCreationService =
        TelegramSubscriptionCreationService(
            userIdentityService =
                TelegramUserIdentityService(
                    recipientReader = FixedRecipientReader(recipient),
                    identityGateway = TelegramUserIdentityGateway { fallbackUserId },
                ),
            createItemDropSubscriptionUseCase = itemDropUseCase,
            createBoosterSubscriptionUseCase = StubBoosterUseCase(),
            createDungeonSubscriptionUseCase = StubDungeonUseCase(),
            createRaidSubscriptionUseCase = raidUseCase,
            updateRaidSubscriptionUseCase = updateRaidUseCase,
            createGlobalSubscriptionUseCase = StubGlobalUseCase(),
        )

    private fun recipient(): RecipientView =
        RecipientView(
            userId = USER_ID,
            telegramId = TELEGRAM_ID,
            priority = RecipientPriority.STANDARD,
        )

    private class RecordingItemDropUseCase : CreateItemDropSubscriptionUseCase {
        var command: CreateItemDropSubscriptionCommand? = null

        override fun execute(command: CreateItemDropSubscriptionCommand): CreateItemDropSubscriptionResult {
            this.command = command

            return CreateItemDropSubscriptionResult(
                subscriptionId = SUBSCRIPTION_ID,
                droppedFor = command.droppedFor,
                itemName = command.itemName,
            )
        }
    }

    private class RecordingRaidUseCase : CreateRaidSubscriptionUseCase {
        var command: CreateRaidSubscriptionCommand? = null

        override fun execute(command: CreateRaidSubscriptionCommand): CreateRaidSubscriptionResult {
            this.command = command

            return CreateRaidSubscriptionResult(SUBSCRIPTION_ID)
        }
    }

    private class RecordingUpdateRaidUseCase : UpdateRaidSubscriptionUseCase {
        var command: UpdateRaidSubscriptionCommand? = null

        override fun execute(command: UpdateRaidSubscriptionCommand): UpdateRaidSubscriptionResult {
            this.command = command

            return UpdateRaidSubscriptionResult(SUBSCRIPTION_ID)
        }
    }

    private class StubBoosterUseCase : CreateBoosterSubscriptionUseCase {
        override fun execute(command: CreateBoosterSubscriptionCommand): CreateBoosterSubscriptionResult =
            CreateBoosterSubscriptionResult(SUBSCRIPTION_ID)
    }

    private class StubDungeonUseCase : CreateDungeonSubscriptionUseCase {
        override fun execute(command: CreateDungeonSubscriptionCommand): CreateDungeonSubscriptionResult =
            CreateDungeonSubscriptionResult(SUBSCRIPTION_ID)
    }

    private class StubGlobalUseCase : CreateGlobalSubscriptionUseCase {
        override fun execute(command: CreateGlobalSubscriptionCommand): CreateGlobalSubscriptionResult =
            CreateGlobalSubscriptionResult(SUBSCRIPTION_ID)
    }

    private class FixedRecipientReader(
        private val recipient: RecipientView?,
    ) : RecipientReader {
        override fun findByUserIds(userIds: Set<UserId>): Map<UserId, RecipientView> = emptyMap()

        override fun findByTelegramId(telegramId: TelegramId): RecipientView? = recipient
    }

    private companion object {
        val USER_ID = UserId(UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72"))
        val TELEGRAM_ID = TelegramId(931458258L)
        val SUBSCRIPTION_ID = SubscriptionId(UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"))
    }
}
