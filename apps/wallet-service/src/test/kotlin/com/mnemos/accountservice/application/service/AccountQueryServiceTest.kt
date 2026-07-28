package com.mnemos.accountservice.application.service

import com.mnemos.accountservice.application.query.account.AccountDetails
import com.mnemos.accountservice.application.query.account.AccountQueryRepository
import com.mnemos.accountservice.application.query.account.AccountSummary
import com.mnemos.accountservice.application.service.account.AccountQueryService
import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import com.mnemos.accountservice.support.InMemoryUserRepository
import com.mnemos.accountservice.support.SecurityContextTest
import com.mnemos.accountservice.support.TEST_TIME
import com.mnemos.accountservice.support.authenticate
import com.mnemos.accountservice.support.currentUserProvider
import com.mnemos.accountservice.support.member
import com.mnemos.accountservice.support.user
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class AccountQueryServiceTest : SecurityContextTest() {
    @Test
    fun `available accounts passes current user and active account`() {
        val activeId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = activeId)
        val repository = RecordingAccountQueryRepository()
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)

        AccountQueryService(currentUserProvider(users), repository).getAvailableAccounts()

        assertEquals(user.id, repository.summaryUserId)
        assertEquals(activeId, repository.summaryActiveAccountId)
    }

    @Test
    fun `current active account returns null without active selection`() {
        val user = user()
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)

        assertNull(AccountQueryService(currentUserProvider(users), RecordingAccountQueryRepository()).getCurrentActiveAccount())
    }

    @Test
    fun `current active account returns scoped details`() {
        val activeId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = activeId)
        val member = member(accountId = activeId, userId = user.id)
        val details =
            AccountDetails(
                activeId,
                UUID.randomUUID(),
                "Player",
                member.id,
                2,
                10,
                true,
                true,
                TEST_TIME,
                TEST_TIME,
            )
        val repository = RecordingAccountQueryRepository(details = details)
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)

        assertEquals(details, AccountQueryService(currentUserProvider(users), repository).getCurrentActiveAccount())
        assertEquals(user.id, repository.detailsUserId)
        assertEquals(activeId, repository.detailsAccountId)
    }

    @Test
    fun `missing active account projection is treated as not found`() {
        val activeId = AccountId(UUID.randomUUID())
        val user = user(activeAccountId = activeId)
        val users = InMemoryUserRepository(listOf(user))
        authenticate(user)

        assertThrows<AccountNotFoundException> {
            AccountQueryService(currentUserProvider(users), RecordingAccountQueryRepository()).getCurrentActiveAccount()
        }
    }

    private class RecordingAccountQueryRepository(
        private val summaries: List<AccountSummary> = emptyList(),
        private val details: AccountDetails? = null,
    ) : AccountQueryRepository {
        var summaryUserId: UserId? = null
        var summaryActiveAccountId: AccountId? = null
        var detailsAccountId: AccountId? = null
        var detailsUserId: UserId? = null

        override fun findSummariesByUserId(
            userId: UserId,
            activeAccountId: AccountId?,
        ): List<AccountSummary> {
            summaryUserId = userId
            summaryActiveAccountId = activeAccountId
            return summaries
        }

        override fun findDetailsByAccountIdAndUserId(
            accountId: AccountId,
            userId: UserId,
            activeAccountId: AccountId?,
        ): AccountDetails? {
            detailsAccountId = accountId
            detailsUserId = userId
            return details
        }
    }
}
