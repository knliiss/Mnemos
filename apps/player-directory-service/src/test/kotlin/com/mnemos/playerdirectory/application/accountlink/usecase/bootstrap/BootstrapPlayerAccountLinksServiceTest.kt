package com.mnemos.playerdirectory.application.accountlink.usecase.bootstrap

import com.mnemos.playerdirectory.application.accountlink.model.AccountBootstrapSnapshot
import com.mnemos.playerdirectory.application.accountlink.port.out.AccountBootstrapSource
import com.mnemos.playerdirectory.application.accountlink.usecase.project.ProjectPlayerAccountLinkCommand
import com.mnemos.playerdirectory.application.accountlink.usecase.project.ProjectPlayerAccountLinkUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class BootstrapPlayerAccountLinksServiceTest {
    @Test
    fun `bootstrap projects historical account links across keyset pages`() {
        val firstAccountId = UUID.fromString("0198f6ce-e934-7000-8000-000000000101")
        val secondAccountId = UUID.fromString("0198f6ce-e934-7000-8000-000000000102")
        val source =
            RecordingAccountBootstrapSource(
                pages =
                    listOf(
                        listOf(snapshot(firstAccountId, "0198f6ce-e934-7000-8000-000000000201")),
                        listOf(snapshot(secondAccountId, "0198f6ce-e934-7000-8000-000000000202")),
                        emptyList(),
                    ),
            )
        val projector = RecordingProjectPlayerAccountLinkUseCase()
        val service = BootstrapPlayerAccountLinksService(source, projector, batchSize = 1)

        service.execute()
        service.execute()
        service.execute()
        service.execute()

        assertEquals(listOf(null, firstAccountId, secondAccountId), source.afterAccountIds)
        assertEquals(listOf(firstAccountId, secondAccountId), projector.commands.map(ProjectPlayerAccountLinkCommand::accountId))
    }

    private fun snapshot(
        accountId: UUID,
        playerId: String,
    ): AccountBootstrapSnapshot =
        AccountBootstrapSnapshot(
            accountId = accountId,
            cristalixUuid = UUID.fromString(playerId),
            createdAt = Instant.parse("2026-08-01T00:00:00Z"),
        )

    private class RecordingAccountBootstrapSource(
        private val pages: List<List<AccountBootstrapSnapshot>>,
    ) : AccountBootstrapSource {
        val afterAccountIds = mutableListOf<UUID?>()
        private var index = 0

        override fun fetch(
            afterAccountId: UUID?,
            limit: Int,
        ): List<AccountBootstrapSnapshot> {
            assertEquals(1, limit)
            afterAccountIds += afterAccountId

            return pages[index++]
        }
    }

    private class RecordingProjectPlayerAccountLinkUseCase : ProjectPlayerAccountLinkUseCase {
        val commands = mutableListOf<ProjectPlayerAccountLinkCommand>()

        override fun execute(command: ProjectPlayerAccountLinkCommand) {
            commands += command
        }
    }
}
