package com.mnemos.account.application.service.user

import com.mnemos.account.application.query.user.InternalTelegramUserContext
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.TelegramId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InternalTelegramUserQueryService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun findByTelegramId(telegramId: TelegramId): InternalTelegramUserContext? =
        userRepository
            .findByTelegramId(telegramId)
            ?.let { user ->
                InternalTelegramUserContext(
                    userId = user.id,
                    activeAccountId = user.activeAccountId,
                )
            }
}
