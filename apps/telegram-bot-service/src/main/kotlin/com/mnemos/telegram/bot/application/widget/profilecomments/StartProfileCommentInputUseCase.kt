package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession

fun interface StartProfileCommentInputUseCase {
    fun execute(widget: TelegramWidgetSession): TelegramDeliveryResult
}
