package com.mnemos.accountservice.application.context

import com.mnemos.accountservice.domain.exception.UserNotFoundException
import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.domain.repository.user.UserRepository
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.security.CurrentMnemosUser
import org.jboss.logging.Logger
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider(
    private val currentMnemosUser: CurrentMnemosUser,
    private val userRepository: UserRepository,
    private val logger: Logger = Logger.getLogger(CurrentUserProvider::class.java.name),
) {
    fun get(): User {
        val keycloakId = KeycloakId(currentMnemosUser.get().keycloakId)
        val user = userRepository.findByKeycloakId(keycloakId)
        if (user == null) {
            logger.warn("User with ID $keycloakId not found")
            throw UserNotFoundException(keycloakId = keycloakId)
        }
        return user
    }
}
