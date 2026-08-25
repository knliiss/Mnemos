package com.mnemos.event.adapter.inbound.http.leaderboard

import com.mnemos.event.adapter.inbound.http.leaderboard.DropLeaderboardApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.leaderboard.DropLeaderboardApiMapper.toResponse
import com.mnemos.event.application.leaderboard.drop.ListDropLeaderboardService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/events/leaderboards/drops")
class DropLeaderboardController(
    private val service: ListDropLeaderboardService,
) {
    @GetMapping
    fun list(
        @Valid
        @ModelAttribute
        request: DropLeaderboardRequest,
    ): List<DropLeaderboardResponse> =
        service
            .execute(request.toQuery())
            .mapIndexed { index, entry ->
                entry.toResponse(rank = index + 1)
            }
}
