package com.mnemos.realtime.adapter.out.persistence.collector

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CollectorCredentialJpaRepository : JpaRepository<CollectorCredentialEntity, UUID>
