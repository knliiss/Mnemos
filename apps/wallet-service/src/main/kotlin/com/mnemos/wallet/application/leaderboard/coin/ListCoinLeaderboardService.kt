package com.mnemos.wallet.application.leaderboard.coin

import com.mnemos.wallet.application.account.port.out.AccountSummaryProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListCoinLeaderboardService(
    private val reader: CoinLeaderboardReader,
    private val accountSummaryProvider: AccountSummaryProvider,
) {
    fun execute(limit: Int): List<CoinLeaderboardEntry> {
        require(limit in MIN_LIMIT..MAX_LIMIT) {
            "Coin leaderboard limit must be between $MIN_LIMIT and $MAX_LIMIT"
        }

        val candidates = reader.read(limit)
        val summaries = accountSummaryProvider.getByIds(candidates.mapTo(linkedSetOf()) { it.accountId })

        return candidates.mapNotNull { candidate ->
            val summary = summaries[candidate.accountId] ?: return@mapNotNull null

            CoinLeaderboardEntry(
                accountId = candidate.accountId,
                nickname = summary.cristalixNickname,
                amount = candidate.amount,
            )
        }
    }

    private companion object {
        const val MIN_LIMIT = 3
        const val MAX_LIMIT = 100
    }
}
