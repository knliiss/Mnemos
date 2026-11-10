package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession

fun interface CreateWidgetProfileCommentUseCase {
    fun create(input: WidgetTextInputSession, widget: TelegramWidgetSession, content: String): TelegramDeliveryResult
}
