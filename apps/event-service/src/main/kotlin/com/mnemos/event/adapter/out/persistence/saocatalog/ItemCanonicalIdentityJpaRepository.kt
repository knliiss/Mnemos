package com.mnemos.event.adapter.out.persistence.saocatalog

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ItemCanonicalIdentityJpaRepository : JpaRepository<ItemCanonicalIdentityEntity, UUID>
