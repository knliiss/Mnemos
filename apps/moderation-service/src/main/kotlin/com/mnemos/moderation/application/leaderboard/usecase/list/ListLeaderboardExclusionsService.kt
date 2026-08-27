package com.mnemos.moderation.application.leaderboard.usecase.list

import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion
import com.mnemos.moderation.domain.leaderboard.repository.LeaderboardExclusionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListLeaderboardExclusionsService(
    private val repository: LeaderboardExclusionRepository,
) : ListLeaderboardExclusionsUseCase {
    override fun execute(): List<LeaderboardExclusion> = repository.findAll()
}
