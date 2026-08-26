package com.mnemos.profile.adapter.out.http.personalization

import com.mnemos.personalization.contracts.internal.v1.InternalProfileAppearanceResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@FeignClient(
    name = "profile-personalization-service-client",
    url = "\${mnemos.profile.personalization-service.base-url}",
    configuration = [ProfilePersonalizationFeignConfiguration::class],
)
interface ProfilePersonalizationServiceClient {
    @GetMapping("/internal/v1/accounts/{accountId}/profile-appearance")
    fun getProfileAppearance(
        @PathVariable("accountId") accountId: UUID,
    ): InternalProfileAppearanceResponse
}
