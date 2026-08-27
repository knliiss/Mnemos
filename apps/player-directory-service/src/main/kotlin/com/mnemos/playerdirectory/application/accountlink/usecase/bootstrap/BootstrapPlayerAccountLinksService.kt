package com.mnemos.playerdirectory.application.accountlink.usecase.bootstrap

import com.mnemos.playerdirectory.application.accountlink.port.out.AccountBootstrapSource
import com.mnemos.playerdirectory.application.accountlink.usecase.project.ProjectPlayerAccountLinkCommand
import com.mnemos.playerdirectory.application.accountlink.usecase.project.ProjectPlayerAccountLinkUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BootstrapPlayerAccountLinksService(
    private val source: AccountBootstrapSource,
    private val projectPlayerAccountLinkUseCase: ProjectPlayerAccountLinkUseCase,
    @Value("\${mnemos.player-directory.account-bootstrap.batch-size:200}")
    private val batchSize: Int,
) : BootstrapPlayerAccountLinksUseCase {
    private var afterAccountId: UUID? = null
    private var completed = false

    @Synchronized
    override fun execute() {
        if (completed) {
            return
        }

        require(batchSize in 1..MAX_BATCH_SIZE) {
            "Player account bootstrap batch size must be between 1 and $MAX_BATCH_SIZE"
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
            projectPlayerAccountLinkUseCase.execute(
                ProjectPlayerAccountLinkCommand(
                    playerId = account.cristalixUuid,
                    accountId = account.accountId,
                    recordedAt = account.createdAt,
                ),
            )
        }

        afterAccountId = batch.last().accountId
    }

    private companion object {
        const val MAX_BATCH_SIZE = 500
    }
}
