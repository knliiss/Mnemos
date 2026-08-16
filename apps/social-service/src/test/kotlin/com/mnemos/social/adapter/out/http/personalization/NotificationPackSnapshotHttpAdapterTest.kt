package com.mnemos.social.adapter.out.http.personalization

import com.fasterxml.jackson.databind.ObjectMapper
import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.personalization.contracts.internal.v1.InternalNotificationPackSelectionResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class NotificationPackSnapshotHttpAdapterTest {
    @Test
    fun `maps authoritative active selection to immutable spotlight snapshot`() {
        val userId = UUID.fromString("019c1ab0-aa00-7000-8000-000000000001")
        val personalizationId = UUID.fromString("019c1ab0-aa00-7000-8000-000000000002")
        val client = mock<PersonalizationServiceClient>()
        val objectMapper = ObjectMapper().findAndRegisterModules()
        val pack =
            NotificationPackSnapshotV1(
                mixedAggregateTitle = "Drops",
                templates = emptyList(),
            )
        val response =
            InternalNotificationPackSelectionResponse(
                userId = userId,
                personalizationId = personalizationId,
                selectionRevision = 7,
                pack = pack,
            )
        val adapter = NotificationPackSnapshotHttpAdapter(client, objectMapper)

        whenever(client.getNotificationPackSelection(userId)).thenReturn(response)

        val snapshot = requireNotNull(adapter.findActive(userId))

        assertEquals(personalizationId, snapshot.personalizationId)
        assertEquals(7, snapshot.revision)
        assertEquals(objectMapper.writeValueAsString(pack), snapshot.snapshotJson)
    }

    @Test
    fun `returns no snapshot when user has no active notification pack`() {
        val userId = UUID.fromString("019c1ab0-aa00-7000-8000-000000000003")
        val client = mock<PersonalizationServiceClient>()
        val adapter = NotificationPackSnapshotHttpAdapter(client, ObjectMapper().findAndRegisterModules())

        whenever(client.getNotificationPackSelection(userId)).thenReturn(null)

        assertNull(adapter.findActive(userId))
    }
}
