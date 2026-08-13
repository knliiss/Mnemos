package com.mnemos.moderation.application.security.port.out

import com.mnemos.moderation.application.security.model.ModeratorActor

interface CurrentModeratorProvider {
    fun current(): ModeratorActor
}
