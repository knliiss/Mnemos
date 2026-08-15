package com.mnemos.social.adapter.out.persistence.spotlight

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationPackSelectionJpaRepository : JpaRepository<NotificationPackSelectionEntity, UUID>
