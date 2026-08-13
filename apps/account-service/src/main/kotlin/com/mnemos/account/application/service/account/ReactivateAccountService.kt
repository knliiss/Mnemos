package com.mnemos.account.application.service.account

import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReactivateAccountService(
    private val accountRepository: AccountRepository,
) {
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    fun reactivate(accountId: AccountId) {
        val account =
            accountRepository.findByIdForUpdate(accountId)
                ?: throw AccountNotFoundException(accountId)

        if (account.status != AccountStatus.DEACTIVATED) {
            return
        }

        account.reactivate()
        accountRepository.update(account)
    }
}
