package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import java.util.UUID

interface StartWidgetTextInputUseCase {
    fun start(session: TelegramWidgetSession, purpose: WidgetTextInputPurpose): TelegramDeliveryResult

    fun startForRole(session: TelegramWidgetSession, roleId: UUID): TelegramDeliveryResult
}
