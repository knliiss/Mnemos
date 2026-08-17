package com.mnemos.profile.application.profile.usecase.get

import com.mnemos.profile.application.profile.port.out.PublicProfileRepository
import com.mnemos.profile.domain.profile.model.PublicProfile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class GetPublicProfileService(
    private val repository: PublicProfileRepository,
) : GetPublicProfileUseCase {
    @Transactional(readOnly = true)
    override fun execute(accountId: UUID): PublicProfile =
        repository.findByAccountId(accountId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Public profile was not found")
}
