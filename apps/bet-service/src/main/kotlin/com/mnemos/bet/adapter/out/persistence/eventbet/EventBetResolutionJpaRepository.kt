package com.mnemos.bet.adapter.out.persistence.eventbet

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EventBetResolutionJpaRepository : JpaRepository<EventBetResolutionEntity, UUID>
