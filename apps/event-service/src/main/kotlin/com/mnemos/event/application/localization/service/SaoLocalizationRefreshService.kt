package com.mnemos.event.application.localization.service

import com.mnemos.event.application.localization.model.SaoLocalizationPack
import com.mnemos.event.application.localization.port.out.SaoLocalizationPackRepository
import com.mnemos.event.application.localization.port.out.SaoLocalizationSource
import com.mnemos.event.application.saocatalog.service.SaoCatalogSynchronizationService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant

@Service
class SaoLocalizationRefreshService(
    private val source: SaoLocalizationSource,
    private val repository: SaoLocalizationPackRepository,
    private val catalogSynchronizationService: SaoCatalogSynchronizationService,
    private val clock: Clock,
) {
    @EventListener(ApplicationReadyEvent::class)
    fun refreshAfterStartup() {
        refresh()
        catalogSynchronizationService.synchronize()
    }

    @Scheduled(
        initialDelayString = "\${mnemos.event.sao-localization.refresh-interval-ms:300000}",
        fixedDelayString = "\${mnemos.event.sao-localization.refresh-interval-ms:300000}",
    )
    fun refreshScheduled() {
        if (refresh()) {
            catalogSynchronizationService.synchronize()
        }
    }

    fun refresh(): Boolean {
        val descriptors =
            try {
                source.listPacks()
            } catch (exception: Exception) {
                logger.warn("Failed to refresh SAO localization manifest; keeping persisted packs", exception)
                return false
            }

        if (descriptors.isEmpty()) {
            logger.warn("Cristalix localization manifest does not contain SAO packs")
            return false
        }

        val currentHashes = repository.findAll().associate { pack -> pack.locale to pack.hash }
        var updatedCount = 0

        for (descriptor in descriptors) {
            if (currentHashes[descriptor.locale] == descriptor.hash) {
                continue
            }

            try {
                val properties = source.downloadProperties(descriptor)

                repository.save(
                    SaoLocalizationPack(
                        locale = descriptor.locale,
                        hash = descriptor.hash,
                        properties = properties,
                        updatedAt = Instant.now(clock),
                    ),
                )
                updatedCount += 1
            } catch (exception: Exception) {
                logger.warn(
                    "Failed to refresh SAO localization pack {}; keeping last-known-good value",
                    descriptor.locale,
                    exception,
                )
            }
        }

        if (updatedCount > 0) {
            logger.info("Updated {} SAO localization pack(s)", updatedCount)
        }

        return updatedCount > 0
    }

    private companion object {
        val logger = LoggerFactory.getLogger(SaoLocalizationRefreshService::class.java)
    }
}
