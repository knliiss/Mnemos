package com.mnemos.account.application.service.user

import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.LocalizationLanguage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InternalTelegramUserSettingsService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun findLocalization(telegramId: TelegramId): LocalizationLanguage? = userRepository.findByTelegramId(telegramId)?.localizationLanguage

    @Transactional
    fun updateLocalization(
        telegramId: TelegramId,
        localizationLanguage: LocalizationLanguage,
    ): LocalizationLanguage? {
        val user = userRepository.findByTelegramId(telegramId) ?: return null

        user.updateLocalization(
            user = user,
            localizationLanguage = localizationLanguage,
        )

        return userRepository.update(user).localizationLanguage
    }
}
