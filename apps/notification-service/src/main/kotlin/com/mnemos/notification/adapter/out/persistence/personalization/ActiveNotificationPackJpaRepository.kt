package com.mnemos.notification.adapter.out.persistence.personalization

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ActiveNotificationPackJpaRepository : JpaRepository<ActiveNotificationPackEntity, UUID>
