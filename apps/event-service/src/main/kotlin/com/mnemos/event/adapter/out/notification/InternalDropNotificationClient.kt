package com.mnemos.event.adapter.out.notification

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.time.Instant
import java.util.UUID

@FeignClient(
    name = "eventInternalDropNotificationClient",
    url = "\${mnemos.clients.notification-service.url}",
    configuration = [InternalDropNotificationFeignConfiguration::class],
)
interface InternalDropNotificationClient {
    @PostMapping("/internal/v1/item-drop-notifications")
    fun notify(@RequestBody request: InternalDropNotificationRequest)
}

data class InternalDropNotificationRequest(
    val sourceId: UUID,
    val occurredAt: Instant,
    val droppedFor: String,
    val itemKey: String?,
    val itemName: String,
    val itemRarity: DomainItemRarity,
    val itemType: DomainItemType,
)

class InternalDropNotificationFeignConfiguration {
    @Bean
    fun internalApiKeyInterceptor(
        @Value("\${spring.application.name}") serviceName: String,
        @Value("\${mnemos.clients.notification-service.api-key}") apiKey: String,
    ): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("X-Mnemos-Service", serviceName)
            template.header("X-Mnemos-API-Key", apiKey)
        }
}
