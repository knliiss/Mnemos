package com.mnemos.bet.adapter.inbound.http.leaderboard

import com.mnemos.bet.application.leaderboard.predictor.ListPredictorLeaderboardService
import com.mnemos.bet.application.leaderboard.predictor.PredictorLeaderboardQuery
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/bets/leaderboards/predictors")
class PredictorLeaderboardController(
    private val service: ListPredictorLeaderboardService,
) {
    @GetMapping
    fun list(
        @Valid
        @ModelAttribute
        request: PredictorLeaderboardRequest,
    ): List<PredictorLeaderboardResponse> =
        service
            .execute(
                PredictorLeaderboardQuery(
                    wipeId = request.wipeId,
                    sort = request.sort,
                    minimumSample = request.minimumSample,
                    limit = request.limit,
                ),
            ).mapIndexed { index, entry ->
                PredictorLeaderboardResponse(
                    rank = index + 1,
                    accountId = entry.accountId.id,
                    nickname = entry.nickname,
                    bets = entry.bets,
                    wins = entry.wins,
                    winRate = entry.winRate,
                    profit = entry.profit,
                    turnover = entry.turnover,
                )
            }
}
