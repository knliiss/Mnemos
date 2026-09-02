package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalWipeListResponse
import com.mnemos.event.contracts.internal.v1.InternalWipeResponse
import com.mnemos.telegram.bot.adapter.out.event.EventWipeMetadataMapper.toOptions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class EventWipeMetadataMapperTest {
    @Test
    fun `wipe options use active wipe and highest preceding number`() {
        val currentId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val previousId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val response =
            InternalWipeListResponse(
                wipes =
                    listOf(
                        wipe(id = previousId, number = 11),
                        wipe(id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"), number = 8),
                        wipe(id = currentId, number = 12, active = true),
                        wipe(id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b74"), number = 13),
                    ),
            )

        val options = response.toOptions()

        assertEquals(currentId, options.current?.id)
        assertEquals(12, options.current?.number)
        assertEquals(previousId, options.previous?.id)
        assertEquals(11, options.previous?.number)
    }

    private fun wipe(
        id: UUID,
        number: Int,
        active: Boolean = false,
    ): InternalWipeResponse =
        InternalWipeResponse(
            id = id,
            number = number,
            name = null,
            startedAt = Instant.parse("2026-08-01T00:00:00Z"),
            endedAt = null,
            active = active,
        )
}
