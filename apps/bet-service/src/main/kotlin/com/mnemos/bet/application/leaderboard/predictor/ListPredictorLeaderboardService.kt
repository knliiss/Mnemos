package com.mnemos.bet.application.leaderboard.predictor

import com.mnemos.bet.adapter.out.event.EventInternalClient
import com.mnemos.bet.application.account.port.out.AccountSummaryProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
@Transactional(readOnly = true)
class ListPredictorLeaderboardService(
    private val reader: PredictorLeaderboardReader,
    private val accountSummaryProvider: AccountSummaryProvider,
    private val eventInternalClient: EventInternalClient,
) {
    fun execute(query: PredictorLeaderboardQuery): List<PredictorLeaderboardEntry> {
        require(query.limit in MIN_LIMIT..MAX_LIMIT) {
            "Predictor leaderboard limit must be between $MIN_LIMIT and $MAX_LIMIT"
        }
        require(query.minimumSample in MIN_SAMPLE..MAX_SAMPLE) {
            "Predictor minimum sample must be between $MIN_SAMPLE and $MAX_SAMPLE"
        }

        val wipe = query.wipeId?.let(eventInternalClient::wipe) ?: eventInternalClient.currentWipe()
        val effectiveMinimumSample =
            if (query.sort == PredictorLeaderboardSort.WIN_RATE) {
                query.minimumSample
            } else {
                MIN_SAMPLE
            }

        val candidates =
            reader.read(
                from = wipe.startedAt,
                to = wipe.endedAt,
                sort = query.sort,
                minimumSample = effectiveMinimumSample,
                limit = query.limit,
            )
        val summaries = accountSummaryProvider.getByIds(candidates.mapTo(linkedSetOf()) { it.accountId })

        return candidates.mapNotNull { candidate ->
            val summary = summaries[candidate.accountId] ?: return@mapNotNull null

            PredictorLeaderboardEntry(
                accountId = candidate.accountId,
                nickname = summary.nickname,
                bets = candidate.bets,
                wins = candidate.wins,
                winRate = calculateWinRate(candidate),
                profit = candidate.profit,
                turnover = candidate.turnover,
            )
        }
    }

    private fun calculateWinRate(candidate: PredictorLeaderboardCandidate): BigDecimal {
        if (candidate.bets == 0L) {
            return BigDecimal.ZERO
        }

        return candidate.wins
            .toBigDecimal()
            .divide(candidate.bets.toBigDecimal(), WIN_RATE_SCALE, RoundingMode.HALF_UP)
    }

    private companion object {
        const val MIN_LIMIT = 3
        const val MAX_LIMIT = 100
        const val MIN_SAMPLE = 1
        const val MAX_SAMPLE = 1_000
        const val WIN_RATE_SCALE = 4
    }
}
