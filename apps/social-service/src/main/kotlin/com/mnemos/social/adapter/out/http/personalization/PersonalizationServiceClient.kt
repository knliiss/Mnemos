package com.mnemos.social.adapter.out.http.personalization

import com.mnemos.personalization.contracts.internal.v1.InternalNotificationPackSelectionResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@FeignClient(
    name = "social-personalization-service-client",
    url = "\${mnemos.social.personalization-service.base-url}",
    configuration = [SocialPersonalizationFeignConfiguration::class],
    dismiss404 = true,
)
interface PersonalizationServiceClient {
    @GetMapping("/internal/v1/users/{userId}/notification-pack-selection")
    fun getNotificationPackSelection(
        @PathVariable userId: UUID,
    ): InternalNotificationPackSelectionResponse?
}
