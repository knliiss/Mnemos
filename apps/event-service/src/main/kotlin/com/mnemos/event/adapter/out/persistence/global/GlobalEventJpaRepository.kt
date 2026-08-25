package com.mnemos.event.adapter.out.persistence.global

import com.mnemos.event.adapter.out.persistence.global.query.GlobalEventViewProjection
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface GlobalEventJpaRepository : JpaRepository<GlobalEventEntity, UUID> {
    @Query(
        """
        SELECT
            e.id AS id,
            e.type AS type,
            e.startedAt AS startedAt
        FROM GlobalEventEntity e
        ORDER BY e.startedAt DESC, e.id DESC
        """,
    )
    fun findViews(pageable: Pageable): Page<GlobalEventViewProjection>

    @Query(
        """
            SELECT
                e.id AS id,
                e.type AS type,
                e.startedAt AS startedAt
            FROM GlobalEventEntity e
            WHERE e.type IN :types
            ORDER BY e.startedAt DESC, e.id DESC
        """,
    )
    fun findViewsByTypes(
        @Param("types") types: Collection<DomainGlobalEventType>,
        pageable: Pageable,
    ): Page<GlobalEventViewProjection>

    @Query(
        """
            SELECT
                e.id AS id,
                e.type AS type,
                e.startedAt AS startedAt
            FROM GlobalEventEntity e
            WHERE e.startedAt > :startedAfter
            ORDER BY e.startedAt DESC, e.id DESC
        """,
    )
    fun findViewsStartedAfter(
        @Param("startedAfter") startedAfter: Instant,
    ): List<GlobalEventViewProjection>
}
