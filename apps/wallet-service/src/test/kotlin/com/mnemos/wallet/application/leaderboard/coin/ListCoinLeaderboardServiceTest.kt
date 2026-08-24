package com.mnemos.wallet.application.leaderboard.coin

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.account.model.AccountSummary
import com.mnemos.wallet.application.account.port.out.AccountSummaryProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class ListCoinLeaderboardServiceTest {
    @Test
    fun `leaderboard preserves ranking and enriches account nicknames`() {
        val firstAccountId = AccountId.generate()
        val secondAccountId = AccountId.generate()
        val missingAccountId = AccountId.generate()
        val reader =
            FakeCoinLeaderboardReader(
                listOf(
                    CoinLeaderboardCandidate(accountId = firstAccountId, amount = 1_500),
                    CoinLeaderboardCandidate(accountId = secondAccountId, amount = 900),
                    CoinLeaderboardCandidate(accountId = missingAccountId, amount = 400),
                ),
            )
        val summaryProvider =
            FakeAccountSummaryProvider(
                mapOf(
                    firstAccountId to summary(firstAccountId, "Alpha"),
                    secondAccountId to summary(secondAccountId, "Beta"),
                ),
            )
        val service = ListCoinLeaderboardService(reader, summaryProvider)

        val result = service.execute(limit = 3)

        assertEquals(3, reader.requestedLimit)
        assertEquals(setOf(firstAccountId, secondAccountId, missingAccountId), summaryProvider.requestedIds)
        assertEquals(
            listOf(
                CoinLeaderboardEntry(
                    accountId = firstAccountId,
                    nickname = "Alpha",
                    amount = 1_500,
                ),
                CoinLeaderboardEntry(
                    accountId = secondAccountId,
                    nickname = "Beta",
                    amount = 900,
                ),
            ),
            result,
        )
    }

    @Test
    fun `leaderboard rejects limits outside public contract`() {
        val service =
            ListCoinLeaderboardService(
                reader = FakeCoinLeaderboardReader(emptyList()),
                accountSummaryProvider = FakeAccountSummaryProvider(emptyMap()),
            )

        assertThrows(IllegalArgumentException::class.java) {
            service.execute(limit = 2)
        }

        assertThrows(IllegalArgumentException::class.java) {
            service.execute(limit = 101)
        }
    }

    private fun summary(
        accountId: AccountId,
        nickname: String,
    ): AccountSummary =
        AccountSummary(
            accountId = accountId,
            cristalixUuid = UUID.randomUUID(),
            cristalixNickname = nickname,
        )

    private class FakeCoinLeaderboardReader(
        private val candidates: List<CoinLeaderboardCandidate>,
    ) : CoinLeaderboardReader {
        var requestedLimit: Int? = null

        override fun read(limit: Int): List<CoinLeaderboardCandidate> {
            requestedLimit = limit

            return candidates
        }
    }

    private class FakeAccountSummaryProvider(
        private val summaries: Map<AccountId, AccountSummary>,
    ) : AccountSummaryProvider {
        var requestedIds: Set<AccountId> = emptySet()

        override fun getByIds(accountIds: Set<AccountId>): Map<AccountId, AccountSummary> {
            requestedIds = accountIds

            return summaries
        }
    }
}
