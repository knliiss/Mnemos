package com.mnemos.realtime.application.collector.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class CollectorAccessKeyParserTest {
    private val parser = CollectorAccessKeyParser()

    @Test
    fun `parses credential id and 256 bit url safe secret`() {
        val credentialId = UUID.fromString("019c1129-ef54-7000-8000-000000000001")
        val secret = "a".repeat(43)

        val accessKey = parser.parse("$credentialId.$secret")

        assertEquals(credentialId, accessKey?.credentialId?.id)
        assertEquals(secret, accessKey?.secret)
    }

    @Test
    fun `rejects malformed and short access keys`() {
        assertNull(parser.parse("not-a-key"))
        assertNull(parser.parse("019c1129-ef54-7000-8000-000000000001.short"))
    }
}
