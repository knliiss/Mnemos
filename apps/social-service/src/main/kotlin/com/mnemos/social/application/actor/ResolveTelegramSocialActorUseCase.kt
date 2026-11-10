package com.mnemos.social.application.actor

import com.mnemos.social.application.actor.model.SocialActor

fun interface ResolveTelegramSocialActorUseCase {
    fun resolve(telegramId: Long): SocialActor?
}
