package com.mnemos.wallet.adapter.inbound.http.leaderboard

import com.mnemos.wallet.application.leaderboard.coin.ListCoinLeaderboardService
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/wallet/leaderboards/coins")
class CoinLeaderboardController(
    private val service: ListCoinLeaderboardService,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "50")
        @Min(3)
        @Max(100)
        limit: Int,
    ): List<CoinLeaderboardResponse> =
        service
            .execute(limit)
            .mapIndexed { index, entry ->
                CoinLeaderboardResponse(
                    rank = index + 1,
                    accountId = entry.accountId.id,
                    nickname = entry.nickname,
                    amount = entry.amount,
                )
            }
}
