package com.mnemos.account.application.service.user

import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InternalActiveAccountUsersService(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun findByNickname(nickname: String): List<User> = userRepository.findAllByActiveAccountNickname(nickname.trim())
}
