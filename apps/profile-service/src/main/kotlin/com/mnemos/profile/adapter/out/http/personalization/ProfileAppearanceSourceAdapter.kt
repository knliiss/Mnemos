package com.mnemos.profile.adapter.out.http.personalization

import com.mnemos.personalization.contracts.event.ProfileAppearanceChangedV1
import com.mnemos.profile.adapter.inbound.messaging.personalization.ProfileAppearanceContractMapper.toDomain
import com.mnemos.profile.application.profile.port.out.ProfileAppearanceSource
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import feign.FeignException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ProfileAppearanceSourceAdapter(
    private val client: ProfilePersonalizationServiceClient,
) : ProfileAppearanceSource {
    override fun fetch(accountId: UUID): ProfileAppearance? =
        try {
            val response = client.getProfileAppearance(accountId)

            ProfileAppearanceChangedV1(
                accountId = response.accountId,
                appearance = response.appearance,
                revision = response.revision,
                occurredAt = response.observedAt,
            ).toDomain()
        } catch (exception: FeignException) {
            logger.warn(
                "Failed to reconcile profile appearance from personalization-service: accountId={}, status={}",
                accountId,
                exception.status(),
            )
            null
        }

    private companion object {
        val logger = LoggerFactory.getLogger(ProfileAppearanceSourceAdapter::class.java)
    }
}
