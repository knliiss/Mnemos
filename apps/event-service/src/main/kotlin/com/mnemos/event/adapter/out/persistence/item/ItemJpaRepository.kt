package com.mnemos.event.adapter.out.persistence.item

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ItemJpaRepository : JpaRepository<ItemEntity, UUID> {
    @Query(
        value = """
            SELECT item.*
            FROM items item
            WHERE
                position(lower(:search) in lower(item.name)) > 0
                OR lower(item.name) % lower(:search)
            ORDER BY
                similarity(lower(item.name), lower(:search)) DESC,
                lower(item.name) ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun searchCatalog(
        @Param("search") search: String,
        @Param("limit") limit: Int,
    ): List<ItemEntity>
}
