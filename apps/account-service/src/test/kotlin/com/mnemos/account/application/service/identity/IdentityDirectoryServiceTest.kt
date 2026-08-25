package com.mnemos.account.application.service.identity

import com.mnemos.account.application.query.identity.IdentityDirectoryOrder
import com.mnemos.account.application.query.identity.IdentityDirectoryPage
import com.mnemos.account.application.query.identity.IdentityDirectoryQuery
import com.mnemos.account.application.query.identity.IdentityDirectoryRepository
import com.mnemos.account.application.query.identity.UserIdentityDirectoryEntry
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class IdentityDirectoryServiceTest {
    @Test
    fun `user directory normalizes blank query without changing paging`() {
        val repository = mock<IdentityDirectoryRepository>()
        val expectedPage =
            IdentityDirectoryPage<UserIdentityDirectoryEntry>(
                content = emptyList(),
                page = 2,
                size = 20,
                totalElements = 0,
                totalPages = 0,
            )

        whenever(repository.searchUsers(any())).thenReturn(expectedPage)

        val service = IdentityDirectoryService(repository)

        val result =
            service.searchUsers(
                IdentityDirectoryQuery(
                    query = "   ",
                    page = 2,
                    size = 20,
                    order = IdentityDirectoryOrder.OLDEST,
                ),
            )

        val queryCaptor = argumentCaptor<IdentityDirectoryQuery>()
        verify(repository).searchUsers(queryCaptor.capture())

        assertEquals(expectedPage, result)
        assertEquals(null, queryCaptor.firstValue.query)
        assertEquals(2, queryCaptor.firstValue.page)
        assertEquals(20, queryCaptor.firstValue.size)
        assertEquals(IdentityDirectoryOrder.OLDEST, queryCaptor.firstValue.order)
    }
}
