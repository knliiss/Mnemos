package com.mnemos.bet.application.leaderboard.predictor

import com.mnemos.bet.application.account.model.AccountSummary
import com.mnemos.bet.application.account.port.out.AccountSummaryProvider
import com.mnemos.bet.application.event.model.WipeWindow
import com.mnemos.bet.application.event.port.out.WipeProvider
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
        val selectedWipeId = UUID.fromString("019c1e23-5000-7000-8000-000000000101")
        val selectedWipe =
            WipeWindow(
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
                mapOf(
                    accountId to
                        AccountSummary(
                            accountId = accountId,
                            nickname = "Predictor",
                        ),
                ),
            )
        val wipeProvider = FakeWipeProvider(selectedWipe)
        val service =
            ListPredictorLeaderboardService(
                reader = reader,
                accountSummaryProvider = accountSummaryProvider,
                wipeProvider = wipeProvider,
            )

        val result =
            service.execute(
                PredictorLeaderboardQuery(
                    wipeId = selectedWipeId,
                    sort = PredictorLeaderboardSort.WIN_RATE,
                    minimumSample = 7,
                    limit = 25,
                ),
            )

        assertEquals(selectedWipeId, wipeProvider.requestedWipeId)
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
            WipeWindow(
                startedAt = Instant.parse("2026-08-15T00:00:00Z"),
                endedAt = null,
            )
        val reader = FakePredictorLeaderboardReader(emptyList())
        val wipeProvider = FakeWipeProvider(currentWipe)
        val service =
            ListPredictorLeaderboardService(
                reader = reader,
                accountSummaryProvider = FakeAccountSummaryProvider(emptyMap()),
                wipeProvider = wipeProvider,
            )

        service.execute(
            PredictorLeaderboardQuery(
                wipeId = null,
                sort = PredictorLeaderboardSort.PROFIT,
                minimumSample = 99,
                limit = 50,
            ),
        )

        assertEquals(1, wipeProvider.currentWipeCalls)
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
    fun `leaderboard validates public limits before wipe lookup`() {
        val wipeProvider =
            FakeWipeProvider(
                WipeWindow(
                    startedAt = Instant.EPOCH,
                    endedAt = null,
                ),
            )
        val service =
            ListPredictorLeaderboardService(
                reader = FakePredictorLeaderboardReader(emptyList()),
                accountSummaryProvider = FakeAccountSummaryProvider(emptyMap()),
                wipeProvider = wipeProvider,
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

        assertEquals(0, wipeProvider.currentWipeCalls)
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
        override fun getByIds(accountIds: Set<AccountId>): Map<AccountId, AccountSummary> = summaries.filterKeys(accountIds::contains)
    }

    private class FakeWipeProvider(
        private val wipe: WipeWindow,
    ) : WipeProvider {
        var currentWipeCalls = 0
        var requestedWipeId: UUID? = null

        override fun current(): WipeWindow {
            currentWipeCalls += 1

            return wipe
        }

        override fun get(wipeId: UUID): WipeWindow {
            requestedWipeId = wipeId

            return wipe
        }
    }
}
