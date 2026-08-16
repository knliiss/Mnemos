package com.mnemos.personalization.application.personalization.custom

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.personalization.custom.port.out.CustomNotificationPackReader
import com.mnemos.personalization.application.personalization.usecase.listcustompacks.ListCustomNotificationPacksService
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.personalization.model.Personalization
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ListCustomNotificationPacksServiceTest {
    @Test
    fun `list mine queries only current users custom packs`() {
        val userId = UserId.generate()
        val actorProvider = mock<CurrentPersonalizationActorProvider>()
        val reader = mock<CustomNotificationPackReader>()
        val page =
            PageQuery(
                page = 1,
                size = 20,
            )
        val expected =
            PageResult<Personalization>(
                items = emptyList(),
                page = 1,
                size = 20,
                totalItems = 40,
                totalPages = 2,
                hasNextPage = false,
            )

        whenever(actorProvider.requireActor()).thenReturn(
            PersonalizationActor(
                userId = userId,
                roles = emptySet(),
            ),
        )
        whenever(
            reader.findByCreatorUserId(
                creatorUserId = userId,
                page = page,
            ),
        ).thenReturn(expected)

        val service =
            ListCustomNotificationPacksService(
                actorProvider = actorProvider,
                reader = reader,
            )

        val result = service.execute(page)

        assertEquals(expected, result)
        verify(reader).findByCreatorUserId(
            creatorUserId = userId,
            page = page,
        )
    }
}
