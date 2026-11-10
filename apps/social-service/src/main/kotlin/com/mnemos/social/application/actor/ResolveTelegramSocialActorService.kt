package com.mnemos.social.application.actor

import com.mnemos.social.application.actor.exception.ActiveSocialAccountRequiredException
import com.mnemos.social.application.actor.model.SocialActor
import com.mnemos.social.application.actor.port.out.AccountAccessPort
import com.mnemos.social.application.actor.port.out.TelegramSocialIdentityPort
import org.springframework.stereotype.Service

@Service
class ResolveTelegramSocialActorService(
    private val identities: TelegramSocialIdentityPort,
    private val accounts: AccountAccessPort,
) : ResolveTelegramSocialActorUseCase {
    override fun resolve(telegramId: Long): SocialActor? {
        if (telegramId <= 0) return null
        val identity = identities.resolve(telegramId) ?: return null
        val activeId = identity.activeAccountId ?: return null
        val resolved = try {
            accounts.resolveActiveActor(identity.userId)
        } catch (_: ActiveSocialAccountRequiredException) {
            return null
        }
        return resolved.takeIf {
            it.userId == identity.userId && it.accountId == activeId
        }
    }
}
