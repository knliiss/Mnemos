package com.mnemos.personalization.application.badge.usecase.getimage

import com.mnemos.personalization.application.badge.exception.BadgeNotFoundException
import com.mnemos.personalization.application.badge.model.BadgeImageContent
import com.mnemos.personalization.application.badge.port.out.BadgeImageStorage
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service

@Service
class GetBadgeImageService(
    private val badgeRepository: BadgeRepository,
    private val badgeImageStorage: BadgeImageStorage,
) : GetBadgeImageUseCase {
    override fun execute(badgeId: BadgeId): BadgeImageContent {
        val badge = badgeRepository.findById(badgeId) ?: throw BadgeNotFoundException()

        return badgeImageStorage.load(badge.imageObjectKey)
            ?: throw IllegalStateException("Badge ${badge.id.id} image is missing from object storage")
    }
}
