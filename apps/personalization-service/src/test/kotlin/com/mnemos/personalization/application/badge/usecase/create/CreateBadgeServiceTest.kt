package com.mnemos.personalization.application.badge.usecase.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.personalization.application.badge.exception.BadgeAlreadyExistsException
import com.mnemos.personalization.application.badge.model.BadgeImageUpload
import com.mnemos.personalization.application.badge.model.CreateBadgeCommand
import com.mnemos.personalization.application.badge.port.out.BadgeImageStorage
import com.mnemos.personalization.application.badge.support.BadgeAdministrationSupport
import com.mnemos.personalization.application.badge.support.BadgeImageValidator
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import com.mnemos.personalization.domain.personalization.model.HexColor
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.imageio.ImageIO

class CreateBadgeServiceTest {
    private val clock =
        Clock.fixed(
            Instant.parse("2026-08-15T20:00:00Z"),
            ZoneOffset.UTC,
        )

    @Test
    fun `concurrent badge code conflict removes stored image and returns application conflict`() {
        val repository = mock<BadgeRepository>()
        val storage = mock<BadgeImageStorage>()
        val service =
            CreateBadgeService(
                support = support(repository),
                repository = repository,
                badgeImageStorage = storage,
                badgeImageValidator = BadgeImageValidator(),
                clock = clock,
            )

        whenever(repository.findByCode("EARLY_SUPPORTER")).thenReturn(null)
        whenever(repository.save(any())).thenThrow(
            DataIntegrityViolationException("duplicate badge code"),
        )

        assertThrows(BadgeAlreadyExistsException::class.java) {
            service.execute(
                CreateBadgeCommand(
                    code = "EARLY_SUPPORTER",
                    name = "Early Supporter",
                    image =
                        BadgeImageUpload(
                            bytes = pngBytes(),
                            contentType = "image/png",
                        ),
                    color = HexColor("#FFFFFF"),
                ),
            )
        }

        verify(storage).delete(any())
    }

    private fun support(repository: BadgeRepository): BadgeAdministrationSupport =
        BadgeAdministrationSupport(
            actorProvider =
                FixedActorProvider(
                    PersonalizationActor(
                        userId = UserId.generate(),
                        roles = setOf(UserRole.ADMIN),
                    ),
                ),
            repository = repository,
        )

    private fun pngBytes(): ByteArray {
        val image = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
        val output = ByteArrayOutputStream()

        ImageIO.write(image, "png", output)

        return output.toByteArray()
    }

    private class FixedActorProvider(
        private val actor: PersonalizationActor,
    ) : CurrentPersonalizationActorProvider {
        override fun requireActor(): PersonalizationActor = actor
    }
}
