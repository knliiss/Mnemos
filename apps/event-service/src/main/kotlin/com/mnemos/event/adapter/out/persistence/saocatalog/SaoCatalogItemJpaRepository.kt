package com.mnemos.event.adapter.out.persistence.saocatalog

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SaoCatalogItemJpaRepository : JpaRepository<SaoCatalogItemEntity, String> {
    @Modifying
    @Query("UPDATE SaoCatalogItemEntity item SET item.active = false WHERE item.active = true")
    fun deactivateAll(): Int

    @Query(
        value = """
            SELECT item.*
            FROM sao_catalog_items item
            WHERE item.active = true
              AND (
                  CAST(:search AS text) IS NULL
                  OR position(CAST(:search AS text) in lower(item.default_name)) > 0
                  OR position(CAST(:search AS text) in lower(item.game_key)) > 0
              )
              AND (CAST(:type AS text) IS NULL OR item.item_type = CAST(:type AS text))
              AND (CAST(:rarity AS text) IS NULL OR item.rarity = CAST(:rarity AS text))
              AND (
                  CAST(:locationId AS integer) IS NULL
                  OR EXISTS (
                      SELECT 1
                      FROM sao_catalog_item_locations item_location
                      WHERE item_location.game_key = item.game_key
                        AND item_location.location_id = CAST(:locationId AS integer)
                  )
              )
            ORDER BY lower(item.default_name), item.game_key
        """,
        countQuery = """
            SELECT count(*)
            FROM sao_catalog_items item
            WHERE item.active = true
              AND (
                  CAST(:search AS text) IS NULL
                  OR position(CAST(:search AS text) in lower(item.default_name)) > 0
                  OR position(CAST(:search AS text) in lower(item.game_key)) > 0
              )
              AND (CAST(:type AS text) IS NULL OR item.item_type = CAST(:type AS text))
              AND (CAST(:rarity AS text) IS NULL OR item.rarity = CAST(:rarity AS text))
              AND (
                  CAST(:locationId AS integer) IS NULL
                  OR EXISTS (
                      SELECT 1
                      FROM sao_catalog_item_locations item_location
                      WHERE item_location.game_key = item.game_key
                        AND item_location.location_id = CAST(:locationId AS integer)
                  )
              )
        """,
        nativeQuery = true,
    )
    fun search(
        @Param("search") search: String?,
        @Param("type") type: String?,
        @Param("rarity") rarity: String?,
        @Param("locationId") locationId: Int?,
        pageable: Pageable,
    ): Page<SaoCatalogItemEntity>
}
