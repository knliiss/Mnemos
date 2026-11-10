package com.mnemos.social.application.actor.model

import java.util.UUID

data class TelegramSocialIdentity(
    val userId: UUID,
    val activeAccountId: UUID?,
)
