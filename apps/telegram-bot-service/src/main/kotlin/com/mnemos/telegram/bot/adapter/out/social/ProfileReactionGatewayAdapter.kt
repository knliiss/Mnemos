package com.mnemos.telegram.bot.adapter.out.social

import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionRequest
import com.mnemos.social.contracts.internal.v1.InternalTelegramReactionType
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionChangeResult
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionState
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionType
import com.mnemos.telegram.bot.application.profilereaction.port.out.ProfileReactionGateway
import feign.FeignException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProfileReactionGatewayAdapter(
    private val client: SocialServiceClient,
) : ProfileReactionGateway {
    override fun get(telegramId: Long, profileId: UUID): ProfileReactionState =
        try {
            val response = client.getReaction(telegramId, profileId)
            ProfileReactionState.Available(response.type?.let { ProfileReactionType.valueOf(it.name) })
        } catch (exception: FeignException) {
            if (exception.status() == 404) ProfileReactionState.AccountRequired
            else ProfileReactionState.Unavailable
        } catch (_: RuntimeException) {
            ProfileReactionState.Unavailable
        }

    override fun set(
        telegramId: Long,
        profileId: UUID,
        type: ProfileReactionType?,
    ): ProfileReactionChangeResult =
        try {
            if (type == null) client.removeReaction(telegramId, profileId)
            else client.setReaction(
                telegramId, profileId,
                InternalTelegramReactionRequest(InternalTelegramReactionType.valueOf(type.name)),
            )
            ProfileReactionChangeResult.APPLIED
        } catch (exception: FeignException) {
            when (exception.status()) {
                404 -> ProfileReactionChangeResult.ACCOUNT_REQUIRED
                403 -> ProfileReactionChangeResult.FORBIDDEN
                400, 409, 422 -> ProfileReactionChangeResult.INVALID
                else -> ProfileReactionChangeResult.UNAVAILABLE
            }
        } catch (_: RuntimeException) {
            ProfileReactionChangeResult.UNAVAILABLE
        }
}
