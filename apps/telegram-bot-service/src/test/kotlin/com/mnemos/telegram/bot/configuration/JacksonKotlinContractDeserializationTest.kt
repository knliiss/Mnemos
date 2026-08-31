package com.mnemos.telegram.bot.configuration

import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmSnapshotResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedResponse
import com.mnemos.event.contracts.internal.v1.InternalWipeListResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import tools.jackson.databind.json.JsonMapper
import java.time.Instant

@SpringBootTest(
    properties = [
        "mnemos.telegram-bot.telegram.enabled=false",
    ],
)
class JacksonKotlinContractDeserializationTest {
    @Autowired
    private lateinit var jsonMapper: JsonMapper

    @Test
    fun `deserializes player directory Kotlin contract`() {
        val response =
            jsonMapper.readValue(
                """
                {
                  "generatedAt": "2026-09-01T00:00:00Z",
                  "trackedOnline": 12,
                  "realms": []
                }
                """.trimIndent(),
                InternalSaoRealmSnapshotResponse::class.java,
            )

        assertEquals(Instant.parse("2026-09-01T00:00:00Z"), response.generatedAt)
        assertEquals(12, response.trackedOnline)
        assertTrue(response.realms.isEmpty())
    }

    @Test
    fun `deserializes event Kotlin contracts`() {
        val feed =
            jsonMapper.readValue(
                """
                {
                  "items": [],
                  "cursor": null
                }
                """.trimIndent(),
                InternalEventFeedResponse::class.java,
            )
        val wipes =
            jsonMapper.readValue(
                """
                {
                  "wipes": []
                }
                """.trimIndent(),
                InternalWipeListResponse::class.java,
            )

        assertTrue(feed.items.isEmpty())
        assertNull(feed.cursor)
        assertTrue(wipes.wipes.isEmpty())
    }
}
