package com.mnemos.personalization.domain.ownership

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.ownership.model.PersonalizationOwner
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class OwnedPersonalizationTest {
    @Test
    fun `owned personalization can be activated and deactivated`() {
        val ownership =
            OwnedPersonalization(
                owner = PersonalizationOwner.User(UserId.generate()),
                personalizationId = PersonalizationId.generate(),
                type = PersonalizationType.NOTIFICATION_PACK,
                acquiredAt = Instant.parse("2026-08-15T00:00:00Z"),
                active = false,
            )

        ownership.activate()
        assertTrue(ownership.active)

        ownership.deactivate()
        assertFalse(ownership.active)
    }

    @Test
    fun `rejects ownership target mismatch`() {
        assertThrows(IllegalArgumentException::class.java) {
            OwnedPersonalization(
                owner = PersonalizationOwner.User(UserId.generate()),
                personalizationId = PersonalizationId.generate(),
                type = PersonalizationType.NICK_STYLE,
                acquiredAt = Instant.parse("2026-08-15T00:00:00Z"),
                active = false,
            )
        }
    }
}
