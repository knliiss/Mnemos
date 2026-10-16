package com.mnemos.account.application.usecase.player

import com.mnemos.account.application.port.out.PlayerIdentityRevisionStore
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class SyncPlayerNicknameServiceTest {
    @Test
    fun `newer nickname revision updates account`() {
        val account = createAccount()
        val accountRepository = mock<AccountRepository>()
        val revisionStore = mock<PlayerIdentityRevisionStore>()
        val service = createService(accountRepository, revisionStore)
        val command = command(externalVersion = 7, nickname = "NewNick")

        whenever(accountRepository.findByCristalixUuidForUpdate(PLAYER_ID)).thenReturn(account)
        whenever(
            revisionStore.advanceIfNewer(
                playerId = eq(PLAYER_ID),
                nickname = eq(CristalixNickname("NewNick")),
                externalVersion = eq(7),
                observedAt = eq(OBSERVED_AT),
                recordedAt = any(),
            ),
        ).thenReturn(true)
        whenever(accountRepository.update(account)).thenReturn(account)

        service.execute(command)

        assertEquals(CristalixNickname("NewNick"), account.cristalixNickname)
        verify(accountRepository).update(account)
    }

    @Test
    fun `duplicate nickname revision is ignored`() {
        verifyIgnoredRevision(externalVersion = 7)
    }

    @Test
    fun `stale nickname revision is ignored`() {
        verifyIgnoredRevision(externalVersion = 6)
    }

    @Test
    fun `repeated latest observation repairs an account with an outdated nickname`() {
        val account = createAccount()
        val accountRepository = mock<AccountRepository>()
        val revisionStore = mock<PlayerIdentityRevisionStore>()
        val service = createService(accountRepository, revisionStore)
        val command = command(externalVersion = 9, nickname = "ActuallyRenamed")

        whenever(accountRepository.findByCristalixUuidForUpdate(PLAYER_ID)).thenReturn(account)
        whenever(revisionStore.matchesCurrent(PLAYER_ID, CristalixNickname("ActuallyRenamed"), 9))
            .thenReturn(true)
        whenever(accountRepository.update(account)).thenReturn(account)

        service.execute(command)

        assertEquals(CristalixNickname("ActuallyRenamed"), account.cristalixNickname)
        verify(accountRepository).update(account)
    }

    @Test
    fun `newer nickname revision updates inactive account`() {
        val account = createAccount()
        account.deactivate(Instant.parse("2026-08-27T09:00:00Z"))

        val accountRepository = mock<AccountRepository>()
        val revisionStore = mock<PlayerIdentityRevisionStore>()
        val service = createService(accountRepository, revisionStore)
        val command = command(externalVersion = 8, nickname = "InactiveNick")

        whenever(accountRepository.findByCristalixUuidForUpdate(PLAYER_ID)).thenReturn(account)
        whenever(
            revisionStore.advanceIfNewer(
                playerId = eq(PLAYER_ID),
                nickname = eq(CristalixNickname("InactiveNick")),
                externalVersion = eq(8),
                observedAt = eq(OBSERVED_AT),
                recordedAt = any(),
            ),
        ).thenReturn(true)
        whenever(accountRepository.update(account)).thenReturn(account)

        service.execute(command)

        assertEquals(CristalixNickname("InactiveNick"), account.cristalixNickname)
        verify(accountRepository).update(account)
    }

    @Test
    fun `missing account still records newest revision`() {
        val accountRepository = mock<AccountRepository>()
        val revisionStore = mock<PlayerIdentityRevisionStore>()
        val service = createService(accountRepository, revisionStore)
        val command = command(externalVersion = 9, nickname = "FutureNick")

        whenever(accountRepository.findByCristalixUuidForUpdate(PLAYER_ID)).thenReturn(null)
        whenever(
            revisionStore.advanceIfNewer(
                playerId = eq(PLAYER_ID),
                nickname = eq(CristalixNickname("FutureNick")),
                externalVersion = eq(9),
                observedAt = eq(OBSERVED_AT),
                recordedAt = any(),
            ),
        ).thenReturn(true)

        service.execute(command)

        verify(revisionStore).advanceIfNewer(
            playerId = eq(PLAYER_ID),
            nickname = eq(CristalixNickname("FutureNick")),
            externalVersion = eq(9),
            observedAt = eq(OBSERVED_AT),
            recordedAt = any(),
        )
        verify(accountRepository, never()).update(any())
    }

    private fun verifyIgnoredRevision(externalVersion: Long) {
        val account = createAccount()
        val accountRepository = mock<AccountRepository>()
        val revisionStore = mock<PlayerIdentityRevisionStore>()
        val service = createService(accountRepository, revisionStore)
        val command = command(externalVersion = externalVersion, nickname = "OldNick")

        whenever(accountRepository.findByCristalixUuidForUpdate(PLAYER_ID)).thenReturn(account)
        whenever(
            revisionStore.advanceIfNewer(
                playerId = eq(PLAYER_ID),
                nickname = eq(CristalixNickname("OldNick")),
                externalVersion = eq(externalVersion),
                observedAt = eq(OBSERVED_AT),
                recordedAt = any(),
            ),
        ).thenReturn(false)

        service.execute(command)

        assertEquals(CristalixNickname("InitialNick"), account.cristalixNickname)
        verify(accountRepository, never()).update(any())
    }

    private fun createService(
        accountRepository: AccountRepository,
        revisionStore: PlayerIdentityRevisionStore,
    ): SyncPlayerNicknameService =
        SyncPlayerNicknameService(
            accountRepository = accountRepository,
            revisionStore = revisionStore,
        )

    private fun createAccount(): Account =
        Account.create(
            id = AccountId.generate(),
            ownerMemberId = AccountMemberId.generate(),
            cristalixUuid = PLAYER_ID,
            cristalixNickname = CristalixNickname("InitialNick"),
        )

    private fun command(
        externalVersion: Long,
        nickname: String,
    ): SyncPlayerNicknameCommand =
        SyncPlayerNicknameCommand(
            playerId = PLAYER_ID.id,
            nickname = nickname,
            externalVersion = externalVersion,
            observedAt = OBSERVED_AT,
        )

    private companion object {
        val PLAYER_ID = CristalixUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val OBSERVED_AT: Instant = Instant.parse("2026-08-27T10:00:00Z")
    }
}
