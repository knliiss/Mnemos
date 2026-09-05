package com.mnemos.notification.application.notification.localization.service

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.notification.application.notification.localization.model.SaoLocalizationPack
import com.mnemos.notification.application.notification.localization.port.out.NotificationLanguageGateway
import com.mnemos.notification.application.notification.localization.port.out.SaoLocalizationGateway
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

class NotificationLocalizationServiceTest {
    @Test
    fun `resolves canonical item value using the users language`() {
        val userId = UserId.generate()
        val languageGateway = NotificationLanguageGateway { LocalizationLanguage.EN }
        val saoGateway =
            SaoLocalizationGateway { locale ->
                assertEquals("en_US", locale)

                SaoLocalizationPack(
                    locale = locale,
                    hash = "hash-en",
                    properties = mapOf("sao.item.l1_1.name" to "¨37efefElucidator"),
                )
            }
        val service =
            NotificationLocalizationService(
                languageGateway = languageGateway,
                saoLocalizationGateway = saoGateway,
                cacheTtl = Duration.ofMinutes(5),
            )

        val localization = service.resolve(userId)
        val itemName =
            localization.itemName(
                ItemDropNotificationPayload(
                    droppedFor = "Knalis",
                    itemKey = "sao.item.l1_1.name",
                    itemName = "Элюцидатор",
                    itemType = NotificationItemType.SWORD,
                    itemRarity = NotificationItemRarity.SECRET,
                ),
            )

        assertEquals(LocalizationLanguage.EN, localization.language)
        assertEquals("Elucidator", itemName)
    }

    @Test
    fun `caches user language and SAO localization pack`() {
        val userId = UserId.generate()
        val languageCalls = AtomicInteger()
        val packCalls = AtomicInteger()
        val languageGateway =
            NotificationLanguageGateway {
                languageCalls.incrementAndGet()
                LocalizationLanguage.UK
            }
        val saoGateway =
            SaoLocalizationGateway { locale ->
                packCalls.incrementAndGet()

                SaoLocalizationPack(
                    locale = locale,
                    hash = "hash-uk",
                    properties = emptyMap(),
                )
            }
        val service =
            NotificationLocalizationService(
                languageGateway = languageGateway,
                saoLocalizationGateway = saoGateway,
                cacheTtl = Duration.ofMinutes(5),
            )

        service.resolve(userId)
        service.resolve(userId)

        assertEquals(1, languageCalls.get())
        assertEquals(1, packCalls.get())
    }

    @Test
    fun `keeps last known SAO pack when refresh fails`() {
        val userId = UserId.generate()
        val packCalls = AtomicInteger()
        val languageGateway = NotificationLanguageGateway { LocalizationLanguage.RU }
        val saoGateway =
            SaoLocalizationGateway { locale ->
                if (packCalls.incrementAndGet() > 1) {
                    error("event-service unavailable")
                }

                SaoLocalizationPack(
                    locale = locale,
                    hash = "hash-ru",
                    properties = mapOf("sao.item.l1_1.name" to "¨37efefЭлюцидатор"),
                )
            }
        val service =
            NotificationLocalizationService(
                languageGateway = languageGateway,
                saoLocalizationGateway = saoGateway,
                cacheTtl = Duration.ZERO,
            )

        service.resolve(userId)
        val localization = service.resolve(userId)
        val itemName =
            localization.itemName(
                ItemDropNotificationPayload(
                    droppedFor = "Knalis",
                    itemKey = "sao.item.l1_1.name",
                    itemName = "Legacy",
                    itemType = NotificationItemType.SWORD,
                    itemRarity = NotificationItemRarity.SECRET,
                ),
            )

        assertEquals("Элюцидатор", itemName)
        assertEquals(2, packCalls.get())
    }
}
