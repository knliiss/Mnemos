package com.mnemos.event.application.saocatalog.service

import com.mnemos.event.application.localization.port.out.SaoLocalizationPackRepository
import com.mnemos.event.application.saocatalog.port.out.LegacyItemIdentityRepository
import com.mnemos.event.application.saocatalog.port.out.SaoCatalogItemRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class SaoCatalogSynchronizationService(
    private val localizationRepository: SaoLocalizationPackRepository,
    private val catalogRepository: SaoCatalogItemRepository,
    private val legacyItemIdentityRepository: LegacyItemIdentityRepository,
    private val parser: SaoCatalogParser,
    private val clock: Clock,
) {
    fun synchronize(): SaoCatalogSynchronizationResult {
        val packs = localizationRepository.findAll()

        if (packs.isEmpty()) {
            return SaoCatalogSynchronizationResult.EMPTY
        }

        val parsed = parser.parse(packs, Instant.now(clock))

        if (parsed.items.isEmpty()) {
            logger.warn("SAO localization packs do not contain canonical item names")
            return SaoCatalogSynchronizationResult.EMPTY
        }

        catalogRepository.replaceAll(parsed.items)

        var resolvedHistoricalItems = 0
        var unresolvedHistoricalItems = 0

        for (item in legacyItemIdentityRepository.findAll()) {
            val gameKey =
                item.gameKey?.takeIf { key -> parsed.items.any { catalogItem -> catalogItem.gameKey == key } }
                    ?: parsed.aliases[
                        LegacyItemAlias(
                            normalizedName = SaoCatalogParser.normalizeName(item.name),
                            type = item.type,
                        ),
                    ]

            if (gameKey == null) {
                unresolvedHistoricalItems += 1
                continue
            }

            legacyItemIdentityRepository.saveCanonicalIdentity(
                itemId = item.itemId,
                gameKey = gameKey,
            )
            resolvedHistoricalItems += 1
        }

        if (parsed.unresolvedRarityKeys.isNotEmpty()) {
            logger.warn(
                "SAO catalog contains {} item(s) with unresolved rarity; no rarity was guessed: {}",
                parsed.unresolvedRarityKeys.size,
                parsed.unresolvedRarityKeys.sorted().joinToString(", "),
            )
        }

        if (unresolvedHistoricalItems > 0) {
            logger.warn(
                "Could not map {} historical item row(s) to a unique canonical SAO game key",
                unresolvedHistoricalItems,
            )
        }

        val result =
            SaoCatalogSynchronizationResult(
                catalogItems = parsed.items.size,
                unresolvedRarityItems = parsed.unresolvedRarityKeys.size,
                resolvedHistoricalItems = resolvedHistoricalItems,
                unresolvedHistoricalItems = unresolvedHistoricalItems,
            )

        logger.info(
            "Synchronized SAO catalog: items={}, unresolvedRarity={}, historicalResolved={}, historicalUnresolved={}",
            result.catalogItems,
            result.unresolvedRarityItems,
            result.resolvedHistoricalItems,
            result.unresolvedHistoricalItems,
        )

        return result
    }

    private companion object {
        val logger = LoggerFactory.getLogger(SaoCatalogSynchronizationService::class.java)
    }
}

data class SaoCatalogSynchronizationResult(
    val catalogItems: Int,
    val unresolvedRarityItems: Int,
    val resolvedHistoricalItems: Int,
    val unresolvedHistoricalItems: Int,
) {
    companion object {
        val EMPTY =
            SaoCatalogSynchronizationResult(
                catalogItems = 0,
                unresolvedRarityItems = 0,
                resolvedHistoricalItems = 0,
                unresolvedHistoricalItems = 0,
            )
    }
}
