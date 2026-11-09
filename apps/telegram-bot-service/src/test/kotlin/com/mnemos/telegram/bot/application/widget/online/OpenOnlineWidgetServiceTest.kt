package com.mnemos.telegram.bot.application.widget.online

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayer
import com.mnemos.telegram.bot.application.online.model.SaoOnlineRealm
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshotResult
import com.mnemos.telegram.bot.application.online.port.out.SaoOnlineSnapshotGateway
import com.mnemos.telegram.bot.application.widget.online.port.out.OnlineWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenOnlineWidgetServiceTest {
    private val source = mock<SaoOnlineSnapshotGateway>()
    private val publisher = mock<OnlineWidgetPublisher>()

    @Test
    fun noExternalCallsWhenWidgetsDisabled() {
        val service = OpenOnlineWidgetService(TelegramWidgetProperties(enabled = false), source, publisher)

        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widgets-disabled"),
            service.execute(11L),
        )
        verifyNoInteractions(source, publisher)
    }

    @Test
    fun buildsRealmOverviewFromExistingSaoSnapshot() {
        whenever(source.getSnapshot()).thenReturn(SaoOnlineSnapshotResult.Available(snapshot()))
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service().execute(11L))

        val view = argumentCaptor<OnlineWidgetView>()
        verify(publisher).publish(eq(11L), view.capture())
        assertEquals(120, view.firstValue.trackedOnline)
        assertEquals(2, view.firstValue.totalRealms)
        assertEquals(listOf(2, 7), view.firstValue.realms.map { it.number })
        assertEquals(null, view.firstValue.selectedRealmNumber)
    }

    @Test
    fun selectedRealmShowsBoundedPlayersWithoutLeakingOtherRealmList() {
        whenever(source.getSnapshot()).thenReturn(SaoOnlineSnapshotResult.Available(snapshot()))

        val result = service().load(11L, 7) as OnlineWidgetSnapshotResult.Available

        assertEquals(7, result.view.selectedRealmNumber)
        assertEquals(1, result.view.players.size)
        assertEquals("Kirito", result.view.players.single().nickname)
        assertEquals(emptyList<OnlineWidgetRealm>(), result.view.realms)
    }

    private fun service() = OpenOnlineWidgetService(TelegramWidgetProperties(enabled = true), source, publisher)

    private fun snapshot() =
        SaoOnlineSnapshot(
            generatedAt = Instant.parse("2026-11-09T19:00:00Z"),
            trackedOnline = 120,
            realms =
                listOf(
                    SaoOnlineRealm("SAO-7", 7, 30, listOf(SaoOnlinePlayer(UUID.randomUUID(), "Kirito", null, Instant.EPOCH))),
                    SaoOnlineRealm("SAO-2", 2, 90, emptyList()),
                ),
        )
}
