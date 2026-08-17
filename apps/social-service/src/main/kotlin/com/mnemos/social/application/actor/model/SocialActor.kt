package com.mnemos.social.application.actor.model

import java.util.UUID

data class SocialActor(
    val userId: UUID,
    val accountId: UUID,
    val cristalixNickname: String,
)
