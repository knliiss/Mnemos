package com.mnemos.profile.application.profile.usecase.bootstrap

import com.mnemos.profile.application.profile.model.AccountBootstrapSnapshot
import com.mnemos.profile.application.profile.port.out.AccountBootstrapSource
import com.mnemos.profile.application.profile.usecase.sync.SyncPublicProfileUseCase
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class BootstrapPublicProfilesServiceTest {
    @Test
    fun `advances cursor and stops after empty batch`() {
        val source = mock<AccountBootstrapSource>()
        val syncPublicProfileUseCase = mock<SyncPublicProfileUseCase>()
        val service =
            BootstrapPublicProfilesService(
                source = source,
                syncPublicProfileUseCase = syncPublicProfileUseCase,
                batchSize = 2,
            )
        val first = snapshot("019c1ab0-9900-7000-8000-000000000001", "First")
        val second = snapshot("019c1ab0-9900-7000-8000-000000000002", "Second")

        whenever(source.fetch(null, 2)).thenReturn(listOf(first, second))
        whenever(source.fetch(second.accountId, 2)).thenReturn(emptyList())

        service.execute()
        service.execute()
        service.execute()

        verify(source).fetch(null, 2)
        verify(source).fetch(second.accountId, 2)
        verify(source, times(2)).fetch(anyOrNull(), eq(2))
        verify(syncPublicProfileUseCase).execute(
            accountId = first.accountId,
            cristalixUuid = first.cristalixUuid,
            cristalixNickname = first.cristalixNickname,
            createdAt = first.createdAt,
        )
        verify(syncPublicProfileUseCase).execute(
            accountId = second.accountId,
            cristalixUuid = second.cristalixUuid,
            cristalixNickname = second.cristalixNickname,
            createdAt = second.createdAt,
        )
    }

    private fun snapshot(
        accountId: String,
        nickname: String,
    ): AccountBootstrapSnapshot =
        AccountBootstrapSnapshot(
            accountId = UUID.fromString(accountId),
            cristalixUuid = UUID.fromString(accountId.replace("9900", "9901")),
            cristalixNickname = nickname,
            createdAt = Instant.parse("2026-08-15T20:00:00Z"),
        )
}
