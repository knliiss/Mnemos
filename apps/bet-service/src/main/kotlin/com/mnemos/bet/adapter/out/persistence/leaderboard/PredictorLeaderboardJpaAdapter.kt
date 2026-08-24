package com.mnemos.bet.adapter.out.persistence.leaderboard

import com.mnemos.bet.application.leaderboard.predictor.PredictorLeaderboardCandidate
import com.mnemos.bet.application.leaderboard.predictor.PredictorLeaderboardReader
import com.mnemos.bet.application.leaderboard.predictor.PredictorLeaderboardSort
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PredictorLeaderboardJpaAdapter(
    private val repository: PredictorLeaderboardJpaRepository,
) : PredictorLeaderboardReader {
    override fun read(
        from: Instant,
        to: Instant?,
        sort: PredictorLeaderboardSort,
        minimumSample: Int,
        limit: Int,
    ): List<PredictorLeaderboardCandidate> =
        repository
            .read(
                from = from,
                to = to,
                sort = sort.name,
                minimumSample = minimumSample,
                limit = limit,
            ).map { projection ->
                PredictorLeaderboardCandidate(
                    accountId = AccountId(projection.accountId),
                    bets = projection.bets,
                    wins = projection.wins,
                    profit = projection.profit,
                    turnover = projection.turnover,
                )
            }
}
