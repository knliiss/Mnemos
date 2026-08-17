package com.mnemos.profile.application.profile.usecase.bootstrap

import com.mnemos.profile.application.profile.port.out.AccountBootstrapSource
import com.mnemos.profile.application.profile.usecase.sync.SyncPublicProfileUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BootstrapPublicProfilesService(
    private val source: AccountBootstrapSource,
    private val syncPublicProfileUseCase: SyncPublicProfileUseCase,
    @Value("\${mnemos.profile.bootstrap.batch-size:200}")
    private val batchSize: Int,
) : BootstrapPublicProfilesUseCase {
    private var afterAccountId: UUID? = null
    private var completed = false

    @Synchronized
    override fun execute() {
        if (completed) {
            return
        }

        require(batchSize in 1..MAX_BATCH_SIZE) {
            "Profile bootstrap batch size must be between 1 and $MAX_BATCH_SIZE"
        }

        val batch =
            source.fetch(
                afterAccountId = afterAccountId,
                limit = batchSize,
            )

        if (batch.isEmpty()) {
            completed = true
            return
        }

        batch.forEach { account ->
            syncPublicProfileUseCase.execute(
                accountId = account.accountId,
                cristalixUuid = account.cristalixUuid,
                cristalixNickname = account.cristalixNickname,
                createdAt = account.createdAt,
            )
        }

        afterAccountId = batch.last().accountId
    }

    private companion object {
        const val MAX_BATCH_SIZE = 500
    }
}
