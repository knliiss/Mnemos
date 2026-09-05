package com.mnemos.account.application.service.user

import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InternalUserLocalizationService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun findLocalization(userId: UserId): LocalizationLanguage? = userRepository.findById(userId)?.localizationLanguage
}
