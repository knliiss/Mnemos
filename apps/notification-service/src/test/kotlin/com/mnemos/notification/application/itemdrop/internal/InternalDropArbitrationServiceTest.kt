package com.mnemos.notification.application.itemdrop.internal

import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventCommand
import com.mnemos.notification.application.itemdrop.usecase.process.ProcessItemDropEventUseCase
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class InternalDropArbitrationServiceTest {
    @Test
    fun `internal alert dispatches immediately`() {
        val dispatched = mutableListOf<ProcessItemDropEventCommand>()
        val service = service(dispatched)
        val command = command()

        service.submitInternal(command)

        assertEquals(listOf(command), dispatched)
    }

    @Test
    fun `standard event suppresses later matching internal alert`() {
        val dispatched = mutableListOf<ProcessItemDropEventCommand>()
        val service = service(dispatched)
        val standard = command()

        assertTrue(service.shouldDispatchStandard(standard))
        service.submitInternal(
            standard.copy(messageId = UUID.randomUUID()),
        )

        assertTrue(dispatched.isEmpty())
    }

    @Test
    fun `internal alert suppresses later matching standard event`() {
        val dispatched = mutableListOf<ProcessItemDropEventCommand>()
        val service = service(dispatched)
        val internal = command()

        service.submitInternal(internal)

        assertEquals(listOf(internal), dispatched)
        assertFalse(
            service.shouldDispatchStandard(
                internal.copy(messageId = UUID.randomUUID()),
            ),
        )
    }

    @Test
    fun `duplicate internal alerts dispatch only once`() {
        val dispatched = mutableListOf<ProcessItemDropEventCommand>()
        val service = service(dispatched)
        val first = command()

        service.submitInternal(first)
        service.submitInternal(first.copy(messageId = UUID.randomUUID()))

        assertEquals(listOf(first), dispatched)
    }

    private fun service(dispatched: MutableList<ProcessItemDropEventCommand>): InternalDropArbitrationService =
        InternalDropArbitrationService(
            processItemDropEventUseCase =
                object : ProcessItemDropEventUseCase {
                    override fun execute(command: ProcessItemDropEventCommand) {
                        dispatched += command
                    }
                },
        )

    private fun command(): ProcessItemDropEventCommand =
        ProcessItemDropEventCommand(
            messageId = UUID.randomUUID(),
            occurredAt = Instant.parse("2026-09-26T19:00:00Z"),
            droppedFor = "Обидно_МНЕ",
            itemKey = "sao.item.test.name",
            itemName = "Радужный тесак",
            itemRarity = NotificationItemRarity.MYTHICAL,
            itemType = NotificationItemType.SWORD,
        )
}
