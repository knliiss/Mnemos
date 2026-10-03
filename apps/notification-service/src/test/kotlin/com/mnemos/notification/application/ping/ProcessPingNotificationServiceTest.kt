package com.mnemos.notification.application.ping

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.PingNotificationPayload
import com.mnemos.notification.application.notification.service.NotificationDispatcher
import com.mnemos.notification.application.ping.port.out.PingDeduplicationStore
import com.mnemos.notification.application.ping.port.out.PingRecipientResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class ProcessPingNotificationServiceTest {
    private val recipientResolver = mock<PingRecipientResolver>()
    private val preferenceService = mock<PingNotificationPreferenceService>()
    private val deduplicationStore = mock<PingDeduplicationStore>()
    private val dispatcher = mock<NotificationDispatcher>()
    private val service =
        ProcessPingNotificationService(
            recipientResolver = recipientResolver,
            preferenceService = preferenceService,
            deduplicationStore = deduplicationStore,
            dispatcher = dispatcher,
        )

    @Test
    fun `dispatches once per user for case-insensitive duplicate mentions`() {
        val userId = UserId(UUID.randomUUID())
        val command = command(mentions = listOf("Knaliz", "KNALIZ"))

        whenever(deduplicationStore.accept(command.sender, command.message)).thenReturn(true)
        whenever(recipientResolver.resolve("Knaliz")).thenReturn(setOf(userId))
        whenever(preferenceService.filterEnabled(setOf(userId))).thenReturn(setOf(userId))

        service.process(command)

        verify(recipientResolver).resolve("Knaliz")
        verify(recipientResolver, never()).resolve("KNALIZ")

        val payload = argumentCaptor<PingNotificationPayload>()
        verify(dispatcher).dispatch(
            sourceId = any(),
            recipientIds = eq(setOf(userId)),
            payload = payload.capture(),
            occurredAt = eq(command.observedAt),
        )

        assertEquals("TranZorXx", payload.firstValue.player)
        assertEquals("Knaliz", payload.firstValue.target)
        assertEquals(command.message, payload.firstValue.message)
        assertEquals(command.text, payload.firstValue.text)
    }

    @Test
    fun `does not resolve recipients when duplicate was already accepted`() {
        val command = command()

        whenever(deduplicationStore.accept(command.sender, command.message)).thenReturn(false)

        service.process(command)

        verifyNoInteractions(recipientResolver, preferenceService, dispatcher)
    }

    @Test
    fun `does not dispatch to users with ping notifications disabled`() {
        val userId = UserId(UUID.randomUUID())
        val command = command()

        whenever(deduplicationStore.accept(command.sender, command.message)).thenReturn(true)
        whenever(recipientResolver.resolve("knaliz")).thenReturn(setOf(userId))
        whenever(preferenceService.filterEnabled(setOf(userId))).thenReturn(emptySet())

        service.process(command)

        verify(dispatcher, never()).dispatch(any(), any(), any(), any())
    }

    @Test
    fun `releases dedup claim when downstream processing fails`() {
        val command = command()

        whenever(deduplicationStore.accept(command.sender, command.message)).thenReturn(true)
        whenever(recipientResolver.resolve("knaliz")).thenThrow(IllegalStateException("account service unavailable"))

        assertThrows(IllegalStateException::class.java) {
            service.process(command)
        }

        verify(deduplicationStore).release(command.sender, command.message)
    }

    private fun command(
        mentions: List<String> = listOf("knaliz"),
    ): ProcessPingNotificationCommand =
        ProcessPingNotificationCommand(
            messageId = UUID.randomUUID(),
            observedAt = Instant.parse("2026-10-03T19:00:00Z"),
            sender = "TranZorXx",
            message = "MVP ┃ TranZorXx [#401] » @knaliz привет как дела?",
            text = "@knaliz привет как дела?",
            mentions = mentions,
        )
}
