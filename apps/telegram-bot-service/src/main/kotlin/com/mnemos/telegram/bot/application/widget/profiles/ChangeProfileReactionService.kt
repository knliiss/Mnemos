package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionChangeResult
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionState
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionType
import com.mnemos.telegram.bot.application.profilereaction.port.out.ProfileReactionGateway
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileReactionQueryGuard
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class ChangeProfileReactionService(
    private val reactions: ProfileReactionGateway,
    private val guard: ProfileReactionQueryGuard,
) : ChangeProfileReactionUseCase {
    override fun execute(
        telegramId: Long,
        profileId: UUID,
        desired: ProfileReactionType?,
        queryId: String,
    ): ProfileReactionChangeResult {
        if (telegramId <= 0) return ProfileReactionChangeResult.FORBIDDEN
        val current = reactions.get(telegramId, profileId)
        if (current !is ProfileReactionState.Available) {
            return if (current == ProfileReactionState.AccountRequired) {
                ProfileReactionChangeResult.ACCOUNT_REQUIRED
            } else ProfileReactionChangeResult.UNAVAILABLE
        }
        if (current.type == desired) return ProfileReactionChangeResult.APPLIED
        if (!guard.claim(telegramId, queryId)) return ProfileReactionChangeResult.INVALID
        return reactions.set(telegramId, profileId, desired)
    }
}
