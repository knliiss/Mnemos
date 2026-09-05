package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalEventFeedSort
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import com.mnemos.telegram.bot.adapter.out.event.EventHistoryMapper.toModel
import com.mnemos.telegram.bot.application.events.model.EventHistoryBoosterType
import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryGlobalType
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItemType
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.model.EventHistorySort
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.settings.TelegramSettingsLanguageResolver
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class EventHistoryGatewayAdapter(
    private val client: EventServiceClient,
    private val languageResolver: TelegramSettingsLanguageResolver,
) : EventHistoryGateway {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val localizationCache = ConcurrentHashMap<String, CachedLocalization>()

    override fun getPage(query: EventHistoryQuery): EventHistoryResult {
        val result =
            try {
                val response =
                    client.listEvents(
                        after = query.after,
                        type = query.filter.toInternalType(),
                        search = query.search,
                        itemRarity = query.itemRarity?.toContract(),
                        itemType = query.itemType?.toContract(),
                        boosterType = query.boosterType?.toContract(),
                        globalType = query.globalType?.toContract(),
                        raidLocation = query.raidLocation,
                        wipeId = query.wipeId,
                        sort = query.sort.toInternalSort(),
                        limit = PAGE_SIZE,
                    )
                val locale = query.telegramId?.let(languageResolver::resolve)?.let(::saoLocale) ?: DEFAULT_LOCALE
                val localization = loadLocalization(locale)

                EventHistoryResult.Available(response.toModel(localization))
            } catch (exception: RuntimeException) {
                logger.warn("Could not load event history from event-service", exception)
                EventHistoryResult.Unavailable
            }

        return result
    }

    private fun loadLocalization(locale: String): Map<String, String> {
        val now = Instant.now()
        val cached = localizationCache[locale]

        if (cached != null && cached.fetchedAt.plus(LOCALIZATION_CACHE_TTL).isAfter(now)) {
            return cached.properties
        }

        return try {
            client.getSaoLocalization(locale).properties.also { properties ->
                localizationCache[locale] = CachedLocalization(properties = properties, fetchedAt = now)
            }
        } catch (exception: RuntimeException) {
            logger.warn("Could not load SAO localization for event history: locale={}", locale, exception)
            cached?.properties.orEmpty()
        }
    }

    private fun EventHistoryFilter.toInternalType(): InternalEventFeedType? =
        when (this) {
            EventHistoryFilter.ALL -> null
            EventHistoryFilter.ITEM_DROP -> InternalEventFeedType.ITEM_DROP
            EventHistoryFilter.BOOSTER -> InternalEventFeedType.BOOSTER
            EventHistoryFilter.GLOBAL -> InternalEventFeedType.GLOBAL
            EventHistoryFilter.RAID -> InternalEventFeedType.RAID
        }

    private fun EventHistorySort.toInternalSort(): InternalEventFeedSort =
        when (this) {
            EventHistorySort.NEWEST -> InternalEventFeedSort.NEWEST
            EventHistorySort.OLDEST -> InternalEventFeedSort.OLDEST
        }

    private fun EventHistoryItemRarity.toContract(): ContractItemRarity = ContractItemRarity.valueOf(name)

    private fun EventHistoryItemType.toContract(): ContractItemType = ContractItemType.valueOf(name)

    private fun EventHistoryBoosterType.toContract(): ContractBoosterType = ContractBoosterType.valueOf(name)

    private fun EventHistoryGlobalType.toContract(): ContractGlobalEventType = ContractGlobalEventType.valueOf(name)

    private fun saoLocale(language: TelegramSettingsLanguage): String =
        when (language) {
            TelegramSettingsLanguage.EN -> "en_US"
            TelegramSettingsLanguage.RU -> "ru_RU"
            TelegramSettingsLanguage.UK -> "uk_UA"
        }

    private data class CachedLocalization(
        val properties: Map<String, String>,
        val fetchedAt: Instant,
    )

    private companion object {
        const val PAGE_SIZE = 8
        const val DEFAULT_LOCALE = "ru_RU"
        val LOCALIZATION_CACHE_TTL: Duration = Duration.ofMinutes(5)
    }
}
