package com.mnemos.notification.application.notification.localization.service

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.notification.application.notification.localization.model.NotificationLocalizationContext
import com.mnemos.notification.application.notification.localization.model.SaoLocalizationPack
import com.mnemos.notification.application.notification.localization.port.out.NotificationLanguageGateway
import com.mnemos.notification.application.notification.localization.port.out.SaoLocalizationGateway
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class NotificationLocalizationService(
    private val languageGateway: NotificationLanguageGateway,
    private val saoLocalizationGateway: SaoLocalizationGateway,
    @Value("\${mnemos.notification.localization.cache-ttl:5m}")
    private val cacheTtl: Duration,
) : NotificationLocalizationResolver {
    private val languageCache = ConcurrentHashMap<UUID, CachedValue<LocalizationLanguage>>()
    private val packCache = ConcurrentHashMap<String, CachedValue<SaoLocalizationPack>>()

    override fun resolve(userId: UserId): NotificationLocalizationContext {
        val language = resolveLanguage(userId)
        val pack = resolvePack(saoLocale(language))

        return NotificationLocalizationContext(
            language = language,
            saoProperties = pack?.properties.orEmpty(),
        )
    }

    private fun resolveLanguage(userId: UserId): LocalizationLanguage {
        val now = Instant.now()
        val cached = languageCache[userId.id]

        if (cached != null && cached.isFresh(now, cacheTtl)) {
            return cached.value
        }

        return synchronized(languageCache) {
            val latest = languageCache[userId.id]

            if (latest != null && latest.isFresh(now, cacheTtl)) {
                return@synchronized latest.value
            }

            try {
                val language = languageGateway.findLanguage(userId) ?: DEFAULT_LANGUAGE

                languageCache[userId.id] = CachedValue(language, now)
                language
            } catch (exception: RuntimeException) {
                logger.warn(
                    "Failed to resolve notification language, using cached/default value: userId={}",
                    userId.id,
                    exception,
                )

                latest?.value ?: DEFAULT_LANGUAGE
            }
        }
    }

    private fun resolvePack(locale: String): SaoLocalizationPack? {
        val now = Instant.now()
        val cached = packCache[locale]

        if (cached != null && cached.isFresh(now, cacheTtl)) {
            return cached.value
        }

        return synchronized(packCache) {
            val latest = packCache[locale]

            if (latest != null && latest.isFresh(now, cacheTtl)) {
                return@synchronized latest.value
            }

            try {
                val pack = saoLocalizationGateway.findPack(locale)

                if (pack != null) {
                    packCache[locale] = CachedValue(pack, now)
                }

                pack ?: latest?.value
            } catch (exception: RuntimeException) {
                logger.warn(
                    "Failed to refresh SAO localization, using cached values: locale={}",
                    locale,
                    exception,
                )

                latest?.value
            }
        }
    }

    private fun saoLocale(language: LocalizationLanguage): String =
        when (language) {
            LocalizationLanguage.EN -> "en_US"
            LocalizationLanguage.RU -> "ru_RU"
            LocalizationLanguage.UK -> "uk_UA"
        }

    private data class CachedValue<T>(
        val value: T,
        val fetchedAt: Instant,
    ) {
        fun isFresh(
            now: Instant,
            ttl: Duration,
        ): Boolean = fetchedAt.plus(ttl).isAfter(now)
    }

    private companion object {
        val DEFAULT_LANGUAGE = LocalizationLanguage.RU
        val logger = LoggerFactory.getLogger(NotificationLocalizationService::class.java)
    }
}
