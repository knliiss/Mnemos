package com.mnemos.accountservice.application.security

import com.mnemos.accountservice.domain.exception.UserNotFoundException
import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.domain.repository.UserRepository
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider(
    private val currentMnemosUser: CurrentMnemosUser,
    private val userRepository: UserRepository
) {

    fun get(): User {
        val keycloakId = KeycloakId(currentMnemosUser.get().keycloakId)
        return userRepository.findByKeycloakId(keycloakId)
            ?: throw UserNotFoundException(keycloakId)
    }

}