package com.mnemos.event.adapter.out.persistence.booster

import com.mnemos.event.adapter.out.persistence.booster.query.BoosterEventViewProjection
import com.mnemos.event.domain.booster.model.DomainBoosterType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface BoosterEventJpaRepository : JpaRepository<BoosterEventEntity, UUID> {
    @Query(
        """
            SELECT 
                e.id,
                e.type,
                e.activatedBy,
                e.activatedAt
            FROM BoosterEventEntity e
            WHERE (:type IS NULL OR e.type = :type)
            AND (
                :activatedByName IS NULL
                OR e.activatedBy ILIKE CONCAT('%', :activatedByName, '%')
            )
            ORDER BY e.activatedAt DESC, e.id DESC
        """,
    )
    fun findViews(
        @Param("type") type: DomainBoosterType?,
        @Param("activatedByName") activatedByName: String?,
        pageable: Pageable,
    ): Page<BoosterEventViewProjection>
}
