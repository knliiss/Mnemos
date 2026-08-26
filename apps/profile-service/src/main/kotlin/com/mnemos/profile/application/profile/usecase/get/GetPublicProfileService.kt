package com.mnemos.profile.application.profile.usecase.get

import com.mnemos.profile.application.profile.exception.PublicProfileNotFoundException
import com.mnemos.profile.application.profile.port.out.ProfileAppearanceRepository
import com.mnemos.profile.application.profile.port.out.ProfileAppearanceSource
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.application.profile.usecase.applyappearance.ApplyProfileAppearanceUseCase
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
class GetPublicProfileService(
    private val repository: PublicProfileRepository,
    private val appearanceRepository: ProfileAppearanceRepository,
    private val appearanceSource: ProfileAppearanceSource,
    private val applyProfileAppearanceUseCase: ApplyProfileAppearanceUseCase,
    private val clock: Clock,
    @Value("\${mnemos.profile.appearance-reconciliation-max-age:30s}")
    private val appearanceReconciliationMaxAge: Duration,
) : GetPublicProfileUseCase {
    override fun execute(accountId: UUID): PublicProfile {
        val profile =
            repository.findByAccountId(accountId)
                ?: throw PublicProfileNotFoundException(accountId)

        if (!profile.appearance.requiresReconciliation()) {
            return profile
        }

        val authoritativeAppearance = appearanceSource.fetch(accountId) ?: return profile

        applyProfileAppearanceUseCase.execute(authoritativeAppearance)

        val reconciledAppearance = appearanceRepository.findByAccountId(accountId) ?: return profile

        return profile.copy(appearance = reconciledAppearance)
    }

    private fun ProfileAppearance.requiresReconciliation(): Boolean {
        if (updatedAt == Instant.EPOCH) {
            return true
        }

        return updatedAt.plus(appearanceReconciliationMaxAge).isBefore(clock.instant())
    }
}
