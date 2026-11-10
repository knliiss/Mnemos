package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionChangeResult
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionType
import java.util.UUID

fun interface ChangeProfileReactionUseCase {
    fun execute(telegramId: Long, profileId: UUID, desired: ProfileReactionType?, queryId: String): ProfileReactionChangeResult
}
