package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.api.dto.account.AccountResponse
import com.mnemos.accountservice.api.dto.account.AvailableAccountResponse
import com.mnemos.accountservice.api.mapper.account.AccountMapper.toAvailableAccountResponse
import com.mnemos.accountservice.api.mapper.account.AccountMapper.toResponse
import com.mnemos.accountservice.application.security.CurrentUserProvider
import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountQueryService(
    private val accountRepository: AccountRepository,
    private val currentUserProvider: CurrentUserProvider
) {

    @Transactional(readOnly = true)
    fun getCurrentActiveAccount(): AccountResponse? {
        val user = currentUserProvider.get()
        val activeAccountId = user.activeAccountId ?: return null

        return accountRepository.findById(activeAccountId)?.toResponse()
    }

    @Transactional(readOnly = true)
    fun getAvailableAccounts(): List<AvailableAccountResponse> {
        val user = currentUserProvider.get()
        return accountRepository.findAllAvailableForUser(user.id).map { it.toAvailableAccountResponse() }
    }

}