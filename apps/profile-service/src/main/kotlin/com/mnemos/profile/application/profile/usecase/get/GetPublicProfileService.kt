package com.mnemos.profile.application.profile.usecase.get

import com.mnemos.profile.application.profile.exception.PublicProfileNotFoundException
import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetPublicProfileService(
    private val repository: PublicProfileRepository,
) : GetPublicProfileUseCase {
    @Transactional(readOnly = true)
    override fun execute(accountId: UUID): PublicProfile =
        repository.findByAccountId(accountId)
            ?: throw PublicProfileNotFoundException(accountId)
}
