package com.mnemos.moderation.adapter.inbound.http.admin.leaderboard

import com.mnemos.moderation.adapter.inbound.http.admin.leaderboard.LeaderboardExclusionApiMapper.toCommand
import com.mnemos.moderation.adapter.inbound.http.admin.leaderboard.LeaderboardExclusionApiMapper.toResponse
import com.mnemos.moderation.application.leaderboard.usecase.create.CreateLeaderboardExclusionUseCase
import com.mnemos.moderation.application.leaderboard.usecase.delete.DeleteLeaderboardExclusionUseCase
import com.mnemos.moderation.application.leaderboard.usecase.list.ListLeaderboardExclusionsUseCase
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusionId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/moderation/leaderboard-exclusions")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
class AdminLeaderboardExclusionController(
    private val createUseCase: CreateLeaderboardExclusionUseCase,
    private val deleteUseCase: DeleteLeaderboardExclusionUseCase,
    private val listUseCase: ListLeaderboardExclusionsUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @Valid
        @RequestBody
        request: CreateLeaderboardExclusionRequest,
    ): LeaderboardExclusionResponse = createUseCase.execute(request.toCommand()).toResponse()

    @GetMapping
    fun list(): List<LeaderboardExclusionResponse> = listUseCase.execute().map { exclusion -> exclusion.toResponse() }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
    ) {
        deleteUseCase.execute(LeaderboardExclusionId(id))
    }
}
