package com.mnemos.event.adapter.inbound.http.leaderboard

import com.mnemos.event.adapter.inbound.http.leaderboard.BoosterLeaderboardApiMapper.toResponse
import com.mnemos.event.application.leaderboard.booster.ListBoosterLeaderboardService
import com.mnemos.event.domain.wipe.model.WipeId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/events/leaderboards/boosters")
class BoosterLeaderboardController(
    private val service: ListBoosterLeaderboardService,
) {
    @GetMapping
    fun list(
        @Valid
        @ModelAttribute
        request: BoosterLeaderboardRequest,
    ): List<BoosterLeaderboardResponse> =
        service
            .execute(
                wipeId = request.wipeId?.let(::WipeId),
                limit = request.limit,
            ).mapIndexed { index, entry ->
                entry.toResponse(rank = index + 1)
            }
}
