package com.mnemos.profile.adapter.out.http.personalization

import com.mnemos.profile.adapter.personalization.ProfileAppearanceSnapshotMapper.toDomain
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

            response.appearance.toDomain(
                accountId = response.accountId,
                revision = response.revision,
                updatedAt = response.observedAt,
            )
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
