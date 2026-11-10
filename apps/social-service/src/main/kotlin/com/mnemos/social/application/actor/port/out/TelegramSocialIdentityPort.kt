package com.mnemos.social.application.actor.port.out

import com.mnemos.social.application.actor.model.TelegramSocialIdentity

fun interface TelegramSocialIdentityPort {
    fun resolve(telegramId: Long): TelegramSocialIdentity?
}
