package com.mnemos.wallet.application.leaderboard.coin

import com.mnemos.wallet.application.account.port.out.AccountSummaryProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListCoinLeaderboardService(
    private val reader: CoinLeaderboardReader,
    private val cache: CoinLeaderboardCache,
    private val accountSummaryProvider: AccountSummaryProvider,
) {
    fun execute(limit: Int): List<CoinLeaderboardEntry> {
        require(limit in MIN_LIMIT..MAX_LIMIT) {
            "Coin leaderboard limit must be between $MIN_LIMIT and $MAX_LIMIT"
        }

        cache.get(limit)?.let { return it }

        val candidates = reader.read(limit)
        val summaries = accountSummaryProvider.getByIds(candidates.mapTo(linkedSetOf()) { it.accountId })
        val entries =
            candidates.mapNotNull { candidate ->
                val summary = summaries[candidate.accountId] ?: return@mapNotNull null

                CoinLeaderboardEntry(
                    accountId = candidate.accountId,
                    playerId = summary.cristalixUuid,
                    nickname = summary.cristalixNickname,
                    amount = candidate.amount,
                )
            }

        cache.put(limit, entries)

        return entries
    }

    private companion object {
        const val MIN_LIMIT = 3
        const val MAX_LIMIT = 100
    }
}
