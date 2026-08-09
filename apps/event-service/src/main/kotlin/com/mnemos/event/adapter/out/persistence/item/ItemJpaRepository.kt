package com.mnemos.event.adapter.out.persistence.item

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ItemJpaRepository : JpaRepository<ItemEntity, UUID>
