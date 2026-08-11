package com.mnemos.account.application.service.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class InternalAccountQueryService(
    private val accountRepository: AccountRepository,
) {
    fun findActiveByUserId(userId: UUID): Account? = accountRepository.findActiveByUserId(UserId(userId))

    fun findActiveByCristalixNickname(nickname: String): Account? = accountRepository.findActiveByCristalixNickname(nickname)
}
