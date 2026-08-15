package com.mnemos.realtime.application.collector.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CollectorSecretHasherTest {
    private val hasher = CollectorSecretHasher()

    @Test
    fun `hashes and compares collector secrets`() {
        val hash = hasher.hash("collector-secret")

        assertEquals(64, hash.length)
        assertTrue(hasher.matches("collector-secret", hash))
        assertFalse(hasher.matches("different-secret", hash))
    }
}
