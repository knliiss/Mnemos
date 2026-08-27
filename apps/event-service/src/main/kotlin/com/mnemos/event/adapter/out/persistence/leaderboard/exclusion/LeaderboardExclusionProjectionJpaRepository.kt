package com.mnemos.event.adapter.out.persistence.leaderboard.exclusion

import org.springframework.data.jpa.repository.JpaRepository

interface LeaderboardExclusionProjectionJpaRepository :
    JpaRepository<LeaderboardExclusionProjectionEntity, LeaderboardExclusionProjectionId>
