package com.mnemos.accountservice.infrastructure.generator.invite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecureInviteCodeGeneratorTest {
    private val generator = SecureInviteCodeGenerator()

    @Test
    fun `generated code has canonical grouped format`() {
        val code = generator.generate().value

        assertEquals(19, code.length)
        assertEquals(listOf(4, 4, 4, 4), code.split('-').map(String::length))
    }

    @Test
    fun `generated code uses only supported symbols`() {
        repeat(1_000) {
            val symbols = generator.generate().value.replace("-", "")
            assertTrue(symbols.all { it in "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" })
        }
    }

    @Test
    fun `generator does not collapse to a constant value`() {
        val generated =
            buildSet {
                repeat(128) { add(generator.generate()) }
            }

        assertTrue(generated.size > 120)
    }
}
