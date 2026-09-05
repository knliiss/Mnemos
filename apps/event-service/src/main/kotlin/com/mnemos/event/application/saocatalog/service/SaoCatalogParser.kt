package com.mnemos.event.application.saocatalog.service

import com.mnemos.event.application.localization.model.SaoLocalizationPack
import com.mnemos.event.application.saocatalog.model.SaoCatalogItem
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType
import com.mnemos.event.domain.item.model.DomainItemType
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SaoCatalogParser {
    fun parse(
        packs: Collection<SaoLocalizationPack>,
        now: Instant,
    ): ParsedSaoCatalog {
        if (packs.isEmpty()) {
            return ParsedSaoCatalog(emptyList(), emptyMap(), emptySet())
        }

        val packsByLocale = packs.associateBy(SaoLocalizationPack::locale)
        val preferredPack = packsByLocale[PREFERRED_LOCALE] ?: packs.minBy(SaoLocalizationPack::locale)
        val itemKeys =
            packs
                .asSequence()
                .flatMap { pack -> pack.properties.keys.asSequence() }
                .filter(::isItemNameKey)
                .toSortedSet()
        val locationIds = findKnownLocationIds(packs)
        val unresolvedRarityKeys = mutableSetOf<String>()

        val items =
            itemKeys.mapNotNull { gameKey ->
                val source = selectSourceValue(gameKey, preferredPack, packs) ?: return@mapNotNull null
                val type = resolveType(gameKey)
                val rarity = resolveRarity(source.value)

                if (rarity == null) {
                    unresolvedRarityKeys += gameKey
                }

                SaoCatalogItem(
                    gameKey = gameKey,
                    defaultName = stripStylePrefix(source.value).trim(),
                    type = type,
                    rarity = rarity,
                    locationIds = resolveLocations(gameKey, type, locationIds),
                    sourceHash = source.pack.hash,
                    updatedAt = now,
                )
            }

        val aliases = buildUnambiguousAliases(items, packs)

        return ParsedSaoCatalog(
            items = items,
            aliases = aliases,
            unresolvedRarityKeys = unresolvedRarityKeys,
        )
    }

    private fun selectSourceValue(
        gameKey: String,
        preferredPack: SaoLocalizationPack,
        packs: Collection<SaoLocalizationPack>,
    ): LocalizedValue? {
        preferredPack.properties[gameKey]?.let { value ->
            return LocalizedValue(preferredPack, value)
        }

        return packs
            .asSequence()
            .sortedBy(SaoLocalizationPack::locale)
            .mapNotNull { pack ->
                pack.properties[gameKey]?.let { value -> LocalizedValue(pack, value) }
            }.firstOrNull()
    }

    private fun buildUnambiguousAliases(
        items: Collection<SaoCatalogItem>,
        packs: Collection<SaoLocalizationPack>,
    ): Map<LegacyItemAlias, String> {
        val itemByKey = items.associateBy(SaoCatalogItem::gameKey)
        val candidates = mutableMapOf<LegacyItemAlias, MutableSet<String>>()

        for (pack in packs) {
            for ((gameKey, localizedName) in pack.properties) {
                if (!isItemNameKey(gameKey)) {
                    continue
                }

                val item = itemByKey[gameKey] ?: continue
                val domainType = item.type.toDomainType() ?: continue
                val alias =
                    LegacyItemAlias(
                        normalizedName = normalizeName(localizedName),
                        type = domainType,
                    )

                candidates.getOrPut(alias, ::mutableSetOf).add(gameKey)
            }
        }

        return candidates
            .filterValues { keys -> keys.size == 1 }
            .mapValues { (_, keys) -> keys.single() }
    }

    private fun findKnownLocationIds(packs: Collection<SaoLocalizationPack>): Set<Int> =
        packs
            .asSequence()
            .flatMap { pack -> pack.properties.keys.asSequence() }
            .mapNotNull { key ->
                LOCATION_NAME_KEY
                    .matchEntire(key)
                    ?.groupValues
                    ?.get(1)
                    ?.toIntOrNull()
            }.toSet()

    private fun resolveLocations(
        gameKey: String,
        type: SaoCatalogItemType,
        knownLocations: Set<Int>,
    ): Set<Int> {
        val itemId = gameKey.removePrefix(ITEM_PREFIX).removeSuffix(NAME_SUFFIX)
        val candidate =
            when (type) {
                SaoCatalogItemType.WEAPON ->
                    WEAPON_LOCATION
                        .matchEntire(itemId)
                        ?.groupValues
                        ?.get(1)
                        ?.toIntOrNull()

                SaoCatalogItemType.PET ->
                    PET_LOCATION
                        .matchEntire(itemId)
                        ?.groupValues
                        ?.get(1)
                        ?.toIntOrNull()

                else -> null
            }

        return candidate
            ?.takeIf(knownLocations::contains)
            ?.let { locationId -> setOf(locationId) }
            .orEmpty()
    }

    private fun resolveType(gameKey: String): SaoCatalogItemType {
        val itemId = gameKey.removePrefix(ITEM_PREFIX).removeSuffix(NAME_SUFFIX)

        return when {
            WEAPON_ID.matches(itemId) -> SaoCatalogItemType.WEAPON
            itemId.startsWith("pet") -> SaoCatalogItemType.PET
            itemId.startsWith("aura") -> SaoCatalogItemType.AURA
            itemId.startsWith("book") -> SaoCatalogItemType.BOOK
            JEWELRY_ID.matches(itemId) -> SaoCatalogItemType.JEWELRY
            else -> SaoCatalogItemType.OTHER
        }
    }

    private fun resolveRarity(value: String): SaoCatalogItemRarity? {
        val style =
            STYLE_PREFIX
                .find(value)
                ?.groupValues
                ?.get(1)
                ?.lowercase()
                ?: return null

        return RARITY_BY_STYLE[style]
    }

    private fun SaoCatalogItemType.toDomainType(): DomainItemType? =
        when (this) {
            SaoCatalogItemType.WEAPON -> DomainItemType.SWORD
            SaoCatalogItemType.PET -> DomainItemType.PET
            SaoCatalogItemType.AURA -> DomainItemType.AURA
            SaoCatalogItemType.BOOK -> DomainItemType.BOOK
            SaoCatalogItemType.JEWELRY -> DomainItemType.JEWELRY
            SaoCatalogItemType.OTHER -> null
        }

    companion object {
        fun normalizeName(value: String): String =
            stripStylePrefix(value)
                .trim()
                .replace(WHITESPACE, " ")
                .lowercase()

        fun stripStylePrefix(value: String): String = value.replaceFirst(STYLE_PREFIX, "")

        private const val PREFERRED_LOCALE = "ru_RU"
        private const val ITEM_PREFIX = "sao.item."
        private const val NAME_SUFFIX = ".name"

        private val WHITESPACE = Regex("\\s+")
        private val STYLE_PREFIX = Regex("^¨([0-9a-fA-F]{6})")
        private val WEAPON_ID = Regex("^l\\d+(?:_|$).*")
        private val JEWELRY_ID = Regex("^d\\d+(?:_|$).*")
        private val WEAPON_LOCATION = Regex("^l(\\d+)(?:_|$).*")
        private val PET_LOCATION = Regex("^pet_l(\\d+)(?:_|$).*")
        private val LOCATION_NAME_KEY = Regex("^sao\\.location\\.(-?\\d+)\\.name$")

        private val RARITY_BY_STYLE =
            mapOf(
                "707070" to SaoCatalogItemRarity.COMMON,
                "2a66bd" to SaoCatalogItemRarity.RARE,
                "8d73c5" to SaoCatalogItemRarity.EPIC,
                "efac00" to SaoCatalogItemRarity.LEGENDARY,
                "a91925" to SaoCatalogItemRarity.MYTHICAL,
                "37efef" to SaoCatalogItemRarity.SECRET,
                "22ae49" to SaoCatalogItemRarity.OTHER,
            )

        private fun isItemNameKey(key: String): Boolean = key.startsWith(ITEM_PREFIX) && key.endsWith(NAME_SUFFIX)
    }

    private data class LocalizedValue(
        val pack: SaoLocalizationPack,
        val value: String,
    )
}

data class ParsedSaoCatalog(
    val items: List<SaoCatalogItem>,
    val aliases: Map<LegacyItemAlias, String>,
    val unresolvedRarityKeys: Set<String>,
)

data class LegacyItemAlias(
    val normalizedName: String,
    val type: DomainItemType,
)
