package com.mnemos.telegram.bot.application.widget.settings

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsMutationResult
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.application.widget.settings.port.out.SettingsWidgetPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class SettingsWidgetActionHandlerTest {
    private val snapshots = mock<SettingsWidgetSnapshotQuery>()
    private val gateway = mock<SettingsGateway>()
    private val publisher = mock<SettingsWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = SettingsWidgetActionHandler(snapshots, gateway, publisher, sessions, responder)
    private val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.SETTINGS, null)
    private val packId = UUID.randomUUID()

    @Test
    fun languageSelectionCallsTypedGatewayAndUpdatesWidget() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            SettingsWidgetSnapshotResult.Available(view()),
            SettingsWidgetSnapshotResult.Available(view().copy(language = TelegramSettingsLanguage.UK)),
        )
        whenever(gateway.updateLanguage(11L, TelegramSettingsLanguage.UK)).thenReturn(TelegramSettingsMutationResult.UPDATED)
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:settings:lang:UK"), session))
        verify(gateway).updateLanguage(11L, TelegramSettingsLanguage.UK)
        verify(sessions).save(session)
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun unseenPackIdCannotBeActivated() {
        whenever(snapshots.load(11L, 0)).thenReturn(SettingsWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:settings:pack:${UUID.randomUUID()}"), session),
        )
        verifyNoInteractions(gateway, publisher, sessions)
    }

    @Test
    fun duplicatedLanguageRequestDoesNotRepeatMutation() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            SettingsWidgetSnapshotResult.Available(view().copy(language = TelegramSettingsLanguage.UK)),
        )
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:settings:lang:UK"), session))
        verifyNoInteractions(gateway)
    }

    private fun query(data: String) = TelegramWidgetQueryContext("q", 11L, 11L, 8, data, "data", true, false)

    private fun view() = SettingsWidgetView(
        language = TelegramSettingsLanguage.RU,
        activePackId = null,
        activePackTitle = null,
        packs = listOf(SettingsWidgetPack(packId, "Custom", false)),
        totalPacks = 1,
        page = 0,
        totalPages = 1,
    )
}
