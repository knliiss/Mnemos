package com.mnemos.bet.application.leaderboard.predictor

import com.mnemos.bet.adapter.out.event.CollectorCoverageResponse
import com.mnemos.bet.adapter.out.event.EventInternalClient
import com.mnemos.bet.adapter.out.event.EventWipeResponse
import com.mnemos.bet.adapter.out.event.FirstItemDropResponse
import com.mnemos.bet.application.account.model.AccountSummary
import com.mnemos.bet.application.account.port.out.AccountSummaryProvider
import com.mnemos.bet.contracts.shared.BetItemType
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class ListPredictorLeaderboardServiceTest {
    @Test
    fun `win rate leaderboard uses selected wipe and configured minimum sample`() {
        val accountId = AccountId.generate()
        val selectedWipeId = UUID.randomUUID()
        val selectedWipe =
            EventWipeResponse(
                id = selectedWipeId,
                startedAt = Instant.parse("2026-08-01T00:00:00Z"),
                endedAt = Instant.parse("2026-08-15T00:00:00Z"),
            )
        val reader =
            FakePredictorLeaderboardReader(
                listOf(
                    PredictorLeaderboardCandidate(
                        accountId = accountId,
                        bets = 8,
                        wins = 5,
                        profit = 240,
                        turnover = 1_200,
                    ),
                ),
            )
        val accountSummaryProvider =
            FakeAccountSummaryProvider(
                mapOf(accountId to AccountSummary(accountId = accountId, nickname = "Predictor")),
            )
        val eventClient = FakeEventInternalClient(selectedWipe)
        val service = ListPredictorLeaderboardService(reader, accountSummaryProvider, eventClient)

        val result =
            service.execute(
                PredictorLeaderboardQuery(
                    wipeId = selectedWipeId,
                    sort = PredictorLeaderboardSort.WIN_RATE,
                    minimumSample = 7,
                    limit = 25,
                ),
            )

        assertEquals(selectedWipeId, eventClient.requestedWipeId)
        assertEquals(
            ReadInvocation(
                from = selectedWipe.startedAt,
                to = selectedWipe.endedAt,
                sort = PredictorLeaderboardSort.WIN_RATE,
                minimumSample = 7,
                limit = 25,
            ),
            reader.invocation,
        )
        assertEquals(
            PredictorLeaderboardEntry(
                accountId = accountId,
                nickname = "Predictor",
                bets = 8,
                wins = 5,
                winRate = BigDecimal("0.6250"),
                profit = 240,
                turnover = 1_200,
            ),
            result.single(),
        )
    }

    @Test
    fun `non win rate sorting uses current wipe and ignores custom sample threshold`() {
        val currentWipe =
            EventWipeResponse(
                id = UUID.randomUUID(),
                startedAt = Instant.parse("2026-08-15T00:00:00Z"),
                endedAt = null,
            )
        val reader = FakePredictorLeaderboardReader(emptyList())
        val eventClient = FakeEventInternalClient(currentWipe)
        val service =
            ListPredictorLeaderboardService(
                reader = reader,
                accountSummaryProvider = FakeAccountSummaryProvider(emptyMap()),
                eventInternalClient = eventClient,
            )

        service.execute(
            PredictorLeaderboardQuery(
                wipeId = null,
                sort = PredictorLeaderboardSort.PROFIT,
                minimumSample = 99,
                limit = 50,
            ),
        )

        assertEquals(1, eventClient.currentWipeCalls)
        assertEquals(
            ReadInvocation(
                from = currentWipe.startedAt,
                to = null,
                sort = PredictorLeaderboardSort.PROFIT,
                minimumSample = 1,
                limit = 50,
            ),
            reader.invocation,
        )
    }

    @Test
    fun `leaderboard validates public limits before service calls`() {
        val eventClient =
            FakeEventInternalClient(
                EventWipeResponse(
                    id = UUID.randomUUID(),
                    startedAt = Instant.EPOCH,
                    endedAt = null,
                ),
            )
        val service =
            ListPredictorLeaderboardService(
                reader = FakePredictorLeaderboardReader(emptyList()),
                accountSummaryProvider = FakeAccountSummaryProvider(emptyMap()),
                eventInternalClient = eventClient,
            )

        assertThrows(IllegalArgumentException::class.java) {
            service.execute(
                PredictorLeaderboardQuery(
                    wipeId = null,
                    sort = PredictorLeaderboardSort.PROFIT,
                    minimumSample = 10,
                    limit = 2,
                ),
            )
        }

        assertThrows(IllegalArgumentException::class.java) {
            service.execute(
                PredictorLeaderboardQuery(
                    wipeId = null,
                    sort = PredictorLeaderboardSort.WIN_RATE,
                    minimumSample = 1_001,
                    limit = 50,
                ),
            )
        }

        assertEquals(0, eventClient.currentWipeCalls)
    }

    private data class ReadInvocation(
        val from: Instant,
        val to: Instant?,
        val sort: PredictorLeaderboardSort,
        val minimumSample: Int,
        val limit: Int,
    )

    private class FakePredictorLeaderboardReader(
        private val candidates: List<PredictorLeaderboardCandidate>,
    ) : PredictorLeaderboardReader {
        var invocation: ReadInvocation? = null

        override fun read(
            from: Instant,
            to: Instant?,
            sort: PredictorLeaderboardSort,
            minimumSample: Int,
            limit: Int,
        ): List<PredictorLeaderboardCandidate> {
            invocation =
                ReadInvocation(
                    from = from,
                    to = to,
                    sort = sort,
                    minimumSample = minimumSample,
                    limit = limit,
                )

            return candidates
        }
    }

    private class FakeAccountSummaryProvider(
        private val summaries: Map<AccountId, AccountSummary>,
    ) : AccountSummaryProvider {
        override fun getByIds(accountIds: Set<AccountId>): Map<AccountId, AccountSummary> =
            summaries.filterKeys(accountIds::contains)
    }

    private class FakeEventInternalClient(
        private val currentWipe: EventWipeResponse,
    ) : EventInternalClient {
        var currentWipeCalls = 0
        var requestedWipeId: UUID? = null

        override fun collectorCoverage(
            from: Instant,
            to: Instant,
        ): CollectorCoverageResponse = error("Not used in leaderboard tests")

        override fun firstItemDrop(
            itemName: String,
            itemType: BetItemType,
            from: Instant,
            to: Instant,
        ): FirstItemDropResponse? = error("Not used in leaderboard tests")

        override fun currentWipe(): EventWipeResponse {
            currentWipeCalls += 1

            return currentWipe
        }

        override fun wipe(wipeId: UUID): EventWipeResponse {
            requestedWipeId = wipeId

            return currentWipe.copy(id = wipeId)
        }
    }
}
