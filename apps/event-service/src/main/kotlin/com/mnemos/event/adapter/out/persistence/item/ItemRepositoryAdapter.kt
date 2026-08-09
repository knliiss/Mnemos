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
import java.sql.Types
import java.util.UUID

@Repository
class ItemRepositoryAdapter(
    private val jdbcOperations: NamedParameterJdbcOperations,
) : ItemRepository {
    @Transactional
    override fun getOrCreate(
        itemName: String,
        type: DomainItemType,
        rarity: DomainItemRarity,
    ): Item {
        val parameters =
            MapSqlParameterSource()
                .addValue("id", ItemId.generate().id, Types.OTHER)
                .addValue("itemName", itemName, Types.VARCHAR)
                .addValue("type", type.name, Types.VARCHAR)
                .addValue("rarity", rarity.name, Types.VARCHAR)

        val entity =
            jdbcOperations.queryForObject(
                FIND_OR_CREATE_SQL,
                parameters,
            ) { rs, _ ->
                ItemEntity(
                    id = rs.getObject("id", UUID::class.java),
                    name = rs.getString("name"),
                    type = DomainItemType.valueOf(rs.getString("type")),
                    rarity = DomainItemRarity.valueOf(rs.getString("rarity")),
                )
            }

        return entity.toDomain()
    }

    private companion object {
        const val FIND_OR_CREATE_SQL = """
            INSERT INTO items AS existing (
                id,
                name,
                type,
                rarity
            ) VALUES (
                :id,
                :itemName,
                :type,
                :rarity
            )
            ON CONFLICT (LOWER(name), type)
            DO UPDATE SET
                name = existing.name
            RETURNING
                existing.id,
                existing.name,
                existing.type,
                existing.rarity
        """
    }
}
