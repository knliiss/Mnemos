package com.mnemos.event.adapter.out.persistence.raid

import com.mnemos.event.adapter.out.persistence.raid.query.RaidEventViewProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface RaidEventJpaRepository : JpaRepository<RaidEventEntity, UUID> {
    @Query(
        """
            SELECT r
            FROM RaidEventEntity r
            WHERE EXISTS (
                SELECT 1 
                FROM RaidEventEntity candidate
                JOIN candidate.locations location
                WHERE candidate.id = r.id
                    AND (:minLocation IS NULL OR location >= :minLocation)
                    AND (:maxLocation IS NULL OR location <= :maxLocation)
            )
            ORDER BY r.startedAt DESC, r.id DESC
        """,
        countQuery =
            """
            SELECT count(r.id)
            FROM RaidEventEntity r
            WHERE EXISTS (
                SELECT 1 
                FROM RaidEventEntity candidate
                JOIN candidate.locations location
                WHERE candidate.id = r.id
                    AND (:minLocation IS NULL OR location >= :minLocation)
                    AND (:maxLocation IS NULL OR location <= :maxLocation)
            )
            """,
    )
    fun findViews(
        @Param("minLocation") minLocation: Int?,
        @Param("maxLocation") maxLocation: Int?,
        pageable: Pageable,
    ): Page<RaidEventViewProjection>
}
