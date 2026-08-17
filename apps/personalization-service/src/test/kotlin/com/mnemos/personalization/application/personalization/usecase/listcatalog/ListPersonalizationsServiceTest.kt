package com.mnemos.personalization.application.personalization.usecase.listcatalog

import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.personalization.catalog.port.out.PersonalizationCatalogReader
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ListPersonalizationsServiceTest {
    @Test
    fun `list forwards filters and bounded page to catalog reader`() {
        val catalogReader = mock<PersonalizationCatalogReader>()
        val page =
            PageQuery(
                page = 2,
                size = 40,
            )
        val expected =
            PageResult<Personalization>(
                items = emptyList(),
                page = 2,
                size = 40,
                totalItems = 81,
                totalPages = 3,
                hasNextPage = false,
            )

        whenever(
            catalogReader.findPublished(
                type = PersonalizationType.NICK_STYLE,
                currency = Currency.COIN,
                page = page,
            ),
        ).thenReturn(expected)

        val service = ListPersonalizationsService(catalogReader = catalogReader)

        val result =
            service.execute(
                ListPersonalizationsQuery(
                    type = PersonalizationType.NICK_STYLE,
                    currency = Currency.COIN,
                    page = page,
                ),
            )

        assertEquals(expected, result)
        verify(catalogReader).findPublished(
            type = PersonalizationType.NICK_STYLE,
            currency = Currency.COIN,
            page = page,
        )
    }
}
