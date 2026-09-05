package com.mnemos.notification.adapter.out.event

import com.mnemos.notification.application.notification.localization.model.SaoLocalizationPack
import com.mnemos.notification.application.notification.localization.port.out.SaoLocalizationGateway
import feign.FeignException
import org.springframework.stereotype.Component

@Component
class EventSaoLocalizationGatewayAdapter(
    private val client: NotificationEventServiceClient,
) : SaoLocalizationGateway {
    override fun findPack(locale: String): SaoLocalizationPack? =
        try {
            val response = client.getSaoLocalization(locale)

            SaoLocalizationPack(
                locale = response.locale,
                hash = response.hash,
                properties = response.properties,
            )
        } catch (_: FeignException.NotFound) {
            null
        }
}
