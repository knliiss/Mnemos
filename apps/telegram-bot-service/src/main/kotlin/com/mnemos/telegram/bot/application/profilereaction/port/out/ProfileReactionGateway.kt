package com.mnemos.telegram.bot.application.profilereaction.port.out

import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionChangeResult
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionState
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionType
import java.util.UUID

interface ProfileReactionGateway {
    fun get(telegramId: Long, profileId: UUID): ProfileReactionState
    fun set(telegramId: Long, profileId: UUID, type: ProfileReactionType?): ProfileReactionChangeResult
}
