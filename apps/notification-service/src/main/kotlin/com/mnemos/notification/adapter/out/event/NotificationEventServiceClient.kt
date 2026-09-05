package com.mnemos.notification.adapter.out.event

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "notification-event-service-client",
    url = "\${mnemos.clients.event-service.url}",
    configuration = [NotificationEventServiceFeignConfiguration::class],
)
fun interface NotificationEventServiceClient {
    @GetMapping("/internal/v1/events/localizations/sao/{locale}")
    fun getSaoLocalization(
        @PathVariable locale: String,
    ): NotificationSaoLocalizationResponse
}
