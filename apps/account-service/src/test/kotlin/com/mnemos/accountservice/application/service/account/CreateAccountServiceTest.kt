package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.domain.exception.UserNotFoundException
import com.mnemos.accountservice.domain.exception.account.AccountIdentityConflictException
import com.mnemos.accountservice.domain.exception.account.CristalixPlayerNotFoundException
import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.model.account.CristalixUuid
import com.mnemos.accountservice.domain.model.user.User
import com.mnemos.accountservice.domain.repository.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.AccountRepository
import com.mnemos.accountservice.domain.repository.UserRepository
import com.mnemos.accountservice.integration.cristalix.CristalixIdentityProvider
import com.mnemos.accountservice.integration.cristalix.model.CristalixPlayer
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.KeycloakId
import com.mnemos.common.core.model.user.Username
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.security.MnemosPrincipal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CreateAccountServiceTest {

    @Mock
    lateinit var accountRepository: AccountRepository

    @Mock
    lateinit var accountMemberRepository: AccountMemberRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var cristalixIdentityProvider: CristalixIdentityProvider

    @Mock
    lateinit var currentMnemosUser: CurrentMnemosUser

    @Mock
    lateinit var principal: MnemosPrincipal

    @InjectMocks
    lateinit var createAccountService: CreateAccountService

    @Test
    fun `should create account and owner membership`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val owner = createUser(keycloakId)
        val requestedNickname = CristalixNickname("kotuk_pvp")
        val canonicalNickname = CristalixNickname("KoTuK_PvP")
        val cristalixUuid = CristalixUuid(UUID.randomUUID())

        mockCurrentUser(keycloakId)

        whenever(userRepository.findByKeycloakId(keycloakId))
            .thenReturn(owner)

        whenever(cristalixIdentityProvider.findByNickname(requestedNickname))
            .thenReturn(
                CristalixPlayer(
                    uuid = cristalixUuid,
                    nickname = canonicalNickname
                )
            )

        whenever(accountRepository.existsByCristalixUuid(cristalixUuid))
            .thenReturn(false)

        whenever(accountRepository.create(any()))
            .thenAnswer { invocation ->
                invocation.getArgument<Account>(0)
            }

        val response = createAccountService.createAccount(requestedNickname)

        val accountCaptor = argumentCaptor<Account>()
        val membershipCaptor = argumentCaptor<AccountMember>()

        verify(accountRepository).create(accountCaptor.capture())
        verify(accountMemberRepository).create(membershipCaptor.capture())

        val createdAccount = accountCaptor.firstValue
        val createdMembership = membershipCaptor.firstValue

        assertEquals(owner.id, createdAccount.ownerUserId)
        assertEquals(cristalixUuid, createdAccount.cristalixUuid)
        assertEquals(canonicalNickname, createdAccount.cristalixNickname)

        assertEquals(createdAccount.id, createdMembership.accountId)
        assertEquals(owner.id, createdMembership.userId)

        assertEquals(createdAccount.id.value, response.id)
        assertEquals(owner.id.value, response.ownerUserId)
        assertEquals(cristalixUuid.value, response.cristalixUuid)
        assertEquals(canonicalNickname.value, response.cristalixNickname)
    }

    @Test
    fun `should throw when current user does not exist`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val nickname = CristalixNickname("KoTuK_PvP")

        mockCurrentUser(keycloakId)

        whenever(userRepository.findByKeycloakId(keycloakId))
            .thenReturn(null)

        assertThrows<UserNotFoundException> {
            createAccountService.createAccount(nickname)
        }

        verify(userRepository).findByKeycloakId(keycloakId)
        verifyNoInteractions(cristalixIdentityProvider)
        verifyNoInteractions(accountRepository)
        verifyNoInteractions(accountMemberRepository)
    }

    @Test
    fun `should throw when cristalix player does not exist`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val owner = createUser(keycloakId)
        val nickname = CristalixNickname("UnknownPlayer")

        mockCurrentUser(keycloakId)

        whenever(userRepository.findByKeycloakId(keycloakId))
            .thenReturn(owner)

        whenever(cristalixIdentityProvider.findByNickname(nickname))
            .thenReturn(null)

        assertThrows<CristalixPlayerNotFoundException> {
            createAccountService.createAccount(nickname)
        }

        verify(cristalixIdentityProvider).findByNickname(nickname)
        verifyNoInteractions(accountRepository)
        verifyNoInteractions(accountMemberRepository)
    }

    @Test
    fun `should throw when cristalix identity is already linked`() {
        val keycloakId = KeycloakId(UUID.randomUUID())
        val owner = createUser(keycloakId)
        val nickname = CristalixNickname("KoTuK_PvP")
        val cristalixUuid = CristalixUuid(UUID.randomUUID())

        mockCurrentUser(keycloakId)

        whenever(userRepository.findByKeycloakId(keycloakId))
            .thenReturn(owner)

        whenever(cristalixIdentityProvider.findByNickname(nickname))
            .thenReturn(
                CristalixPlayer(
                    uuid = cristalixUuid,
                    nickname = nickname
                )
            )

        whenever(accountRepository.existsByCristalixUuid(cristalixUuid))
            .thenReturn(true)

        assertThrows<AccountIdentityConflictException> {
            createAccountService.createAccount(nickname)
        }

        verify(accountRepository).existsByCristalixUuid(cristalixUuid)
        verify(accountRepository, never()).create(any())
        verifyNoInteractions(accountMemberRepository)
    }

    private fun mockCurrentUser(keycloakId: KeycloakId) {
        whenever(currentMnemosUser.get())
            .thenReturn(principal)

        whenever(principal.keycloakId)
            .thenReturn(keycloakId.value)
    }

    private fun createUser(keycloakId: KeycloakId): User {
        return User.create(
            keycloakId = keycloakId,
            telegramId = TelegramId(randomTelegramId()),
            username = Username(uniqueUsername())
        )
    }

    private fun uniqueUsername(): String {
        return "test_${UUID.randomUUID().toString().substring(0, 8)}"
    }

    private fun randomTelegramId(): Long {
        return UUID.randomUUID()
            .mostSignificantBits
            .let(Math::abs)
            .coerceAtLeast(1)
    }
}