package com.mnemos.personalization.domain.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class PersonalizationTest {
    private val creatorUserId = UserId.generate()
    private val createdAt = Instant.parse("2026-08-15T00:00:00Z")

    @Test
    fun `creator personalization follows moderation lifecycle`() {
        val personalization = createNickStyleDraft()
        val moderatedPrice = PersonalizationPrice(Currency.COIN, 1_500)

        personalization.submitForReview(createdAt.plusSeconds(1))
        personalization.reject("Reduce glow intensity", createdAt.plusSeconds(2))
        personalization.updateDraft(
            name = "Ice Gradient",
            content = nickStyleContent(),
            price = PersonalizationPrice(Currency.COIN, 1_200),
            now = createdAt.plusSeconds(3),
        )
        personalization.submitForReview(createdAt.plusSeconds(4))
        personalization.approve(moderatedPrice, createdAt.plusSeconds(5))
        personalization.publish(createdAt.plusSeconds(6))

        assertEquals(PersonalizationStatus.PUBLISHED, personalization.status)
        assertEquals(PersonalizationVisibility.PUBLIC, personalization.visibility)
        assertEquals(moderatedPrice, personalization.price)

        personalization.unlist(createdAt.plusSeconds(7))
        assertEquals(PersonalizationStatus.UNLISTED, personalization.status)

        personalization.relist(createdAt.plusSeconds(8))
        assertEquals(PersonalizationStatus.PUBLISHED, personalization.status)
    }

    @Test
    fun `published personalization cannot be edited`() {
        val personalization = createNickStyleDraft()

        personalization.submitForReview(createdAt.plusSeconds(1))
        personalization.approve(
            moderatedPrice = PersonalizationPrice(Currency.COIN, 1_000),
            now = createdAt.plusSeconds(2),
        )
        personalization.publish(createdAt.plusSeconds(3))

        assertThrows(IllegalArgumentException::class.java) {
            personalization.updateDraft(
                name = "Changed",
                content = nickStyleContent(),
                price = PersonalizationPrice(Currency.COIN, 1_100),
                now = createdAt.plusSeconds(4),
            )
        }
    }

    @Test
    fun `custom origin cannot restore non notification pack`() {
        assertThrows(IllegalArgumentException::class.java) {
            restore(
                origin = PersonalizationOrigin.CUSTOM,
                visibility = PersonalizationVisibility.PRIVATE,
                status = PersonalizationStatus.PRIVATE,
                rejectionReason = null,
            )
        }
    }

    @Test
    fun `published personalization cannot restore with private visibility`() {
        assertThrows(IllegalArgumentException::class.java) {
            restore(
                origin = PersonalizationOrigin.CREATOR,
                visibility = PersonalizationVisibility.PRIVATE,
                status = PersonalizationStatus.PUBLISHED,
                rejectionReason = null,
            )
        }
    }

    @Test
    fun `rejected personalization requires rejection reason on restore`() {
        assertThrows(IllegalArgumentException::class.java) {
            restore(
                origin = PersonalizationOrigin.CREATOR,
                visibility = PersonalizationVisibility.PRIVATE,
                status = PersonalizationStatus.REJECTED,
                rejectionReason = null,
            )
        }
    }

    private fun createNickStyleDraft(): Personalization =
        Personalization.createCreatorDraft(
            creatorUserId = creatorUserId,
            type = PersonalizationType.NICK_STYLE,
            name = "Ice Gradient",
            content = nickStyleContent(),
            price = PersonalizationPrice(Currency.COIN, 1_000),
            now = createdAt,
        )

    private fun restore(
        origin: PersonalizationOrigin,
        visibility: PersonalizationVisibility,
        status: PersonalizationStatus,
        rejectionReason: String?,
    ): Personalization =
        Personalization(
            id = PersonalizationId.generate(),
            creatorUserId = creatorUserId,
            type = PersonalizationType.NICK_STYLE,
            origin = origin,
            visibility = visibility,
            name = "Ice Gradient",
            content = nickStyleContent(),
            price = PersonalizationPrice(Currency.COIN, 1_000),
            status = status,
            rejectionReason = rejectionReason,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

    private fun nickStyleContent(): PersonalizationContent.NickStyle =
        PersonalizationContent.NickStyle(
            colors =
                listOf(
                    GradientStop(HexColor("#F5FBFF"), 0),
                    GradientStop(HexColor("#8ACBFF"), 100),
                ),
            angleDegrees = 90,
            glow = null,
        )
}
