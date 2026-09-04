package com.mnemos.telegram.bot.application.command.start

import com.mnemos.telegram.bot.application.command.port.out.TelegramCommandMessageRenderer
import com.mnemos.telegram.bot.application.identity.port.out.TelegramUserIdentityGateway
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import com.mnemos.telegram.bot.application.settings.TelegramSettingsLanguageResolver
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class HandleTelegramStartServiceTest {
    private val renderer = mock<TelegramCommandMessageRenderer>()
    private val messageGateway = mock<TelegramMessageGateway>()
    private val recipientGateway = mock<NotificationRecipientGateway>()
    private val userIdentityGateway = mock<TelegramUserIdentityGateway>()
    private val languageResolver = mock<TelegramSettingsLanguageResolver>()
    private val service =
        HandleTelegramStartService(
            renderer = renderer,
            messageGateway = messageGateway,
            recipientGateway = recipientGateway,
            userIdentityGateway = userIdentityGateway,
            languageResolver = languageResolver,
        )

    @Test
    fun `start bootstraps Mnemos identity with Telegram username`() {
        val context =
            TelegramStartContext(
                telegramId = 910484582,
                firstName = "Knalis",
                username = "knalis",
            )
        val message = mock<OutboundTelegramMessage>()

        whenever(userIdentityGateway.bootstrap(910484582, "knalis")).thenReturn(true)
        whenever(languageResolver.resolve(910484582)).thenReturn(TelegramSettingsLanguage.RU)
        whenever(renderer.renderStart(context, TelegramSettingsLanguage.RU)).thenReturn(message)
        whenever(messageGateway.send(910484582, message)).thenReturn(TelegramDeliveryResult.Delivered)

        val result = service.handle(context)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(userIdentityGateway).bootstrap(910484582, "knalis")
        verify(recipientGateway).activate(910484582)
    }

    @Test
    fun `start remains retryable when identity bootstrap is unavailable`() {
        val context =
            TelegramStartContext(
                telegramId = 910484582,
                username = "knalis",
            )

        whenever(userIdentityGateway.bootstrap(910484582, "knalis")).thenReturn(false)

        val result = service.handle(context)

        assertEquals(TelegramDeliveryResult.Retryable, result)
        verify(recipientGateway, never()).activate(any())
        verify(languageResolver, never()).resolve(any())
        verify(messageGateway, never()).send(any(), any())
    }
}
