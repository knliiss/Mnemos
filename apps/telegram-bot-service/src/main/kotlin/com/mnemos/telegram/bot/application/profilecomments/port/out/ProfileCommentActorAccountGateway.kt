package com.mnemos.telegram.bot.application.profilecomments.port.out

import java.util.UUID

fun interface ProfileCommentActorAccountGateway {
    fun activeAccountId(telegramId: Long): UUID?
}
