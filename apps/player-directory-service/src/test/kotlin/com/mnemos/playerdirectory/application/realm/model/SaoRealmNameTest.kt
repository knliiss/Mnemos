package com.mnemos.playerdirectory.application.realm.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SaoRealmNameTest {
    @Test
    fun `accepts only canonical positive SAO realms`() {
        assertEquals(1, SaoRealmName.parseOrNull("SAO-1")?.number)
        assertEquals(27, SaoRealmName.parseOrNull("SAO-27")?.number)

        assertNull(SaoRealmName.parseOrNull("SAO-0"))
        assertNull(SaoRealmName.parseOrNull("SAO-01"))
        assertNull(SaoRealmName.parseOrNull("sao-1"))
        assertNull(SaoRealmName.parseOrNull("SAO-1x"))
        assertNull(SaoRealmName.parseOrNull("LOBBY"))
        assertNull(SaoRealmName.parseOrNull(null))
    }

    @Test
    fun `sorts realms by numeric suffix`() {
        val realms =
            listOf(
                requireNotNull(SaoRealmName.parseOrNull("SAO-10")),
                requireNotNull(SaoRealmName.parseOrNull("SAO-2")),
                requireNotNull(SaoRealmName.parseOrNull("SAO-1")),
            )

        val sorted = realms.sorted().map(SaoRealmName::value)

        assertEquals(listOf("SAO-1", "SAO-2", "SAO-10"), sorted)
    }
}
