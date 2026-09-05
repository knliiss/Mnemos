package com.mnemos.event.adapter.out.persistence.item

import com.mnemos.event.adapter.out.persistence.item.ItemPersistenceMapper.toDomain
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.item.model.Item
import com.mnemos.event.domain.item.model.ItemId
import com.mnemos.event.domain.item.repository.ItemRepository
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.ResultSet
import java.sql.Types
import java.util.UUID

@Repository
class ItemRepositoryAdapter(
    private val jdbcOperations: NamedParameterJdbcOperations,
) : ItemRepository {
    @Transactional
    override fun getOrCreate(
        itemKey: String?,
        itemName: String,
        type: DomainItemType,
        rarity: DomainItemRarity,
    ): Item {
        val parameters =
            MapSqlParameterSource()
                .addValue("id", ItemId.generate().id, Types.OTHER)
                .addValue("itemKey", itemKey, Types.VARCHAR)
                .addValue("itemName", itemName, Types.VARCHAR)
                .addValue("type", type.name, Types.VARCHAR)
                .addValue("rarity", rarity.name, Types.VARCHAR)

        val sql =
            if (itemKey == null) {
                FIND_OR_CREATE_LEGACY_SQL
            } else {
                FIND_OR_CREATE_CANONICAL_SQL
            }

        val entity =
            requireNotNull(
                jdbcOperations.queryForObject(
                    sql,
                    parameters,
                    ::mapEntity,
                ),
            )

        return entity.toDomain()
    }

    private fun mapEntity(
        resultSet: ResultSet,
        rowNumber: Int,
    ): ItemEntity =
        ItemEntity(
            id = resultSet.getObject("id", UUID::class.java),
            gameKey = resultSet.getString("game_key"),
            name = resultSet.getString("name"),
            type = DomainItemType.valueOf(resultSet.getString("type")),
            rarity = DomainItemRarity.valueOf(resultSet.getString("rarity")),
        )

    private companion object {
        const val FIND_OR_CREATE_CANONICAL_SQL = """
            WITH candidate AS (
                SELECT id
                FROM (
                    SELECT
                        id,
                        0 AS priority
                    FROM items
                    WHERE game_key = :itemKey

                    UNION ALL

                    SELECT
                        id,
                        1 AS priority
                    FROM items
                    WHERE game_key IS NULL
                      AND LOWER(name) = LOWER(:itemName)
                      AND type = :type
                ) candidates
                ORDER BY priority
                LIMIT 1
            ),
            updated AS (
                UPDATE items
                SET game_key = :itemKey
                WHERE id = (SELECT id FROM candidate)
                  AND (game_key IS NULL OR game_key = :itemKey)
                RETURNING
                    id,
                    game_key,
                    name,
                    type,
                    rarity
            ),
            inserted AS (
                INSERT INTO items (
                    id,
                    game_key,
                    name,
                    type,
                    rarity
                )
                SELECT
                    :id,
                    :itemKey,
                    :itemName,
                    :type,
                    :rarity
                WHERE NOT EXISTS (SELECT 1 FROM updated)
                ON CONFLICT (game_key) WHERE game_key IS NOT NULL
                DO UPDATE SET
                    game_key = EXCLUDED.game_key
                RETURNING
                    id,
                    game_key,
                    name,
                    type,
                    rarity
            )
            SELECT * FROM updated
            UNION ALL
            SELECT * FROM inserted
            LIMIT 1
        """

        const val FIND_OR_CREATE_LEGACY_SQL = """
            WITH candidate AS (
                SELECT
                    id,
                    game_key,
                    name,
                    type,
                    rarity
                FROM items
                WHERE LOWER(name) = LOWER(:itemName)
                  AND type = :type
                ORDER BY
                    CASE WHEN game_key IS NULL THEN 1 ELSE 0 END,
                    id
                LIMIT 1
            ),
            inserted AS (
                INSERT INTO items AS existing (
                    id,
                    game_key,
                    name,
                    type,
                    rarity
                )
                SELECT
                    :id,
                    NULL,
                    :itemName,
                    :type,
                    :rarity
                WHERE NOT EXISTS (SELECT 1 FROM candidate)
                ON CONFLICT (LOWER(name), type) WHERE game_key IS NULL
                DO UPDATE SET
                    name = existing.name
                RETURNING
                    existing.id,
                    existing.game_key,
                    existing.name,
                    existing.type,
                    existing.rarity
            )
            SELECT * FROM candidate
            UNION ALL
            SELECT * FROM inserted
            LIMIT 1
        """
    }
}
