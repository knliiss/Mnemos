package com.mnemos.wallet.application.leaderboard.coin

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.account.model.AccountSummary
import com.mnemos.wallet.application.account.port.out.AccountSummaryProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.UUID

class ListCoinLeaderboardServiceTest {
    @Test
    fun `leaderboard preserves ranking and enriches account identities`() {
        val firstAccountId = AccountId.generate()
        val secondAccountId = AccountId.generate()
        val missingAccountId = AccountId.generate()
        val firstPlayerId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val secondPlayerId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val reader =
            FakeCoinLeaderboardReader(
                listOf(
                    CoinLeaderboardCandidate(accountId = firstAccountId, amount = 1_500),
                    CoinLeaderboardCandidate(accountId = secondAccountId, amount = 900),
                    CoinLeaderboardCandidate(accountId = missingAccountId, amount = 400),
                ),
            )
        val cache = FakeCoinLeaderboardCache()
        val summaryProvider =
            FakeAccountSummaryProvider(
                mapOf(
                    firstAccountId to summary(firstAccountId, firstPlayerId, "Alpha"),
                    secondAccountId to summary(secondAccountId, secondPlayerId, "Beta"),
                ),
            )
        val service =
            ListCoinLeaderboardService(
                reader = reader,
                cache = cache,
                accountSummaryProvider = summaryProvider,
            )

        val result = service.execute(limit = 3)

        assertEquals(3, reader.requestedLimit)
        assertEquals(setOf(firstAccountId, secondAccountId, missingAccountId), summaryProvider.requestedIds)
        assertEquals(
            listOf(
                CoinLeaderboardEntry(
                    accountId = firstAccountId,
                    playerId = firstPlayerId,
                    nickname = "Alpha",
                    amount = 1_500,
                ),
                CoinLeaderboardEntry(
                    accountId = secondAccountId,
                    playerId = secondPlayerId,
                    nickname = "Beta",
                    amount = 900,
                ),
            ),
            result,
        )
        assertEquals(result, cache.get(3))
    }

    @Test
    fun `cached leaderboard bypasses reader and account enrichment`() {
        val accountId = AccountId.generate()
        val cachedEntries =
            listOf(
                CoinLeaderboardEntry(
                    accountId = accountId,
                    playerId = UUID.fromString("33333333-3333-3333-3333-333333333333"),
                    nickname = "Cached",
                    amount = 700,
                ),
            )
        val reader = FakeCoinLeaderboardReader(emptyList())
        val cache = FakeCoinLeaderboardCache()
        val summaryProvider = FakeAccountSummaryProvider(emptyMap())
        val service =
            ListCoinLeaderboardService(
                reader = reader,
                cache = cache,
                accountSummaryProvider = summaryProvider,
            )

        cache.put(limit = 3, entries = cachedEntries)

        val result = service.execute(limit = 3)

        assertEquals(cachedEntries, result)
        assertNull(reader.requestedLimit)
        assertEquals(emptySet<AccountId>(), summaryProvider.requestedIds)
    }

    @Test
    fun `leaderboard rejects limits outside public contract`() {
        val service =
            ListCoinLeaderboardService(
                reader = FakeCoinLeaderboardReader(emptyList()),
                cache = FakeCoinLeaderboardCache(),
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
        playerId: UUID,
        nickname: String,
    ): AccountSummary =
        AccountSummary(
            accountId = accountId,
            cristalixUuid = playerId,
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

    private class FakeCoinLeaderboardCache : CoinLeaderboardCache {
        private val entriesByLimit = mutableMapOf<Int, List<CoinLeaderboardEntry>>()

        override fun get(limit: Int): List<CoinLeaderboardEntry>? = entriesByLimit[limit]

        override fun put(
            limit: Int,
            entries: List<CoinLeaderboardEntry>,
        ) {
            entriesByLimit[limit] = entries
        }

        override fun clear() {
            entriesByLimit.clear()
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
