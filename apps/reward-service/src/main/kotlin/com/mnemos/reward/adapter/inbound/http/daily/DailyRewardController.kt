package com.mnemos.reward.adapter.inbound.http.daily

import com.mnemos.reward.adapter.inbound.http.daily.DailyRewardApiMapper.toResponse
import com.mnemos.reward.application.daily.usecase.claim.ClaimDailyRewardService
import com.mnemos.reward.application.daily.usecase.get.GetDailyRewardStateService
import com.mnemos.reward.application.daily.usecase.restore.RestoreDailyStreakService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/rewards/daily")
class DailyRewardController(
    private val getStateService: GetDailyRewardStateService,
    private val claimService: ClaimDailyRewardService,
    private val restoreService: RestoreDailyStreakService,
) {
    @GetMapping
    fun getState(): DailyRewardStateResponse = getStateService.get().toResponse()

    @PostMapping("/claim")
    fun claim(): ClaimDailyRewardResponse = claimService.claim().toResponse()

    @PostMapping("/restore")
    fun restore(): RestoreDailyStreakResponse =
        RestoreDailyStreakResponse(
            currentStreak = restoreService.restore(),
        )
}
