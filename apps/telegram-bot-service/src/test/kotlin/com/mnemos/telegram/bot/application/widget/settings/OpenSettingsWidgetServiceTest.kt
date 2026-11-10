package com.mnemos.telegram.bot.application.widget.settings

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.settings.model.TelegramNotificationPackOption
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsSnapshot
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import com.mnemos.telegram.bot.application.widget.settings.port.out.SettingsWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class OpenSettingsWidgetServiceTest {
    private val gateway = mock<SettingsGateway>()
    private val publisher = mock<SettingsWidgetPublisher>()
    private val service = OpenSettingsWidgetService(TelegramWidgetProperties(enabled = true), gateway, publisher)

    @Test
    fun readsExistingSettingsAndPaginatesAvailablePacks() {
        val ids = List(5) { UUID.randomUUID() }
        val packs = ids.mapIndexed { index, id -> TelegramNotificationPackOption(id, "Pack $index", false) }
        whenever(gateway.getSettings(11)).thenReturn(TelegramSettingsSnapshot(TelegramSettingsLanguage.UK, null, null, packs))

        val snapshot = service.load(11, 1) as SettingsWidgetSnapshotResult.Available

        assertEquals(1, snapshot.view.page)
        assertEquals(2, snapshot.view.totalPages)
        assertEquals(5, snapshot.view.totalPacks)
        assertEquals(ids.last(), snapshot.view.packs.single().id)
    }

    @Test
    fun publishesSettingsWhenEnabled() {
        whenever(gateway.getSettings(11)).thenReturn(TelegramSettingsSnapshot(TelegramSettingsLanguage.EN, null, null, emptyList()))
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L))
        verify(publisher).publish(eq(11L), any())
    }

    @Test
    fun disabledWidgetNeverQueriesSettingsBackend() {
        val disabled = OpenSettingsWidgetService(TelegramWidgetProperties(enabled = false), gateway, publisher)
        assertEquals(TelegramDeliveryResult.PermanentFailure("widgets-disabled"), disabled.execute(11L))
        verifyNoInteractions(gateway, publisher)
    }
}
