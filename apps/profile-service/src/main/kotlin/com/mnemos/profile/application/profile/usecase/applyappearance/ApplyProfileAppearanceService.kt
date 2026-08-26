package com.mnemos.profile.application.profile.usecase.applyappearance

import com.mnemos.profile.application.profile.port.out.ProfileAppearanceRepository
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApplyProfileAppearanceService(
    private val repository: ProfileAppearanceRepository,
) : ApplyProfileAppearanceUseCase {
    @Transactional
    override fun execute(appearance: ProfileAppearance) {
        repository.ensureState(appearance.accountId)

        val current =
            requireNotNull(repository.findByAccountIdForUpdate(appearance.accountId)) {
                "Profile appearance projection state was not created"
            }

        if (current.revision >= appearance.revision) {
            return
        }

        repository.save(appearance)
    }
}
