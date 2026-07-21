package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.api.dto.account.AccountResponse
import com.mnemos.accountservice.api.dto.account.CreateAccountRequest
import com.mnemos.accountservice.api.mapper.account.AccountMapper.toResponse
import com.mnemos.accountservice.application.security.CurrentUserProvider
import com.mnemos.accountservice.domain.exception.account.AccountIdentityConflictException
import com.mnemos.accountservice.domain.exception.account.CristalixPlayerNotFoundException
import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.repository.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.AccountRepository
import com.mnemos.accountservice.integration.cristalix.CristalixIdentityProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateAccountService(
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val cristalixIdentityProvider: CristalixIdentityProvider,
    private val currentUserProvider: CurrentUserProvider
) {

    @Transactional
    fun createAccount(request: CreateAccountRequest): AccountResponse {
        val owner = currentUserProvider.get()
        val cristalixNickname = CristalixNickname(request.cristalixNickname)

        val cristalixPlayer = cristalixIdentityProvider.findByNickname(cristalixNickname)
            ?: throw CristalixPlayerNotFoundException(cristalixNickname)

        if (accountRepository.existsByCristalixUuid(cristalixPlayer.uuid)) {
            throw AccountIdentityConflictException(cristalixPlayer.uuid)
        }

        val account = Account.create(
            ownerUserId = owner.id,
            cristalixUuid = cristalixPlayer.uuid,
            cristalixNickname = cristalixPlayer.nickname,
        )

        val createdAccount = accountRepository.create(account)

        val ownerMembership = AccountMember.create(
            accountId = createdAccount.id,
            userId = owner.id,
        )

        accountMemberRepository.create(ownerMembership)

        return createdAccount.toResponse()
    }
}