package com.mnemos.playerdirectory.adapter.out.persistence.accountlink

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PlayerAccountLinkJpaRepository : JpaRepository<PlayerAccountLinkEntity, UUID>
